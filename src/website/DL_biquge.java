package website;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.BookBasicInfo;
import core.Chapter;
import core.DLBook;
import ui.PanelControl;

public class DL_biquge extends DLBook{

	public DL_biquge(PanelControl pc) {
		super(pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		ArrayList<BookBasicInfo> allbookinfo = new ArrayList<BookBasicInfo>();
		String SearchUrl = null;
		try {
			SearchUrl = "https://sou.xanbhx.com/search?siteid=qula&q=" + URLEncoder.encode(key, "utf-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("解码失败(utf-8,biquge):" + key);
			e.printStackTrace();
			return allbookinfo;
		}
		String htmlinfo = getHtmlInfo(SearchUrl, "utf-8");
		if (htmlinfo == null) return allbookinfo;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements bookname = doc.select(".s2");
		Elements lastChapter = doc.select(".s3");
		Elements author = doc.select(".s4");
		Elements isfinal = doc.select(".s7");	

		for(int i = 1; i< bookname.size(); i++)
		{
			BookBasicInfo bookinfo = new BookBasicInfo();
			boolean finalflag = false;
			bookinfo.setBookName(bookname.get(i).text());
			bookinfo.setBookUrl(bookname.get(i).getElementsByTag("a").attr("href"));
			bookinfo.setAuthor(author.get(i).text());
			if(isfinal.get(i).text().equals("完成")) finalflag = true;
			bookinfo.setIsfinal(finalflag);
			bookinfo.setLastChapter(lastChapter.get(i).text());
			bookinfo.setWebsite(websitename);
			allbookinfo.add(bookinfo);
		}
		
		pc.setStateMsg(String.format("%tT:总搜索结果:%d,解析成功:%d,解析失败:%d(%s)", 
				new Date(), allbookinfo.size(), allbookinfo.size(), 0, this.websitename), true);
		return allbookinfo;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = getHtmlInfo(Url, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements Catalogs = doc.getElementsByTag("dl").first().getAllElements();
		
		ArrayList<String> results = new ArrayList<String>();
		boolean dtuse= false;
		for(Element catalog : Catalogs)
		{
			if(catalog.tagName().equals("dt"))
			{
				if(catalog.text().contains("已启用缓存技术，最新章节可能会延时显示"))
				{
					dtuse = false;
				}
				else
				{
					dtuse = true;
				}
			}
			if(dtuse && catalog.tagName().equals("a"))
			{
				results.add("https://www.qu.la" + catalog.attr("href"));
			}		
		}
		
		return results;
	}

	@Override
	protected Chapter getChapters(String Url)
	{
		int trytime = 3;
		String htmlinfo,title,text = null;
		
		do
		{
			htmlinfo = getHtmlInfo(Url, "utf-8");
			if (htmlinfo == null) return null;
			trytime--;
			Document doc = Jsoup.parse(htmlinfo);
			title = doc.select(".bookname>h1").text();
			text = doc.select("#content").html();			
			if(text.length() == 0)
			{
				int fristindex = htmlinfo.indexOf("'");
				int secondindex = htmlinfo.indexOf("'", fristindex + 1);
				Url = htmlinfo.substring(fristindex + 1, secondindex);
			}
		}while(text.length() == 0 && trytime > 0);

		if(text.length() == 0) return null;
		text = text.replaceAll("<script>chaptererror\\(\\);</script>", "");
		return new Chapter(title, text);
	}
}
