package ui;

import static Config.config.*;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class TextFieldStat extends JTextField{
	public TextFieldStat()
	{
		setBounds(5, config.getFrameh()-35-30+5, config.getFramew()-15, 25);
		setEditable(false);			
	}
}
