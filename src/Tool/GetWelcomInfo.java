package Tool;

import core.BookBasicInfo;

public interface GetWelcomInfo {
	/*如果要多线程爬取搜索结果，必须要实现这个方法
	 * url 入参，欢迎页面地址
	 * htmlinfo 根据url获取到的html信息
	 * 此处之所以传入url是为了防止欢迎页面中直接包含目录导致获取不到目录地址
	 * */
	BookBasicInfo getbookinfoByhtmlinfo(String url, String htmlinfo);
}
