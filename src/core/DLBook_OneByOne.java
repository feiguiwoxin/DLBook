package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ui.PanelControl;

//适用于逐章下载，即下载了一章之后才能获取到下一章节的地址的场景
public abstract class DLBook_OneByOne extends DLBook{

	public DLBook_OneByOne(PanelControl pc) {
		super(pc);
	}
	
	protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
	/*第一个元素存放章节数目，如果获取不到，就存放为max
	*第二个元素存放起始章节地址
	*第三个元素存放最终章节地址，如果获取不到，就存放为null*/
	protected abstract ArrayList<String> getCatalog(String Url);
	//必须要在Chapter中填入nextUrl
	protected abstract Chapter getChapters(String Url);
	
	public void SaveIntoFile(BookBasicInfo bookinfo)
	{
		String filename = "./" + bookinfo.getBookName() + " " + bookinfo.getAuthor() + ".txt";
		BufferedWriter bw = null;
		int failnum = 0;
		
		pc.setStateMsg(String.format("书籍信息 书名:%s 作者:%s 网址:%s", bookinfo.getBookName(),bookinfo.getAuthor(),bookinfo.getBookUrl()), true, Thread.currentThread().getStackTrace()[1]);
		failnum = DLChapters(bookinfo);		

		pc.setStateMsg("将数据写入txt文本中",true, Thread.currentThread().getStackTrace()[1]);
		try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));	
			
			for(Chapter c : chapters)
			{
				c.format2text();
				bw.write("ψψψψ" + c.getTitle() + "\r\n");
				bw.write(c.getText() + "\r\n");
			}

			if(0 == chapters.size())
			{
				pc.setStateMsg("读取网络目录失败，数据库无数据，没有保存任何数据", true, Thread.currentThread().getStackTrace()[1]);
			}
			else
			{
				pc.setStateMsg("写入完成o(∩_∩)o,失败章节数"+failnum, true, Thread.currentThread().getStackTrace()[1]);
			}
			chapters.clear();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("写入文件失败" + filename);
			return;
		}
		finally
		{
			try {
				if (bw!=null) bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("写入文件失败" + filename);
				return;
			}
		}
	}
	
	/*根据几个重要的抽象方法多线程下载小说到本地缓存。
	 * 分发线程的时候同时放入一个id用于记录章节的顺序，
	 * 待全部下载完成后，根据id的顺序进行排序，保证顺序不会错误*/
	private int DLChapters(BookBasicInfo bookinfo)
	{
		if(bookinfo.getBookUrl() == null) return -1;
		pc.setStateMsg("从网络中获取目录",true, Thread.currentThread().getStackTrace()[1]);
		ArrayList<String> catalogs = getCatalog(bookinfo.getBookUrl());
		if(catalogs == null || catalogs.size()<= 0)
		{
			pc.setStateMsg("获取目录失败",true, Thread.currentThread().getStackTrace()[1]);
			return -1;
		}
		
		DLChaptersOneByOne(catalogs);
		return 0;	
	}
	
	/*无法获取目录则逐章下载，下载完成后返回下一章的链接*/
	private void DLChaptersOneByOne(ArrayList<String> catalogs)
	{
		int chapternum = 0;
		try
		{
			chapternum = Integer.parseInt(catalogs.get(0));
		}
		catch(Exception e)
		{
			chapternum = -1;
		}
		
		String fristurl = catalogs.get(1);
		String endurl = catalogs.get(2);
		String stringchapternum = chapternum == -1? "-" : String.valueOf(chapternum);
		Chapter chapter = null;
		int id = 0;
		
		do
		{
			id++;
			pc.setStateMsg(String.format("逐章下载中，目前下载章节数:%d/%s", id, stringchapternum),false, Thread.currentThread().getStackTrace()[1]);
			chapter = this.getChapters(fristurl);
			if(chapter == null)
			{
				pc.setStateMsg(String.format("下载未完成，停止章节为%d,地址为%s", id, fristurl), true, Thread.currentThread().getStackTrace()[1]);
				return;
			}
			
			chapter.setId(id);
			fristurl = chapter.getNextUrl();
			chapter.format2html();
			chapters.add(chapter);
			
			if(id >= 80000)
			{
				pc.setStateMsg(String.format("下载章节数超过80000，强制停止避免死机"),true, Thread.currentThread().getStackTrace()[1]);
				break;
			}
		}
		while(!fristurl.equals(endurl) && fristurl!= null);
	}

}
