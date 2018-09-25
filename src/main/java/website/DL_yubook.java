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

public class DL_yubook extends DLBook{

	public DL_yubook(PanelControl pc) {
		super(pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		ArrayList<String> bookurls = new ArrayList<String>();
		String searchurl = null;
		int searchnum = 15;
		
		try {
			searchurl = "https://m.yubook.net/search.html?key=" + URLEncoder.encode(key, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("解码失败(utf-8,yubook):"+key);
			return bookinfos;
		}
		
		String htmlinfo = this.getHtmlInfo(searchurl, "utf-8");
		Document doc = Jsoup.parse(htmlinfo);
		Elements allbookurl = doc.select(".common-bookele>a");
		
		for(Element bookurl:allbookurl)
		{
			searchnum--;
			bookurls.add("https://m.yubook.net" + bookurl.attr("href"));
			if(searchnum == 0) break;
		}
		
		this.getbookinfos(bookurls, bookinfos, "utf-8");
		
		return bookinfos;
	}
	
	@Override
	protected BookBasicInfo getbookinfoByhtmlinfo(String url,String htmlinfo)
	{		
		BookBasicInfo bookinfo = new BookBasicInfo();
		Document doc = Jsoup.parse(htmlinfo);
		bookinfo.setAuthor(doc.select(".article_info_td>div>h2").text());
		bookinfo.setBookName(doc.select(".h_nav_items>li").get(1).text());
		bookinfo.setBookUrl(url);
		bookinfo.setIsfinal(false);
		bookinfo.setLastChapter(doc.select(".article_info_td>div>a").get(1).text());
		bookinfo.setWebsite(websitename);
		return bookinfo;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = this.getHtmlInfo(Url, "utf-8");
		if(htmlinfo == null) return null;
		
		ArrayList<String> catalogs = new ArrayList<String>();
		Document doc = Jsoup.parse(htmlinfo);
		Elements urls = doc.select(".lb_mulu.chapterList>ul>li>a");
		catalogs.add(urls.last().text());
		catalogs.add(urls.first().attr("href"));
		catalogs.add(Url);
		return catalogs;
	}

	@Override
	protected Chapter getChapters(String Url) {
		String htmlinfo = this.getHtmlInfo(Url, "utf-8");
		if(htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.select(".nr_title").text();
		String text = doc.select("#nr>#nr1").html();
		String nexturl = doc.select(".next>a").attr("href");
		
		Chapter c = new Chapter(title, text, nexturl);
		return c;
	}

}
