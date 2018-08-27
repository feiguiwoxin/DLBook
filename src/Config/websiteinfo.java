package Config;

public class websiteinfo {
	private int poolsize;
	private String websitename;
	private boolean webswitch = true;
	
	public websiteinfo(int poolsize, String websitename)
	{
		this.poolsize = poolsize;
		this.websitename = websitename;
	}

	public int getPoolsize() {
		return poolsize;
	}

	public String getWebsitename() {
		return websitename;
	}

	public boolean getWebswitch() {
		return webswitch;
	}

	public void setWebswitch(boolean webswitch) {
		this.webswitch = webswitch;
	}
	
}
