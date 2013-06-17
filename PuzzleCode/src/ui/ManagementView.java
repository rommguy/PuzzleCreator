package ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.ItemSelectable;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import org.japura.gui.CheckComboBox;
import org.japura.gui.model.ListCheckModel;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import db.utils.DBUtils;
import db.utils.KnowledgeManagement;
import javax.swing.JTabbedPane;
import java.awt.Color;

@SuppressWarnings("serial")
public class ManagementView extends JPanel {
	private int definitionCounter = 0;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField newTextField;
	private JComboBox<String> existingTextField;
	private Map<String,Integer> allTopics; // map of all topics
	private Map<String,Integer> allEntities; // Map : GET_PROPER_NAME(entity_name) (which is unique) -> PK. .
	private String[] allTopicNamesArray;
	private Map<String,Integer> allDefinitions = null;

	//global variables containing the chosen entity details
	private int chosenEntityID = -1; // // for existing entities
	private String chosenEntityString = ""; // for added entities


	private static final String USER_UPDATES_TOPIC = "User Updates";
	private static final int ADD_ROWS_NUM = 1;
	private static final int MAX_NUM_DEFS = 20;

	private JPanel definitionPanel = null;
	private JPanel hintsPanel = null;
	private JTabbedPane tabbedPane;


	static JPanel start() throws SQLException {
		return new ManagementView();
	}
	/**
	 * Create the panel.
	 * @throws SQLException 
	 */
	private ManagementView() throws SQLException {

		initialize(); 
	}


