package Config;

import java.awt.Toolkit;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

public class config {
	public static final config config = new config();
	
	private String username = null;
	private String password = null;
	private String database = null;
	private String ip = null;
	private String port = null;
	private int database_state = 0;
	private boolean can_delete = false;
	private String dburl = null;
	private LinkedHashMap<String, Integer> websites = new LinkedHashMap<String, Integer>();
	private int framew = 0;
	private int frameh = 0;
	private int screenwidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private int screenwheight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	
	private config()
	{
		websites.put("website.DL_79xs", 8);
		websites.put("website.DL_biquge", 8);
		websites.put("website.DL_bookbao8", 3);
		websites.put("website.DL_shushu8", 8);
		
		OrderProperty pro = new OrderProperty();
		FileReader fr = null;
		try {
			fr = new FileReader("./config.properity");
			pro.load(fr);
			username = pro.getProperty("username");
			password = pro.getProperty("password");
			database = pro.getProperty("database");
			ip = pro.getProperty("server_ip","127.0.0.1");
			port = pro.getProperty("port", "3306");
			database_state = Integer.parseInt(pro.getProperty("database_state", "0"));
			dburl = "jdbc:mysql://"+ip+":"+port+"/";
			
			framew = Integer.parseInt(pro.getProperty("width", "480"));
			frameh = Integer.parseInt(pro.getProperty("height", "600"));
			framew = framew <= 480 ? 480 : framew;  
			frameh = frameh <= 200 ? 200 : frameh;
			framew = framew >= screenwidth? screenwidth : framew;
			frameh = frameh >= screenwheight? screenwheight : frameh;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "读取配置文件config.properity失败，请将该文件放置在当前java程序的同一级目录。",
											"错误说明", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		finally
		{	
			try {
				if(fr != null) fr.close();
			} catch (IOException e) {
				System.out.println("配置文件关闭失败，配置文件可能被清空");
				e.printStackTrace();
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}

	public int getDatabase_state() {
		return database_state;
	}

	public String getDburl() {
		return dburl;
	}

	public LinkedHashMap<String, Integer> getWebsites() {
		return websites;
	}

	public int getFramew() {
		return framew;
	}

	public int getFrameh() {
		return frameh;
	}

	public int getScreenwidth() {
		return screenwidth;
	}

	public int getScreenwheight() {
		return screenwheight;
	}

	public void setDatabase_state(int database_state) {
		this.database_state = database_state;
	}

	public boolean isCan_delete() {
		return can_delete;
	}

	public void setCan_delete(boolean can_delete) {
		this.can_delete = can_delete;
	}
}
