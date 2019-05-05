package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ui.PanelControl;

//适用于开始就能获取所有章节，然后多线程按章节下载的场景
public abstract class DLBook_AllChapter extends DLBook{

	public DLBook_AllChapter(PanelControl pc) {
		super(pc);
	}
	
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
			if(c == null)
			{
				System.out.println("下载失败:"+url);
				return null;
			}
			c.setId(chapterid);
			c.format2html();

			return c;
		}
	}

	public void SaveIntoFile(BookBasicInfo bookinfo)
	{
		String filename = "./" + bookinfo.getBookName() + " " + bookinfo.getAuthor() + ".txt";
		BufferedWriter bw = null;
		int failnum = 0;
		
		pc.setStateMsg(String.format("书籍信息 书名:%s 作者:%s 网址:%s", bookinfo.getBookName(),bookinfo.getAuthor(),bookinfo.getBookUrl()), true, Thread.currentThread().getStackTrace()[1]);
		failnum = DLChapters(bookinfo);		

		pc.setStateMsg("将数据写入txt文本中",true, Thread.currentThread().getStackTrace()[1]);
		try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));	
			
			for(Chapter c : chapters)
			{
				c.format2text();
				bw.write("ψψψψ" + c.getTitle() + "\r\n");
				bw.write(c.getText() + "\r\n");
			}

			if(0 == chapters.size())
			{
				pc.setStateMsg("读取网络目录失败，没有保存任何数据", true, Thread.currentThread().getStackTrace()[1]);
			}
			else
			{
				pc.setStateMsg("写入完成o(∩_∩)o,失败章节数"+failnum, true, Thread.currentThread().getStackTrace()[1]);
			}
			chapters.clear();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("写入文件失败" + filename);
			return;
		}
		finally
		{
			try {
				if (bw!=null) bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("写入文件失败" + filename);
				return;
			}
		}
	}
	
	/*根据几个重要的抽象方法多线程下载小说到本地缓存。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private int DLChapters(BookBasicInfo bookinfo)
	{
		if(bookinfo.getBookUrl() == null) return -1;
		pc.setStateMsg("从网络中获取目录",true, Thread.currentThread().getStackTrace()[1]);
		ArrayList<String> catalogs = getCatalog(bookinfo.getBookUrl());
		if(catalogs == null || catalogs.size()<= 0)
		{
			pc.setStateMsg("获取目录失败",true, Thread.currentThread().getStackTrace()[1]);
			return -1;
		}
		
		return DLChaptersByCatalogs(catalogs);
	}
	
	/*在能够获取到全部目录的情况下，使用多线程下载小说。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private int DLChaptersByCatalogs(ArrayList<String> catalogs)
	{
		int id = 0,failnum = 0,successnum = 0,wholenum = catalogs.size();
		
		ExecutorService pool = Executors.newFixedThreadPool(poolsize);
		ArrayList<Future<Chapter>> futures = new ArrayList<Future<Chapter>>();
		pc.setStateMsg(String.format("章节共计%d,需要下载%d", catalogs.size(), wholenum),true, Thread.currentThread().getStackTrace()[1]);
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
				pc.setStateMsg(String.format("已完成/失败/总计:%d/%d/%d", successnum,failnum,wholenum),false, Thread.currentThread().getStackTrace()[1]);
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
				System.out.println("部分章节缓存失败" + (successnum + failnum));
				continue;
			}
		}
		
		pc.setStateMsg("数据存入数据库,需要存入数:" + successnum,true, Thread.currentThread().getStackTrace()[1]);
		return failnum;
	}
}
