package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class DLBook {
	protected ArrayList<BookBasicInfo> bookinfos = null;
	protected CopyOnWriteArrayList<Chapter> chapters = null;
	
	/*实现这3个方法可以添加任意网站进行下载
	 * getBookInfoByKey用于根据搜索关键字返回搜索结果
	 * getCatalog用于根据getBookInfoByKey中的书籍的URL地址返回该书所有章节的URL地址
	 * getChapters用于根据getCatalog中的章节URL地址返回本书的章节名和内容
	 * */
	protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
	protected abstract ArrayList<String> getCatalog(String Url);
	protected abstract Chapter getChapters(String Url);
	
	//内部类，该类用于多线程下载，并返回下载的内容
	private class DLChapter implements Callable<Chapter>
	{
		private int chapterid;
		private String url;
		
		public DLChapter(int chapterid,String url)
		{
			this.chapterid = chapterid;
			this.url = url;
		}

		@Override
		public Chapter call() throws Exception {
			Chapter c = getChapters(url);
			if(c == null) return null;
			c.setId(chapterid);
			return c;
		}
	}
	
	//在构造对象的同时直接生成搜索结果
	public DLBook(String key)
	{
		bookinfos = getBookInfoByKey(key);
	}
		
	//根据网址获取网页内容
	protected String getHtmlInfo(String Urladdress)
	{
		URL url;
		StringBuffer result = new StringBuffer();
		int trytime = 3;

		while(trytime > 0)
		{
			try {
				url = new URL(Urladdress);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				HttpURLConnection.setFollowRedirects(true);
				con.setRequestProperty("Connection", "keep-alive");
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
				con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				con.setConnectTimeout(4 * 1000);
				con.setReadTimeout(4 * 1000);
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line = null;
				while((line = br.readLine()) != null)
				{
					result.append(line);
				}
				return result.toString();
			} catch (Exception e) {
				//TODO:增加日志系统
				System.out.println(Urladdress + "连接超时，重试" + trytime);
				trytime--;			
			}
		}
		return null;
	}
	
	//将本地缓存的小说填入txt文件
	public void SaveIntoFile(BookBasicInfo bookinfo)
	{
		if(null == chapters) DLChapters(bookinfo);
		String filename = "./" + bookinfo.getBookName() + " " + bookinfo.getAuthor() + ".txt";
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));
			for(Chapter c : chapters)
			{
				bw.write(c.getTitle() + "\r\n");
				bw.write(c.getText() + "\r\n");
			}
			System.out.println("Save Over~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//将本地缓存的数据填入数据库
	public void SaveIntoDB(BookBasicInfo bookinfo)
	{
		if(null == chapters) DLChapters(bookinfo);
	}
	
	//返回搜索结果集
	public ArrayList<BookBasicInfo> getBookinfos() {
		return bookinfos;
	}
	
	/*根据几个重要的抽象方法多线程下载小说到本地缓存。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private void DLChapters(BookBasicInfo bookinfo)
	{
		chapters = new CopyOnWriteArrayList<Chapter>();
		ArrayList<String> catalogs = getCatalog(bookinfo.getBookUrl());
		int id = 0;
		
		ExecutorService pool = Executors.newFixedThreadPool(8);
		ArrayList<Future<Chapter>> futures = new ArrayList<Future<Chapter>>();
		System.out.println("Whole chapters:" + catalogs.size());
		for(String catalog : catalogs)
		{
			id ++;
			futures.add(pool.submit(new DLChapter(id, catalog)));
		}
		pool.shutdown();
		
		for(Future<Chapter> future : futures)
		{
			Chapter c = null;
			try {
				c = future.get();
				if(c == null) continue;
				chapters.add(c);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		chapters.sort(new Comparator<Chapter>()
		{
			@Override
			public int compare(Chapter o1, Chapter o2)
			{
				return o1.getId() - o2.getId();
			}
		});
		System.out.println("DL Over,DL Chapters:" + chapters.size());
		
		return;
	}
}
