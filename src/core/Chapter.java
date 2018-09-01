package core;

public class Chapter {
	private String title;
	private String text;
	private int id;
	private String nextUrl;
	
	public Chapter(String title,String text)
	{
		this.title = title;
		this.text = text;
	}
	
	public Chapter(String title,String text,String nextUrl)
	{
		this.title = title;
		this.text = text;
		this.nextUrl = nextUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getNextUrl() {
		return nextUrl;
	}
	
	public void format2html()
	{
		/*对网页内容进行处理，方便后续阅读
		1.由于mysql只能存储1-3字节的utf-8，因此去除4字节的utf-8，主要包含emoji表情，不影响汉字保存
		2.将<br><br/><p></p>等常见的html换行符转化为\r\n
		3.将\n全部替换为\r\n
		4.合并\r\n将空行去除
		5.为了方便网页阅读将换行换成<br><br>与2个空格
		*/
		text = text.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
		text = text.replaceAll("<br>|<p>|</p>|<br/>", "\r\n");
		text = text.replaceAll("\n|\r\n", "\r\n");
		text = text.replaceAll("　| |&nbsp;", "").replaceAll("\n[\\s]*\r", "");
		text = "　　" + text.replaceAll("\r\n", "<br><br>　　");
	}
}
