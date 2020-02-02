package containerViews;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Border;
import javafx.util.Callback;
import models.PropertyModel;
import utils.Memory;
import childViews.PropertyStatusView;

public class PropertyBrowserView extends TableView<PropertyModel>{
	
	private static ObservableList<PropertyModel> contents;
	
	public PropertyBrowserView(){
		reBuildTable();
		this.setBorder(Border.EMPTY);
	}
	
	private void reBuildTable() {
		TableColumn<PropertyModel, String> propIDColumn = new TableColumn<>("Prop. ID");
		propIDColumn.setCellValueFactory(new PropertyValueFactory<>("PropID"));
		
		TableColumn<PropertyModel,Button> editColumn = new TableColumn<>("Edit");
		editColumn.setCellValueFactory(new PropertyValueFactory<PropertyModel,Button>("editButton"));
		
		TableColumn<PropertyModel,PropertyStatusView> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusView"));
		
		TableColumn<PropertyModel,String> textColumn = new TableColumn<>("Text");
		textColumn.setCellValueFactory(new PropertyValueFactory<>("displayForm"));
		
		TableColumn<PropertyModel,String> catIDColumn = new TableColumn<>("Category");
		catIDColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
		
		TableColumn<PropertyModel, Boolean> deleteColumn = new TableColumn<>("Delete");
		deleteColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PropertyModel,Boolean>, ObservableValue<Boolean>>() {

			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<PropertyModel, Boolean> arg0) {
				return new SimpleBooleanProperty(arg0.getValue() != null);
			}
		});
		
		deleteColumn.setCellFactory(new Callback<TableColumn<PropertyModel,Boolean>, TableCell<PropertyModel,Boolean>>() {
			
			@Override
			public TableCell<PropertyModel, Boolean> call(TableColumn<PropertyModel, Boolean> arg0) {
				return new DeleteCell();
			}
		});
		
		getColumns().addAll(propIDColumn, statusColumn, textColumn, catIDColumn, editColumn, deleteColumn);
		
		initContents();
	}

	private void initContents() {
		
		this.getItems().clear();
		contents = Memory.getPropertiesInMemory(); 
		this.getItems().addAll(contents);
	}

	public void refreshContents(String searchString) {
		
		this.getItems().clear();
		ObservableList<PropertyModel> filteredItems = FXCollections.observableArrayList();
		if (contents.size() > 0)
		for (PropertyModel propMod : contents){
			if (searchString.length()==0 || propMod.contains(searchString)){
				filteredItems.add(propMod);
			}
		}
		this.getItems().addAll(filteredItems);
	}
	
	private class DeleteCell extends TableCell<PropertyModel, Boolean>{
		final Button delbutton = new Button("Delete");
		
		public DeleteCell() {
			delbutton.setOnAction((ActionEvent) -> {
				PropertyModel thisBp = DeleteCell.this.getTableView().getItems().get(DeleteCell.this.getIndex());
					PropertyBrowserView.this.getItems().remove(thisBp);
					PropertyBrowserView.contents.remove(thisBp);
				});
		}

		@Override
		protected void updateItem(Boolean t, boolean empty){
			super.updateItem(t, empty);
			if (!empty) {
				setGraphic(delbutton);
			}
			else {
				setGraphic(null);
			}
		}
	}
}
