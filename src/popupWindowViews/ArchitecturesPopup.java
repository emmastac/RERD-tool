package popupWindowViews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.Architecture;
import models.PropertyModel;
import utils.Memory;

public class ArchitecturesPopup extends Stage {
	private static final String TITLE_TEXT = "Select architecture";
	private static final String TOP_DESCRIPTION_TEXT = "Select architecture to associate with property";
	ObservableList<Architecture> generalList; // The general contents (All in
												// memory)
	// ObservableList<RequirementModel> specificList; // This item's contents
	// (All in item)
	Architecture choice;
	Label archLabel;
	PropertyModel source;

	private HBox newArchBox;
	private HBox errorBox;
	private HBox existingArchBox;

	public ArchitecturesPopup(Window owner, Label archLabel,PropertyModel source) {

		this.archLabel = archLabel;
		this.generalList = FXCollections.observableArrayList();
		this.generalList.addAll(Memory.getArchitectures().values());
		this.source = source;
		setTitle(TITLE_TEXT);
		initModality(Modality.APPLICATION_MODAL);
		initOwner(owner);

		setScene(new Scene(mainContents()));

		show();
	}

	protected BorderPane mainContents() {

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		VBox centerContents = new VBox(10);
		this.newArchBox =  newArch();
		this.existingArchBox = existingArch();
		this.errorBox =errorBox();
		centerContents.getChildren().addAll( this.existingArchBox, this.errorBox, this.newArchBox);

		borderPane.setCenter(centerContents);
		borderPane.setTop(new Label(TOP_DESCRIPTION_TEXT));
		BorderPane.setMargin(borderPane.getCenter(), new Insets(10));
		BorderPane.setMargin(borderPane.getTop(), new Insets(10));

		return borderPane;
	}
	
	protected HBox errorBox(){
		HBox errorMsg =  new HBox(10);
		errorMsg.getChildren( ).add( new Label("") );
		return errorMsg;
		
	}
	
	protected void setTextErrorBox(String text){
		((Label) this.errorBox.getChildren( ).get( 0 )).setText( text );
	}

	protected HBox existingArch() {
		HBox existingArchPanel = new HBox(10);

		TableView<Architecture> fullTable = new TableView<>(generalList);

		Button doneButton = new Button("Choose selected");
		doneButton.setOnAction((ActionEvent) -> {
			setTextErrorBox("");
			choice = fullTable.getSelectionModel().getSelectedItem();
			connectToProp();
			ArchitecturesPopup.this.close();
		});
		existingArchPanel.getChildren().addAll(fullTable, doneButton);

		TableColumn<Architecture, String> userDefColumn = new TableColumn<>("User Defined");
		userDefColumn.setCellValueFactory(new PropertyValueFactory<>("userSpecified"));
		userDefColumn.prefWidthProperty().bind(fullTable.widthProperty().multiply(0.50));
		TableColumn<Architecture, String> fullIDColumn = new TableColumn<>("Arch. ID");
		fullIDColumn.setCellValueFactory(new PropertyValueFactory<>("archID"));
		fullIDColumn.prefWidthProperty().bind(fullTable.widthProperty().multiply(0.49));
		fullTable.getColumns().addAll(userDefColumn,fullIDColumn);

		refreshExistingList();

		return existingArchPanel;
	}
	
	protected void refreshExistingList(){
		this.generalList.clear();
		this.generalList.addAll(Memory.getArchitectures().values());
	}
	
	protected HBox newArch() {
		HBox newArchPanel = new HBox(10);

		TextField userDefined = new TextField();
		userDefined.setPromptText("Type here");
	
		
		ComboBox<String> archIDSelect = new ComboBox<>();
		archIDSelect.getItems().addAll(Memory.getBaseArchIDs().keySet( ));

		Button addNewButton = new Button("Add New");

		newArchPanel.getChildren().addAll(userDefined, archIDSelect);

		if (!archIDSelect.getItems().isEmpty()) {
			archIDSelect.selectionModelProperty().get().select(0);
			Architecture newArch = new Architecture(archIDSelect.getSelectionModel().getSelectedItem());
			newArch.archID.bind(archIDSelect.selectionModelProperty().get().selectedItemProperty());
			newArch.userSpecified.bind(userDefined.textProperty());
			
			addNewButton.setOnAction((ActionEvent) -> {
				String errorText = checkUserSpecifiedIsUnique (newArch);
				if ( !errorText.equals( "" ) ){
					setTextErrorBox(errorText);
				}else{
					setTextErrorBox("");
					Memory.addArchitecture(newArch);
					choice = newArch;
					connectToProp();
				}
				
			});
			
			
		} else {
			userDefined.setText("Unable to load arch IDs from memory");
			newArchPanel.setDisable(true);
		}
		newArchPanel.getChildren().add(addNewButton);
		
		return newArchPanel;
	}
	
	private String checkUserSpecifiedIsUnique(Architecture arch ){
		if( Memory.getArchitectures( ).containsKey( arch.serialize( ) ) ){
			return "The chosen architecture name already exists. Please select a new one.";
		}
		else return "";
	}
	
	private void connectToProp(){
		this.source.setArchitecture(choice);
		refreshExistingList();
//		ArchitecturesPopup.this.close();
	}
}
