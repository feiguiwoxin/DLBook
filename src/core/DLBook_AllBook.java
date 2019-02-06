package core;

import static Config.config.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import ui.PanelControl;

//适用于直接有书籍，然后直接下载的场景
public abstract class DLBook_AllBook extends DLBook{

	public DLBook_AllBook(PanelControl pc) {
		super(pc);
	}

	protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
	/*这种模式下获取的是书籍的下载地址
	*/
	protected abstract ArrayList<String> getCatalog(String Url);

	@Override
	protected Chapter getChapters(String Url) {
		//这种下载模式下无需获取章节内容
		return null;
	}

	@Override
	public void SaveIntoFile(BookBasicInfo bookinfo){
		//获取书籍的下载地址
		System.out.println(String.format("书籍信息 书名:%s 作者:%s 网址:%s", bookinfo.getBookName(),bookinfo.getAuthor(),bookinfo.getBookUrl()));
		ArrayList<String> catalogs = this.getCatalog(bookinfo.getBookUrl());
		if(catalogs == null)
		{
			pc.setStateMsg("读取网络目录失败，没有保存任何数据", true);
			return;
		}
		
		String bookurl = catalogs.get(0);
		if(bookurl == null)
		{
			pc.setStateMsg("目录中的链接无效，没有保存任何数据", true);
			return;
		}
		
		pc.setStateMsg("开始下载文件", true);
		//获取下载类型，拼接保存文件名
		File file = new File(bookurl);
		String filename = file.getName();
		String filetype = filename.substring(filename.lastIndexOf('.')+1).toLowerCase();
		filename = bookinfo.getBookName() + " " + bookinfo.getAuthor() + "." + filetype;
		
		//下载并保存文件
		byte[] FileData = GetFileData(bookurl, bookinfo.getBookUrl());
		if(FileData == null)
		{
			pc.setStateMsg("下载文件失败，地址:" + bookurl, true);
			return;
		}
		
		FileOutputStream fops = null;
		try {
			try {
				fops = new FileOutputStream(filename);
				fops.write(FileData);
				fops.flush();
			} catch (Exception e) {
				e.printStackTrace();
				pc.setStateMsg("保存文件失败", true);
			}
			finally
			{
				if(fops != null)
				{
					fops.close();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		pc.setStateMsg("下载完成", true);
		return;
	}
	
	//获取文件的二进制码流
	private byte[] GetFileData(String UrlAdd, String refer)
	{
		byte[] BookData = null;
		//如果连接失败，有3次重连机会
		int ConnectTime = 3;
		if(null == UrlAdd) return null;
		
		while(ConnectTime > 0)
		{
			try {
				URL url = new URL(UrlAdd);
				URLConnection urlcon = url.openConnection();
				if(UrlAdd.startsWith("https"))
				{
					((HttpsURLConnection)urlcon).setSSLSocketFactory(config.getSsl().getSocketFactory());
				}				
				urlcon.setRequestProperty("Referer", refer);
				urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
				urlcon.setConnectTimeout(5 * 1000);
				urlcon.setReadTimeout(8 * 1000);
				
				BookData = GetbyteFromStream(urlcon.getInputStream());
				return BookData;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(String.format("%s倒数第%d次重连，信息%s", UrlAdd, ConnectTime, e.getMessage()));
				ConnectTime --;
				continue;
			}
		}
		
		return null;
	}
	
	//将文件的Http流转化为二进制流
	private byte[] GetbyteFromStream(InputStream in) throws IOException
	{
		int len;
		int totallen = 0;
		
		byte[] buffer = new byte[3 * 100 * 1024];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while((len = in.read(buffer)) != -1)
		{
			out.write(buffer, 0 ,len);
			totallen += len;
			pc.setStateMsg("已下载:" + totallen/1024 + "KB", false);
		}
	
		byte[] bookData = out.toByteArray();
		in.close();
		out.close();
		
		return bookData;
	}

}
