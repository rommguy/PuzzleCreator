package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.GridLayout;


public class CrosswordView extends JPanel {

	private JPanel contentPane;
	private TimerJLabel timer;
	private JButton btnPause;
	private JPanel crossWordPanel;
	private boolean isPaused = false;

	static JPanel start() {
		CrosswordView view = new CrosswordView();
		@SuppressWarnings("unused")
		CrosswordController controller = new CrosswordController(null, view);
		return view;
	}
	/**
	 * Create the frame.
	 */
	public CrosswordView() {
		initialize();
		this.setVisible(true);
	}

	private void initialize() {
		setBounds(100, 100, 584, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		JPanel timerPanel = new JPanel();

		timer = new TimerJLabel();
		timerPanel.add(timer);
		timer.start();
		contentPane.add(timerPanel);


		crossWordPanel = new JPanel();
		crossWordPanel.setBackground(Color.blue);
		crossWordPanel.setMinimumSize(new Dimension(600, 600));
		crossWordPanel.setPreferredSize(new Dimension(600, 600));
		contentPane.add(crossWordPanel);
		crossWordPanel.setLayout(new GridLayout(1, 0, 0, 0));

		JPanel BtnPanel = new JPanel();
		contentPane.add(BtnPanel);

		JButton btnCheck = new JButton("Check");

		btnPause = new JButton("Pause");
		btnPause.setPreferredSize(new Dimension(100, btnPause.getPreferredSize().height + 10));
		BtnPanel.add(btnPause);
		BtnPanel.add(btnCheck);

		JButton btnDone = new JButton("Done");
		BtnPanel.add(btnDone);

	}

	void pause() {
		if (!isPaused) {
			timer.pause();
			isPaused = true;
			btnPause.setText("Resume");
			crossWordPanel.setEnabled(false);
		}
		else {
			timer.resume();
			isPaused = false;
			btnPause.setText("Pause");
			crossWordPanel.setEnabled(true);
		}
	}

	void addPauseListener(ActionListener listener) {
		btnPause.addActionListener(listener);
	}
}
