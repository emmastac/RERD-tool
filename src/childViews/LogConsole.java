package childViews;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.LogEntryModel;
import popupWindowViews.OntologyAdderPopup;

public class LogConsole extends TableView<LogEntryModel> {

	@SuppressWarnings("unchecked")
	public LogConsole() {
		super();

		TableColumn<LogEntryModel, String> levelColumn = new TableColumn<>("Log Level");
		levelColumn.setCellValueFactory(new PropertyValueFactory<LogEntryModel, String>("logLevel"));
		levelColumn.prefWidthProperty().bind(widthProperty().multiply(0.39));
		
		TableColumn<LogEntryModel, String> messageColumn = new TableColumn<>("Log Message");
		messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntryModel, String>("logText"));
		messageColumn.prefWidthProperty().bind(widthProperty().multiply(0.6));

		getColumns().addAll(levelColumn, messageColumn);

		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LogEntryModel>() {

			@Override
			public void changed(ObservableValue<? extends LogEntryModel> arg0, LogEntryModel arg1,
					LogEntryModel arg2) {
				if (arg2!=null) {
					if (arg2.getLogText().startsWith(LogEntryModel.UNKNOWN_OBJECT)){
						arg2.focusOnItem();
						new OntologyAdderPopup(getScene().getWindow(), arg2.getItem());
					}
				}
			}
		});
	}
}
