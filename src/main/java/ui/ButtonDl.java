package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import static config.Config.*;

@SuppressWarnings("serial")
public class ButtonDl extends JButton{
	private PanelControl pc = null;
	
	private class ClickDl extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			if (isEnabled() && pc.getselection_pos() >= 0)
			{
				if(config.getDatabase_state()  == 0)
				{
					JOptionPane.showMessageDialog(null, "请稍候，后台正在初始化数据库资源", "提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				pc.startDl();
			}
		}
	}
	
	public ButtonDl(PanelControl pc)
	{
		this.pc = pc;
		setText("下载");
		setEnabled(false);
		setBounds(config.getFramew() - 75 - 10, 5, 75, 25);
		addMouseListener(new ClickDl());
	}
}
