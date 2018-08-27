package Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class OrderProperty extends LinkedHashMap<String, String>{

	public void load(FileInputStream fis) throws IOException
	{
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, "utf-8"));
		while((line = br.readLine()) != null)
		{
			if(line.charAt(0) == '#')
			{
				put(line,null);
			}
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
	
	public void store(FileOutputStream fos) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
		for(String key : this.keySet())
		{
			if(key.charAt(0) == '#')
			{
				bw.write(key);
			}
			else
			{
				bw.write(key + "=" + this.get(key));
			}
			bw.newLine();
		}
		bw.flush();
	}
	
	public static void ModifyAndSave(String key, String value)
	{
		OrderProperty pro = new OrderProperty();
		FileInputStream fr = null;
		FileOutputStream fw = null;
		try {
			fr = new FileInputStream("./config.properity");
			pro.load(fr);
			fr.close();
			fw = new FileOutputStream("./config.properity");
			pro.setProperty(key, value);
			pro.store(fw);
		} catch (Exception e) {
			System.out.println("写入"+key+"失败"+e.getMessage());
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
}
