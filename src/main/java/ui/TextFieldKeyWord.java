package ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import static config.Config.*;

@SuppressWarnings("serial")
public class TextFieldKeyWord extends JTextField{
	private PanelControl pc = null;
	
	public TextFieldKeyWord(PanelControl panelcontrol)
	{
		this.pc = panelcontrol;
		setBounds(5, 5, config.getFramew()-300-35, 25);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					pc.doSearch();
				}
			}
		});				
	}
}
