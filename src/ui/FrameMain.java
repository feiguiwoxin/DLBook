package ui;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;

import javax.swing.JFrame;
import static Config.config.*;

@SuppressWarnings("serial")
public class FrameMain extends JFrame {

	public static void main(String[] args) throws FileNotFoundException {
		System.setOut(new PrintStream("DLBookLog.txt"));
		System.out.println(String.format("%1$tF %1$tT", new Date()));
		FrameMain frame = new FrameMain();
		frame.setVisible(true);
	}

	public FrameMain() 
	{
		setMainFrame();
		PanelControl pc = new PanelControl();
		setContentPane(pc);
	}
	
	private void setMainFrame()
	{
		setResizable(false);
		setTitle("小说下载器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		int screenwidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int screenwheight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		setBounds(screenwidth/2 - framew/2, screenwheight/2- frameh/2, framew, frameh);
	}
}