	private void initialize() throws SQLException {

		allDefinitions = DBUtils.getAllDefinitions(); // get all definition strings in definitions table
		allTopics = DBUtils.getAllTopicIDsAndNames(); // get all pairs of topic: topic ID, topic name
		Object[] array = allTopics.keySet().toArray(); // topic array for JList combobox
		allTopicNamesArray = new String[array.length];

		for (int i = 0 ; i< array.length; i++) {
			allTopicNamesArray[i] = array[i].toString();
		}

		//get all entities for search from DB
		allEntities = DBUtils.getAllEntities();

		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Choose Knowledge Fact", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel topBtnPanel = new JPanel();
		panel.add(topBtnPanel, BorderLayout.EAST);
		topBtnPanel.setLayout(new GridLayout(2, 1, 0, 5));

		final JButton btnAddNewFact = new JButton("");
		btnAddNewFact.setIcon(new ImageIcon(getClass().getClassLoader().getResource("resources/add_tiny.png")));
		btnAddNewFact.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String entityText = newTextField.getText(); 
				if (allEntities.containsKey(entityText)) { // entity already exists
					Utils.showErrorMessage("Entity already exists!");
					newTextField.setText("");
					return;
				}
				if (!entityText.isEmpty()) {
					definitionCounter = 0;
					chosenEntityID = -1;
					chosenEntityString = entityText;
					try {
						buildDefinitionPanel(-1);
						buildHintsPanel(-1);
					} catch (SQLException e) {
						Utils.showErrorMessage("Could not tabs for knowledge fact");
						return;
					}
					finally {
						//disable hints tab
						tabbedPane.setEnabledAt(tabbedPane.getTabCount()-1, false);
						tabbedPane.setSelectedIndex(0);
					}
				}
			}
		});
		topBtnPanel.add(btnAddNewFact);

		final JButton btnSearchFact = new JButton("");
		btnSearchFact.setEnabled(false);
		btnSearchFact.setIcon(new ImageIcon(getClass().getClassLoader().getResource("resources/search_tiny.png")));
		btnSearchFact.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String searchText = existingTextField.getSelectedItem().toString();

				if (searchText.isEmpty())
					return; // do nothing if no text entered

				definitionCounter = 0;
				chosenEntityID  = allEntities.get(searchText);

				if (chosenEntityID != -1) { // found
					try {
						buildDefinitionPanel(chosenEntityID);
						buildHintsPanel(chosenEntityID);
					} catch (SQLException e1) {
						Utils.showErrorMessage("Could not tabs for knowledge fact");
					} 
				}
			}
		});

		topBtnPanel.add(btnSearchFact);

		JPanel radioBtnPanel = new JPanel();
		panel.add(radioBtnPanel, BorderLayout.WEST);

		JRadioButton newCheckBox = new JRadioButton("New Knowledge Fact");
		newCheckBox.setSelected(true);
		newCheckBox.addItemListener(new ItemListener() {

			void setEnabled(boolean flag) {
				btnAddNewFact.setEnabled(flag);
				newTextField.setEnabled(flag);
				if (!flag) {
					definitionPanel.removeAll();
				}
			}
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(true);
				}
				else {
					setEnabled(false);
				}
			}
		});
		radioBtnPanel.setLayout(new GridLayout(2, 1, 0, 5));

		buttonGroup.add(newCheckBox);
		radioBtnPanel.add(newCheckBox);

		JRadioButton existingCheckBox = new JRadioButton("Existing Knowledge Fact");
		existingCheckBox.addItemListener(new ItemListener() {

			void setEnabled(boolean flag) {
				btnSearchFact.setEnabled(flag);
				existingTextField.setEnabled(flag);
				if (!flag) {
					definitionPanel.removeAll();
				}
				existingTextField.revalidate();
				existingTextField.getParent().revalidate();
			}

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(true);
				}
				else { 
					setEnabled(false);
				}
			}
		});

		buttonGroup.add(existingCheckBox);
		radioBtnPanel.add(existingCheckBox);

		JPanel fieldPanel = new JPanel();
		panel.add(fieldPanel, BorderLayout.CENTER);
		fieldPanel.setLayout(new GridLayout(2, 1, 0, 5));

		newTextField = new JTextField();
		fieldPanel.add(newTextField);
		newTextField.setColumns(10);

		existingTextField = createAutoCompleteBox(allEntities.keySet(), "", true);
		existingTextField.setEnabled(false);
		fieldPanel.add(existingTextField);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.LIGHT_GRAY);
		add(tabbedPane, BorderLayout.CENTER);

		
		definitionPanel = new JPanel();
		JScrollPane scroll = new JScrollPane(definitionPanel);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setSize(new Dimension(10, 1300));
		scroll.setPreferredSize(tabbedPane.getPreferredSize());
		scroll.setMinimumSize(tabbedPane.getMinimumSize());
		scroll.setMaximumSize(tabbedPane.getMaximumSize());


		tabbedPane.addTab("Definitions", null, scroll,null);
		
		

		hintsPanel = new JPanel();
		tabbedPane.addTab("Hints", null, hintsPanel,null);

		JPanel btnPanel = new JPanel();
		btnPanel.setAlignmentX(FlowLayout.CENTER);
		JButton btnBack = new JButton();
		btnBack.setFont(btnBack.getFont().deriveFont(15f));
		btnBack.setIcon(new ImageIcon(getClass().getClassLoader().getResource("resources/back.png")));

		btnBack.addActionListener(new BackButtonListener());
		btnPanel.add(btnBack);
		add(btnPanel, BorderLayout.SOUTH);
	}

	private Map<String,Integer> getTopicsForDefinition(int entityID) throws SQLException {
		return DBUtils.getTopicsByDefinitionID(entityID);
	}

	private Map<String,Integer> getDefinitionsForEntity(int entityID) throws SQLException {
		return DBUtils.getDefinitionsByEntityID(entityID);

	}

	private void buildDefinitionPanel(int entityID) throws SQLException {

		int row_num = 0;

		definitionPanel.removeAll();
		definitionPanel.setLayout(new GridLayout(MAX_NUM_DEFS, 1, 0, 10));

		if (entityID != -1) { // panel for an existing entity
			Map<String, Integer> definitions = getDefinitionsForEntity(entityID);

			row_num = definitions.size();
			definitionCounter = row_num;

			for (String definition : definitions.keySet()) { // definition : MAP : DEFINITON STRING - > ID
				DefinitionLine line = new DefinitionLine(chosenEntityID, definitions.get(definition), definition);
				definitionPanel.add(line);
			}
		}

		definitionPanel.add(new NewDefinitionLine());

		// add padding lines, if needed
		for (int i = 0; i<MAX_NUM_DEFS - ADD_ROWS_NUM - row_num; i++) {
			definitionPanel.add(new JPanel());
		}

		definitionPanel.revalidate();
		tabbedPane.revalidate();

		return;
	}

	private void buildHintsPanel(int entityID) throws SQLException {

		int row_num = 0;

		hintsPanel.removeAll();
		hintsPanel.setLayout(new GridLayout(MAX_NUM_DEFS, 1, 0, 10));

		if (entityID != -1) { // existing entity 
			Map<Integer,String> hintResults = DBUtils.getHintsByEntityID(entityID);

			List<HintTuple> hintTupleLst = new ArrayList<HintTuple>();

			for (Entry<Integer,String> entry : hintResults.entrySet()) {
				hintTupleLst.add(new HintTuple(entry.getKey(),entry.getValue()));
			}

			row_num = hintResults.size();

			for (HintTuple hint : hintTupleLst) { 
				HintResultLine line = new HintResultLine(hint);
				hintsPanel.add(line);
			}
		}

		hintsPanel.add(new NewHintLine());

		// add padding lines, if needed
		for (int i = 0; i<MAX_NUM_DEFS - ADD_ROWS_NUM - row_num; i++) {
			hintsPanel.add(new JPanel());
		}

		hintsPanel.revalidate();
		tabbedPane.revalidate();

	}

	/**
	 * one line in definitions tab: topic(s),definition,delete 
	 * @author yonatan
	 *
	 */
	private class DefinitionLine extends JPanel {
		private CheckComboBox topicBox;
		private JTextField definitionBox;
		private  JButton deleteBtn;

		private int definitionID;
		private String definitionText;
		private int entityID;


		protected JPanel btnPanel;

		public DefinitionLine(int entityID, int definitionID, String definition) throws SQLException {
			this.definitionText = definition;
			this.definitionID = definitionID;
			this.entityID = entityID; 
			initialize();
		}

		private void initialize() throws SQLException {
			setLayout(new BorderLayout());

			Map<String,Integer> topics = getTopicsForDefinition(definitionID);
			topicBox = new TopicsCheckComboBox(allTopics.keySet(), topics.keySet(), true);
			super.add(topicBox, BorderLayout.WEST);
			definitionBox = new LimitedTextField(70);
			definitionBox.setText(definitionText);
			definitionBox.setEditable(false);

			super.add(definitionBox, BorderLayout.CENTER);

			btnPanel = new JPanel();
			btnPanel.setLayout(new GridLayout(1,2));

			deleteBtn = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/delete_small.png")));		
			deleteBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (definitionCounter - 1 >= 1){
						if (!DefinitionLine.this.definitionBox.getText().toString().isEmpty()) {
							try {
								KnowledgeManagement.deleteEntityDefinition(DefinitionLine.this.entityID, DefinitionLine.this.definitionID);
							} catch (SQLException e1) {
								Utils.showErrorMessage("Could not delete definition");
								return;
							}
							definitionCounter--;
							JPanel parent = (JPanel) DefinitionLine.this.getParent();
							parent.remove(DefinitionLine.this);
							parent.revalidate();
						}
						try {
							buildDefinitionPanel(entityID);
						} catch (SQLException e1) {
							Utils.showErrorMessage("Could not tabs for knowledge fact");
							return;
						}
					}
					else { // popup error message
						JOptionPane.showMessageDialog(MainView.getView().getFrame(),
								"Knowledge Fact must have at least one definition.");
					}
				}
			});

			btnPanel.add(deleteBtn);
			add(btnPanel, BorderLayout.EAST);
		}
	}

	private class HintResultLine extends JPanel {

		protected JButton deleteBtn;
		protected JPanel btnPanel;
		private HintTuple hint;
		private JTextField field;


		public HintResultLine(HintTuple hint) {
			setLayout(new BorderLayout());
			this.hint = hint;

			field = new JTextField(hint.getText());
			field.setEditable(false);
			add(field, BorderLayout.CENTER);

			btnPanel = new JPanel();
			btnPanel.setLayout(new BorderLayout());
			deleteBtn = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/delete_small.png")));		
			deleteBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!field.getText().isEmpty()) {
						try {
							KnowledgeManagement.deleteHint(HintResultLine.this.hint.getId());
						} catch (SQLException e1) {
							Utils.showErrorMessage("Could not delete hint");
							return;
						}

						JPanel parent = (JPanel) HintResultLine.this.getParent();
						parent.remove(HintResultLine.this);
						parent.revalidate();
					}
				}
			});

			//			btnPanel.add(saveBtn);
			btnPanel.add(deleteBtn, BorderLayout.CENTER);
			add(btnPanel, BorderLayout.EAST);
		}
	}



	/**
	 *  one line in definitions tab: topic(s),new definition, add
	 * @author yonatan
	 *
	 */
	private class NewDefinitionLine extends JPanel {

		JComboBox<String> field;
		TopicsCheckComboBox topicBox;
		int entityID = -1;
		String entityText = null;

		NewDefinitionLine() { // for existing entity
			entityID = chosenEntityID;
			entityText = new String(chosenEntityString);
			initialize();
		}

		private void initialize()  {

			setLayout(new BorderLayout());

			field = createAutoCompleteBox(allDefinitions.keySet(), "", false);

			// changes topics combobox according to definition entered 
			field.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) { 
					NewDefinitionLine.this.remove(topicBox);
					ItemSelectable is = itemEvent.getItemSelectable();
					String text =  selectedString(is);
					if (allDefinitions.containsKey(text)) { // existing definition in definitions table
						// show the topics linked to this definition, cannot be changed
						Map<String, Integer> chosenDefinitionTopics;
						try {
							chosenDefinitionTopics = getTopicsForDefinition(allDefinitions.get(text));
						} catch (SQLException e) {
							Utils.showErrorMessage("Could not load topics for knowledge fact");
							return;
						} 
						topicBox = new TopicsCheckComboBox(allTopics.keySet(), chosenDefinitionTopics.keySet(), true);
					}
					else {
						// user may choose topics himself, beacuse this is a new definition
						topicBox = new TopicsCheckComboBox(allTopics.keySet(), Collections.singleton(USER_UPDATES_TOPIC), false);
					}
					NewDefinitionLine.this.add(topicBox,BorderLayout.WEST);
					NewDefinitionLine.this.revalidate();

				}

				private String selectedString(ItemSelectable is) {
					Object selected[] = is.getSelectedObjects();
					return ((selected.length == 0) ? "null" : (String) selected[0]);
				}
			});

			add(field, BorderLayout.CENTER);

			topicBox = new TopicsCheckComboBox(allTopics.keySet(), Collections.<String>emptySet() , true);
			add(topicBox, BorderLayout.WEST);

			JButton saveBtn = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/add_small.png")));
			saveBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (topicBox.getModel().getCheckeds().size() < 1 )  {
						JOptionPane.showMessageDialog(MainView.getView().getFrame(),"<html><center>You must enter definition and then choose at least one topic.</html>" );
						return;
					}
					if (field.getSelectedItem() != null ) {
						String definitionText = field.getSelectedItem().toString();
//						if	( !definitionText.isEmpty() && 
//								(isValidString(definitionText))) { // text is entered and valid
						if	( !definitionText.isEmpty()) {
							definitionCounter++;
							Integer retID = allDefinitions.get(definitionText);
							int definitionID = (retID == null) ? -1 : retID; 
							int[] ret = null;
							try {
								ret = KnowledgeManagement.addDefinitionToEntitiy(
										NewDefinitionLine.this.entityID,
										NewDefinitionLine.this.entityText, 
										definitionID,
										definitionText, 
										topicBox.getUserSelectedTopics()
										);
							} catch (SQLException e) {
								Utils.showErrorMessage("Failed to add definition. ");
								return;
							}

							// update the entityID;
							if (ret[0] != NewDefinitionLine.this.entityID) { 
								// add to entity search box
								ManagementView.this.allEntities.put(entityText, ret[0]); 
								existingTextField = createAutoCompleteBox(allEntities.keySet(), "", true); 
								// add to definition search box
								allDefinitions.put(definitionText, ret[1]); 
							}
							
							NewDefinitionLine.this.entityID = ret[0]; 
							ManagementView.this.chosenEntityID = ret[0];
							chosenEntityString = entityText;


							try {
								buildDefinitionPanel(entityID);
							} catch (SQLException e) {
								Utils.showErrorMessage("Could not load definition tab for knowledge fact");
								return;
							}
							tabbedPane.setEnabledAt(tabbedPane.getTabCount()-1, true);

						}
						else  { // not valid / empty text - show error message
							JOptionPane.showMessageDialog(MainView.getView().getFrame(),
									"<html><center>Invalid or Empty Text.</html>");
						}

					}
				}
			});

			add(saveBtn, BorderLayout.EAST);
		}


	}

	private class NewHintLine extends JPanel {


		private JButton saveBtn;
		private JTextField field;
		private int entityID;


		NewHintLine() {
			this.entityID = chosenEntityID;
			initialize();
		}

		private void initialize() {
			setLayout(new BorderLayout());

			field = new JTextField();
			add(field, BorderLayout.CENTER);
			field.addKeyListener(new KeyAdapter() {

				@Override
				public void keyTyped(KeyEvent arg0) {
					saveBtn.setEnabled(true);					
				}
			});

			saveBtn = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/add_small.png")));
			saveBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String hintText = field.getText();
//					if (!hintText.isEmpty() && isValidString(hintText)) {
					if (!hintText.isEmpty()) {
						//get updated entityID, in case we created a new entity and we just got its ID
						NewHintLine.this.entityID = ManagementView.this.chosenEntityID;
						//call DB add procedure
						try {
							KnowledgeManagement.addHint(NewHintLine.this.entityID, hintText);
							buildHintsPanel(entityID);

						} catch (SQLException e1) {
							Utils.showErrorMessage("failed to add hint.");
							return;
						}

						//rebuild panel
						tabbedPane.setSelectedIndex(tabbedPane.getComponentCount()-1);
					}
					else  { // show error message
						JOptionPane.showMessageDialog(MainView.getView().getFrame(),
								"Invalid Text.");
					}
				}
			});
			add(saveBtn, BorderLayout.EAST);
		}
	}


	/**
	 * because hint text may not be unique, keep the ID and String together
	 * @author yonatan
	 *
	 */
	private class HintTuple {
		private int id;
		private String text;

		public HintTuple(int id, String text) {
			this.id = id;
			this.text = text;
		}

		public int getId() {
			return id;
		}

		public String getText() {
			return text;
		}
	}


	private class BackButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			MainView.getView().showWelcomeView();
		}
	}


	/**
	 * AutoComplete for SearchBox
	 * @param valueList
	 * @param firstvalue
	 * @param strict
	 * @return
	 */
	private JComboBox<String> createAutoCompleteBox(Set<String> valueList, String firstvalue, boolean strict) {

		JComboBox<String> box = new JComboBox<String>();
		Arrays.sort(valueList.toArray());
		AutoCompleteSupport<Object> autoBox =  AutoCompleteSupport.install(box, GlazedLists.eventListOf(valueList.toArray()));
		autoBox.setFirstItem(firstvalue);
		autoBox.setStrict(strict);
		return box;
	}


