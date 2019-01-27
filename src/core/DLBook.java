package core;

import java.util.ArrayList;

import static Config.config.config;
import ui.PanelControl;

public abstract class DLBook {
	protected PanelControl pc = null;//绘图面板
	protected ArrayList<BookBasicInfo> bookinfos = null;//获取到的所有书籍信息
	protected ArrayList<Chapter> chapters = new ArrayList<Chapter>();//获取到的所有章节信息
	protected String websitename = null;//本书的站点
	protected int poolsize = 8;//本书的多线程大小
	
	/*实现这3个方法可以添加任意网站进行下载
	 * getBookInfoByKey用于根据搜索关键字返回搜索结果
	 * getCatalog用于根据getBookInfoByKey中的书籍的URL地址返回该书所有章节的URL地址
	 * getChapters用于根据getCatalog中的章节URL地址返回章节名和内容
	 * */
	protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
	protected abstract ArrayList<String> getCatalog(String Url);
	protected abstract Chapter getChapters(String Url);
	
	public abstract void SaveIntoFile(BookBasicInfo bookinfo);
	
	public DLBook(PanelControl pc)
	{	
		if(config.getWebsites().get(this.getClass().getName()) != null)
		{
			poolsize = config.getWebsites().get(this.getClass().getName()).getPoolsize();
			websitename = config.getWebsites().get(this.getClass().getName()).getWebsitename();
		}
		else
		{
			poolsize = 8;
			websitename = this.getClass().getName();
		}		
		if(poolsize <= 0 || poolsize >= 16) poolsize = 8;
		
		this.pc = pc;
	}

	//进行搜索
	public void StartSearch(String key)
	{
		bookinfos = getBookInfoByKey(key);
	}
	
	//返回搜索结果集
	public ArrayList<BookBasicInfo> getBookinfos() {
		return bookinfos;
	}
	
	//返回站点名
	public String getWebsitename() {
		return websitename;
	}
}
