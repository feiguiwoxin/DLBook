package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import static Config.config.*;

@SuppressWarnings("serial")
public class ButtonDl extends JButton{
	private PanelControl pc = null;
	
	private class ClickDl extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			if (isEnabled() && pc.getselection_pos() >= 0)
			{
				pc.startDl();
			}
		}
	}
	
	public ButtonDl(PanelControl pc)
	{
		this.pc = pc;
		setText("下载");
		setEnabled(false);
		setBounds(framew - 75 - 10, 5, 75, 25);
		addMouseListener(new ClickDl());
	}
}
