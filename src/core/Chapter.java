package core;

public class Chapter {
	private String title;
	private String text;
	private int id;
	
	public Chapter(String title,String text)
	{
		this.title = title;
		this.text = text;
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
}
