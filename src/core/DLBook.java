package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import static Config.config.config;
import dao.DbControl;
import ui.PanelControl;

public abstract class DLBook {
	protected String websitename = null;
	protected PanelControl pc = null;
	private ArrayList<BookBasicInfo> bookinfos = null;
	private ArrayList<Chapter> chapters = null;
	private int poolsize = 8;
	
	/*实现这4个方法可以添加任意网站进行下载
	 * getBookInfoByKey用于根据搜索关键字返回搜索结果
	 * getCatalog用于根据getBookInfoByKey中的书籍的URL地址返回该书所有章节的URL地址
	 * getChapters用于根据getCatalog中的章节URL地址返回章节名和内容
	 * */
	protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
	/*如果能获取到所有目录，则直接获取所有目录
	 * 如果获取不到所有目录，还提供了按章节下载，需要依次填入总章节数目（获取不到就填max）,起始网址，结束网址*/
	protected abstract ArrayList<String> getCatalog(String Url);
	//如果使用按章节下载的方式，则需要填入nexturl字段，否则无需填入
	protected abstract Chapter getChapters(String Url);
	
	/*部分网站在第一次搜索关键字返回的结果往往不能满足我们的需要，比如我们希望从搜索结果中
	 * 获得一个能够读取到小说目录的url，但往往搜索结果中返回的是一个小说的欢迎页面，需要在欢迎
	 * 页面中进一步爬取需要的数据才能够正确的返回搜索结果。如果单线程访问每个搜索结果会使得搜索
	 * 速度过慢，针对这些网站，可以使用下面这两个方法。*/
	/*如果要多线程爬取搜索结果，必须要实现这个方法
	 * url 入参，欢迎页面地址
	 * htmlinfo 根据url获取到的html信息
	 * 此处之所以传入url是为了防止欢迎页面中直接包含目录导致获取不到目录地址
	 * */
	protected BookBasicInfo getbookinfoByhtmlinfo(String url, String htmlinfo)
	{
		return null;
	}
	/*bookurls:入参，待爬取的HTML地址集
	 * bookinfos:出参，返回搜索结果集
	 * charset:入参，网站的编码字符集
	 * 多线程爬取搜索结果，如果有这方面的需求，可以调用这个函数。*/
	protected final void getbookinfos(ArrayList<String> bookurls, ArrayList<BookBasicInfo> bookinfos,String charset)
	{
		ExecutorService pool = Executors.newFixedThreadPool(poolsize);
		ArrayList<Future<BookBasicInfo>> futures = new ArrayList<Future<BookBasicInfo>>();
		if(bookinfos == null || bookurls == null || charset == null) return;
		
		int successnum = 0;
		for(String bookurl:bookurls)
		{
			futures.add(pool.submit(new getBookinfo(bookurl, charset)));
		}
		pool.shutdown();
		
		for(Future<BookBasicInfo> future : futures)
		{
			try {
				BookBasicInfo bookinfo = future.get();
				if(bookinfo == null) continue;
				bookinfos.add(bookinfo);
				successnum ++;
			} catch (InterruptedException |ExecutionException e) {
				System.out.println("部分目录获取失败");
				e.printStackTrace();
				continue;
			}
		}
		pc.setStateMsg(String.format("%tT:总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
						new Date(), bookurls.size(), successnum, bookurls.size() - successnum, this.websitename), true);
	}
	
