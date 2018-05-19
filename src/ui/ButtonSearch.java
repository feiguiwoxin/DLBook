package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import static Config.config.*;

import javax.swing.JButton;
import core.DLBook;

@SuppressWarnings("serial")
public class ButtonSearch extends JButton{
	private PanelControl pc = null;
	private String lastkey = null;
	
	private class MouseClick implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!isEnabled()) return;
			int index = 0;
			
			String key = pc.getKey().trim();
			if(key.equals(lastkey) || key.length() == 0)
			{
				pc.setStateMsg("两次搜索结果一致或关键字为空，不进行搜索",false);
				return;
			}
			lastkey = key;
			pc.emptySearchRst();
			pc.setStateMsg(String.format("开始搜索%s(0/%d)", key, websites.length),true);
			setEnabled(false);
			setText("搜索中...");
			paintImmediately(0, 0, getWidth(), getHeight());
			for(String website : websites)
			{
				try {
					Class<?> cls = Class.forName(website);
					Constructor<?> con = cls.getConstructor(String.class,PanelControl.class);
					DLBook dlbook = (DLBook) con.newInstance(key, pc);
					pc.addBookinfos(dlbook);
					index++;
					pc.setStateMsg(String.format("搜索进度(%d/%d)", index, websites.length),true);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.out.println("类操作失败，类名"+website);
					continue;
				}
			}
			pc.setStateMsg("搜索完成",true);
			pc.flashtablelist();
			setEnabled(true);
			setText("搜索");
		}
	}
	
	public ButtonSearch(PanelControl pc)
	{
		this.pc = pc;
		setText("搜索");
		setBounds(framew - 150 - 15, 5, 75, 25);
		this.addActionListener(new MouseClick());
	}
}
