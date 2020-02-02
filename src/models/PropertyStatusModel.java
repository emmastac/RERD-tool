package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PropertyStatusModel extends AbstractStatusModel {

	public static String MISSING = "Missing Values";
	public static String INVALID = "Invalid";
	public static String NOTYETDISCHARGED = "Not Yet Discharged";
	public static String VERIFIED = "Verified";
	
	public PropertyStatusModel() {
		USER_DEFINED_STATES = FXCollections.observableArrayList(
				NOTYETDISCHARGED, VERIFIED);
		
		DEFAULTSTATUS = new SimpleStringProperty(NOTYETDISCHARGED);
		currentStatus = new SimpleStringProperty(DEFAULTSTATUS.get());
	}
}
