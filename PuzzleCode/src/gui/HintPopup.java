package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;

final class HintPopup extends JPopupMenu {
	private Hint[] hintArray;
	JDefinitionLabel definitionLabel;

	HintPopup(JDefinitionLabel label, int entityID) {
		//GuiDBConnector conn = new GuiDDBconnectorImpl();
		//hintsList = conn.getHints(entityID);
		// for now
		// hintList = Hint[hintsList.size]; static decleration
		hintArray = new Hint[5];// for now
		this.definitionLabel = label;
		buildPopupSubMenus();
	}

	private void buildPopupSubMenus() {

		for (int i=0; i<hintArray.length; i++) {
			Hint hint = new Hint("19/2/84", "Was Born On ?", this); //replacer replaces "?" in predicateString
			hintArray[i] = hint;
		}

		Arrays.sort(hintArray); // sort before replace with entity name

		// add the hints to the popup menu
		for (int i=0; i<hintArray.length; i++){ // sorted by predicate
			add(new HintPopupSubMenu("Hint #" + (i+1), hintArray[i].getHintText())); 
		}
	}

	private class HintPopupSubMenu extends JMenu {
		private String popupText;
		private HintItem hintItem;
		private HintPopupSubMenu(String popupText, String hintText) {
			super();
			this.setIcon(new ImageIcon(CrosswordView.class.getResource("/resources/tip.png")));
			this.popupText = popupText;
			hintItem = createHintItem(hintText);
			add(hintItem);
		}

		private HintItem createHintItem(String hintText) {
			return new HintItem(hintText);
		}
		public String getText() {
			return popupText;
		}

		private class HintItem extends JPanel {
			private final String hintText;
			private JLabel lbl;
			private HintItem(String hintText) {

				super();

				this.hintText = hintText; // save the hint text for later
				this.setLayout(new BorderLayout());
				lbl = new JLabel("<html><p><left>" + createEchoString(hintText.length()) + "</p></html>");
				add(lbl, BorderLayout.CENTER);
				JButton imageBtn = new JButton(new ImageIcon(HintPopup.class.getResource("/resources/locked.png")));

				// set button behavior
				imageBtn.addActionListener(new LockButtonListener(this.lbl));

				add(imageBtn, BorderLayout.EAST);
			}
			
			private String createEchoString(int length) {
				StringBuilder str = new StringBuilder();
				for (int i = 0; i<length; i++ ) {
					str.append('*');
				}
				return str.toString();
			}
			
			private class LockButtonListener implements ActionListener {
				boolean isLocked = true;
				JLabel lbl;
				private LockButtonListener(JLabel lbl) {
					this.lbl = lbl;
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					if (isLocked) {
						JButton btn = (JButton) e.getSource();
						btn.setIcon(new ImageIcon(HintPopup.class.getResource("/resources/unlocked.png")));
						btn.setEnabled(false);
						this.lbl.setText(HintItem.this.hintText);
						
					}
				}
			}
		}
	}

}