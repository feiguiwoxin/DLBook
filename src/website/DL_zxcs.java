package website;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Tool.DLTools;
import core.BookBasicInfo;
import core.DLBook_AllBook;
import ui.PanelControl;

public class DL_zxcs extends DLBook_AllBook {

	public DL_zxcs(PanelControl pc) {
		super(pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		String htmlurl = null;
		
		try {
			htmlurl = "http://www.zxcs.me/index.php?keyword=" + URLEncoder.encode(key, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("解码失败(utf-8,yubook):"+key);
			return bookinfos;
		}
		
		String htmlinfo = DLTools.getHtmlInfo(htmlurl, "utf-8");
		if (htmlinfo == null) return bookinfos;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements allbook = doc.select(".wrap>#pleft>#plist");
		
		for(Element ele : allbook)
		{
			BookBasicInfo bookinfo = new BookBasicInfo();
			String BookAndAnthor = ele.select("dt>a").text();
			BookAndAnthor = BookAndAnthor.replaceAll("（校对版全本）", "");
			int index = BookAndAnthor.indexOf("作者：");
			String bookname = BookAndAnthor.substring(0, index);
			String anthor = BookAndAnthor.substring(index).replaceAll("作者：", "");
			String bookurl = ele.select("dt>a").attr("href");
			bookinfo.setAuthor(anthor);
			bookinfo.setBookName(bookname);
			bookinfo.setBookUrl(bookurl);
			bookinfo.setIsfinal(true);
			bookinfo.setLastChapter("NA");
			bookinfo.setWebsite(this.websitename);
			bookinfos.add(bookinfo);
		}
		
		pc.setStateMsg(String.format("总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
				bookinfos.size(), bookinfos.size(), 0, this.websitename), true, Thread.currentThread().getStackTrace()[1]);
		
		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		ArrayList<String> dlurl = new ArrayList<String>();
		
		//获取下载页面
		String htmlinfo = DLTools.getHtmlInfo(Url, "utf-8");
		if(htmlinfo == null) return null;	
		Document doc = Jsoup.parse(htmlinfo);
		String bookdlurl = doc.select(".wrap>#pleft>#content>.pagefujian>.filecont>.filetit>a").attr("href");
		
		//在下载页面中获取下载地址
		htmlinfo = DLTools.getHtmlInfo(bookdlurl, "utf-8");
		if(htmlinfo == null) return null;
		doc = Jsoup.parse(htmlinfo);
		String finalurl = doc.select(".downfile>a").first().attr("href");
		dlurl.add(finalurl);
		
		return dlurl;
	}
	
//	public static void main(String[] args)
//	{
//		PanelControl pc = new PanelControl();
//		DL_zxcs dlbook = new DL_zxcs(pc);
//		dlbook.StartSearch("魔法");
//		dlbook.getCatalog(dlbook.getBookinfos().get(0).getBookUrl());
//	}

}
