package config;

public class Websiteinfo {
	private int poolsize;
	private String websitename;
	private boolean webswitch = true;
	
	public Websiteinfo(int poolsize, String websitename)
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