	//根据网址和编码集获取网页内容，get方式获取
	protected String getHtmlInfo(String Urladdress, String charset)
	{
		if(Urladdress == null || charset == null) return null;
		URL url;
		StringBuffer result = new StringBuffer();
		int trytime = 3;
		BufferedReader br = null;
		HttpURLConnection con = null;

		while(trytime > 0)
		{
			try {
				url = new URL(Urladdress);
				con = (HttpURLConnection)url.openConnection();
				//增加https证书认证
				if(Urladdress.startsWith("https"))
				{
					((HttpsURLConnection)con).setSSLSocketFactory(config.getSsl().getSocketFactory());
				}
				HttpURLConnection.setFollowRedirects(true);
				con.setRequestProperty("Connection", "keep-alive");
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
				con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				con.setConnectTimeout(10 * 1000);
				con.setReadTimeout(10 * 1000);
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
				String line = null;
				while((line = br.readLine()) != null)
				{
					result.append(line+"\r\n");
				}
				return result.toString();
			} catch (Exception e) {
				System.out.println(Urladdress + "连接超时，重试" + trytime);
				trytime--;			
			}
			finally
			{
				
				try {
					if (br != null) br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	//根据网址和编码集获取网页内容，post方式获取
	protected String postHtmlInfo(String Urladdress, LinkedHashMap<String,String> values, String inputcharset, String outputcharset)
	{
		if(Urladdress == null || inputcharset == null || outputcharset == null) return null;
		URL url;
		StringBuffer result = new StringBuffer();
		int trytime = 3;
		boolean frist = true;
		PrintWriter pw = null;
		BufferedReader br = null;
		
		while(trytime > 0)
		{
			try {
				url = new URL(Urladdress);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				//增加https证书认证
				if(Urladdress.startsWith("https"))
				{
					((HttpsURLConnection)con).setSSLSocketFactory(config.getSsl().getSocketFactory());
				}
				HttpURLConnection.setFollowRedirects(true);		
				con.setDoOutput(true);//设置打开输入流，这是post必须要使用的
				con.setRequestProperty("Connection", "keep-alive");
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
				con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				con.setConnectTimeout(10 * 1000);
				con.setReadTimeout(10 * 1000);
				pw = new PrintWriter(con.getOutputStream());
				frist = true;
				
				//填入需要post的表单数据
				for(String key:values.keySet())
				{
					if(!frist) pw.print("&");
					pw.print(key+"="+URLEncoder.encode(values.get(key), outputcharset));
					frist = false;
				}
				pw.flush();
				
				//获取返回的网页信息
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), inputcharset));
				String line = null;
				while((line = br.readLine()) != null)
				{
					result.append(line+"\r\n");
				}
				return result.toString();
			} catch (Exception e) {
				System.out.println(Urladdress + "连接超时，重试" + trytime);
				trytime--;			
			}
			finally
			{				
				try {
					if(pw != null) pw.close();
					if(br != null) br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
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
	
	//内部类，用于多线程获取书籍信息
	private class getBookinfo implements Callable<BookBasicInfo>
	{
		private String url;
		private String charset;
		
		public getBookinfo(String url, String charset)
		{
			this.url = url;
			this.charset = charset;
		}
		
		@Override
		public BookBasicInfo call() throws Exception {
			String htmlinfo = getHtmlInfo(url, charset);
			if (htmlinfo == null)
			{
				System.out.println("目录解析失败:" + url);
				return null;
			}
			
			BookBasicInfo bookinfo = getbookinfoByhtmlinfo(url, htmlinfo);
			
			return bookinfo;
		}		
	}
	
	//在构造对象的同时直接生成搜索结果
	public DLBook(PanelControl pc)
	{	
		if(config.getWebsites().get(this.getClass().getName()) != null)
		{
			poolsize = config.getWebsites().get(this.getClass().getName()).getPoolsize();
			websitename = config.getWebsites().get(this.getClass().getName()).getWebsitename();
		}
		else
		{
			poolsize = 8;
			websitename = this.getClass().getName();
		}		
		if(poolsize <= 0 || poolsize >= 16) poolsize = 8;
		
		this.pc = pc;
	}
	
	/*1、如果chapterid=-1，表示数据库中找不到该书；
	 * 2、如果chapterid>=0，表示数据库中有数据。
	 * 将会从数据库中获取已有数据，从网络中更新新的章节，然后将新的章节写入数据库。最后汇总数据库数据和网络下载数据写入txt中；
	 * */
	public void SaveIntoFile(BookBasicInfo bookinfo)
	{
		ArrayList<Chapter> chaptersindb = new ArrayList<Chapter>();
		String filename = "./" + bookinfo.getBookName() + " " + bookinfo.getAuthor() + ".txt";
		BufferedWriter bw = null;
		int failnum = 0;
		
		System.out.println(String.format("书籍信息 书名:%s 作者:%s 网址:%s", bookinfo.getBookName(),bookinfo.getAuthor(),bookinfo.getBookUrl()));
		pc.setStateMsg("正在从数据库中获取已存储的章节",true);
		int chapterid = DbControl.dbcontrol.getbookchapters(bookinfo, chaptersindb);
		if(chapterid > 0)
		{
			pc.setStateMsg(String.format("数据库获取数据成功，一共获取了%d个章节", chaptersindb.size()),true);
		}		
		if(chapters == null) failnum = DLChapters(bookinfo, chapterid);
		DbControl.dbcontrol.AddBook(bookinfo, chaptersindb, chapters);		

		pc.setStateMsg("将数据写入txt文本中",true);
		try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));
			
			if(null != chaptersindb)
			{
				for(Chapter c : chaptersindb)
				{
					c.format2text();
					bw.write("ψψψψ" + c.getTitle() + "\r\n");
					bw.write(c.getText() + "\r\n");
				}
			}		
			
			for(Chapter c : chapters)
			{
				c.format2text();
				bw.write("ψψψψ" + c.getTitle() + "\r\n");
				bw.write(c.getText() + "\r\n");
			}
			chapters = null;
			if(failnum < 0 && null != chaptersindb)
			{
				pc.setStateMsg("读取网络目录失败，保存数据库章节:" + chaptersindb.size(), true);
			}
			else if(failnum < 0 && null == chaptersindb)
			{
				pc.setStateMsg("读取网络目录失败，数据库无数据，没有保存任何数据", true);
			}
			else
			{
				pc.setStateMsg("写入完成o(∩_∩)o,失败章节数"+failnum, true);
			}		
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
	
	public void StartSearch(String key)
	{
		bookinfos = getBookInfoByKey(key);
	}
	
	//返回搜索结果集
	public ArrayList<BookBasicInfo> getBookinfos() {
		return bookinfos;
	}
	
	public void addBookinfo(BookBasicInfo bookinfo)
	{
		if(bookinfos == null) bookinfos = new ArrayList<BookBasicInfo>();
		bookinfos.add(bookinfo);
	}
	
	/*根据几个重要的抽象方法多线程下载小说到本地缓存。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private int DLChapters(BookBasicInfo bookinfo,int chapterid)
	{
		if(bookinfo.getBookUrl() == null) return -1;
		pc.setStateMsg("从网络中获取目录",true);
		ArrayList<String> catalogs = getCatalog(bookinfo.getBookUrl());
		if(catalogs == null || catalogs.size()<= 0)
		{
			pc.setStateMsg("获取目录失败",true);
			return -1;
		}
		
		chapterid = chapterid < 0 ? 0 : chapterid;
		chapters = new ArrayList<Chapter>();
		if(!catalogs.get(0).startsWith("http"))
		{
			DLChaptersOneByOne(chapterid, catalogs);
			return 0;
		}
		else
		{
			return DLChaptersByCatalogs(chapterid, catalogs);
		}	
	}
	/*在能够获取到全部目录的情况下，使用多线程下载小说。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private int DLChaptersByCatalogs(int chapterid, ArrayList<String> catalogs)
	{
		int id = 0,failnum = 0,successnum = 0,wholenum = catalogs.size() - chapterid;
		
		ExecutorService pool = Executors.newFixedThreadPool(poolsize);
		ArrayList<Future<Chapter>> futures = new ArrayList<Future<Chapter>>();
		pc.setStateMsg(String.format("章节共计%d,数据库已缓存%d，需要下载%d", catalogs.size(), chapterid, wholenum),true);
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
				pc.setStateMsg(String.format("已完成/失败/总计:%d/%d/%d", successnum,failnum,wholenum),false);
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
		
		pc.setStateMsg("数据存入数据库,需要存入数:" + successnum,true);
		return failnum;
	}
	/*无法获取目录则逐章下载，下载完成后返回下一章的链接*/
	private void DLChaptersOneByOne(int chapterid, ArrayList<String> catalogs)
	{
		int chapternum = 0;
		try
		{
			chapternum = Integer.parseInt(catalogs.get(0));
		}
		catch(Exception e)
		{
			chapternum = -1;
		}
		
		if(chapternum <= chapterid && chapternum != -1)
		{
			pc.setStateMsg("无内容需更新，直接从数据库获取数据", true);
			return;
		}
		
		String fristurl = catalogs.get(1);
		String endurl = catalogs.get(2);
		String stringchapternum = chapternum == -1? "-" : String.valueOf(chapternum);
		Chapter chapter = null;
		int id = 0;
		
		do
		{
			id++;
			pc.setStateMsg(String.format("逐章下载中，目前下载章节数:%d/%s", id, stringchapternum),false);
			chapter = this.getChapters(fristurl);
			if(chapter == null)
			{
				pc.setStateMsg(String.format("下载未完成，停止章节为%d,地址为%s", id, fristurl), true);
				return;
			}
			
			chapter.setId(id);
			fristurl = chapter.getNextUrl();
			if(id <= chapterid)
			{
				continue;
			}
			chapter.format2html();
			chapters.add(chapter);
			
			if(id >= 80000)
			{
				pc.setStateMsg(String.format("下载章节数超过80000，强制停止避免死机"),true);
				break;
			}
		}
		while(!fristurl.equals(endurl) && fristurl!= null);
	}
	
	public String getWebsitename() {
		return websitename;
	}
}
