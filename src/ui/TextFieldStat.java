package ui;

import static Config.config.*;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class TextFieldStat extends JTextField{
	public TextFieldStat()
	{
		setBounds(5, frameh-35-30+5, framew-15, 25);
		setColumns(10);
		setEditable(false);			
	}
}
