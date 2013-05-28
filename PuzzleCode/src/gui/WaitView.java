package gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JButton;

public class WaitView extends JPanel {

	static WaitView start() {
		return new WaitView();
	}
	/**
	 * Create the panel.
	 */
	public WaitView() {
		initialize();
	}
	
	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		
		JLabel lblWeArePreparing = new JLabel("<HTML><center>We are preparing you Crossword.<br>To get the juices going, answer this question</HTML>");
		lblWeArePreparing.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblWeArePreparing.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblWeArePreparing, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel animatedIconPanel = new JPanel();
		animatedIconPanel.add(getProcessingAnimation());
		panel.add(animatedIconPanel, BorderLayout.WEST);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);
		
		JButton btnNewButton = new JButton("New button");
		panel_1.add(btnNewButton);
		
		getProcessingAnimation();

	}
	
	private JLabel getProcessingAnimation() {
		
	}

}
