package website;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.BookBasicInfo;
import core.Chapter;
import core.DLBook;
import ui.PanelControl;

public class DL_bookbao8 extends DLBook{

	public DL_bookbao8(String key, PanelControl pc) {
		super(key, pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		String allurl = "https://www.bookbao8.com/Search/q_" + key;
		String htmlinfo = getHtmlInfo(allurl, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		ArrayList<String> htmlinfos = new ArrayList<String>();
		Elements indexs = doc.select(".txt>.t>a");
		for(Element index : indexs)
		{
			String tmpurl = "https://www.bookbao8.com/" +index.attr("href");
			htmlinfo = getHtmlInfo(tmpurl, "utf-8");
			if (htmlinfo != null) htmlinfos.add(htmlinfo);
		}
		
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		for(String info : htmlinfos)
		{
			BookBasicInfo bookinfo = new BookBasicInfo();
			doc = Jsoup.parse(info);
			String finalflag = doc.select("#info>p").get(3).text();
			bookinfo.setBookName(doc.select("#info>h1").text());
			bookinfo.setAuthor(doc.select("#info>p>a").first().text());
			bookinfo.setBookUrl("https://www.bookbao8.com" + doc.select(".am-btn.am-btn-primary.am-btn-sm.am-radius").get(1).attr("href"));
			bookinfo.setLastChapter(doc.select("#info>p>a").last().text());
			bookinfo.setIsfinal(finalflag.equals("状态：连载中")?false:true);
			bookinfo.setWebsite("书包网");
			bookinfos.add(bookinfo);
		}
			
		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = getHtmlInfo(Url, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements catalogs = doc.select("#chapterlist li a");
		
		ArrayList<String> chapterurls = new ArrayList<String>();
		for(Element catalog : catalogs)
		{
			chapterurls.add("https://www.bookbao8.com" + catalog.attr("href"));
		}
		
		return chapterurls;
	}

	@Override
	protected Chapter getChapters(String Url) {
		String htmlinfo = getHtmlInfo(Url, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.select("#amain>dl>dd>h1").first().ownText();
		String text = doc.select("#contents").html();
		Chapter c = new Chapter(title,text);
		
		return c;
	}
}
