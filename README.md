# 功能说明
用于搜索小说，并多线程下载小说。 
下载策略为：  
1. 在指定的网站（可以通过实现一个抽象方法来动态添加网站，方法见后文）搜索小说，返回结果列表；
2. 从网络中读取目录，按章节下载小说。  

PS：DLBookLog为运行日志，如果出现软件运行结果与预测不符合，可以查看日志。

# 配置文件说明
相关配置文件说明如下：
<table>
	<th>配置</th>
	<th>说明</th>
	<tr>
		<td>width</td>
		<td>软件宽度</td>
	</tr>
	<tr>
		<td>height</td>
		<td>软件高度</td>
	</tr>
	<tr>
		<td>search_switch</td>
		<td>控制搜索范围，请在UI界面中设置</td>
	</tr>
</table>


# 添加网站的方法
## 实现DLBook中的抽象方法
总共考虑了3种下载小说的方式：  
1、能够获取所有目录信息，则使用多线程同时对多个目录下载，需要实现DLBook_AllChapter这个类  
2、无法获取目录信息，则需要根据起始地址依次逐章节下载，需要实现DLBook_OneByOne这个类  
3、直接下载书籍，而不是按章节下载，需要实现DLBook_AllBook这个类  
如果条件允许，推荐第一种和第三种，速度更快。

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
websites.put("website.DL_79xs", new websiteinfo(8, "79小说"));
websites.put("website.DL_biquge", new websiteinfo(8, "笔趣阁"));
websites.put("website.DL_bookbao8", new websiteinfo(3, "书包网"));
websites.put("website.DL_shushu8", new websiteinfo(8, "书书吧"));
websites.put("website.DL_hunhun520", new websiteinfo(8, "混混小说"));
```
类似上面那样增加类路径和对该网站下载时使用的多线程数（不得超过16线程，不能低于1线程，否则会被强制为8线程）。  
如果一些网站下载的时候出现一大片因为连接超时导致的下载失败，可以尝试降低线程数。  
PS:使用System.out.println()将直接输出到DLBookLog运行日志中。 

## 可能用到的工具函数
```
//说明，以下这些方法并不强制需要实现或调用，而是公用方法来增加爬取效率

//根据网址和编码集获取网页内容，get方式获取
public String getHtmlInfo(String Urladdress, String charset);

//根据网址和编码集获取网页内容，post方式获取,要求输入网址，表单集，填写表单的字符编码和最终获取网页的字符编码
public String postHtmlInfo(String Urladdress, LinkedHashMap<String,String> values, String inputcharset, String outputcharset)

/*一些网站返回的搜索结果中只包含小说的欢迎页面地址，需要进一步进入这些地址才能怕取到我们想要的内容。
这两个方法用于多线程爬取这些页面，加快搜索速度*/
//需要在子类实现GetWelcomInfo接口，根据传入的网页内容返回书籍信息
public BookBasicInfo getbookinfoByhtmlinfo(String htmlinfo);
//直接调用即可，传入的参数bookurls为待爬取的url集，bookinfos中会自动填入爬取的结果，charset设置编码集，welcominfo即实现了GetWelcomInfo接口的类，poolsize为搜索线程
public static final void getbookinfos(ArrayList<String> bookurls, 
								 	 ArrayList<BookBasicInfo> bookinfos,
								 	 String charset, 
								 	 GetWelcomInfo welcominfo,
								 	 int poolsize)；
```