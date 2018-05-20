package ui;

import core.BookBasicInfo;
import core.DLBook;

public class BookList {
	private BookBasicInfo bookinfo = null;
	private DLBook dlbook = null;
	private int threadsize = 0;
	
	public BookList(BookBasicInfo bookinfo, DLBook dlbook,int threadsize)
	{
		this.bookinfo = bookinfo;
		this.dlbook = dlbook;
		this.threadsize = threadsize;
	}

	public BookBasicInfo getbookinfo() {
		return bookinfo;
	}

	public DLBook getDlbook() {
		return dlbook;
	}

	public int getThreadsize() {
		return threadsize;
	}	
}
