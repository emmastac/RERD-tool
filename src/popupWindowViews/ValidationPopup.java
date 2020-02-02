package popupWindowViews;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.ValidationResultModel;
import utils.OntologyIO;
import containerViews.ValidationResultView;

	//Needs to run inference model in the background first
	// And then all other methods will be able to run
public class ValidationPopup extends Stage {
	private Scene scene;
	private final static int WIDTH = 600;
	private final static int HEIGHT = 150;
	private static final int WRAPPER_PADDING = 20;
	private static final int ITEM_SPACING = 50;
	protected BorderPane mainPane;
	protected VBox buttonBox;
	protected HBox bottomPanel;
	private VBox loading;
	
	public ValidationPopup(Window owner) {
		
		buttonBox = selectValtoRun();
		mainPane = new BorderPane(loading = loadingBox());
		mainPane.setPadding(new Insets(WRAPPER_PADDING));
		mainPane.setBottom(bottomPanel = createbottomPanel());
		setScene(scene = new Scene(mainPane));
		setTitle("Validate Model");
		initModality(Modality.APPLICATION_MODAL);
		initOwner(owner);

		show();
		
		Runnable spinInfs = () -> { 
			OntologyIO.runSpinInferences();
			Platform.runLater(() -> {
				mainPane.setCenter(buttonBox);
				mainPane.autosize();
				ValidationPopup.this.sizeToScene();;
				});
		};
		new Thread(spinInfs).start();
	}
	
	private VBox loadingBox(){
		VBox loadingBox = new VBox();
		loadingBox.setAlignment(Pos.BASELINE_CENTER);
		loadingBox.setPadding(new Insets(WRAPPER_PADDING));
		loadingBox.setSpacing(ITEM_SPACING);
		
		loadingBox.getChildren().add(new Label("Running analysis on SPIN model, please wait..."));
		loadingBox.getChildren().add(new Label("This process may take up to a few minutes ..."));
		
		return loadingBox;
	}
	
	private HBox createbottomPanel() {
		HBox bottomBox = new HBox(ITEM_SPACING);
		bottomBox.setPadding(new Insets(WRAPPER_PADDING));
		
		Button backButton = new Button("Back to validation select");
		backButton.setOnAction(aE -> {
			mainPane.setCenter(buttonBox);
			bottomPanel.setVisible(false);
		});
		
		bottomBox.getChildren().add(backButton);
		bottomBox.setVisible(false);
		return bottomBox;
	}

	protected VBox selectValtoRun(){
		VBox valButtonBox =  new VBox(50);
		valButtonBox.setAlignment(Pos.BASELINE_CENTER);
		valButtonBox.setPadding(new Insets(WRAPPER_PADDING));
		
		Button nonInstant = new Button("Non Instantiated Abstract Requirements");
		nonInstant.setOnAction(aE -> {
			mainPane.setCenter(showResultsFor(OntologyIO.nonInstantiatedAbstrReqs()));
		});
		Button nonConc = new Button("Non Concretized Abstract Requirements");
		nonConc.setOnAction(aE -> {
			mainPane.setCenter(showResultsFor(OntologyIO.NonConcretizedAbstrReqs()));
		});
		Button entWoReqs = new Button("Entities without Requirements");
		entWoReqs.setOnAction(aE -> {
			mainPane.setCenter(showResultsFor(OntologyIO.EntitiesWithourReqs()));
		});
		Button subjEntWoReqs = new Button("Subject entities without Requirements");
		subjEntWoReqs.setOnAction(aE -> {
			mainPane.setCenter(showResultsFor(OntologyIO.SubjectEntitiesWithourReqs()));
		});
		Button actorEntWoReqs = new Button("Actor entities without Requirements");
		actorEntWoReqs.setOnAction(aE -> {
			mainPane.setCenter(showResultsFor(OntologyIO.ActortEntitiesWithourReqs()));
		});
		Button contrReqs = new Button("Contradicting Requirements");
		contrReqs.setOnAction(aE -> {
			mainPane.setCenter(showResultsFor(OntologyIO.ContradictReqs()));
		});
		
		valButtonBox.getChildren().addAll(nonInstant,nonConc,entWoReqs,subjEntWoReqs,actorEntWoReqs,contrReqs);
		
		return valButtonBox;
	}
	
	protected ValidationResultView showResultsFor(ValidationResultModel valResults) {
		bottomPanel.setVisible(true);
		return new ValidationResultView(valResults);
	}
}
