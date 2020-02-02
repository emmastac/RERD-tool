package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RequirementStatusModel extends AbstractStatusModel{

	public static String MISSING = "Missing Values";
	public static String INVALID = "Invalid";
	public static String NOTVERIFIABLE = "Not Verifiable";
	public static String NOTYETCOVERED = "Not Yet Covered";
	public static String COVERED = "Covered";
	public static String DISCHARGED = "Discharged";
	
	public RequirementStatusModel() {
		USER_DEFINED_STATES = FXCollections.observableArrayList(
				NOTVERIFIABLE, NOTYETCOVERED, COVERED, DISCHARGED);

		DEFAULTSTATUS = new SimpleStringProperty(MISSING);
		currentStatus = new SimpleStringProperty(DEFAULTSTATUS.get());
	}
}
