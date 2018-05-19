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

public class DL_79xs extends DLBook{

	public DL_79xs(String key, PanelControl pc) {
		super(key, pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		String allurl,htmlinfo = null;
		try {
			allurl = "http://www.79xs.com/Book/Search.aspx?SearchKey="+URLEncoder.encode(key, "gb2312")+"&SearchClass=1";
			htmlinfo = getHtmlInfo(allurl, "gb2312");
			if (htmlinfo == null) return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("解码失败(gb2312):" + key);
			return null;
		}
		//这个网站的搜索结果只能导向小说的index页面，这里先获取index页面的网址
		Document doc = Jsoup.parse(htmlinfo);
		Elements indexs = doc.select("#CListTitle");
		ArrayList<String> bookidxurls = new ArrayList<String>();
		for(Element index : indexs)
		{
			bookidxurls.add("http://www.79xs.com" + index.getElementsByTag("a").first().attr("href"));
		}
		//在index页面中进一步获取书籍的主要信息
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		for(String bookidxurl:bookidxurls)
		{
			htmlinfo = getHtmlInfo(bookidxurl, "gb2312");
			doc = Jsoup.parse(htmlinfo);
			boolean finalflag = true;
			String isfinal = doc.getElementsByTag("td").get(1).getElementsByTag("span").text();
			if(isfinal.equals("连载中")) finalflag = false;
			BookBasicInfo bookinfo = new BookBasicInfo();
			bookinfo.setAuthor(doc.select("#info>h1>span").text().replaceFirst("文 / ", ""));
			bookinfo.setBookName(doc.select("#info>h1").text().replaceFirst("文 / " + bookinfo.getAuthor(), ""));
			bookinfo.setIsfinal(finalflag);
			bookinfo.setBookUrl("http://www.79xs.com" + doc.select(".b1>a").attr("href"));
			bookinfo.setLastChapter(doc.select(".dd2>p>a").text());
			bookinfo.setWebsite("79小说");
			bookinfos.add(bookinfo);
		}
		
		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = getHtmlInfo(Url, "gb2312");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements chapters = doc.select("dd>ul>li");
		String path = Url.replaceFirst("Index.html", "");
		
		ArrayList<String> chapterurls = new ArrayList<String>();
		for(Element chapter : chapters)
		{
			String chapterurl = chapter.select("a").attr("href");
			if (chapterurl.length() == 0) continue;
			chapterurls.add(path + chapterurl);
		}
		
		return chapterurls;
	}

	@Override
	protected Chapter getChapters(String Url) {
		String htmlinfo = getHtmlInfo(Url, "gb2312");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.select("#htmltimu").text();
		String text = doc.select(".contentbox").html();
		if(text.length() == 0) return null;
		text = text.replaceAll("\n|\r\n", "\r\n");
		Chapter c = new Chapter(title, text);
		return c;
	}
}
