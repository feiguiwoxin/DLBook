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
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	 * setWebsiteName用于设置网站名，子类间的网站名不得重复
	 * */
	protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
	protected abstract ArrayList<String> getCatalog(String Url);
	protected abstract Chapter getChapters(String Url);
	protected abstract String setWebsiteName();
	
	/*部分网站在第一次搜索关键字返回的结果往往不能满足我们的需要，比如我们希望从搜索结果中
	 * 获得一个能够读取到小说目录的url，但往往搜索结果中返回的是一个小说的欢迎页面，需要在欢迎
	 * 页面中进一步爬取需要的数据才能够正确的返回搜索结果。如果单线程访问每个搜索结果会使得搜索
	 * 速度过慢，针对这些网站，可以使用下面这两个方法。*/
	//如果要多线程爬取搜索结果，必须要实现这个方法，这个方法需要实现传入网页内容，输出书籍搜索信息。
	protected BookBasicInfo getbookinfoByhtmlinfo(String htmlinfo)
	{
		return null;
	}
	/*bookurls:入参，待爬取的HTML地址集
	 * bookinfos:出参，返回搜索结果集
	 * charset:入参，网站的编码字符集
	 * 多线程爬取搜索结果，如果有这方面的需求，可以调用这个函数。*/
	protected void getbookinfos(ArrayList<String> bookurls, ArrayList<BookBasicInfo> bookinfos,String charset)
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
	
	//根据网址和编码集获取网页内容
	protected String getHtmlInfo(String Urladdress, String charset)
	{
		if(Urladdress == null || charset == null) return null;
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
				System.out.println(Urladdress + "连接超时，重试" + trytime);
				trytime--;			
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
			
			/*对网页内容进行处理，方便后续阅读
			1.由于mysql只能存储1-3字节的utf-8，因此去除4字节的utf-8，主要包含emoji表情，不影响汉字保存
			2.将<br><br/><p></p>等常见的html换行符转化为\r\n
			3.将\n全部替换为\r\n
			4.合并\r\n将空行去除
			5.为了方便网页阅读将换行换成<br><br>与2个空格
			*/
			String text = c.getText();
			text = text.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
			text = text.replaceAll("<br>|<p>|</p>|<br/>", "\r\n");
			text = text.replaceAll("\n|\r\n", "\r\n");
			text = text.replaceAll("　| |&nbsp;", "").replaceAll("\n[\\s]*\r", "");
			text = "　　" + text.replaceAll("\r\n", "<br><br>　　");
			c.setText(text);
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
			
			BookBasicInfo bookinfo = getbookinfoByhtmlinfo(htmlinfo);
			return bookinfo;
		}		
	}
	
	//在构造对象的同时直接生成搜索结果
	public DLBook(String key, PanelControl pc, int poolsize)
	{
		if(poolsize <= 0 || poolsize >= 16) poolsize = 8;
		this.poolsize = poolsize;
		this.websitename = setWebsiteName();
		if(websitename == null) websitename = this.getClass().getName();
		this.pc = pc;
		bookinfos = getBookInfoByKey(key);
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
		
		DbControl db = new DbControl(pc);
		System.out.println(String.format("书籍信息 书名:%s 作者:%s 网址:%s", bookinfo.getBookName(),bookinfo.getAuthor(),bookinfo.getBookUrl()));
		pc.setStateMsg("正在从数据库中获取已存储的章节",true);
		int chapterid = db.queryBookInfo(bookinfo);
		if(chapterid > 0)
		{
			chaptersindb = db.getbookchapters(bookinfo);
			if (chaptersindb == null)
			{
				pc.setStateMsg("数据库获取数据失败，开始从网络下载",true);
				chapterid = -1;
			}
			else
			{
				pc.setStateMsg(String.format("数据库获取数据成功，一共获取了%d个章节", chaptersindb.size()),true);
			}
		}		
		if(chapters == null) failnum = DLChapters(bookinfo, chapterid);
		if(chapterid != -1) db.AddBook(bookinfo, chapters);		

		pc.setStateMsg("将数据写入txt文本中",true);
		try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));
			
			if(null != chaptersindb)
			{
				for(Chapter c : chaptersindb)
				{
					bw.write("ψψψψ" + c.getTitle() + "\r\n");
					bw.write(c.getText().replaceAll("<br>", "\r\n") + "\r\n");
				}
			}		
			
			for(Chapter c : chapters)
			{
				bw.write("ψψψψ" + c.getTitle() + "\r\n");
				bw.write(c.getText().replaceAll("<br>", "\r\n") + "\r\n");
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
		chapterid = chapterid < 0 ? 0 : chapterid;
		chapters = new ArrayList<Chapter>();
		pc.setStateMsg("从网络中获取目录",true);
		if(bookinfo.getBookUrl() == null) return -1;
		ArrayList<String> catalogs = getCatalog(bookinfo.getBookUrl());
		if(catalogs == null) return -1;
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
}
