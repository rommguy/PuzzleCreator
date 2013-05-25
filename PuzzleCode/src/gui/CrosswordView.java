package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class CrosswordView extends JFrame {

	private JPanel contentPane;
	private TimerWidget timer;

	/**
	 * Create the frame.
	 */
	public CrosswordView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 584, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		JPanel timerPanel = new JPanel();

		//TimerWidget timer = new TimerWidget(clockListener)
		//timerPanel.add();
		contentPane.add(timerPanel);


		JPanel crossWordPanel = new JPanel();
		crossWordPanel.setBackground(Color.blue);
		crossWordPanel.setMinimumSize(new Dimension(600, 600));
		crossWordPanel.setPreferredSize(new Dimension(600, 600));
		contentPane.add(crossWordPanel);
		crossWordPanel.setLayout(new GridLayout(1, 0, 0, 0));

		JPanel BtnPanel = new JPanel();
		contentPane.add(BtnPanel);

		JButton btnCheck = new JButton("Check");

		JButton btnPause = new JButton("Pause");
		BtnPanel.add(btnPause);
		BtnPanel.add(btnCheck);

		JButton btnDone = new JButton("Done");
		BtnPanel.add(btnDone);

		this.pack();
	}

}