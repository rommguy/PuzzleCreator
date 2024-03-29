package ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Random;
import javax.swing.border.BevelBorder;
import core.algorithm.AlgorithmWorker;
import db.utils.DBUtils;

/**
 * The window that is shown to the user while the crossword is created
 * @author yonatan
 *
 */
@SuppressWarnings("serial")
public class WaitView extends JPanel {

	private JButton btnGo;
	private ActionListener goListener;
	private JLabel infoLabel;
	private JPanel animationPanel;
	private int[] topics;
	private int difficulty;
	private JLabel questionLabel;
	private JPanel answerPanel; // holds squares
	private String answer;
	private JButton btnSolve;

	public void setBoard() {
	}
	/**
	 * 
	 * @param view - to get the topics and difficulty selected by user
	 * @return
	 * @throws SQLException 
	 */
	static WaitView start(int[] topics, int difficulty) throws SQLException {
		return new WaitView(topics,difficulty);
	}
	/**
	 * Create the panel.
	 * @throws SQLException 
	 */
	public WaitView(int[] topics, int difficulty) throws SQLException {
		this.topics = topics;
		this.difficulty = difficulty;
		initialize();
	}

	private void initialize() throws SQLException {

		setLayout(new BorderLayout(0, 0));

		JLabel lblWeArePreparing = new JLabel("<HTML><center>We're Preparing A Crossword Tailored For You.<br> Meanwhile, Get Your Juices Going!</HTML>");
		lblWeArePreparing.setBackground(Color.WHITE);
		lblWeArePreparing.setOpaque(true);
		lblWeArePreparing.setFont(new Font("Tw Cen MT Condensed", Font.PLAIN, 25));
		lblWeArePreparing.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblWeArePreparing, BorderLayout.NORTH);

		animationPanel = new JPanel();
		animationPanel.setLayout(new BorderLayout());
		animationPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		animationPanel.setBackground(Color.WHITE);
		add(animationPanel, BorderLayout.SOUTH);

		infoLabel = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("resources/about.png")));
		infoLabel.setHorizontalAlignment(SwingConstants.LEFT);
		infoLabel.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 16));
		animationPanel.add(infoLabel, BorderLayout.CENTER);
		infoLabel.setText("Starting...");
		animationPanel.invalidate();

		//replace rotating animation with skip button
		btnGo = new JButton("GO!");
		btnGo.setEnabled(false);
		goListener = new GoListener();
		btnGo.addActionListener(goListener);
		animationPanel.add(btnGo, BorderLayout.EAST);
		animationPanel.invalidate();


		//trivia question 

		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(Color.WHITE);
		add(centerPanel, BorderLayout.CENTER);	
		centerPanel.setLayout(new GridLayout(7, 1));

		drawTriviaQuestion(centerPanel);

		final JPanel checkPanel = new JPanel();
		checkPanel.setBackground(Color.WHITE);

		final JButton  btnCheck = new JButton("Check My Answer", new ImageIcon(getClass().getClassLoader().getResource("resources/check_btn.png")));
		btnCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean isCorrect = isCorrectAnswer(answerPanel, answer);
				if (isCorrect) {
					btnCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("resources/check_medium.png")));
					btnSolve.setEnabled(false);
				}
				else  {
					btnCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("resources/fail_medium.png")));
				}
			}
		});
		checkPanel.add(btnCheck);

		btnSolve = new JButton("Solve", new ImageIcon(getClass().getClassLoader().getResource("resources/surrender.png")));
		btnSolve.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SolveAnswer();
				btnCheck.setEnabled(false);
				btnSolve.setEnabled(false);
			}
		});

		checkPanel.add(btnSolve);

		centerPanel.add(checkPanel);
		
		startAlgorithmCalculation();

	}
	private void startAlgorithmCalculation() {
		// start running algorithm in background
		startAlgorithmCalculationThread(topics, difficulty);
	}
	private void drawTriviaQuestion(JPanel centerPanel) throws SQLException {
		questionLabel = new JLabel("New label");
		questionLabel.setFont(questionLabel.getFont().deriveFont(15f));
		questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		centerPanel.add(questionLabel);

		String[] triviaQuestion = DBUtils.getTriviaQuestion();	
		String question = triviaQuestion[1];
		answer = triviaQuestion[0];

		questionLabel.setText("<html><p><Center>" + question + "</p></html>");

		answerPanel = new JPanel();
		answerPanel.setLayout(new GridLayout(1, answer.length()));

		Random rand = new Random();
		// ideally want different letters revealed, but one is OK too.
		int firstLetterIndex  = rand.nextInt(answer.length());
		int secondLetterIndex = (answer.length() < 2) ? -1 : rand.nextInt(answer.length());
		int thirdLetterIndex =  (answer.length() < 5) ? -1 : rand.nextInt(answer.length());
		int fourthLetterIndex = (answer.length() < 7) ? -1 : rand.nextInt(answer.length());

		for (int i= 0; i<answer.length(); i++) {
			SquareTextField field = new SquareTextField();
			InputSquare square = new InputSquare(field, 0, i);
			if (i == firstLetterIndex || i == secondLetterIndex || i == thirdLetterIndex || i == fourthLetterIndex) {
				field.setText(Character.toString(answer.charAt(i)));
				field.setEditable(false);
				field.setFocusable(false);
				field.setBackground(Color.LIGHT_GRAY);
			}
			answerPanel.add(square);	
		}

		centerPanel.add(answerPanel);
	}

	private void startAlgorithmCalculationThread(int[] topics, int difficulty)  {
		AlgorithmWorker worker = new AlgorithmWorker(this, topics, difficulty);
		worker.execute();
	}

	public void setGoBtn(boolean enable) {
		JPanel parent = (JPanel) btnGo.getParent();
		parent.remove(btnGo);
		btnGo = new JButton("GO!");
		btnGo.setEnabled(enable);
		btnGo.addActionListener(new GoListener());
		parent.add(btnGo, BorderLayout.EAST);
	}
	

	public void setProgressMessage(String text) {
		infoLabel.setText(text);
	}

	private boolean isCorrectAnswer(JPanel answerPanel, String answer) {
		int index = 0;
		for (Component comp : answerPanel.getComponents()) {
			InputSquare square = (InputSquare) comp;
			String letterString = square.getField().getText().toLowerCase();
			if (letterString.length() < 1) {
				return false;
			}

			if (letterString.toLowerCase().charAt(0) != answer.charAt(index++))
				return false;
		}
		return true;
	}

	private void SolveAnswer() {
		for (int i = 0 ; i<answer.length(); i++) {
			InputSquare square = (InputSquare) answerPanel.getComponent(i);
			square.getField().setText(Character.toString(answer.charAt(i)));
		}
	}

	public void setSkipBtnToTryAgain() {
		JPanel parent = (JPanel) btnGo.getParent();
		parent.remove(btnGo);
		btnGo = new JButton("Try Again");
		btnGo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startAlgorithmCalculation()	;	
				setGoBtn(false);
			}
		});
		parent.add(btnGo, BorderLayout.EAST);
	}

	private class GoListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MainView.getView().showCrosswordview(); // must be available only after BoardSolution was created (board != null)
		}
	};
}

