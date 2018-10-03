package Config;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ui.PanelControl;

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
	private LinkedHashMap<String, websiteinfo> websites = new LinkedHashMap<String, websiteinfo>();
	private int framew = 0;
	private int frameh = 0;
	private int screenwidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private int screenwheight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private final Image setting_icon = new ImageIcon(PanelControl.class.getResource("/Image/setting.png")).getImage();
	private final Image soft_icon = new ImageIcon(PanelControl.class.getResource("/image/icon.png")).getImage();

	private SSLContext ssl = null;
	
	private config()
	{
		websites.put("website.DL_79xs", new websiteinfo(8, "79小说"));
		websites.put("website.DL_biquge", new websiteinfo(8, "笔趣阁"));
		websites.put("website.DL_bookbao8", new websiteinfo(3, "书包网"));
		websites.put("website.DL_shushu8", new websiteinfo(8, "书书吧"));
		websites.put("website.DL_hunhun520", new websiteinfo(8, "混混小说"));
		websites.put("website.DL_yubook", new websiteinfo(8, "御宅屋"));
		
		//设置ssl证书，自动忽略https的证书验证
		try {
			TrustManager[] trustallcert = {new X509TrustManager()
			{
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			}};
			ssl = SSLContext.getInstance("SSL");
			ssl.init(null, trustallcert, new SecureRandom());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		OrderProperty pro = new OrderProperty();
		FileInputStream fr = null;
		try {
			fr = new FileInputStream("./config.properity");
			pro.load(fr);
			username = pro.getProperty("username", "root");
			password = pro.getProperty("password", "mysql");
			database = pro.getProperty("database", "dlbook");
			ip = pro.getProperty("server_ip", "127.0.0.1");
			port = pro.getProperty("port", "3306");
			database_state = Integer.parseInt(pro.getProperty("database_state", "0"));
			dburl = "jdbc:mysql://"+ip+":"+port+"/";
			
			int num = 0;
			String[] switchs = pro.getProperty("search_switch", "1").split(",", websites.size());
			for(String key : websites.keySet())
			{
				if(num < switchs.length)
				{
					boolean tmpswitch = switchs[num].equals("0") ? false : true;
					websites.get(key).setWebswitch(tmpswitch);
				}
				else
				{
					websites.get(key).setWebswitch(true);
				}
				num ++;
			}
			
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

	public LinkedHashMap<String, websiteinfo> getWebsites() {
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

	public Image getSettingIcon() {
		return setting_icon;
	}
	
	public Image getSoft_icon() {
		return soft_icon;
	}

	public SSLContext getSsl() {
		return ssl;
	}
}
