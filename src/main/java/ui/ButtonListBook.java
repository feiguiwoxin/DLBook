package ui;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import core.BookBasicInfo;
import core.DLBook;
import dao.DbControl;

import static config.Config.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ButtonListBook extends JButton{
	private PanelControl pc = null; 
	
	private class ClickListBook implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(!isEnabled() || config.getDatabase_state() != 1)
			{
				JOptionPane.showMessageDialog(null, "状态不可用或数据库不可用", "提示", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			pc.UiEnabled(false);
			pc.emptySearchRst();
			ArrayList<BookBasicInfo> bookinfos = DbControl.dbcontrol.queryallbooks();
			if(bookinfos.size() == 0)
			{
				pc.UiEnabled(true);
				return;
			}
			
			ArrayList<DLBook> dlbooks = new ArrayList<DLBook>();
			for(String website : config.getWebsites().keySet())
			{
				try {
					Class<?> cls = Class.forName(website);
					Constructor<?> con = cls.getConstructor(PanelControl.class);
					DLBook dlbook = (DLBook)con.newInstance(pc);
					dlbooks.add(dlbook);
				} catch (Exception e1) {
					System.out.println("加载类失败(ClickListBook):" + website);
					e1.printStackTrace();
					continue;
				}			
			}
			
			for(BookBasicInfo bookinfo : bookinfos)
			{
				for(DLBook book : dlbooks)
				{
					if(book.getWebsitename().equals(bookinfo.getWebsite()))
					{
						book.addBookinfo(bookinfo);
						break;
					}
				}
			}
			
			for(DLBook book : dlbooks)
			{
				pc.addBookinfos(book);
			}
			
			config.setCan_delete(true);
			pc.flashtablelist();
			pc.UiEnabled(true);
		}
	}
	
	public ButtonListBook(PanelControl pc)
	{
		this.pc = pc;
		setBounds(config.getFramew() - 300 - 25, 5, 75, 25);
		setText("缓存");
		addActionListener(new ClickListBook());
	}
}
