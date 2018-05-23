package dao;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import Config.OrderProperty;
import core.BookBasicInfo;
import core.Chapter;
import ui.PanelControl;

import static Config.config.*;

public class DbControl {
	private Connection con = null;
	private PanelControl pc = null;
	
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
						"  PRIMARY KEY (`bookid`)" + 
						") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
				cs.addBatch("CREATE TABLE  IF NOT EXISTS `chapters` (" + 
						"  `chaptername` varchar(128) DEFAULT NULL," + 
						"  `html` mediumtext," + 
						"  `bookid` int(11) NOT NULL," + 
						"  `chapterid` int(11) NOT NULL," + 
						"  PRIMARY KEY (`bookid`,`chapterid`)," + 
						"  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`bookid`) REFERENCES `books` (`bookid`)" + 
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
				cs.addBatch("DROP PROCEDURE IF EXISTS insert_bookinfo;");
				cs.addBatch("DROP PROCEDURE IF EXISTS query_bookinfo;");
				cs.addBatch("CREATE PROCEDURE `insert_bookinfo`(IN `bookname` varchar(60),IN `author` varchar(60),IN `lastchapter` varchar(128),IN `isfinal` tinyint,IN `websitename` varchar(60),IN `websiteurl` varchar(128),OUT `bookid` int,OUT `chapterid` int)" + 
						"BEGIN" + 
						"  DECLARE num int DEFAULT -1;" + 
						"  DECLARE finalchapter VARCHAR(60);" + 
						"  DECLARE weburl VARCHAR(128);" + 
						"  DECLARE finalflag TINYINT;" + 
						"  SELECT books.bookid,books.lastchapter,books.websiteurl,books.isfinal" + 
						"  into num,finalchapter,weburl,isfinal FROM books" + 
						"  where books.bookname=bookname and books.author=author and books.websitename=websitename;" + 
						"  IF (num>0) THEN" + 
						"    SET bookid = num;" + 
						"    SELECT MAX(chapters.chapterid) into chapterid FROM chapters where chapters.bookid=num;" + 
						"    IF (finalchapter != lastchapter OR weburl != websiteurl  OR finalflag != isfinal) THEN" + 
						"      UPDATE books SET books.lastchapter=lastchapter,books.isfinal=isfinal,books.websiteurl=websiteurl WHERE books.bookid = bookid;  \r\n" + 
						"    END IF;" + 
						"  ELSE" + 
						"    INSERT INTO books(books.author,books.bookname,books.isfinal,books.lastchapter,books.websitename,books.websiteurl)" + 
						"    VALUES(author,bookname,isfinal,lastchapter,websitename,websiteurl);" + 
						"    SELECT books.bookid into bookid FROM books where books.bookname=bookname and books.author=author and books.websitename=websitename;     set chapterid =0;" + 
						"  END IF;" + 
						"END;");
				cs.addBatch("CREATE PROCEDURE `query_bookinfo` (IN `bookname` varchar(60),IN `author` varchar(60),IN `websitename` varchar(60),OUT `id` int)" + 
						"BEGIN" + 
						"  DECLARE num int DEFAULT -1;" + 
						"  SELECT books.bookid into num FROM books where books.bookname=bookname and books.author=author and books.websitename=websitename;" + 
						"  IF (num>0) THEN" + 
						"    SELECT MAX(chapters.chapterid) into id FROM chapters where chapters.bookid=num;" + 
						"  ELSE" + 
						"    SET id = 0;" + 
						"  END IF;" + 
						"END;");
				cs.executeBatch();
				con.commit();
				pc.setStateMsg("数据库配置完成",true);
				WriteStateInfoFile();
			} catch (SQLException e) {
				config.setDatabase_state(2);
				pc.setStateMsg("初始化数据库失败,如需使用数据库，在确认配置后重新打开本程序。",true);
				e.printStackTrace();
				return;
			}
			finally
			{
				config.setDatabase_state(1);
				try {
					if(cs != null) cs.close();
					if(con != null) con.close();
					
				} catch (SQLException e) {
					System.out.println("关闭资源失败(CreateDb)");
					e.printStackTrace();
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
	
	public DbControl(PanelControl pc)
	{
		this.pc = pc;
	}
	
	private void OpenConnection() throws SQLException
	{
		con = DriverManager.getConnection(config.getDburl() + config.getDatabase() + "?useSSL=false", config.getUsername(), config.getPassword());
	}
	
	private void CloseConnection() throws SQLException
	{
		con.close();
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
	public void AddBook(BookBasicInfo bookinfo, ArrayList<Chapter> chapters)
	{
		if (config.getDatabase_state() != 1) return;
		CallableStatement cs = null;
		PreparedStatement ps = null;
		
		try {
			this.OpenConnection();
			cs = con.prepareCall("call insert_bookinfo(?,?,?,?,?,?,?,?)");
			cs.setString(1, bookinfo.getBookName());
			cs.setString(2, bookinfo.getAuthor());
			cs.setString(3, bookinfo.getLastChapter());
			cs.setInt(4, bookinfo.isIsfinal()?1:0);
			cs.setString(5, bookinfo.getWebsite());
			cs.setString(6, bookinfo.getBookUrl());
			cs.registerOutParameter(7, Types.INTEGER);
			cs.registerOutParameter(8, Types.INTEGER);
			cs.execute();
			int bookid = cs.getInt(7);
			int chapterid = cs.getInt(8);
			
			con.setAutoCommit(false);
			ps = con.prepareStatement("insert into chapters values(?,?,?,?)");
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
				if (cs != null) cs.close();
				if (ps != null) ps.close();
				if (con != null) this.CloseConnection();
			} catch (SQLException e) {
				System.out.println("操作数据库失败(AddBook),关闭资源失败"+e.getMessage());
				e.printStackTrace();
				return;
			}		
		}
	}
	
	//查询书籍的最新章节信息，如果没有书籍，则返回-1或0
	public int queryBookInfo(BookBasicInfo bookinfo)
	{
		if (config.getDatabase_state()  != 1) return -1;
		CallableStatement cs = null;
		int id = 0;
		
		try {
			this.OpenConnection();
			cs = con.prepareCall("call query_bookinfo(?,?,?,?)");
			cs.setString(1, bookinfo.getBookName());
			cs.setString(2, bookinfo.getAuthor());
			cs.setString(3, bookinfo.getWebsite());
			cs.registerOutParameter(4, Types.INTEGER);
			cs.execute();
			id = cs.getInt(4);
			return id;
		} catch (SQLException e) {
			System.out.println("操作数据库失败(queryBookInfo):"+e.getMessage());
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try {
				if (cs != null) cs.close();
				if (con != null) this.CloseConnection();
			} catch (SQLException e) {
				System.out.println("操作数据库失败，关闭资源失败(queryBookInfo)"+e.getMessage());
				e.printStackTrace();
				return -1;
			}		
		}
	}
	
	//获取当前缓存的章节内容
	public ArrayList<Chapter> getbookchapters(BookBasicInfo bookinfo)
	{
		PreparedStatement ps = null;
		ArrayList<Chapter> chapters = new ArrayList<Chapter>();
		if (config.getDatabase_state()  != 1) return chapters;
		try {
			this.OpenConnection();
			ps = con.prepareStatement("select bookid from books where bookname=? and author=? and websitename=?;");
			ps.setString(1, bookinfo.getBookName());
			ps.setString(2, bookinfo.getAuthor());
			ps.setString(3, bookinfo.getWebsite());
			ResultSet rs = ps.executeQuery();
			rs.last();
			int bookid = rs.getInt(1);
			ps.close();
			
			ps = con.prepareStatement("select chaptername,html from chapters where bookid=? order by chapterid;");
			ps.setInt(1, bookid);
			rs = ps.executeQuery();
			while(rs.next())
			{
				chapters.add(new Chapter(rs.getString(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			System.out.println("操作数据库失败(getbookchapters):"+e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally
		{
			try {
				if (ps != null) ps.close();
				if (con != null) this.CloseConnection();
			} catch (SQLException e) {
				System.out.println("操作数据库失败，关闭资源失败(getbookchapters):"+e.getMessage());
				e.printStackTrace();
				return null;
			}		
		}
		
		return chapters;
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
				if(con!=null) con.close();
			} catch (SQLException e) {
				System.out.println("关闭sql失败(queryallbooks):"+e.getMessage());
				e.printStackTrace();
				return bookinfos; 
			}
			
		}
		return bookinfos;
	}
}
