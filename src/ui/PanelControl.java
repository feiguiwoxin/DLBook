package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import core.BookBasicInfo;
import core.DLBook;


import static Config.config.*;

@SuppressWarnings("serial")
public class PanelControl extends JPanel{
	private ButtonDl buttondl = null;
	private ButtonSearch buttonsearch = null;
	private TextFieldKeyWord textfieldkeyword = null;
	private TextFieldStat textfieldstat = null;
	private TableResultList tablelist = null;
	private ArrayList<BookList> booklists = new ArrayList<BookList>();
	private int selection_pos = -1;
	
	public PanelControl()
	{
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		buttondl = new ButtonDl(this);
		buttonsearch = new ButtonSearch(this);
		textfieldkeyword = new TextFieldKeyWord(this);
		textfieldstat = new TextFieldStat();
		tablelist = new TableResultList(this);
		add(textfieldkeyword);
		add(buttonsearch);
		add(buttondl);
		addTable();
		add(textfieldstat);
		
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.getX() >= config.getFramew()-40 && e.getX()<=config.getFramew()-40+30
					&& e.getY() >= config.getFrameh()-38-30+5 && e.getY() <= config.getFrameh()-38-30+35)
				{
					PanelControl.this.setEnabled(false);
					new DialogSetting();
				}
			}
		});
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
				
		g.drawImage(config.getSettingIcon(), config.getFramew()-40, config.getFrameh()-38-30+5, 30, 30, null);
	}
	
	private void addTable()
	{
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 35, config.getFramew()-15, config.getFrameh()-35-35-30);
		scrollPane.setViewportView(tablelist);
		add(scrollPane);				
	}
	
	public String getKey()
	{
		return textfieldkeyword.getText();
	}
	
	public void addBookinfos(DLBook dlbook)
	{
		ArrayList<BookBasicInfo> bookinfos = dlbook.getBookinfos();
		if(bookinfos == null) return;
		
		for(BookBasicInfo bookinfo : bookinfos)
		{
			booklists.add(new BookList(bookinfo, dlbook));
		}
	}
	
	public void emptySearchRst()
	{
		booklists.clear();
		selection_pos = -1;
		flashtablelist();
	}
	
	public void flashtablelist()
	{
		tablelist.flashtable(booklists);
		tablelist.paintImmediately(0, 0, tablelist.getWidth(), tablelist.getHeight());
	}
	
	public void setselection_pos(int pos)
	{
		selection_pos = pos;
		if(selection_pos >= 0)
		{
			buttondl.setEnabled(true);
		}
		else
		{
			buttondl.setEnabled(false);
		}

	}
	
	public int getselection_pos()
	{
		return selection_pos;
	}
	
	private class downloadthread implements Runnable
	{
		@Override
		public void run() {
			BookList booklist = booklists.get(selection_pos);	
			DLBook dlbook = booklist.getDlbook();
			
			dlbook.SaveIntoFile(booklist.getbookinfo());
			
			UiEnabled(true);
			tablelist.setForeground(Color.BLACK);
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null, "下载完成", "消息提示",JOptionPane.INFORMATION_MESSAGE);
		}	
	}
	
	public void startDl()
	{
		UiEnabled(false);
		tablelist.setForeground(Color.GRAY);
		
		if(selection_pos < 0 || booklists.size() == 0) return;
		
		Thread t = new Thread(new downloadthread());
		t.start();
	}
	
	public void doSearch()
	{
		buttonsearch.doClick();
	}
	
	public void setStateMsg(String msg,boolean intolog)
	{
		if(msg == null) return;
		textfieldstat.setText(msg);
		if(intolog) System.out.println(msg);
		//textfieldstat.paintImmediately(0, 0, textfieldstat.getWidth(), textfieldstat.getHeight());
	}
	
	public void UiEnabled(boolean enabled)
	{
		buttonsearch.setEnabled(enabled);
		buttondl.setEnabled(enabled);
		tablelist.setEnabled(enabled);
		
		if(selection_pos < 0)
		{
			buttondl.setEnabled(false);
		}
		
		if(!enabled)
		{
			buttonsearch.paintImmediately(0, 0, buttonsearch.getWidth(), buttonsearch.getHeight());
			buttondl.paintImmediately(0, 0, buttondl.getWidth(), buttondl.getHeight());
			tablelist.paintImmediately(0, 0, tablelist.getWidth(), tablelist.getHeight());
		}		
	}
}
