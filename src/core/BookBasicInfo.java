package core;

public class BookBasicInfo {
	private String BookUrl;//书籍URL
	private String BookName;//书籍名字
	private String author;//作者名字
	private String lastChapter;//最后的章节名
	private String website;//站点名
	private boolean isfinal;//是否完结 0 未完结 1完结
	
	public String getBookUrl() {
		return BookUrl;
	}
	public void setBookUrl(String bookUrl) {
		BookUrl = bookUrl;
	}
	public String getBookName() {
		return BookName;
	}
	public void setBookName(String bookName) {
		BookName = bookName;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getLastChapter() {
		return lastChapter;
	}
	public void setLastChapter(String lastChapter) {
		this.lastChapter = lastChapter;
	}
	public boolean isIsfinal() {
		return isfinal;
	}
	public void setIsfinal(boolean isfinal) {
		this.isfinal = isfinal;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
}
