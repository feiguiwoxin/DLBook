package ui;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;

import javax.swing.JFrame;

import dao.DbControl;

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
		new DbControl(pc).initDb();
		setContentPane(pc);
	}
	
	private void setMainFrame()
	{
		setResizable(false);
		setTitle("小说下载器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		setBounds(config.getScreenwidth()/2 - config.getFramew()/2, config.getScreenwheight()/2- config.getFrameh()/2, config.getFramew(), config.getFrameh());
	}
}
