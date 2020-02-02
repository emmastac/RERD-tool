package popupWindowViews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.AbstractSpecificationModel;
import utils.ExtStrings;

public class DoubleListPopup<M extends AbstractSpecificationModel> extends Stage {
	private static final String DESC_ENDING_PATTERN = "Desc";
	ObservableList<M> generalList;  // The general contents (All in memory)
	ObservableList<M> specificList; // This item's contents (All in item)
	
	public DoubleListPopup(Window owner, String title, 
			ObservableList<M> generalList , ObservableList<M> specificList, 
			M[] excludedList){

		this.specificList = specificList;
		this.generalList = FXCollections.observableArrayList();
		this.generalList.addAll(generalList);
		this.generalList.removeAll(specificList);
		this.generalList.removeAll(excludedList);
		setTitle((title!=null && !title.isEmpty()) ? title : "");
		initModality(Modality.APPLICATION_MODAL);
		initOwner(owner);
		
		setScene(new Scene(mainContents()));

		show();
	}
	
	protected BorderPane mainContents(){
		
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));
		
		HBox centerContents = new HBox(10);
		
		ObservableList<M> allReqs = FXCollections.observableArrayList();
		allReqs.addAll(this.generalList);
		TableView<M> fullTable = new TableView<>(allReqs);
		fullTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		TableColumn<M, String> fullIDColumn = new TableColumn<>("Req. ID");
		fullIDColumn.setCellValueFactory(new PropertyValueFactory<>("reqID"));
		fullIDColumn.prefWidthProperty().bind(fullTable.widthProperty().multiply(0.99));
		fullTable.getColumns().add(fullIDColumn);
		
		TableView<M> derivedTable = new TableView<>(this.specificList);
		derivedTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		TableColumn<M, String> idColumn = new TableColumn<>("Req. ID");
		idColumn.setCellValueFactory(new PropertyValueFactory<>("reqID"));
		idColumn.prefWidthProperty().bind(derivedTable.widthProperty().multiply(0.99));
		derivedTable.getColumns().add(idColumn);
		
		VBox buttonBox = new VBox(20);
		buttonBox.setAlignment(Pos.BASELINE_CENTER);
		buttonBox.setPadding(new Insets(20));
		
		Button addButton = new Button("->");
		Button removeButton = new Button("<-");
		addButton.setOnAction((ActionEvent) -> {
			ObservableList<M> selectedItems = fullTable.getSelectionModel().getSelectedItems();
			if (selectedItems!=null){
				derivedTable.getItems().addAll(selectedItems);
				fullTable.getItems().removeAll(selectedItems);
			}
		});
		removeButton.setOnAction((ActionEvent) -> {
			removeFromSelected(fullTable, derivedTable);
		});
		Button doneButton = new Button("Done selecting");
		doneButton.setOnAction((ActionEvent) -> {
			DoubleListPopup.this.close();
		});
		
		buttonBox.getChildren().addAll(addButton,removeButton,doneButton);
		
		centerContents.getChildren().addAll(fullTable,buttonBox,derivedTable);

		borderPane.setCenter(buttonBox);
		borderPane.setLeft(fullTable);
		borderPane.setRight(derivedTable);
		borderPane.setTop(new Label(ExtStrings.getString(getTitle().replace(" ", "_")+DESC_ENDING_PATTERN)));
		BorderPane.setMargin(borderPane.getCenter(), new Insets(10));
		BorderPane.setMargin(borderPane.getTop(), new Insets(10));
		BorderPane.setMargin(borderPane.getLeft(), new Insets(10));
		BorderPane.setMargin(borderPane.getRight(), new Insets(10));
		
		return borderPane;
	}
	
	private void removeFromSelected(TableView<M> fullTable, TableView<M> derivedTable) {
		ObservableList<M> selectedItems = derivedTable.getSelectionModel().getSelectedItems();
		if (selectedItems!=null) {
			fullTable.getItems().addAll(selectedItems);
			FXCollections.sort(fullTable.getItems());
			derivedTable.getItems().removeAll(selectedItems);
		}
	}
}
