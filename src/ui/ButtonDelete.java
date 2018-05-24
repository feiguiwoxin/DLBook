package ui;

import static Config.config.config;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ButtonDelete extends JButton{
	private PanelControl pc = null;
	
	private class ClickDelete extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			if(!isEnabled() || !config.isCan_delete()) return;		

			pc.startDelete();
		}
	}
	
	public ButtonDelete(PanelControl pc)
	{
		this.pc = pc;
		setBounds(config.getFramew() - 225 - 20, 5, 75, 25);
		setText("删除");
		setEnabled(false);
		this.addMouseListener(new ClickDelete());
	}
}
