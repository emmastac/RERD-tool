package models;

import java.util.HashSet;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import utils.ExtStrings;

public class Architecture {
	

	private static final Logger logger = Logger.getLogger(Architecture.class.getName());
	public SimpleStringProperty userSpecified;
	public SimpleStringProperty archID;
	public SimpleStringProperty outputCodePath;
	public SimpleStringProperty outputConfPath;
	static final String ARCH_DELIMITER = ExtStrings.getString("ArchDelimiter");
	public HashSet<PropertyModel> relatedProperties;

	public Architecture(String name) {

		this.archID = new SimpleStringProperty("");
		this.userSpecified = new SimpleStringProperty("");
		this.outputConfPath = new SimpleStringProperty("");
		this.outputCodePath = new SimpleStringProperty("");
		relatedProperties = new HashSet<>();

		String[] tokens = null;
		if (name.contains(ARCH_DELIMITER)){
			tokens = name.split(ARCH_DELIMITER);
		}
		else if (name.contains("-")){
			tokens = name.split("-");
		}
		
		if (tokens!=null && tokens.length == 2) {
			this.userSpecified.set(tokens[0]);
			this.archID.set(tokens[1]);
		}

	}
	

	public String getArchID() {

		return archID.get();
	}
	
	public String getUserSpecified() {

		return userSpecified.get();
	}

	public void updateStatus(PropertyStatusModel newStatus) {
		logger.info("Update status called");
		if (!relatedProperties.isEmpty() && newStatus != null) {
			for (PropertyModel propModel : relatedProperties) {
				logger.info("Updating status on "+propModel.getPropID());
				propModel.getStatusModel().setCurrentStatus(newStatus.getCurrentStatus());
			}
		}
	}

	public void addProperty(PropertyModel propModel) {
		if (propModel != null){
			logger.info("Adding propModel "+propModel.getPropID()+" to update list");
			relatedProperties.add(propModel);
		}
	}

	public String serialize() {
		return userSpecified.get() + ARCH_DELIMITER + archID.get() + ARCH_DELIMITER ;
	}

	@Override
	public String toString() {
		if (userSpecified.get().isEmpty() && archID.get().isEmpty()) return "";
		return 	userSpecified.get() + "-" +
				archID.get() ;
	}

}
