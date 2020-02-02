package containerViews;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.OntologyWordModel;
import models.ValidationResultModel;

public class ValidationResultView extends HBox {
	protected final int spacing = 50;
	
	public ValidationResultView(){
		super();
		this.getChildren().clear();
		this.setSpacing(spacing);
	}
	
	public ValidationResultView(ValidationResultModel valResults) {
		this();
		for (ArrayList<OntologyWordModel> resultList : valResults.getAllResults()){
			TableView<OntologyWordModel> tableList = new TableView<>();
			
			TableColumn<OntologyWordModel, String> col = new TableColumn<>();
			col.setCellValueFactory(new PropertyValueFactory<>("shortName"));
			col.prefWidthProperty().bind(tableList.widthProperty().multiply(0.99));
			
			tableList.getColumns().add(col);
			
			ObservableList<OntologyWordModel> castList = FXCollections.observableArrayList();
			castList.addAll(resultList);
			tableList.setItems(castList);
			this.getChildren().add(tableList);
		}
	}
}