//	private boolean isValidString(String string) {
//		return string.matches("^[A-Za-z0-9 \\(\\)]+(-[A-Za-z0-9 \\(\\)]+)*$");
//	}

	class TopicsCheckComboBox extends CheckComboBox {
		private boolean lock;
		//private Set<String> allTopics;
		private Set<String> topics;

		public TopicsCheckComboBox(Set<String> allTopics, Set<String> topics, boolean lock) {
			this.lock = lock;
			this.topics = topics;

			super.setTextFor(CheckComboBox.NONE, "* no items selected *"); 
			super.setTextFor(CheckComboBox.MULTIPLE, "* multiple items *"); 
			super.setTextFor(CheckComboBox.ALL, "* all selected *"); 

			addTopicCheckBoxes();

		}

		private void addTopicCheckBoxes() {
			ListCheckModel model = this.getModel(); 
			for (String topic : allTopics.keySet()) { 
				model.addElement(topic);
				if (topics.contains(topic)) {
					model.addCheck(topic);
					if (topic.compareTo(USER_UPDATES_TOPIC) == 0) { // if USER_UPDATES box should be checked, make it locked
						model.addLock(topic);
					}
				}
			}
			if (lock)
				model.lockAll();


		}

		private List<Integer> getUserSelectedTopics() {
			List<Integer>  lst = new ArrayList<Integer>();
			for (Object object : getModel().getCheckeds()) {
				lst.add(allTopics.get(object.toString()));
			}
			return lst;
		}
	}

}