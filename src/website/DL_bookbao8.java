package website;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

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

public class DL_bookbao8 extends DLBook_AllChapter implements GetWelcomInfo{

	public DL_bookbao8(PanelControl pc) {
		super(pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		String allurl = null;
		//由于该网站打开网页速度过慢，为保证搜索速度，因此搜索结果限制在15条
		int searchnum = 15;
		try {
			allurl = "https://www.bookbao8.com/Search/q_" + URLEncoder.encode(key, "utf-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("解码失败(utf-8,bookbao8):" + key);
			e.printStackTrace();
			return bookinfos;
		}
		String htmlinfo = DLTools.getHtmlInfo(allurl, "utf-8");
		if (htmlinfo == null) return bookinfos;
		
		Document doc = Jsoup.parse(htmlinfo);
		ArrayList<String> bookurls = new ArrayList<String>();
		Elements indexs = doc.select(".txt>.t>a");
		for(Element index : indexs)
		{
			searchnum --;
			if(searchnum < 0) break;
			bookurls.add("https://www.bookbao8.com" +index.attr("href"));
		}
		
		DLTools.getbookinfos(bookurls, bookinfos, "utf-8", this, poolsize);
		pc.setStateMsg(String.format("%tT:总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
				new Date(), bookurls.size(), bookinfos.size(), bookurls.size() - bookinfos.size(), this.websitename), true);
		
		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = DLTools.getHtmlInfo(Url, "utf-8");
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
		String htmlinfo = DLTools.getHtmlInfo(Url, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.select("#amain>dl>dd>h1").first().ownText();
		String text = doc.select("#contents").html();
		Chapter c = new Chapter(title,text);
		
		return c;
	}
	
	@Override
	public BookBasicInfo getbookinfoByhtmlinfo(String url, String htmlinfo)
	{
		BookBasicInfo bookinfo = new BookBasicInfo();
		Document doc = Jsoup.parse(htmlinfo);
		String finalflag = doc.select("#info>p").get(3).text();
		bookinfo.setBookName(doc.select("#info>h1").text());
		bookinfo.setAuthor(doc.select("#info>p>a").first().text());
		bookinfo.setBookUrl("https://www.bookbao8.com" + doc.select(".am-btn.am-btn-primary.am-btn-sm.am-radius").get(1).attr("href"));
		bookinfo.setLastChapter(doc.select("#info>p>a").last().text());
		bookinfo.setIsfinal(finalflag.equals("状态：连载中")?false:true);
		bookinfo.setWebsite(websitename);
		
		return bookinfo;
	}
}
