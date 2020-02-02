package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class AbstractStatusModel {
	
	public SimpleStringProperty currentStatus;
	public static SimpleStringProperty DEFAULTSTATUS;
	public static ObservableList<String> USER_DEFINED_STATES;
	
	public String getCurrentStatus() {
		return currentStatus.get();
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus.set(currentStatus);
	}
}
