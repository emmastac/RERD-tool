package popupWindowViews;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.OntologyWordModel;
import utils.Memory;
import utils.OntologyIO;

public class OntologyAdderPopup extends Stage {
	private static String MESSAGE_STRING;
	private Scene scene;
	private String unknownItemName;
	private SimpleStringProperty chosenCat;
	private VBox wrapperBox;
	private TextField nameField;
	private TextField descField;
	private Label catLabel;
	private final static int WIDTH = 600;
	private final static int HEIGHT = 150;
	private static final int WRAPPER_PADDING = 20;
	private static final Logger logger = Logger.getLogger( OntologyAdderPopup.class.getName( ) );

	/**
	 * Deactivated until we can split the listeners into specific cases for complex objects
	 * @param owner
	 * @param unknownItemName
	 */
	public OntologyAdderPopup(Window owner, String unknownItemName) {

		this.unknownItemName = unknownItemName;
		chosenCat = new SimpleStringProperty();
		MESSAGE_STRING = "Item " + unknownItemName + " was not found in ontology. Would you like to add it?";
		if (unknownItemName.length() < 50)
			setScene(scene = new Scene(initialQuestion()));
		else
			setScene(scene = new Scene(initialQuestion(), WIDTH, HEIGHT));
		setTitle("Add item to ontology");
		initModality(Modality.APPLICATION_MODAL);
		initOwner(owner);

		show();
	}

	private VBox initialQuestion() {

		wrapperBox = new VBox(50);
		wrapperBox.setPadding(new Insets(WRAPPER_PADDING));

		Label infoLabel = new Label(MESSAGE_STRING);
		infoLabel.setWrapText(true);
		infoLabel.setFont(new Font(18));

		HBox buttonBox = new HBox();
		// buttonBox.setPadding(new Insets(buttonBoxPadding=5));
		Button yes = new Button("Yes");
		yes.setOnAction((ActionEvent) -> setScene(scene = new Scene(addItemPanel())));
		Button no = new Button("No");
		no.setOnAction((ActionEvent) -> OntologyAdderPopup.this.close());
		yes.setFont(new Font(20));
		no.setFont(new Font(20));
		buttonBox.spacingProperty().bind(widthProperty().subtract(yes.widthProperty()).subtract(no.widthProperty())
				.subtract(2 * (WRAPPER_PADDING) + 10));

		buttonBox.getChildren().addAll(no, yes);

		wrapperBox.getChildren().addAll(infoLabel, buttonBox);
		return wrapperBox;
	}

	private BorderPane addItemPanel() {
		BorderPane wrapperPane = new BorderPane();
		wrapperPane.setPadding(new Insets(WRAPPER_PADDING));
		
		VBox wrapperBox = new VBox(10);
		wrapperBox.setPadding(new Insets(0, WRAPPER_PADDING, 0, 0));

		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);

		Label name = new Label("Name: ");
		Label desc = new Label("Description: ");
		Label classLabel = new Label("Class: ");

		nameField = new TextField(unknownItemName);
		descField = new TextField();
		catLabel = new Label();
		catLabel.textProperty().bind(chosenCat);
		pane.addColumn(0, name, desc, classLabel);
		pane.addColumn(1, nameField, descField,catLabel);

		wrapperBox.getChildren().add(pane);
		
		wrapperPane.setLeft(wrapperBox);
		wrapperPane.setCenter(availableOntologyClasses());
		wrapperPane.setBottom(buttonBox());
		return wrapperPane;
	}
	
	private VBox availableOntologyClasses(){
		VBox wrapperBox = new VBox(10);
		
		VBox radioPane = new VBox();
		ToggleGroup radioGroup = new ToggleGroup();
		
		ArrayList<OntologyWordModel> availableClasses = new OntologyIO().getUserInstanceEnabledClasses();
		availableClasses.sort(new Comparator<OntologyWordModel>() {

			@Override
			public int compare(OntologyWordModel o1, OntologyWordModel o2) {
				return o1.getShortName().compareTo(o2.getShortName());
			}
		});
		ArrayList<RadioButton> buttons = new ArrayList<>();
		availableClasses.forEach((item) -> {
			RadioButton rb = new RadioButton(item.getShortName());
			rb.setToggleGroup(radioGroup);
			rb.setOnAction((ActionEvent) -> chosenCat.setValue(item.getFullName()));
			buttons.add(rb);
		});
		radioPane.getChildren().addAll(buttons);
		
		ScrollPane scrollPane = new ScrollPane(radioPane);
		scrollPane.setPadding(new Insets(10));
		scrollPane.setFitToWidth(true);
		scrollPane.setMinWidth(100);
		
		TextField searchField = new TextField();
		searchField.setTooltip(new Tooltip("Search"));
		searchField.setPromptText("Search available categories...");
		
		searchField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				radioPane.getChildren().clear();
				radioGroup.getToggles().clear();
				buttons.clear();
				availableClasses.forEach((item) -> {
					if (item.getFullName().contains(searchField.getText())){
						RadioButton rb = new RadioButton(item.getShortName());
						rb.setToggleGroup(radioGroup);
						rb.setOnAction((ActionEvent) -> chosenCat.setValue(item.getFullName()));
						buttons.add(rb);
					}
				});
				radioPane.getChildren().addAll(buttons);
			}
		});
		
		wrapperBox.getChildren().addAll(searchField,scrollPane);
		
		return wrapperBox;
	}
	
	private HBox buttonBox(){
		HBox buttonBox = new HBox();
		buttonBox.setPadding(new Insets(WRAPPER_PADDING, 0, 0, 0));
		Button confirm = new Button("Add to ontology");
		confirm.setOnAction((ActionEvent) -> {
			try {
				OntologyIO.addAnyInstance(nameField.getText(), chosenCat.getValue(), descField.getText());
			} catch (FileNotFoundException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
//			OntologyIO.addIndividualToClass(nameField.getText(), descField.getText(), chosenCat.getValue());
//			RequirementEditViewWrapper.getConsole().getItems()
//					.add(new LogEntry(LogLevel.LOG, unknownItemName + " added to ontology class "+chosenCat.getValue()));
			OntologyAdderPopup.this.close();
		});
		Button cancel = new Button("Cancel");
		cancel.setOnAction((ActionEvent) -> OntologyAdderPopup.this.close());
		confirm.setFont(new Font(20));
		cancel.setFont(new Font(20));
		buttonBox.spacingProperty().bind(widthProperty().subtract(confirm.widthProperty())
				.subtract(cancel.widthProperty()).subtract(2 * (WRAPPER_PADDING) + 10));

		buttonBox.getChildren().addAll(cancel, confirm);
		
		return buttonBox;
	}
}
