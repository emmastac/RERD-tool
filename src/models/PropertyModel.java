package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import utils.ExtStrings;
import utils.Memory;
import childViews.PropertyStatusView;
import containerViews.PropertyTab;

public class PropertyModel extends AbstractSpecificationModel{

	private static HashMap<String, Integer> propModelSerialStructure = new HashMap<String, Integer>();

//	protected SimpleStringProperty artifact_id;
//	protected SimpleObjectProperty<OntologyWordModel> category;
//	protected SimpleObjectProperty<Button> editButton;
//	private SimpleStringProperty displayForm;
	
	private SimpleObjectProperty<PropertyStatusView> statusView;
	protected SimpleObjectProperty<PropertyStatusModel> statusModelProperty;
	protected SimpleObjectProperty<PropertyPattern> patternProperty;
	protected ObservableList<RequirementModel> derivedBy;
	public SimpleObjectProperty<Architecture> architectureProperty;
	public static OntologyWordModel[] CATEGORIES = { new OntologyWordModel("CSSP:EnforceableProperty"),
			new OntologyWordModel("CSSP:VerifiableProperty") };
	public static final OntologyWordModel DEFAULT_CATEGORY = CATEGORIES[0];
	protected static final String MIDDLE_DELIM = ExtStrings.getString("SerializeBLPMiddleDelimiter");
	protected static final String BOTTOM_DELIM = ExtStrings.getString("SerializeBLPBottomDelimiter");

	static {

		propModelSerialStructure.put("propIDString", 0);
		propModelSerialStructure.put("categoryString", 1);
		propModelSerialStructure.put("statusModelProperty", 2);
		propModelSerialStructure.put("propertyPattern", 3);
		propModelSerialStructure.put("reqs", 4);
		propModelSerialStructure.put("architecture", 5);
	}

	public PropertyModel(String id, OntologyWordModel category, PropertyStatusModel statusModel,
			ObservableList<RequirementModel> derivedBy, Architecture architecture) {
		
		this.artifact_id = new SimpleStringProperty(id);
		this.category = new SimpleObjectProperty<>(category);

//		this.artifact_id = new SimpleStringProperty(id);
//		this.category = new SimpleObjectProperty<>(category);
		if (architecture!=null && !architecture.toString().isEmpty()) {
			architecture.addProperty(this);
			Memory.addArchitecture(architecture);
			this.architectureProperty = new SimpleObjectProperty<>(architecture);
		} else this.architectureProperty = new SimpleObjectProperty<>(new Architecture(""));
		this.statusModelProperty = new SimpleObjectProperty<>(
				statusModel != null ? statusModel : new PropertyStatusModel());
		this.statusView = new SimpleObjectProperty<>(new PropertyStatusView(statusModel));
		this.patternProperty = new SimpleObjectProperty<>();
		this.derivedBy = (derivedBy != null ? derivedBy : FXCollections.observableArrayList());
		this.displayForm = new SimpleStringProperty();

		Button editButtonTemp = new Button("Edit");
		editButtonTemp.setOnAction((ActionEvent) -> PropertyTab.sendForEditing(this));
		editButton = new SimpleObjectProperty<>(editButtonTemp);
	}

	public PropertyModel(String propID, OntologyWordModel category, PropertyStatusModel statusModel,
			ObservableList<RequirementModel> derivedBy, Architecture architecture, PropertyPattern propertyPattern) {

		this(propID, category, statusModel, derivedBy, architecture);
		if (propertyPattern != null) {
			setPattern(propertyPattern);
		}
	}

	public PropertyModel(PropertyModel propertyModel) {

		this(propertyModel.getPropID(), propertyModel.getCategory(), propertyModel.getStatusModel(),
				propertyModel.getDerivedBy(), propertyModel.getArchitecture(), propertyModel.getPattern());
	}

//	public PropertyModel(String serializedForm) {
//
//		this(deserialize(serializedForm));
//	}

	public String serialize() {

		String serializedString = "";
		serializedString += artifact_id.get() + BOTTOM_DELIM;
		serializedString += category.get() + BOTTOM_DELIM;
		serializedString += statusModelProperty.get().getCurrentStatus() + BOTTOM_DELIM;
		serializedString += (patternProperty.get() != null ? patternProperty.get().getID() : " ") + BOTTOM_DELIM; 
		// Even if there is no pattern, add the delim
		
		if (!derivedBy.isEmpty()) {
			for (RequirementModel reqMdl : derivedBy)
				serializedString += reqMdl.getReqID() + MIDDLE_DELIM;
		} else
			serializedString += " "; // So that split still returns 5 items
		serializedString += BOTTOM_DELIM;

		serializedString += architectureProperty.get().serialize() + BOTTOM_DELIM;
		return serializedString;
	}

