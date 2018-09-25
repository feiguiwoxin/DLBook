package core;

public class BookBasicInfo {
	private String BookUrl;
	private String BookName;
	private String author;
	private String lastChapter;
	private String website;
	private boolean isfinal;
	
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
