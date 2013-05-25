package gui;

import gui.KnowledgeManagement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MainController {

	private MainModel model = null; 
	private MainView view = null;
	
	MainController(MainModel model, MainView view) {
		this.model = model;
		this.view = view;
		
		//add Controller listeners to View
		view.addMassiveImportListener(new MassiveImportListener());	
		view.addUpdateKnowledgeListener(new UpdateKnowledgeListener());
		view.addPlayListener(new playListener());
	}
	
	class MassiveImportListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	}
	
	class UpdateKnowledgeListener implements ActionListener {


		@Override
		public void actionPerformed(ActionEvent e) {
			KnowledgeManagement view = new KnowledgeManagement();
			view.setVisible(true);
		}
	}
	
	class playListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			CrosswordView view = new CrosswordView();
			view.setVisible(true);
		}
	}
}