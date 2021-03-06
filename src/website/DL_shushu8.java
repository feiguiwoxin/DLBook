package website;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Tool.DLTools;
import Tool.GetWelcomInfo;
import core.BookBasicInfo;
import core.Chapter;
import core.DLBook_AllChapter;
import ui.PanelControl;

public class DL_shushu8 extends DLBook_AllChapter implements GetWelcomInfo{

	public DL_shushu8(PanelControl pc) {
		super(pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		String allurl = null;
		try {
			allurl = "http://www.shushu8.com/bookso.php?kw=" + URLEncoder.encode(key, "gb2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("解码失败(gb2312,shushu8):"+key);
			return bookinfos;
		}
		String htmlinfo = DLTools.getHtmlInfo(allurl, "gb2312");
		if(htmlinfo == null) return bookinfos;
		
		Document doc = Jsoup.parse(htmlinfo);
		ArrayList<String> bookurls = new ArrayList<String>();
		if(doc.select(".listconltop>.width369").size() > 0)
		{
			Elements indexs = doc.select(".width369.jhfd");
			for(Element index : indexs)
			{
				bookurls.add("http://www.shushu8.com" + index.getElementsByTag("a").first().attr("href"));
			}
			DLTools.getbookinfos(bookurls, bookinfos, "gb2312", this, poolsize);
			pc.setStateMsg(String.format("总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
					bookurls.size(), bookinfos.size(), bookurls.size() - bookinfos.size(), this.websitename), true, Thread.currentThread().getStackTrace()[1]);
		}
		else
		{
			bookinfos.add(getbookinfoByhtmlinfo(allurl, htmlinfo));
			pc.setStateMsg(String.format("总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
					1, 1, 0, this.websitename), true, Thread.currentThread().getStackTrace()[1]);
		}

		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = DLTools.getHtmlInfo(Url, "gb2312");
		if(htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements catalogs = doc.select(".clearfix.dirconone>ul>li");
		
		ArrayList<String> result = new ArrayList<String>();
		for(Element catalog : catalogs)
		{
			result.add("http://www.shushu8.com" + catalog.select("a").attr("href"));
		}
		
		return result;
	}

	@Override
	protected Chapter getChapters(String Url) {
		String htmlinfo = DLTools.getHtmlInfo(Url, "gb2312");
		if(htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.select(".page-body>h1").text();
		String text = doc.select("#content").text();
		if(text.length() == 0) return null;
		return new Chapter(title, text);
	}
	
	@Override
	public BookBasicInfo getbookinfoByhtmlinfo(String url, String htmlinfo)
	{
		BookBasicInfo bookinfo = new BookBasicInfo();
		boolean isfinal = false;
		Document doc = Jsoup.parse(htmlinfo);
		if(doc.select(".ywjico").size()>0) isfinal = true;
		bookinfo.setAuthor(doc.select(".author>.black").text());
		bookinfo.setBookName(doc.select(".r420>h1").text());
		bookinfo.setBookUrl("http://www.shushu8.com" + doc.select(".diralinks").attr("href"));
		bookinfo.setLastChapter(doc.select(".lastrecord>strong").text());
		bookinfo.setIsfinal(isfinal);
		bookinfo.setWebsite(websitename);
		
		return bookinfo;
	}
}
