package website;

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

	public DL_biquge(String key, PanelControl pc) {
		super(key, pc);
	}

	@Override
	protected ArrayList<BookBasicInfo> getBookInfoByKey(String key) {
		String SearchUrl = "https://www.qu.la/SearchBook.php?" + 
							"t=" + new Date().getTime() + "&keyword=" + key;
		String htmlinfo = getHtmlInfo(SearchUrl, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements bookname = doc.select(".s2");
		Elements lastChapter = doc.select(".s3");
		Elements author = doc.select(".s4");
		Elements isfinal = doc.select(".s7");	
		ArrayList<BookBasicInfo> allbookinfo = new ArrayList<BookBasicInfo>();
		for(int i = 1; i< bookname.size(); i++)
		{
			BookBasicInfo bookinfo = new BookBasicInfo();
			boolean finalflag = false;
			bookinfo.setBookName(bookname.get(i).text());
			bookinfo.setBookUrl("https://www.qu.la" + bookname.get(i).getElementsByTag("a").attr("href"));
			bookinfo.setAuthor(author.get(i).text());
			if(isfinal.get(i).text().equals("完成")) finalflag = true;
			bookinfo.setIsfinal(finalflag);
			bookinfo.setLastChapter(lastChapter.get(i).text());
			bookinfo.setWebsite("笔趣阁");
			allbookinfo.add(bookinfo);
		}
		
		return allbookinfo;
	}

	@Override
	protected ArrayList<String> getCatalog(String Url) {
		String htmlinfo = getHtmlInfo(Url, "utf-8");
		if (htmlinfo == null) return null;
		
		Document doc = Jsoup.parse(htmlinfo);
		Elements Catalogs = doc.getElementsByTag("dl").first().getAllElements();
		
		ArrayList<String> results = new ArrayList<String>();
		int dtnum = 0;
		for(Element catalog : Catalogs)
		{
			//System.out.println(catalog.tagName());
			if(catalog.tagName().equals("dt")) dtnum ++;
			if(dtnum >1 && catalog.tagName().equals("a"))
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

		if(text.length() == 0)
		{
			System.out.println(Url+"下载失败!!!!!!!!!!!");
			return null;
		}
		text = text.replaceAll("<script>chaptererror\\(\\);</script>", "");
		text = text.replaceAll("\n|\r\n", "\r\n");
		return new Chapter(title, text);
	}
	
//	public static void main(String[] args)
//	{
//		DL_biquge dl = new DL_biquge("武炼巅峰");
//		BookBasicInfo bbi = dl.getBookinfos().get(0);
//		dl.SaveIntoFile(bbi);
//	}
}
