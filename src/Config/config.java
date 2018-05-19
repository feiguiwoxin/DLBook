package Config;

import java.io.FileReader;
import java.util.Properties;

import javax.swing.JOptionPane;

public class config {
	public static String username = null;
	public static String password = null;
	public static String dburl = "jdbc:mysql://127.0.0.1:3306/";
	public static String[] websites = {"website.DL_79xs","website.DL_biquge","website.DL_shushu8"};
	public static int framew = 0;
	public static int frameh = 0;
	
	static
	{
		Properties pro = new Properties();
		String database = null;
		try {
			pro.load(new FileReader("./config.properity"));
			username = pro.getProperty("username");
			password = pro.getProperty("password");
			database = pro.getProperty("database");
			framew = Integer.parseInt(pro.getProperty("width"));
			frameh = Integer.parseInt(pro.getProperty("height"));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "读取配置文件config.properity失败，请将该文件放置在当前java程序的同一级目录。",
											"错误说明", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		dburl = dburl + database + "?useSSL=false";
	}
}
