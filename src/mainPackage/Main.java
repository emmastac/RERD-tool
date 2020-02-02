package mainPackage;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.Memory;
import utils.OntologyIO;
import containerViews.MainAppFrame;

public class Main extends Application{

	public static void main(String[] args) throws IOException {
		
		
		OntologyIO jtest = new OntologyIO();
		OntologyIO.init(); // Initialize ontology models
		
		Memory.load();
		launch(MainAppFrame.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
	}

}