	public static PropertyModel deserialize(String serializedString) {

		if (serializedString.isEmpty())
			return null;
		String[] tokens = serializedString.split(BOTTOM_DELIM);
		if (tokens.length != propModelSerialStructure.keySet().size()) {
			System.out.println("Badly serialized string");
			return null;
		} else {
			String propIDString = tokens[propModelSerialStructure.get("propIDString")];
			String categoryString = tokens[propModelSerialStructure.get("categoryString")];
			OntologyWordModel category = new OntologyWordModel(categoryString);
			PropertyStatusModel statusModelProperty = new PropertyStatusModel();
			if (!tokens[propModelSerialStructure.get("statusModelProperty")].isEmpty())
				statusModelProperty.setCurrentStatus(tokens[propModelSerialStructure.get("statusModelProperty")]);
			PropertyPattern propertyPattern = new PropertyPattern(
					Memory.getPatternsInMemory().get(tokens[propModelSerialStructure.get("propertyPattern")]));
			ObservableList<RequirementModel> derivedByList = FXCollections.observableArrayList();
			if (!tokens[propModelSerialStructure.get("reqs")].isEmpty()) {
				HashMap<String, RequirementModel> reqMap = new HashMap<>();
				for (RequirementModel reqMdl : Memory.getRequirementsInMemory()) {
					reqMap.put(reqMdl.getReqID(), reqMdl);
				}
				for (String reqID : tokens[propModelSerialStructure.get("reqs")].split(MIDDLE_DELIM)) {
					RequirementModel res = reqMap.get(reqID);
					if (res != null)
						derivedByList.add(res);
				}
			}
			String architectureString = tokens[propModelSerialStructure.get("architecture")];
			Architecture architectureObj = null;
			if (!architectureString.isEmpty()){
				architectureObj = Memory.addArchitecture(architectureString, null);
			}

			return new PropertyModel(propIDString, category, statusModelProperty, derivedByList, architectureObj,
					propertyPattern);
		}
	}

	public String generatePropID() {

		String catName = category.get().getShortName();

		if (catName != null && catName.length() > 0) {
			String baseReqID = catName.replaceAll("[a-z]*", "") + "-";
			// Keep only capital letters
			ArrayList<String> matches = new ArrayList<>();
			Memory.getPropertiesInMemory().forEach((PropertyModel propMdl) -> {
				if (propMdl.getPropID().startsWith(baseReqID) && !propMdl.getPropID().equals(artifact_id.get()))
					matches.add(propMdl.getPropID());
			});
			int counter = 1;
			if (matches.isEmpty())
				return baseReqID + counter;
			else {
				while (matches.contains(baseReqID + counter)) {
					counter += 1;
				}
				return baseReqID + counter;
			}
		}

		return "";
	}

	public boolean contains(String query) {
	
		if (patternProperty.get() != null && patternProperty.get().contains(query))
			return true;
		if (artifact_id.getValue().contains(query))
			return true;
		if (category.getValue().getFullName().contains(query))
			return true;
		if (statusModelProperty.get().currentStatus.get().contains(query))
			return true;
		return false;
	}

	public ArrayList<LogEntryModel> validate() {

		ArrayList<LogEntryModel> errors = new ArrayList<>();
		if (patternProperty.get() != null)
			errors.addAll(patternProperty.get().validate());
		return errors;
	}

	public String getPropID() {

		return artifact_id.get();
	}

	public String getPropIDNoNS() {

		if (artifact_id.get().contains(":"))
			return artifact_id.get().split(":", 2)[1].replace(" ", "");
		return artifact_id.get();
	}

	public void setPropID(String propID) {
		this.artifact_id.set(propID);
	}

	public OntologyWordModel getCategory() {

		return category.get();
	}
	
	public String getCatNoNS() {

		return category.get().getShortName();
	}

	public void setCategory(OntologyWordModel category) {

		this.category.set(category);
	}

	public PropertyStatusModel getStatusModel() {

		return statusModelProperty.get();
	}

	public ObservableList<RequirementModel> getDerivedBy() {

		return derivedBy;
	}

	public PropertyPattern getPattern() {

		return this.patternProperty.get();
	}

	public PropertyModel setPattern(PropertyPattern patternProperty) {

		this.patternProperty.set(patternProperty);
		this.displayForm.set(patternProperty.toString());
		return this;
	}

	public Button getEditButton() {

		return editButton.get();
	}

	public PropertyStatusView getPropertyStatusView() {

		return statusView.get();
	}

	public String getDisplayForm() {

		return displayForm.get();
	}

	public SimpleStringProperty getPropIDProperty() {

		return artifact_id;
	}

	public Collection<AbstractBoilerPlate> saveToList(Collection<AbstractBoilerPlate> allBlps) {
		if (patternProperty.get() != null){
			allBlps.add(patternProperty.get());
			patternProperty.get().saveToList(allBlps);
		}
		return allBlps;
	}

	public Architecture getArchitecture() {
		
		return this.architectureProperty.get();
	}
	
	public void setArchitecture(Architecture architecture) {

		this.architectureProperty.set(architecture);
	}

}
