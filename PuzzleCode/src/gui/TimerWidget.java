package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

 class TimerWidget extends JLabel{

	private Timer t;
	
	TimerWidget() {
		//super( ,JLabel.HORIZONTAL);

		t = new Timer(1000, new ClockListener());
		t.setInitialDelay(0);
	}
	
	void start() {
		t.start();
	}
	
	void pause() {
		t.stop();
	}
	
	void reset() {
		t.restart();
	}
	
	
	class ClockListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//send event to CrosswordView
		}
		
	}
}
