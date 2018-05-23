package Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class OrderProperty extends LinkedHashMap<String, String>{

	public void load(FileReader fis) throws IOException
	{
		String line = null;
		BufferedReader br = new BufferedReader(fis);
		while((line = br.readLine()) != null)
		{
			String[] values = line.split("=", 2);
			if(values.length == 1) continue;
			put(values[0], values[1]);
		}
	}
	
	public String getProperty(String key)
	{
		return this.get(key);
	}
	
	public String getProperty(String key, String defaultvalue)
	{
		String value = null;
		value = this.get(key);
		if(value == null)
		{
			return defaultvalue;
		}
		else
		{
			return value;
		}
	}
	
	public void setProperty(String key, String value)
	{
		this.replace(key, value);
	}
	
	public void store(FileWriter fw) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(fw);
		for(String key : this.keySet())
		{
			bw.write(key + "=" + this.get(key));
			bw.newLine();
		}
		bw.flush();
	}
}
