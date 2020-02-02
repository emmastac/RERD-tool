package containerViews;

import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import models.PropertyPattern;
import utils.Memory;

public class PropertyPatternBrowser extends BorderPane {

	TableView<PropertyPattern> behaviourTable, archTable;

	public PropertyPatternBrowser() {

		behaviourTable = new TableView<>();
		archTable = new TableView<>();

		TableColumn<PropertyPattern, String> behIDColumn = new TableColumn<>("ID");
		behIDColumn.setCellValueFactory(new PropertyValueFactory<PropertyPattern, String>("ID"));
		TableColumn<PropertyPattern, String> ptrnContentColumn = new TableColumn<>("Pattern");
		ptrnContentColumn.setCellValueFactory(new PropertyValueFactory<PropertyPattern, String>("templateForm"));

		TableColumn<PropertyPattern, String> archIDColumn = new TableColumn<>("ID");
		archIDColumn.setCellValueFactory(new PropertyValueFactory<PropertyPattern, String>("ID"));
		TableColumn<PropertyPattern, String> archContentColumn = new TableColumn<>("ArchPattern");
		archContentColumn.setCellValueFactory(new PropertyValueFactory<PropertyPattern, String>("templateForm"));

		behaviourTable.getColumns().addAll(behIDColumn, ptrnContentColumn);
		archTable.getColumns().addAll(archIDColumn, archContentColumn);

		archIDColumn.prefWidthProperty().bind(archTable.widthProperty().multiply(0.35));
		archContentColumn.prefWidthProperty().bind(archTable.widthProperty().multiply(0.64));
		behIDColumn.prefWidthProperty().bind(behaviourTable.widthProperty().multiply(0.25));
		ptrnContentColumn.prefWidthProperty().bind(behaviourTable.widthProperty().multiply(0.74));

		behaviourTable.setOnMouseClicked((MouseEvent) -> {
			PropertyTab.sendPatternForEditing(
					new PropertyPattern(behaviourTable.getSelectionModel().getSelectedItem().serialize()));
		});

		archTable.setOnMouseClicked((MouseEvent) -> {
			PropertyTab.sendPatternForEditing(
					new PropertyPattern(archTable.getSelectionModel().getSelectedItem().serialize()));
		});

		this.setPadding(new Insets(10));

//		setSpacing(10);
//		getChildren().addAll(behaviourTable, archTable);
		
		setLeft(behaviourTable);
		setCenter(archTable);

		displayAllPatterns();
	}

	private void displayAllPatterns() {
		behaviourTable.getItems().clear();
		archTable.getItems().clear();

		behaviourTable.getItems().addAll(Memory.getPatternTemplates().values());
		archTable.getItems().addAll(Memory.getArchPatternTemplates().values());
		// try{
		// Memory.getPatterns().values().forEach((pattern) -> {
		// try{
		// behaviourTable.getItems().add(pattern);
		// } catch (NullPointerException e){ System.out.println("Failed to
		// template parse pattern "+pattern.getID()); }
		// });
		// } catch (NullPointerException e){ System.out.println("Empty
		// patterns"); }

		// try{
		// Memory.getArchPatterns().entrySet().forEach((pattern) -> {
		// try {
		// archTable.getItems().add(new PropertyPattern(pattern.getKey(),
		// pattern.getValue()));
		// } catch (NullPointerException e){ System.out.println("Failed to
		// template parse archPattern "+pattern.getKey()); }
		// });
		// } catch (NullPointerException e){ System.out.println("Empty Arch
		// patterns"); }
	}
}
