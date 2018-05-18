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

import dao.DbControl;
import ui.PanelControl;

public abstract class DLBook {
	protected ArrayList<BookBasicInfo> bookinfos = null;
	protected CopyOnWriteArrayList<Chapter> chapters = null;
	private PanelControl pc = null;
	
	/*实现这3个方法可以添加任意网站进行下载
	 * getBookInfoByKey用于根据搜索关键字返回搜索结果
	 * getCatalog用于根据getBookInfoByKey中的书籍的URL地址返回该书所有章节的URL地址
	 * getChapters用于根据getCatalog中的章节URL地址返回章节名和内容
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
			System.out.println(c.getTitle());
			return c;
		}
	}
	
	//在构造对象的同时直接生成搜索结果
	public DLBook(String key, PanelControl pc)
	{
		bookinfos = getBookInfoByKey(key);
		this.pc = pc;
	}
		
	//根据网址和编码集获取网页内容
	protected String getHtmlInfo(String Urladdress, String charset)
	{
		URL url;
		StringBuffer result = new StringBuffer();
		int trytime = 5;

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
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
				String line = null;
				while((line = br.readLine()) != null)
				{
					result.append(line+"\r\n");
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
	
	/*如果chapterid=-1，表示数据库出现问题，则直接从网络下载全部章节并保存
	 * 如果chapterid=-0，表示数据库OK但没有数据，则不从数据库获取数据，但需要将数据同时保存在数据库和txt
	 * 如果chapterid>0，表示数据库汇总有数据，需要从数据库获取数据，同时下载剩余数据更新到数据库并保存到txt
	 * */
	public void SaveIntoFile(BookBasicInfo bookinfo)
	{
		ArrayList<Chapter> chaptersindb = null;
		String filename = "./" + bookinfo.getBookName() + " " + bookinfo.getAuthor() + ".txt";
		BufferedWriter bw = null;
		int failnum = 0;
		
		DbControl db = new DbControl();
		pc.setStateMsg("正在从数据库中获取已存储的章节");
		int chapterid = db.queryBookInfo(bookinfo);
		if(chapterid > 0)
		{
			chaptersindb = db.getbookchapters(bookinfo);
			if (chaptersindb == null)
			{
				pc.setStateMsg("数据库获取数据失败，开始从网络下载");
				chapterid = -1;
			}
			else
			{
				pc.setStateMsg(String.format("数据库获取数据成功，一共获取了%d个章节", chaptersindb.size()));
			}
		}		
		if(chapters == null) failnum = DLChapters(bookinfo, chapterid);
		if(chapterid != -1) db.AddBook(bookinfo, chapters);		

		pc.setStateMsg("将数据写入txt文本中");
		try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));
			
			if(null != chaptersindb)
			{
				for(Chapter c : chaptersindb)
				{
					bw.write("ψψψψ" + c.getTitle() + "\r\n");
					bw.write(c.getText().replaceAll("<br>", "\r\n").replaceAll("&nbsp;", "")
										.replaceAll("　", " ").replaceAll("\n[\\s]*\r", "")+ "\r\n");
				}
			}		
			
			for(Chapter c : chapters)
			{
				bw.write("ψψψψ" + c.getTitle() + "\r\n");
				bw.write(c.getText().replaceAll("<br>", "\r\n").replaceAll("&nbsp;", "")
									.replaceAll("　", " ").replaceAll("\n[\\s]*\r", "")+ "\r\n");
			}
			chapters = null;
			pc.setStateMsg("写入完成o(∩_∩)o,失败章节数"+failnum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				if (bw!=null) bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pc.finishDl();
	}
	
	//返回搜索结果集
	public ArrayList<BookBasicInfo> getBookinfos() {
		return bookinfos;
	}
	
	/*根据几个重要的抽象方法多线程下载小说到本地缓存。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private int DLChapters(BookBasicInfo bookinfo,int chapterid)
	{
		chapterid = chapterid<0?0:chapterid;
		chapters = new CopyOnWriteArrayList<Chapter>();
		pc.setStateMsg("从网络中获取目录");
		ArrayList<String> catalogs = getCatalog(bookinfo.getBookUrl());
		int id = 0,failnum = 0,successnum = 0,wholenum = catalogs.size() - chapterid;
		
		ExecutorService pool = Executors.newFixedThreadPool(8);
		ArrayList<Future<Chapter>> futures = new ArrayList<Future<Chapter>>();
		pc.setStateMsg(String.format("章节共计%d,数据库已缓存%d，需要下载%d", catalogs.size(), chapterid, wholenum));
		for(String catalog : catalogs)
		{
			id ++;
			//如果数据库中已经缓存了chapterid章，则判断对于chapterid以前的内容不进行缓存
			if(id <= chapterid) continue;
			futures.add(pool.submit(new DLChapter(id, catalog)));
		}
		pool.shutdown();
		
		for(Future<Chapter> future : futures)
		{
			Chapter c = null;
			try {
				pc.setStateMsg(String.format("已完成/失败/总计:%d/%d/%d", successnum,failnum,wholenum));
				c = future.get();
				if(c == null) 
				{
					failnum++;
					continue;
				}
				chapters.add(c);
				successnum++;
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
		pc.setStateMsg("数据存入数据库,需要存入数:" + successnum);
		return failnum;
	}
}
