package ui;

import javax.swing.JButton;

import core.BookBasicInfo;
import core.DLBook;
import dao.DbControl;

import static Config.config.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ButtonListBook extends JButton{
	private PanelControl pc = null; 
	
	private class ClickListBook extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			if(!isEnabled() || config.getDatabase_state() != 1) return;
			
			pc.emptySearchRst();
			ArrayList<BookBasicInfo> bookinfos = new DbControl(pc).queryallbooks();
			if(bookinfos == null || bookinfos.size() == 0) return;
			
			ArrayList<DLBook> dlbooks = new ArrayList<DLBook>();
			for(String website : config.getWebsites().keySet())
			{
				try {
					Class<?> cls = Class.forName(website);
					Constructor<?> con = cls.getConstructor(PanelControl.class, int.class);
					int poolsize = config.getWebsites().get(website);
					DLBook dlbook = (DLBook)con.newInstance(pc, poolsize);
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
			
			pc.flashtablelist();
			setEnabled(true);
		}
	}
	
	public ButtonListBook(PanelControl pc)
	{
		this.pc = pc;
		setBounds(config.getFramew() - 225 - 20, 5, 75, 25);
		setText("缓存");
		addMouseListener(new ClickListBook());
	}
}
