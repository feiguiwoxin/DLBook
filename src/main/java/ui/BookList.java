package ui;

import core.BookBasicInfo;
import core.DLBook;

public class BookList {
	private BookBasicInfo bookinfo = null;
	private DLBook dlbook = null;
	
	public BookList(BookBasicInfo bookinfo, DLBook dlbook)
	{
		this.bookinfo = bookinfo;
		this.dlbook = dlbook;
	}

	public BookBasicInfo getbookinfo() {
		return bookinfo;
	}

	public DLBook getDlbook() {
		return dlbook;
	}
}
