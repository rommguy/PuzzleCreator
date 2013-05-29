package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import Utils.AlgorithmUtils;
import Utils.Logger;

import puzzleAlgorithm.AlgorithmRunner;
import puzzleAlgorithm.BoardSolution;
import puzzleAlgorithm.PuzzleDefinition;
import puzzleAlgorithm.PuzzleSquare;
import sun.security.krb5.internal.PAEncTSEnc;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.BorderLayout;


public class CrosswordView extends JPanel {
	
	private TimerJLabel timer;
	private JButton btnPause;
	private boolean isPaused = false;
	private JPanel boardPanel;
	private Map<Integer, Map<Integer,List<PuzzleDefinition>>> boardDefs;
	private JPanel[][] boardPanelHolders;
	JPanel[][] getBoardPanelHolders() {
		return boardPanelHolders;
	}
	List<PuzzleDefinition> getDefinitions() {
		return definitions;
	}

	private int[][] boardDefCount;
	List<PuzzleDefinition> definitions;
	private JButton btnCheck;

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

		setLayout(new BorderLayout(0, 0));

		JPanel timerPanel = new JPanel();

		timer = new TimerJLabel();
		timerPanel.add(timer);
		timer.start();
		add(timerPanel, BorderLayout.NORTH);

		boardPanel = new JPanel();
		add(boardPanel, BorderLayout.CENTER);

		JPanel BtnPanel = new JPanel();
		add(BtnPanel, BorderLayout.SOUTH);

		btnCheck = new JButton("Check");

		btnPause = new JButton("Pause");
		btnPause.setPreferredSize(new Dimension(100, btnPause.getPreferredSize().height + 10));
		BtnPanel.add(btnPause);
		BtnPanel.add(btnCheck);

		JButton btnDone = new JButton("Done");
		BtnPanel.add(btnDone);

