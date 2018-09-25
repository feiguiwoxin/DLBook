package ui;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import core.BookBasicInfo;

@SuppressWarnings("serial")
public class TableResultList extends JTable{
	private PanelControl pc = null;
	private String[] columnNames = {"书名", "作者", "最后更新", "状态", "来源"};
	
	private class SearchResult extends AbstractTableModel{
		private int columns = columnNames.length;
		ArrayList<BookList> booklists = null;
		
		public SearchResult(ArrayList<BookList> booklists)
		{
			this.booklists = booklists;
		}
		
		@Override
		public int getRowCount() {
			if(booklists == null) return 0;
			return booklists.size();
		}

		@Override
		public int getColumnCount() {
			return columns;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String result = null;
			if(booklists == null) return null;
			
			BookBasicInfo bookinfo = booklists.get(rowIndex).getbookinfo();
			switch(columnIndex)
			{
			case 0:
				result = bookinfo.getBookName();
				break;
			case 1:
				result = bookinfo.getAuthor();
				break;
			case 2:
				result = bookinfo.getLastChapter();
				break;
			case 3:
				result = bookinfo.isIsfinal()?"完结":"连载中";
				break;
			case 4:
				result = bookinfo.getWebsite();
				break;			
			}
			return result;
		}
		
		@Override
		public String getColumnName(int column)
		{
			return columnNames[column];
		}
	}
	
	private class selectlist implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e) {
			pc.setselection_pos(getSelectedRow());		
		}
	}
	
	public TableResultList(PanelControl pc)
	{
		this.pc = pc;
		getTableHeader().setReorderingAllowed(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(new selectlist());
		flashtable(null);
	}
	
	public void flashtable(ArrayList<BookList> booklists)
	{
		SearchResult sr = new SearchResult(booklists);
		setModel(sr);
	}
}
