package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.Box.Filler;
import javax.swing.Icon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JSplitPane;
import javax.swing.JLayeredPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MainView {

	private JFrame frame;
	private Map<String,JButton> menuPanelBtns;
	JButton[] menuPanelBtnsArray;
	Map<JButton, JLabel> btnLabels;

	private final int FRAME_HEIGHT = 850;
	private final int FRAME_WIDTH = 800;
	private final int MAX_NUM_BUTTONS_IN_MENU = 8;
	private final String[] CARD_NAMES = {"Welcome", "PrepareGame", "Crossword", "AddDef", "AddHint", "Massive Import", "Help", "About"};
	private JPanel PrepareGame = null;
	private JPanel crosswordView = null;

	private JPanel menuPanel;
	private JPanel cardPanel;
	private int menuBtnCounter = 0;

	/**
	 * Launch the application.
	 */
	public static void startGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainView window = new MainView();
					@SuppressWarnings("unused")
					MainController controller = new MainController(null, window);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainView() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

		// center screen
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize(); 
		int x=(int)((dimension.getWidth() - 800)/2);
		int y=(int)((dimension.getHeight() - 800)/2);
		frame.setLocation(x, y);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// build mainPanel
		menuPanel = new JPanel();
		menuPanel.setSize((int) Math.round(0.5 * FRAME_WIDTH), FRAME_HEIGHT);
		menuPanel.setBorder(new TitledBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, null), "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		menuPanel.setLayout(new GridBagLayout());
		frame.getContentPane().add(menuPanel, BorderLayout.WEST);

		menuPanelBtns = new HashMap<String,JButton>();
		menuPanelBtnsArray = new JButton[MAX_NUM_BUTTONS_IN_MENU];
		btnLabels = new HashMap<JButton,JLabel>();

		// top buttons
		createButton("Play", "game.png");

		createButton("Continue Game", "continue.png");

		createButton("Hall of Fame", "best.png");

		// middle buttons
		createButton("Add Definition", "add.png");

		createButton("Add Hints", "add.png");

		createButton("Massive Import", "addDB.png");

		// bottom buttons

		createButton("Help", "help.png");

		createButton("About", "about.png");

		addButtonsTPanel(menuPanelBtnsArray);

		//build main panel 
		cardPanel = new JPanel();
		cardPanel.setLayout(new CardLayout());

		JPanel welcomePanel = new JPanel();
		welcomePanel.setLayout(new BorderLayout());
		welcomePanel.setBackground(Color.WHITE);
		JLabel logo = new JLabel(new ImageIcon(MainView.class.getResource("/resources/crossword.jpg")));
		welcomePanel.add(logo, BorderLayout.CENTER);
		cardPanel.add(welcomePanel, CARD_NAMES[0]);

		//add formPanel - this panel will change
		frame.getContentPane().add(cardPanel, BorderLayout.CENTER);

	}

	private void createButton(String text, String resourceName) {
		JButton btn = new JButton();
		btn.setLayout(new BorderLayout());
		JLabel label = new JLabel(text);
		btnLabels.put(btn, label);
		label.setHorizontalAlignment(JLabel.CENTER);
		JLabel image = new JLabel(new ImageIcon(MainView.class.getResource("/resources/" + resourceName)));
		btn.add(label, BorderLayout.CENTER);
		btn.add(image, BorderLayout.WEST);
		menuPanelBtns.put(text, btn);
		menuPanelBtnsArray[menuBtnCounter++] = btn;
	}

	private void addButtonsTPanel(JButton[] btnArray) {
		int row = 0;

		//Button constraint
		GridBagConstraints btnConstraint = new GridBagConstraints();
		btnConstraint.gridx = 0;
		btnConstraint.gridy = 0;
		btnConstraint.fill = GridBagConstraints.BOTH;
		btnConstraint.weightx = 1;
		btnConstraint.weighty = 1;
		btnConstraint.insets = new Insets(5, 5, 5, 5);

		int buttonCounter = 0;

		// add buttons to Jpanel
		for (row = 0; row <=9; row++ ) {
			btnConstraint.gridy = row;
			if ((row == 3) || (row== 7)) { // seperator
				JSeparator seperator = new JSeparator();
				seperator.setPreferredSize(new Dimension(1,1));
				btnConstraint.insets = new Insets(0, 0, 0, 0);
				btnConstraint.weightx = 0.3;
				btnConstraint.weighty = 0.3;
				btnConstraint.fill = GridBagConstraints.HORIZONTAL;
				menuPanel.add(seperator, btnConstraint);

				//return old values
				btnConstraint.insets = new Insets(5, 5, 5, 5);
				btnConstraint.fill = GridBagConstraints.BOTH;
				btnConstraint.weightx = 1;
				btnConstraint.weighty = 1;
			}
			else { //button 
				JButton btn = btnArray[buttonCounter++];
				menuPanel.add(btn, btnConstraint);
			}
		}
	}

	void addMenuBtnsListener(ActionListener listener) {
		for (JButton btn : menuPanelBtns.values())
			btn.addActionListener(listener);
	}

	void playBtnClicked() {
		if (PrepareGame == null) {
			PrepareGame = prepareGameView.startPrepareGame();
			cardPanel.add(PrepareGame, CARD_NAMES[1]);
		}
		CardLayout cl = (CardLayout)(cardPanel.getLayout());
		cl.show(cardPanel, CARD_NAMES[1]);
	}
	
	void showCrosswordview() {
		if (crosswordView == null) {
			crosswordView = CrosswordView.startCrosswordView();
			cardPanel.add(PrepareGame, CARD_NAMES[2]);
		}
		
		CardLayout cl = (CardLayout)(cardPanel.getLayout());
		cl.show(cardPanel, CARD_NAMES[2]);
	}
}