		CrosswordModel.getBoardSolutionAndDraw(this);

	}

	void drawBoard(PuzzleSquare[][] board, List<PuzzleDefinition> definitions) {
		this.definitions = definitions; // save a reference in crossview
		
		int size = board.length;
		boardPanelHolders = new JPanel[size][size];
		boardDefCount = new int[size][size];
		boardDefs =  new HashMap<Integer, Map<Integer, List<PuzzleDefinition>>>(); // Map required because cannot make such an array

		initializeCellToDefMap(size); 

		initializeBoardPanelHolder(size);

		initializeBoardDefCount(size);

		//count number of definitions in each square
		for (PuzzleDefinition definition : definitions) {
			int row = definition.getTextRow();
			int col = definition.getTextCol();

			boardDefCount[row][col]++; 
			boardDefs.get(row).get(col).add(definition);
		}

		// place definitions in cells
		for (int i = 0; i<size; i++) {
			for (int j=0; j<board[i].length; j++) {
				JPanel currentPanel = boardPanelHolders[i][j]; 
				switch (boardDefCount[i][j]) {
				case 0 : { // regular square
					currentPanel.setBackground(Color.WHITE);
					JTextField txtLbl = new JSquareTextField(currentPanel.getBackground());
					currentPanel.add(txtLbl, BorderLayout.CENTER);
					break;
				}
				case 1:  {
					//definition square with one definition
					JLabel lbl = createDefinitionLabel(i, j,0);
					currentPanel.setBorder(new BevelBorder(BevelBorder.RAISED));

					currentPanel.add(lbl, BorderLayout.CENTER);
					break;
				}
				case 2: { // definition square with two definitions
					currentPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
					currentPanel.setLayout(new GridLayout(2,1));
					JLabel lbl1 = createDefinitionLabel(i, j,0);
					JLabel lbl2= createDefinitionLabel(i, j, 1);
					lbl1.setBackground(Color.GRAY);
					lbl2.setBackground(Color.GRAY);

					PuzzleDefinition def1 = boardDefs.get(i).get(j).get(0); // definition #1 in list
					PuzzleDefinition def2 = boardDefs.get(i).get(j).get(1); // definition #2 in list

					// place definitions according to where the arrows would be
					if (isDefinitionTop(def1, i)) {  
						currentPanel.add(lbl1); //definition #1 is cell top
						lbl1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK)); // seperator
						currentPanel.add(lbl2); //definition #2 is cell bottom
					}
					else {
						if (isDefinitionBottom(def1, i)) {
							currentPanel.add(lbl2);
							lbl1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
							currentPanel.add(lbl1);
						}
						else {
							if (isDefinitionTop(def2, i)) {
								currentPanel.add(lbl2);
								lbl1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
								currentPanel.add(lbl1);
							}
							else {
								if (isDefinitionBottom(def2, i)) {
									currentPanel.add(lbl1);
									lbl1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
									currentPanel.add(lbl2);
								}
								else { // random
									currentPanel.add(lbl1);
									currentPanel.add(lbl2);
								}
							}
						}
					}
					break;
				}
				default : {
					Logger.writeErrorToLog("Invalid sqaure to draw: Definition sqaure may have up to 2 definitions.");
				}
				}
			}
		}

		boardPanel.setLayout(new GridLayout(size, size));

		//add panels to boardPanel in right order
		for (int i = 0; i<size; i++){
			for (int j=0; j<size; j++) {
				boardPanel.add(boardPanelHolders[i][j]);
			}
		}
		
		addDefinitionSquareListener(new DefinitionSquareListener()); // add dynamically the definitions listeners.
		
		boardPanel.repaint();	
	}
	private void initializeBoardPanelHolder(int size) {
		for (int i = 0; i<size; i++) {
			for (int j=0; j<size; j++) {
				boardPanelHolders[i][j] = new JPanel();	
				boardPanelHolders[i][j].setLayout(new BorderLayout());
			}
		}
	}

	private void initializeBoardDefCount(int size) {
		for (int i = 0; i<size; i++) {
			for (int j=0; j<size; j++) {
				boardDefCount[i][j] = 0;
			}
		}
	}
	private void initializeCellToDefMap(int size) {
		for (int i = 0; i<size; i++) {
			boardDefs.put(i, new HashMap<Integer, List<PuzzleDefinition>>());
		}

		for (int i = 0; i<size; i++) {
			for (int j=0; j<size; j++) {
				boardDefs.get(i).put(j, new ArrayList<PuzzleDefinition>());
			}
		}
	}

	private void drawArrows() {

	}

	private boolean isDefinitionTop(PuzzleDefinition def, int row) {
		return (def.getBeginRow() == row - 1);
	}

	private boolean isDefinitionBottom(PuzzleDefinition def, int row) {
		return (def.getBeginRow() == row + 1);
	}

	private JLabel createDefinitionLabel(int i,int j, int defNum) {
		//JLabel lbl = new JLabel("<html><p>" + boardDefs.get(i).get(j).get(defNum) + "</p></html>");
		return new JDefinitionLabel( boardDefs.get(i).get(j).get(defNum)); 
	}

	private void colorDefinitionArea(PuzzleDefinition def, Color color) {

		char direction = def.getDirection();
		switch (direction) {
		case 'r': {
			for (int col = def.getBeginColumn(); col<def.getBeginColumn() + def.getAnswer().length; col++) {
				boardPanelHolders[def.getBeginRow()][col].getComponent(0).setBackground(color);
				((JSquareTextField)boardPanelHolders[def.getBeginRow()][col].getComponent(0)).setCaretColor(color);
			}
			break;
		}
		case 'l':{
			for (int col = def.getBeginColumn(); col>def.getBeginColumn() - def.getAnswer().length; col--) {
				boardPanelHolders[def.getBeginRow()][col].getComponent(0).setBackground(color);
				((JSquareTextField)boardPanelHolders[def.getBeginRow()][col].getComponent(0)).setCaretColor(color);
			}
			break;
		}
		case 'u': {
			for (int row = def.getBeginRow(); row>def.getBeginRow() - def.getAnswer().length; row--) {
				boardPanelHolders[row][def.getBeginColumn()].getComponent(0).setBackground(color);
				((JSquareTextField)boardPanelHolders[row][def.getBeginColumn()].getComponent(0)).setCaretColor(color);
			}
			break;
		}
		case 'd': {
			for (int row = def.getBeginRow(); row<def.getBeginRow() + def.getAnswer().length; row++) {
				boardPanelHolders[row][def.getBeginColumn()].getComponent(0).setBackground(color);
				((JSquareTextField)boardPanelHolders[row][def.getBeginColumn()].getComponent(0)).setCaretColor(color);
			}
			break;
		}
		default: {
			Logger.writeErrorToLog("Invalid direction in puzzle definition");
		}
		}
	}

	void unColorDefinitionAread(PuzzleDefinition def) {
		colorDefinitionArea(def, Color.WHITE);
	}

	void addColorDefinotionArea(PuzzleDefinition def, Color color) {
		colorDefinitionArea(def, color);
	}

	class DefinitionSquareListener extends MouseAdapter { // had to put it here because definitions List does not exist at view & controller initialize, and didnt want to have controller refernce in this class
		private Color COLOR = Color.BLUE;

		@Override
		public void mouseEntered(MouseEvent e) {
			System.out.println("mouseEnterd");
			JDefinitionLabel lbl =(JDefinitionLabel) e.getSource();
			addColorDefinotionArea(lbl.getDef(), COLOR);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JDefinitionLabel lbl =(JDefinitionLabel) e.getSource();
			unColorDefinitionAread(lbl.getDef());
		}
	}
		
	void addDefinitionSquareListener(MouseListener listener) {
		for (PuzzleDefinition definition : definitions) {
			System.out.println("adding listener to definition " + definition.getTextRow() + "," + definition.getTextCol());
			JPanel square = boardPanelHolders[definition.getTextRow()][definition.getTextCol()];
			for (Component comp : square.getComponents()) {
				if (comp.getListeners(MouseListener.class).length < 1) {
					comp.addMouseListener(listener);
				}
			}
		}
	}
	
	void addCheckListener(ActionListener listener) {
		btnCheck.addActionListener(listener);
	}
	
	void notifyCorrectness(boolean isCorrect) { // show to user that he was correct or wrong
		if (isCorrect) {
			btnCheck.setBackground(Color.GREEN);
		}
		else {
			btnCheck.setBackground(Color.RED);
		}
	}
	void pause() {
		if (!isPaused) {
			timer.pause();
			isPaused = true;
			enableComponents(boardPanel, false);
			btnPause.setText("Resume");
			boardPanel.setEnabled(false);
		}
		else {
			timer.resume();
			isPaused = false;
			boardPanel.setEnabled(true);
			enableComponents(boardPanel, true);
			btnPause.setText("Pause");
			boardPanel.setEnabled(true);
		}
	}

	public void enableComponents(Container container, boolean enable) {
		Component[] components = container.getComponents();
		for (Component component : components) {
			component.setEnabled(enable);
			if (component instanceof Container) {
				enableComponents((Container)component, enable);
			}
		}
	}

	void addPauseListener(ActionListener listener) {
		btnPause.addActionListener(listener);
	}
}
