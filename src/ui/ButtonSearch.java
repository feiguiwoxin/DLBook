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
			
			String key = pc.getKey();
			if(key.equals(lastkey))
			{
				pc.setStateMsg("两次搜索结果一致，不进行搜索");
				return;
			}
			lastkey = key;
			pc.emptySearchRst();
			pc.setStateMsg(String.format("开始搜索(0/%d)", websites.length));
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
					pc.setStateMsg(String.format("搜索进度(%d/%d)", index, websites.length));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
			}
			pc.setStateMsg("搜索完成");
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
