package ui;

import java.awt.Toolkit;
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
	}
	
	private void addTable()
	{
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 35, framew-15, frameh-35-35-30);
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
		buttondl.setEnabled(true);
	}
	
	public int getselection_pos()
	{
		return selection_pos;
	}
	
	public void startDl()
	{
		buttonsearch.setEnabled(false);
		buttondl.setEnabled(false);
		buttonsearch.paintImmediately(0, 0, buttonsearch.getWidth(), buttonsearch.getHeight());
		buttondl.paintImmediately(0, 0, buttondl.getWidth(), buttondl.getHeight());
		
		BookList booklist = booklists.get(selection_pos);	
		DLBook dlbook = booklist.getDlbook();
		dlbook.SaveIntoFile(booklist.getbookinfo());
	}
	
	public void finishDl()
	{
		buttonsearch.setEnabled(true);
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null, "下载完成", "消息提示",JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void doSearch()
	{
		buttonsearch.doClick();
	}
	
	public void setStateMsg(String msg,boolean intolog)
	{
		textfieldstat.setText(msg);
		if(intolog) System.out.println(msg);
		textfieldstat.paintImmediately(0, 0, textfieldstat.getWidth(), textfieldstat.getHeight());
	}
}
