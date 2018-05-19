package website;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.BookBasicInfo;
import core.Chapter;
import core.DLBook;
import ui.PanelControl;

public class DL_shushu8 extends DLBook{

	public DL_shushu8(String key,PanelControl pc) {
		super(key, pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		String allurl = null;
		try {
			allurl = "http://www.shushu8.com/bookso.php?kw=" + URLEncoder.encode(key, "gb2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("解码失败(gb2312):"+key);
			return null;
		}
		String htmlinfo = getHtmlInfo(allurl, "gb2312");
		if(htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		ArrayList<String> htmlinfos = new ArrayList<String>();
		if(doc.select(".listconltop>.width369").size() > 0)
		{
			Elements indexs = doc.select(".width369.jhfd");
			for(Element index : indexs)
			{
				allurl = "http://www.shushu8.com" + index.getElementsByTag("a").first().attr("href");
				String info = getHtmlInfo(allurl, "gb2312");
				if(info == null) continue;
				htmlinfos.add(info);
			}
		}
		else
		{
			htmlinfos.add(htmlinfo);
		}
		
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		for(String info : htmlinfos)
		{
			BookBasicInfo bookinfo = new BookBasicInfo();
			boolean isfinal = false;
			doc = Jsoup.parse(info);
			if(doc.select(".ywjico").size()>0) isfinal = true;
			bookinfo.setAuthor(doc.select(".author>.black").text());
			bookinfo.setBookName(doc.select(".r420>h1").text());
			bookinfo.setBookUrl("http://www.shushu8.com" + doc.select(".diralinks").attr("href"));
			bookinfo.setLastChapter(doc.select(".lastrecord>strong").text());
			bookinfo.setIsfinal(isfinal);
			bookinfo.setWebsite("书书吧");
			bookinfos.add(bookinfo);
		}
			
		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = getHtmlInfo(Url, "gb2312");
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
		String htmlinfo = getHtmlInfo(Url, "gb2312");
		if(htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.select(".page-body>h1").text();
		String text = doc.select("#content").text();
		if(text.length() == 0) return null;
		return new Chapter(title, text);
	}
}
