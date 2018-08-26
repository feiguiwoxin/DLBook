package website;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.BookBasicInfo;
import core.Chapter;
import core.DLBook;
import ui.PanelControl;

public class DL_hunhun520 extends DLBook{

	public DL_hunhun520(PanelControl pc, int poolsize) {
		super(pc, poolsize);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
		values.put("searchkey", key);
		String htmlinfo = postHtmlInfo("https://www.hunhun520.com/novel.php?action=search", values, "gb2312", "gb2312");
		if (htmlinfo == null) return bookinfos;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements bookname = doc.select(".s2");
		Elements author = doc.select(".s4");
		Elements lastchapter = doc.select(".s3");
		for(int i=0; i < bookname.size(); i++)
		{
			BookBasicInfo bookinfo = new BookBasicInfo();
			bookinfo.setAuthor(author.get(i).text());
			bookinfo.setBookName(bookname.get(i).getElementsByTag("a").first().text());
			bookinfo.setBookUrl(bookname.get(i).getElementsByTag("a").first().attr("href"));
			bookinfo.setIsfinal(false);
			bookinfo.setLastChapter(lastchapter.get(i).getElementsByTag("a").first().text());
			bookinfo.setWebsite(this.websitename);
			bookinfos.add(bookinfo);
		}
		
		pc.setStateMsg(String.format("%tT:总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
				new Date(), bookinfos.size(), bookinfos.size(), 0, this.websitename), true);
		return bookinfos;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = getHtmlInfo(Url, "gb2312");
		if(htmlinfo == null) return null;
		
		ArrayList<String> chapters = new ArrayList<String>();
		Document doc = Jsoup.parse(htmlinfo);
		Elements eles = doc.select("#list>dl>dd>a");
		
		for(Element chapter:eles)
		{
			chapters.add(chapter.attr("href"));
		}
		
		return chapters;
	}

	@Override
	protected Chapter getChapters(String Url) {
		String htmlinfo = getHtmlInfo(Url, "gb2312");
		if(htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		String title = doc.selectFirst(".bookname>h1").text();
		String text = doc.selectFirst("#content").html();
		Elements otherpage = doc.select("#content>.text>a");
		//除去当前页和最后的下一页标签，因此减2
		int pagenum = otherpage.size()-2;
		//这个网站将章节内容进行了分节，因此要进一步获取分节内容
		for(int i=2;i<2+pagenum;i++)
		{
			htmlinfo = getHtmlInfo(Url.replace(".html", "_"+i+".html"), "gb2312");
			doc = Jsoup.parse(htmlinfo);
			text += doc.selectFirst("#content").html();
		}
		text = text.replaceAll("<div(.|\\s)+?div>", "");
		text = text.replaceAll("<!--over-->", "");
		if(text.length() == 0) return null;
		Chapter c = new Chapter(title, text);
		
		return c;
	}

	@Override
	protected String setWebsiteName() {
		return "混混小说";
	}
}
