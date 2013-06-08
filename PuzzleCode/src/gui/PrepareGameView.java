package gui;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JRadioButton;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;

import utils.DBUtils;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class PrepareGameView extends JPanel {
	private final ButtonGroup difficultyBtnsGrp = new ButtonGroup();
	List<String> topicsList;
	private List<JCheckBox> topicsCheckBoxes;
	private JPanel centerPanel;
	private JButton goBtn;
	private JButton backBtn;
	private JPanel topicsPanel;

	public PrepareGameView() {
		initialize();
		this.setVisible(true);
	}

	static JPanel start() {
		return new PrepareGameView();
	}

	private void initialize() {

		getTopics();
		setLayout(new BorderLayout());

		JPanel difficultyPanel = new JPanel();
		add(difficultyPanel, BorderLayout.NORTH);
		difficultyPanel.setBorder(new TitledBorder(null, "Difficulty", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		difficultyPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));


		JRadioButton easyRadioBtn = new JRadioButton("Easy");
		difficultyPanel.add(easyRadioBtn);
		difficultyBtnsGrp.add(easyRadioBtn);

		JRadioButton mediumHardBtn = new JRadioButton("Medium");
		difficultyPanel.add(mediumHardBtn);
		mediumHardBtn.setSelected(true);
		difficultyBtnsGrp.add(easyRadioBtn);


		JRadioButton hardRadioBtn = new JRadioButton("Hard");
		difficultyPanel.add(hardRadioBtn);
		difficultyBtnsGrp.add(hardRadioBtn);
		centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new GridLayout(2, 1, 0, 0));

		topicsPanel = new JPanel();
		topicsPanel.setBorder(new TitledBorder(null, "Topics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		centerPanel.add(topicsPanel);
		topicsPanel.setLayout(new GridLayout(4, 2, 0, 0));

		JPanel btnPanel = new JPanel();
		centerPanel.add(btnPanel);

		goBtn = new JButton(new ImageIcon(PrepareGameView.class.getResource("/resources/forward.png")));
		goBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				goBtnClicked();
			}
		});
		backBtn = new JButton(new ImageIcon(PrepareGameView.class.getResource("/resources/back.png")));
		backBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainView.view.showWelcomeView();
			}
		});
		btnPanel.add(backBtn);
		btnPanel.add(goBtn);

		addTopicsCheckBoxes();
	}

	void goBtnClicked() {
		if (getUserSelectedTopics().size() < 1) { // must choose at least one topic
			// Dialog Box
			System.out.println("Error veze");
		}
		else {
			MainView.view.showWaitView();
		}
	}


	/**
	 * 
	 * @return user selected topics
	 */
	public List<String> getUserSelectedTopics() {
		List<String> lst = new LinkedList<String>();
		for (JCheckBox box : topicsCheckBoxes) {
			if (box.isSelected())
				lst.add(box.getText());
		}

		//queryDB for topic IDs

		return lst;
	}

	public int getDifficulty() {
		for (Enumeration<AbstractButton> buttons = difficultyBtnsGrp.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				if (button.getText().compareTo("Easy") == 0)
					return 0;
				if (button.getText().compareTo("Medium") == 0)
					return 1;
				if (button.getText().compareTo("Hard") == 0)
					return 2;
			}
		}
		return -1; //Error - no btn selected.

	}

	private void addTopicsCheckBoxes() {
		topicsCheckBoxes = new LinkedList<JCheckBox>();
		for (String topic :topicsList) {
			JCheckBox box = new JCheckBox();
			box.setSelected(true);
			box.setText(topic);
			topicsCheckBoxes.add(box);
			topicsPanel.add(box);
		}
	}

	private void getTopics() {
		topicsList =new ArrayList<String>();
		topicsList.addAll(DBUtils.getAllTopicIDsAndNames().keySet());
	}
}
