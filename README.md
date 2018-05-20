# 功能说明
用于搜索小说，并多线程下载小说。如果配置了mysql数据库，还能够将下载的小说按章节缓存到数据库中。 
下载策略为：  
1. 在指定的网站（可以通过实现一个抽象方法来动态添加网站，方法见后文）搜索小说，返回结果列表；
2. 搜索数据库，如果在数据库中能找到相关数据，则从数据库中读取数据；
3. 从网络中读取目录，根据目录章节数与数据库中取得的章节数对比。如果章节数少于数据库中的数据，则不缓存网络章节。如果多于数据库章节数，则缓存多余的部分；
4. 将新增的章节填入数据库（如果有的话），将数据库获取到的数据和网络中获取到的数据存入txt中。 
PS：DLBookLog为运行日志，如果出现软件运行结果与预测不符合，可以查看日志。

# 添加网站的方法
## 实现DLBook中的3个抽象方法
```
//根据搜索关键字返回一个搜索结果列表
protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);
//根据小说的网址返回小说的目录信息
protected abstract ArrayList<String> getCatalog(String Url);
//根据小说章节地址返回小说的章节内容
protected abstract Chapter getChapters(String Url);
```
注意：  
1. getBookInfoByKey返回的BookBasicInfo中的BookUrl将用于getCatalog的输入，getCatalog返回的Url用于getChapters的输入；
2. 尽量对getChapters中的网页内容做前期处理(比如一些广告什么的)，这会使得输出的格式更加合乎阅读要求。 
 
## 在config类中增加以上新增的类路径  
```
websites.put("website.DL_79xs", 8);
websites.put("website.DL_biquge", 8);
websites.put("website.DL_bookbao8", 2);
websites.put("website.DL_shushu8", 8);
```
类似上面那样增加类路径和对该网站下载时使用的多线程数（不得超过16线程，不能低于1线程，否则会被强制为8线程）。  
如果一些网站下载的时候出现一大片因为连接超时导致的下载失败，可以尝试降低线程数。  
PS:使用System.out.println()将直接输出到DLBookLog运行日志中。 


# 配置mysql数据库

如果要实现将数据入数据的功能，要对数据库做一些配置：  
1. 网上搜寻一下，将mysql数据的编码字符修改为UTF-8  
2. 在config.properity配置mysql数据库帐号，密码，数据库名。  
3. 在数据库中增加如下数据库、数据表和存储过程。

## 数据库：
```
CREATE DATABASE book DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
```
## 数据表： 
```
CREATE TABLE `books` (  
  `bookid` int(11) NOT NULL AUTO_INCREMENT,  
  `bookname` varchar(60) DEFAULT NULL,  
  `author` varchar(60) DEFAULT NULL,  
  `lastchapter` varchar(128) DEFAULT NULL,  
  `isfinal` tinyint(1) DEFAULT NULL,  
  `updatetime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  
  `websitename` varchar(60) NOT NULL,  
  `websiteurl` varchar(128) NOT NULL,  
  PRIMARY KEY (`bookid`)  
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `chapters` (  
  `chaptername` varchar(128) DEFAULT NULL,  
  `html` mediumtext,  
  `bookid` int(11) NOT NULL,  
  `chapterid` int(11) NOT NULL,  
  PRIMARY KEY (`bookid`,`chapterid`),  
  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`bookid`) REFERENCES `books` (`bookid`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8;  
```
## 存储过程  
```
DELIMITER ;;

CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_bookinfo`(IN `bookname` varchar(60),IN `author` varchar(60),IN `lastchapter` varchar(128),IN `isfinal` tinyint,IN `websitename` varchar(60),IN `websiteurl` varchar(128),OUT `bookid` int,OUT `chapterid` int)  
BEGIN  
  DECLARE num int DEFAULT -1;  
  DECLARE finalchapter VARCHAR(60);    
  DECLARE weburl VARCHAR(128);  
  DECLARE finalflag TINYINT;  
  SELECT books.bookid,books.lastchapter,books.websiteurl,books.isfinal  
  into num,finalchapter,weburl,isfinal FROM books  
  where books.bookname=bookname and books.author=author and books.websitename=websitename;  
  IF (num>0) THEN  
    SET bookid = num;  
    SELECT MAX(chapters.chapterid) into chapterid FROM chapters where chapters.bookid=num;  
    IF (finalchapter != lastchapter OR weburl != websiteurl  OR finalflag != isfinal) THEN  
      UPDATE books SET books.lastchapter=lastchapter,books.isfinal=isfinal,books.websiteurl=websiteurl WHERE books.bookid = bookid;  
    END IF;  
  ELSE  
    INSERT INTO books(books.author,books.bookname,books.isfinal,books.lastchapter,books.websitename,books.websiteurl)   
    VALUES(author,bookname,isfinal,lastchapter,websitename,websiteurl);  
    SELECT books.bookid into bookid FROM books where books.bookname=bookname and books.author=author and books.websitename=websitename;     set chapterid =0;  
  END IF;  
END;;  

CREATE DEFINER=`root`@`localhost` PROCEDURE `query_bookinfo`(IN `bookname` varchar(60),IN `author` varchar(60),IN `websitename` varchar(60),OUT `id` int)  
BEGIN  
  DECLARE num int DEFAULT -1;  
  SELECT books.bookid into num FROM books where books.bookname=bookname and books.author=author and books.websitename=websitename;  
  IF (num>0) THEN  
    SELECT MAX(chapters.chapterid) into id FROM chapters where chapters.bookid=num;  
  ELSE  
    SET id = 0;  
  END IF;  
END;;  

DELIMITER ;  
```

