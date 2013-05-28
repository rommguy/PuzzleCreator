package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import puzzleAlgorithm.PuzzleDefinition;
import puzzleAlgorithm.PuzzleSquare;

import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.util.List;
import java.awt.BorderLayout;


public class CrosswordView extends JPanel {

	private JPanel contentPane;
	private TimerJLabel timer;
	private JButton btnPause;
	private boolean isPaused = false;
	private JPanel boardPanel;

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
		contentPane.setLayout(new BorderLayout(0, 0));
		
				JPanel timerPanel = new JPanel();
				
						timer = new TimerJLabel();
						timerPanel.add(timer);
						timer.start();
						contentPane.add(timerPanel, BorderLayout.NORTH);
		
		boardPanel = new JPanel();
		contentPane.add(boardPanel, BorderLayout.CENTER);

		JPanel BtnPanel = new JPanel();
		contentPane.add(BtnPanel, BorderLayout.SOUTH);

		JButton btnCheck = new JButton("Check");

		btnPause = new JButton("Pause");
		btnPause.setPreferredSize(new Dimension(100, btnPause.getPreferredSize().height + 10));
		BtnPanel.add(btnPause);
		BtnPanel.add(btnCheck);

		JButton btnDone = new JButton("Done");
		BtnPanel.add(btnDone);

	}
	
	private void drawBoard(PuzzleSquare[][] board, List<PuzzleDefinition> definitions) {
		int boardOrder = board.length;
		boardPanel.setLayout(new GridLayout(boardOrder, boardOrder));
		
		//count number of definitions in each square
		int[][] boardDefNum = new int[boardOrder][boardOrder];
		
		// init
		for (int i = 0; i<boardOrder; i++) {
			for (int j=0; j<board[i].length; j++) {
				boardDefNum[i][j] = 0;
			}
		}
		
		for (PuzzleDefinition definition : definitions) {
			boardDefNum[1][2]++; 
		}
		
		
	}

	void pause() {
		if (!isPaused) {
			timer.pause();
			isPaused = true;
			btnPause.setText("Resume");
			boardPanel.setEnabled(false);
		}
		else {
			timer.resume();
			isPaused = false;
			btnPause.setText("Pause");
			boardPanel.setEnabled(true);
		}
	}

	
	void addPauseListener(ActionListener listener) {
		btnPause.addActionListener(listener);
	}
}
