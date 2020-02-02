package childViews;

import java.io.IOException;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import popupWindowViews.ExportSettingsPopup;
import popupWindowViews.PreferencesPopup;
import utils.ExtStrings;
import utils.Memory;
import containerViews.MainAppFrame;

/**
 * Creates the menubar to be used
 * @author alkoclick
 *
 */
public class CustomMenuBar extends MenuBar {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a frontend JMenuBar
	 */
	public CustomMenuBar(){
		this.getMenus().add(fileMenu());
//		this.getMenus().add(editMenu());
//		this.getMenus().add(viewMenu());
//		this.getMenus().add(helpMenu());
	}

	/**
	 * Builds and returns the file menu
	 * @return the file menu
	 */
	private Menu fileMenu(){
		Menu menu = new Menu("File");
//		menu.setMnemonic(KeyEvent.);
		
		MenuItem open = new MenuItem("Open Ontology File");
		MenuItem save = new MenuItem("Save to Ontology");
//		MenuItem saveAs = new MenuItem("Save As...");
		MenuItem expReq = new MenuItem("Export Requirements");
		MenuItem expProp = new MenuItem("Export Properties");
		MenuItem exit = new MenuItem("Exit");
		MenuItem pref = new MenuItem("Preferences");
		
		pref.setOnAction((ActionEvent) -> {
			new PreferencesPopup(getScene().getWindow()) ;
		});
		
		save.setOnAction((ActionEvent) -> {
			try {
				Memory.export();
			} catch (IOException e1) {
				System.out.println("Ontology file not writable");
			}
		});
		
		exit.setOnAction((ActionEvent) -> {
			MainAppFrame.exit();
		}); 
		
		
		open.setDisable( true );

		expReq.setOnAction(aE -> {
			new ExportSettingsPopup(getScene().getWindow(), ExtStrings.getString("ExportSettingsReqPopupTitle") );
		});
		
		expProp.setOnAction(aE -> {
			new ExportSettingsPopup(getScene().getWindow(), ExtStrings.getString("ExportSettingsPropPopupTitle") );
		});
		

		menu.getItems().add(open);
		menu.getItems().add(save);
//		menu.getItems().add(saveAs);
		menu.getItems().add(expReq);
		menu.getItems().add(expProp);
		menu.getItems().add(exit);
		menu.getItems().add(pref);
		
		return menu;
	}
	

	
	/** 
	 * Builds and returns the edit menu
	 * @return
	 */
	private Menu editMenu() {
		Menu menu = new Menu("Edit");
//		menu.setMnemonic(KeyEvent.VK_E);
		
		MenuItem undo = new MenuItem("Undo"); 
//		undo.setToolTipText("Undo last action"); // TODO Make these dynamic?
		menu.getItems().add(undo);
		
		MenuItem redo = new MenuItem("Redo"); 
//		redo.setToolTipText("Redo last action");
		menu.getItems().add(redo);
		
		MenuItem search = new MenuItem("Search");
//		search.setToolTipText("Search the project for a keyword");
		menu.getItems().add(search);
		
		return menu;
	}

	/**
	 * Builds and returns the view menu
	 * @return
	 */
	private Menu viewMenu() {
		Menu menu = new Menu("View");
//		menu.setMnemonic(KeyEvent.VK_V);
		
		MenuItem typesVis = new MenuItem("Types visible"); // TODO Should be a checkbox
//		typesVis.setToolTipText("Toggle visibility of ontology types");
		menu.getItems().add(typesVis);
		
		return menu;
	}
	
	/**
	 * Builds and returns the help menu
	 * @return
	 */
	private Menu helpMenu() {
		Menu menu = new Menu("Help");
//		menu.setMnemonic(KeyEvent.VK_H);
		
		MenuItem welcome = new MenuItem("Welcome"); 
//		welcome.setToolTipText("Initial help");
		menu.getItems().add(welcome);
		
		MenuItem help = new MenuItem("Help Contents"); 
//		help.setToolTipText("Complete help archive");
		menu.getItems().add(help);
		
		MenuItem updateCheck = new MenuItem("Check for Updates"); 
//		updateCheck.setToolTipText("Check for updated versions of the software");
		menu.getItems().add(updateCheck);
		
		MenuItem contactDevs = new MenuItem("Contact Developers"); 
//		contactDevs.setToolTipText("Check for updated versions of the software");
		menu.getItems().add(contactDevs);
		
		MenuItem about = new MenuItem("About");
//		about.setToolTipText("About this editor"); // TODO Externalize and find a name
		menu.getItems().add(about);
		
		return menu;
	}
}
