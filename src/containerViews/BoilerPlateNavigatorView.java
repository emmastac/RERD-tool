package containerViews;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.GenericBoilerPlate;
import models.OntologyWordModel;
import utils.Memory;

public class BoilerPlateNavigatorView extends BorderPane{
	
	TableView<GenericBoilerPlate> prefixes,mainBodies,suffixes;
//	protected AbsLevelAndCatNameHeaderView categoryBox;
//	protected HBox topPanel, bottomPanel;
	private BorderPane centerBox;
	
	public BoilerPlateNavigatorView(String category){
		
//		this.setTop(categoryBox = new AbsLevelAndCatNameHeaderView());
//		this.setTop( topPanel = drawTopPanel() );
		this.setCenter(centerBox = new BorderPane() );
//		this.setBottom(bottomPanel = drawBottomPanel(""));
		this.setMinHeight(200);
		init();
	}
	
	protected void init() {
		
		VBox prefixPane = new VBox(10);
		prefixPane.setPadding(new Insets(5));
		prefixPane.getChildren().add(prefixes = new TableView<GenericBoilerPlate>());
		
		VBox mainBodyPane = new VBox(10);
		mainBodyPane.setPadding(new Insets(5));
		mainBodyPane.getChildren().add(mainBodies = new TableView<GenericBoilerPlate>());
		
		VBox suffixPane = new VBox(10);
		suffixPane.setPadding(new Insets(5));
		suffixPane.getChildren().add(suffixes = new TableView<GenericBoilerPlate>());

		TableColumn<GenericBoilerPlate, String> prefixIDColumn= new TableColumn<>("ID");
		prefixIDColumn.setCellValueFactory(new PropertyValueFactory<GenericBoilerPlate,String>("ID"));
		TableColumn<GenericBoilerPlate, String> prefixColumn = new TableColumn<>("Prefixes");
		prefixColumn.setCellValueFactory(new PropertyValueFactory<GenericBoilerPlate,String>("templateForm"));
		
		TableColumn<GenericBoilerPlate, String> mainBodyIDColumn= new TableColumn<>("ID");
		mainBodyIDColumn.setCellValueFactory(new PropertyValueFactory<GenericBoilerPlate,String>("ID"));
		TableColumn<GenericBoilerPlate, String> mainBodyColumn = new TableColumn<>("Main Bodies");
		mainBodyColumn.setCellValueFactory(new PropertyValueFactory<GenericBoilerPlate,String>("templateForm"));
		
		TableColumn<GenericBoilerPlate, String> suffixIDColumn= new TableColumn<>("ID");
		suffixIDColumn.setCellValueFactory(new PropertyValueFactory<GenericBoilerPlate,String>("ID"));
		TableColumn<GenericBoilerPlate, String> suffixColumn = new TableColumn<>("Suffixes");
		suffixColumn.setCellValueFactory(new PropertyValueFactory<GenericBoilerPlate,String>("templateForm"));
		
		prefixes.getColumns().addAll(prefixIDColumn,prefixColumn);
		mainBodies.getColumns().addAll(mainBodyIDColumn,mainBodyColumn);
		suffixes.getColumns().addAll(suffixIDColumn,suffixColumn);
		
		prefixColumn.prefWidthProperty().bind(prefixes.widthProperty().multiply(0.7));
		mainBodyColumn.prefWidthProperty().bind(mainBodies.widthProperty().multiply(0.7));
		suffixColumn.prefWidthProperty().bind(suffixes.widthProperty().multiply(0.7));
		prefixIDColumn.prefWidthProperty().bind(prefixes.widthProperty().multiply(0.29));
		mainBodyIDColumn.prefWidthProperty().bind(mainBodies.widthProperty().multiply(0.29));
		suffixIDColumn.prefWidthProperty().bind(suffixes.widthProperty().multiply(0.29));
		
		// add event handler for clicking on the main bodies table
		mainBodies.setOnMouseClicked((MouseEvent) -> {
				RequirementTab.sendMainForEditing(new GenericBoilerPlate( mainBodies.getSelectionModel().getSelectedItem().serialize( )));
			});
		
		// add event handler for clicking on the prefixes table
		prefixes.setOnMouseClicked((MouseEvent) -> {
			// create a GenericBoilerPlate object out the selected prefix (in serialized form) and send it to the editing panel
				RequirementTab.sendPrefixForEditing(new GenericBoilerPlate( prefixes.getSelectionModel().getSelectedItem().serialize( )));
			});
		
		// add event handler for clicking on the suffixes table
		suffixes.setOnMouseClicked((MouseEvent) -> {
				RequirementTab.sendSuffixForEditing(new GenericBoilerPlate( suffixes.getSelectionModel().getSelectedItem().serialize( )));
			});
		
		centerBox.setPadding(new Insets(10));
		centerBox.setLeft(prefixPane);
		centerBox.setCenter(mainBodyPane);
		centerBox.setRight(suffixPane);
		displayAllBoilerplates();
	}
	
//	protected HBox drawTopPanel(){
//		return categoryBox = new AbsLevelAndCatNameHeaderView();
//	}

//	public HBox drawBottomPanel(String category){
//		
//		HBox headerBox = new HBox(10);
//		headerBox.setPadding(new Insets(10));
//		
//		Button catButton = new Button("Back to Categories");
//		catButton.setOnAction((ActionEvent) -> {
//				RequirementTab.switchToCategorySelection();
//			});
//		
//		headerBox.getChildren().add(catButton);
//		
//		return headerBox;
//	}
	
	public void displayAllBoilerplates() {
		prefixes.getItems().clear();
		mainBodies.getItems().clear();
		suffixes.getItems().clear();
		try{
			Memory.getPrefixTemplates().keySet().forEach((ID) -> {
				try{
					prefixes.getItems().add(new GenericBoilerPlate(ID, Memory.getPrefixTemplates().get(ID)));
				}
				catch (NullPointerException e){ System.out.println("Failed to template parse prefix "+ID); }
			});
		}catch (NullPointerException e){ /*e.printStackTrace();*/ }
		
		try{
			Memory.getBodyTemplates().keySet().forEach((ID) -> {
				try{
					mainBodies.getItems().add(new GenericBoilerPlate(ID, Memory.getBodyTemplates().get(ID)));
				}
				catch (NullPointerException e){ System.out.println("Failed to template parse body "+ID); }
			});
		}catch (NullPointerException e){ /*e.printStackTrace();*/ }
		
		try{
			Memory.getSuffixTemplates().keySet().forEach((ID) -> {
				try{
				suffixes.getItems().add(new GenericBoilerPlate(ID, Memory.getSuffixTemplates().get(ID)));
				}
				catch (NullPointerException e){ System.out.println("Failed to template parse suffix "+ID); }
			});
		}catch (NullPointerException e){ /*e.printStackTrace();*/ }
	}

//	public void setCategory(OntologyWordModel newCat) {
//		
//		categoryBox.setCategoryName(newCat);
////		reloadBoilerPlates(newCat);
//	}
//
//	public void setAbsLevel(OntologyWordModel newAbsLevel) {
//		
//		categoryBox.setAbsLevel(newAbsLevel);
//	}
//
//	public AbsLevelAndCatNameHeaderView getCategoryBox() {
//		return categoryBox;
//	}
}
