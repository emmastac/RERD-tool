package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * This is a single log object, represented as a line in the Log console.
 * It is optionally connected to a ValidatableNode which can be highlighted 
 * This is useful when a user clicks a line and he is guided to the error
 * 
 * @author alexanpl
 *
 */
public class LogEntryModel {
	protected SimpleStringProperty logLevel;
	protected SimpleStringProperty logText;
	protected ValidatableNode focusPoint;
	private EventHandler<ActionEvent> onAction;
	private String item;
	public static final String UNKNOWN_OBJECT = "Object not detected in ontology : ";
	public static final String ID_CONFLICT = "Duplicate requirement ID found";

	public LogEntryModel() {
		this.logLevel = new SimpleStringProperty("");
		this.logText = new SimpleStringProperty("");
		this.focusPoint = null;
	}

	public LogEntryModel(String logLevel, String errorText) {
		this.logLevel = new SimpleStringProperty(logLevel);
		this.logText = new SimpleStringProperty(errorText);
		this.focusPoint = null;
	}

	public LogEntryModel(String logLevel, String errorText, ValidatableNode focusPoint) {
		this(logLevel, errorText);
		this.focusPoint = focusPoint;
	}
	
	public LogEntryModel setAsUnknownObject(String unknownObjectName, ValidatableNode focusPoint){
		this.logLevel = new SimpleStringProperty(LogLevel.WARNING);
		this.logText = new SimpleStringProperty(UNKNOWN_OBJECT+unknownObjectName);
		this.item = unknownObjectName;
		this.focusPoint = focusPoint;
		return this;
	}

	public void focusOnItem() {
		if (focusPoint != null) {
			focusPoint.select();
		}
	}

	public String getLogText() {
		return logText.get();
	}

	public void setLogText(String logText) {
		this.logText.setValue(logText);
	}

	public String getLogLevel() {
		return logLevel.get();
	}

	public void setLogLevel(String logLevel) {
		this.logLevel.setValue(logLevel);
	}

	public EventHandler<ActionEvent> getOnAction() {
		return onAction;
	}

	public String getItem() {
		return item;
	}

}
