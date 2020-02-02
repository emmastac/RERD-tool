package containerViews;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.ExtStrings;
import utils.OntologyIO;
import childViews.CustomMenuBar;

public class MainAppFrame extends Application{
	private static final long serialVersionUID = 1L;
	private static final String MAIN_APP_TITLE = ExtStrings.getString("MainTitle");
	public Color themeColor;
	private TabPane tabPane;
	private BorderPane mainPane;
	private Tab requirementsTab,propertiesTab,dictionaryTab;
	private Tab modelsTab;
	private Stage primaryStage;

	public MainAppFrame(){
		
		initContents();
	}

	private void initContents() {
		
		initTabs();
	}
	
	protected void initTabs(){
		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		requirementsTab = new RequirementTab();
		propertiesTab = new PropertyTab();
		dictionaryTab = new DictionaryTab();
		modelsTab = new ModelsTab();
		
		tabPane.getTabs().addAll(requirementsTab,propertiesTab,dictionaryTab, modelsTab);
		
		mainPane = new BorderPane(tabPane);
		mainPane.setTop(new CustomMenuBar());
	}

	private void drawMainWindow(Color themeColor) {
		primaryStage.setTitle(MAIN_APP_TITLE);
		primaryStage.setMaximized(true);
		
		Scene scene = new Scene(mainPane);
		File file = new File("styles/MainEditFrame.css");
		URL url;
		try {
			url = file.toURI().toURL();
			scene.getStylesheets().add(url.toExternalForm());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		primaryStage.setScene(scene);
        primaryStage.show();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		drawMainWindow(themeColor);
	}
	
	public static void exit(){
		OntologyIO.close();
		System.exit(0);
	}
	
	
}
