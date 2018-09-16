package ui;

import static Config.config.config;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import Config.OrderProperty;
import Config.websiteinfo;

@SuppressWarnings("serial")
public class DialogSetting extends JDialog{
	private final int width = 180;
	private final int height = 300;
	private ButtonConfirm confirm = new ButtonConfirm();
	private ScorllSetting setting = new ScorllSetting();
	
	private class ButtonConfirm extends JButton
	{
		public ButtonConfirm()
		{
			setBounds(45, 235, 90, 30);
			setText("确认");
			
			this.addMouseListener(new MouseAdapter() 
			{
				public void mousePressed(MouseEvent e)
				{
					boolean frist = true;
					StringBuilder switchs = new StringBuilder();
					for(String website : config.getWebsites().keySet())
					{
						JCheckBox jcb = setting.getcheckboxs().get(website);
						String current_switch = jcb.isSelected()? "1" : "0";
						config.getWebsites().get(website).setWebswitch(jcb.isSelected());
						if(!frist)
						{
							switchs.append(",");
						}
						switchs.append(current_switch);
						frist = false;
					}				
					OrderProperty.ModifyAndSave("search_switch", switchs.toString());
					
					DialogSetting.this.dispose();
				}
			});
		}
	}
	
	private class ScorllSetting extends JScrollPane
	{
		private LinkedHashMap<String, JCheckBox> checkboxs = new LinkedHashMap<String, JCheckBox>();
		
		public ScorllSetting()
		{
			boolean webswitch = false;
			
			setBounds(5, 5, 165, 225);
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			for(String website : config.getWebsites().keySet())
			{
				websiteinfo webinfo = config.getWebsites().get(website);
				JCheckBox tmpbox = new JCheckBox(webinfo.getWebsitename());
				webswitch = webinfo.getWebswitch();
				tmpbox.setSelected(webswitch);
				
				checkboxs.put(website, tmpbox);
				jp.add(tmpbox);
			}
			this.setViewportView(jp);
		}
		
		public LinkedHashMap<String, JCheckBox> getcheckboxs()
		{
			return checkboxs;
		}
	}

	public DialogSetting()
	{
		setResizable(false);
		setTitle("搜索引擎设置");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
		setBounds(config.getScreenwidth()/2 - width/2, config.getScreenwheight()/2- height/2, width, height);
		setModal(true);
		setLayout(null);
		add(confirm);
		add(setting);
		
		setVisible(true);
	}

}
