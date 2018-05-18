package Config;

import java.io.FileReader;
import java.util.Properties;

public class config {
	public static String username = null;
	public static String password = null;
	public static String dburl = "jdbc:mysql://127.0.0.1:3306/";
	public static String[] websites = null;
	public static int framew = 0;
	public static int frameh = 0;
	
	static
	{
		Properties pro = new Properties();
		String database = null;
		try {
			pro.load(new FileReader("./src/config.properity"));
			username = pro.getProperty("username");
			password = pro.getProperty("password");
			database = pro.getProperty("database");
			websites = pro.getProperty("class").split(",");
			framew = Integer.parseInt(pro.getProperty("width"));
			frameh = Integer.parseInt(pro.getProperty("height"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		dburl = dburl + database + "?useSSL=false";
	}
	
	public static void main(String[] args)
	{
		new config();
	}
}
