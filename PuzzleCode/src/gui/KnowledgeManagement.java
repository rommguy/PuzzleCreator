package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;

public class KnowledgeManagement extends JFrame {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public KnowledgeManagement() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 578, 532);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel addTabPanel = new JPanel();
		tabbedPane.addTab("Add New Knowledge", null, addTabPanel, null);
		addTabPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel updateTabPanel = new JPanel();
		tabbedPane.addTab("New tab", null, updateTabPanel, null);
	}

}
