package dao;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Config.OrderProperty;
import core.BookBasicInfo;
import core.Chapter;
import ui.PanelControl;

import static Config.config.*;

public class DbControl {
	public final static DbControl dbcontrol = new DbControl();
	private Connection con = null;
	private PanelControl pc = null;
	
	private DbControl(){}
	
	public void setPC(PanelControl pc)
	{
		this.pc = pc;
	}
	
	private class CreateDb implements Runnable
	{
		@Override
		public void run() {
			Statement cs = null;
			pc.setStateMsg("开始配置数据库", true);
			try {
				con = DriverManager.getConnection(config.getDburl() + "mysql?useSSL=false", config.getUsername(), config.getPassword());
				con.setAutoCommit(false);
				cs = con.createStatement();
				cs.addBatch("CREATE DATABASE IF NOT EXISTS " + config.getDatabase() + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;");
				cs.addBatch("use " +  config.getDatabase() + ";");
				cs.addBatch("CREATE TABLE IF NOT EXISTS `books` (" + 
						"  `bookid` int(11) NOT NULL AUTO_INCREMENT," + 
						"  `bookname` varchar(60) DEFAULT NULL," + 
						"  `author` varchar(60) DEFAULT NULL," + 
						"  `lastchapter` varchar(128) DEFAULT NULL," + 
						"  `isfinal` tinyint(1) DEFAULT NULL," + 
						"  `updatetime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," + 
						"  `websitename` varchar(60) NOT NULL," + 
						"  `websiteurl` varchar(128) NOT NULL," + 
						"  PRIMARY KEY (`bookid`)," + 
						"  UNIQUE KEY (`bookname`, `author`, `websitename`)" + 
						") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
				cs.addBatch("CREATE TABLE  IF NOT EXISTS `chapters` (" + 
						"  `chaptername` varchar(128) DEFAULT NULL," + 
						"  `html` mediumtext," + 
						"  `bookid` int(11) NOT NULL," + 
						"  `chapterid` int(11) NOT NULL," + 
						"  PRIMARY KEY (`bookid`,`chapterid`)," + 
						"  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`bookid`) REFERENCES `books` (`bookid`)" + 
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
				cs.executeBatch();
				con.commit();
				config.setDatabase_state(1);
				pc.setStateMsg("数据库配置完成",true);
				WriteStateInfoFile();
			} catch (SQLException e) {
				config.setDatabase_state(2);
				System.out.println(e.getMessage());
				pc.setStateMsg("初始化数据库失败,如需使用数据库，在确认配置后重新打开本程序。",true);
				e.printStackTrace();
				return;
			}
			finally
			{
				try {
					if(cs != null) cs.close();
					CloseConnection();				
				} catch (SQLException e) {
					System.out.println("关闭资源失败(CreateDb)");
					e.printStackTrace();
					config.setDatabase_state(2);
					return;
				}
			}
		}	
	}
	
	private void WriteStateInfoFile()
	{
		OrderProperty pro = new OrderProperty();
		FileReader fr = null;
		FileWriter fw = null;
		try {
			fr = new FileReader("./config.properity");
			pro.load(fr);
			fr.close();
			fw = new FileWriter("./config.properity");
			pro.setProperty("database_state", "1");
			pro.store(fw);
		} catch (Exception e) {
			System.out.println("写入database_state失败"+e.getMessage());
			e.printStackTrace();
			return;
		}
		finally 
		{
			try {
				if(fw != null) fw.close();
			} catch (IOException e) {
				System.out.println("文件流关闭错误，配置文件可能被清空" + e.getMessage());
				e.printStackTrace();
				return;
			}		
		}
	}
	
	private void OpenConnection() throws SQLException
	{
		con = DriverManager.getConnection(config.getDburl() + config.getDatabase() + "?useSSL=false", config.getUsername(), config.getPassword());
	}
	
	private void CloseConnection() throws SQLException
	{
		if(con != null)
		{
			con.close();
		}
	}
	
	public void initDb()
	{
		if(config.getDatabase_state() != 0)
		{
			config.setDatabase_state(1);
			pc.setStateMsg("读取配置成功，当前状态为数据库已配置", true);
			return;
		}
		Thread t = new Thread(new CreateDb());
		t.start();		
	}
	
	//用于更新书籍，如果书籍不存在则存储并缓存所有章节。如果已有部分章节，则只缓存新增章节
	public void AddBook(BookBasicInfo bookinfo, ArrayList<Chapter> chaptersindb, ArrayList<Chapter> chapters)
	{
		if (config.getDatabase_state() != 1) return;
		PreparedStatement ps = null;
		int bookid,chapterid;
		
		try {
			this.OpenConnection();
			
			con.setAutoCommit(false);
			//用于插入或更新书籍信息
			ps = con.prepareStatement("INSERT INTO books(books.author,books.bookname,books.isfinal,books.lastchapter,books.websitename,books.websiteurl) "+
										"values(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE books.lastchapter=?,books.isfinal=?,books.websiteurl=?;");
			ps.setString(1, bookinfo.getAuthor());
			ps.setString(2, bookinfo.getBookName());
			ps.setInt(3, bookinfo.isIsfinal()?1:0);
			ps.setString(4, bookinfo.getLastChapter());		
			ps.setString(5, bookinfo.getWebsite());
			ps.setString(6, bookinfo.getBookUrl());
			ps.setString(7, bookinfo.getLastChapter());
			ps.setInt(8, bookinfo.isIsfinal()?1:0);
			ps.setString(9, bookinfo.getBookUrl());
			ps.executeUpdate();
			ps.close();
			//用于查询bookid和chapterid
			ps = con.prepareStatement("select bookid from books where author=? and bookname=? and websitename=?");
			ps.setString(1, bookinfo.getAuthor());
			ps.setString(2, bookinfo.getBookName());
			ps.setString(3, bookinfo.getWebsite());
			ResultSet rs = ps.executeQuery();
			rs.last();
			if(rs.getRow()<1)
			{
				return;
			}
			bookid = rs.getInt(1);
			ps.close();
			ps = con.prepareStatement("select max(chapterid) from chapters where bookid=?");
			ps.setInt(1, bookid);
			rs = ps.executeQuery();
			rs.last();
			if(rs.getRow() < 1)
			{
				chapterid = 0;
			}
			else
			{
				chapterid = rs.getInt(1);
			}
			ps.close();
			//插入数据
			ps = con.prepareStatement("insert into chapters values(?,?,?,?)");
			if(chaptersindb.size() > 0)
			{
				if(chapterid < chaptersindb.get(chaptersindb.size() - 1).getId())
				{
					for(Chapter chapter : chaptersindb)
					{
						if(chapter.getId() <= chapterid) continue;
						ps.setString(1, chapter.getTitle());
						ps.setString(2, chapter.getText());
						ps.setInt(3, bookid);
						ps.setInt(4, chapter.getId());
						ps.executeUpdate();
					}
				}
			}
						
			for(Chapter chapter : chapters)
			{
				if(chapter.getId() <= chapterid) continue;
				ps.setString(1, chapter.getTitle());
				ps.setString(2, chapter.getText());
				ps.setInt(3, bookid);
				ps.setInt(4, chapter.getId());
				ps.executeUpdate();
			}
			con.commit();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("操作数据库失败(AddBook):"+e.getMessage());
			return;
		}
		finally
		{
			try {
				if (ps != null) ps.close();
				CloseConnection();
			} catch (SQLException e) {
				System.out.println("操作数据库失败(AddBook),关闭资源失败"+e.getMessage());
				e.printStackTrace();
				return;
			}		
		}
	}
	
	//获取当前缓存的章节内容,返回最大章节ID，如果没有此书，则返回-1
	public int getbookchapters(BookBasicInfo bookinfo,ArrayList<Chapter> chaptersindb)
	{
		int chapterid = -1;
		PreparedStatement ps = null;
		if (config.getDatabase_state()  != 1) return -1;
		try {
			this.OpenConnection();
			
			ps = con.prepareStatement("select chapterid,chaptername,html from chapters where bookid=(select bookid from books where bookname=? and author=? and websitename=? limit 1) order by chapterid;");
			ps.setString(1, bookinfo.getBookName());
			ps.setString(2, bookinfo.getAuthor());
			ps.setString(3, bookinfo.getWebsite());
			ResultSet rs= ps.executeQuery();
			while(rs.next())
			{
				Chapter chapter = new Chapter(rs.getString(2), rs.getString(3));
				chapter.setId(rs.getInt(1));
				chaptersindb.add(chapter);
				if(rs.isLast())
				{
					chapterid = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.out.println("操作数据库失败(getbookchapters):"+e.getMessage());
			e.printStackTrace();
			return chapterid;
		}
		finally
		{
			try {
				if (ps != null) ps.close();
				CloseConnection();
			} catch (SQLException e) {
				System.out.println("操作数据库失败，关闭资源失败(getbookchapters):"+e.getMessage());
				e.printStackTrace();
				return chapterid;
			}		
		}
		
		return chapterid;
	}
	
	public ArrayList<BookBasicInfo> queryallbooks()
	{
		ArrayList<BookBasicInfo> bookinfos = new ArrayList<BookBasicInfo>();
		PreparedStatement ps = null;
		try {
			OpenConnection();
			ps = con.prepareStatement("select * from books");
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				BookBasicInfo bookinfo = new BookBasicInfo();
				bookinfo.setBookName(rs.getString(2));
				bookinfo.setAuthor(rs.getString(3));
				bookinfo.setBookUrl(rs.getString(8));
				bookinfo.setIsfinal(rs.getInt(5) == 1 ? true : false);
				bookinfo.setLastChapter(rs.getString(4));
				bookinfo.setWebsite(rs.getString(7));
				bookinfos.add(bookinfo);
			}
		} catch (SQLException e) {
			pc.setStateMsg("加载缓存失败,请检查数据库是否运行，或帐号密码是否正确", true);
			e.printStackTrace();
			return bookinfos;
		}
		finally
		{
			try {
				CloseConnection();
			} catch (SQLException e) {
				System.out.println("关闭sql失败(queryallbooks):"+e.getMessage());
				e.printStackTrace();
				return bookinfos; 
			}
			
		}
		return bookinfos;
	}
	
	public boolean DeleteBook(BookBasicInfo bookinfo)
	{
		PreparedStatement ps = null;
		int bookid;
		try {
			OpenConnection();
			con.setAutoCommit(false);
			//先锁住主键来防止新增书籍与删除书籍形成的死锁
			ps = con.prepareStatement("select bookid from books where bookname=? and author=? and websitename=? for update");
			ps.setString(1, bookinfo.getBookName());
			ps.setString(2, bookinfo.getAuthor());
			ps.setString(3, bookinfo.getWebsite());
			ResultSet rs = ps.executeQuery();
			rs.last();
			if(rs.getRow() < 1)
			{
				pc.setStateMsg("删除书籍成功(*)", true);
				return true;
			}
			bookid = rs.getInt(1);
			ps.close();
			//删除书籍
			ps = con.prepareStatement("delete from chapters where bookid=?;");
			ps.setInt(1, bookid);
			ps.executeUpdate();
			ps.close();
			ps = con.prepareStatement("delete from books where bookid=?;");
			ps.setInt(1, bookid);
			ps.executeUpdate();
			ps.close();
			
			con.commit();
			pc.setStateMsg("删除书籍成功", true);
		} catch (SQLException e) {
			pc.setStateMsg("数据库错误,删除书籍失败", true);
			e.printStackTrace();
			return false;
		}
		finally
		{
			try {
				CloseConnection();
			} catch (SQLException e) {
				System.out.println("关闭sql失败(queryallbooks):"+e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
