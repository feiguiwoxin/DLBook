package Tool;

import static Config.config.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import core.BookBasicInfo;
import Tool.GetWelcomInfo;

//工具类，辅助下载
public class DLTools {
	//根据网址和编码集获取网页内容，get方式获取
	public static String getHtmlInfo(String Urladdress, String charset)
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
	public static String postHtmlInfo(String Urladdress, LinkedHashMap<String,String> values, String inputcharset, String outputcharset)
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
	
	/*部分网站在第一次搜索关键字返回的结果往往不能满足我们的需要，比如我们希望从搜索结果中
	 * 获得一个能够读取到小说目录的url，但往往搜索结果中返回的是一个小说的欢迎页面，需要在欢迎
	 * 页面中进一步爬取需要的数据才能够正确的返回搜索结果。如果单线程访问每个搜索结果会使得搜索
	 * 速度过慢，针对这些网站，可以使用下面这两个方法。*/
	
	//内部类，用于多线程获取书籍信息
	private static class getBookinfo implements Callable<BookBasicInfo>
	{
		private String url;
		private String charset;
		private GetWelcomInfo welcominfo;
		
		public getBookinfo(String url, String charset,GetWelcomInfo welcominfo)
		{
			this.url = url;
			this.charset = charset;
			this.welcominfo = welcominfo;
		}
		
		@Override
		public BookBasicInfo call() throws Exception {
			String htmlinfo = DLTools.getHtmlInfo(url, charset);
			if (htmlinfo == null)
			{
				System.out.println("目录解析失败:" + url);
				return null;
			}
			
			BookBasicInfo bookinfo = welcominfo.getbookinfoByhtmlinfo(url, htmlinfo);
			
			return bookinfo;
		}		
	}
		
	/*bookurls:入参，待爬取的HTML地址集
	 * bookinfos:出参，返回搜索结果集
	 * charset:入参，网站的编码字符集
	 * 多线程爬取搜索结果，如果有这方面的需求，可以调用这个函数。*/
	public static final void getbookinfos(ArrayList<String> bookurls, 
									 ArrayList<BookBasicInfo> bookinfos,
									 String charset, 
									 GetWelcomInfo welcominfo,
									 int poolsize)
	{
		ExecutorService pool = Executors.newFixedThreadPool(poolsize);
		ArrayList<Future<BookBasicInfo>> futures = new ArrayList<Future<BookBasicInfo>>();
		if(bookinfos == null || bookurls == null || charset == null) return;
		
		for(String bookurl:bookurls)
		{
			futures.add(pool.submit(new getBookinfo(bookurl, charset, welcominfo)));
		}
		pool.shutdown();
		
		for(Future<BookBasicInfo> future : futures)
		{
			try {
				BookBasicInfo bookinfo = future.get();
				if(bookinfo == null) continue;
				bookinfos.add(bookinfo);
			} catch (InterruptedException |ExecutionException e) {
				System.out.println("部分目录获取失败");
				e.printStackTrace();
				continue;
			}
		}
	}

}
