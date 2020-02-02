package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import childViews.RequirementStatusView;
import containerViews.RequirementTab;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import utils.Memory;
import views.AbstractStatusView;

public class RequirementModel extends AbstractSpecificationModel{
	
	
	public SimpleObjectProperty<OntologyWordModel> absLevel;
	protected SimpleObjectProperty<AbstractStatusModel> statusModelProperty;
	protected SimpleObjectProperty<RequirementStatusView> statusView;
	public ObservableList<GenericBoilerPlate> prefixProperties, suffixProperties;
	public SimpleObjectProperty<GenericBoilerPlate> mainBodyProperty;
	protected ObservableList<RequirementModel> refines, isRefinedFrom;
	protected ObservableList<RequirementModel> concretizes, isConcretizedFrom;
	
	public RequirementModel(String id, OntologyWordModel category) {
		
		this.artifact_id = new SimpleStringProperty(id);
		this.category = new SimpleObjectProperty<>(category);
		
	}

	public RequirementModel(String id, OntologyWordModel category, OntologyWordModel absLevel,
			AbstractStatusModel abstractStatusModel, ObservableList<RequirementModel> refines,
			ObservableList<RequirementModel> isRefinedFrom, ObservableList<RequirementModel> concretizes,
			ObservableList<RequirementModel> isConcretizedFrom) {
		this(id, category);
		this.statusModelProperty = new SimpleObjectProperty<>(abstractStatusModel);
		this.statusView = new SimpleObjectProperty<>(new RequirementStatusView(abstractStatusModel));
		this.absLevel = new SimpleObjectProperty<>(absLevel);
		this.prefixProperties = FXCollections.observableArrayList();
		this.mainBodyProperty = new SimpleObjectProperty<>();
		this.suffixProperties = FXCollections.observableArrayList();
		this.displayForm = new SimpleStringProperty();
		this.refines = (refines != null) ? refines : FXCollections.observableArrayList();
		this.isRefinedFrom = (isRefinedFrom != null) ? isRefinedFrom : FXCollections.observableArrayList();
		this.concretizes = (concretizes != null) ? concretizes : FXCollections.observableArrayList();
		this.isConcretizedFrom = (isConcretizedFrom != null) ? isConcretizedFrom : FXCollections.observableArrayList();

		Button editButtonTemp = new Button("Edit");
		editButtonTemp.setOnAction((ActionEvent) -> RequirementTab.sendForEditing(this));
		editButton = new SimpleObjectProperty<>(editButtonTemp);
	}

	public RequirementModel(RequirementModel reqModel) {
		this(reqModel.getReqID(), reqModel.getCatID(), reqModel.getAbsLevel(), reqModel.getStatusModel(),
				FXCollections.observableArrayList(reqModel.getRefines()),
				FXCollections.observableArrayList(reqModel.getIsRefinedBy()),
				FXCollections.observableArrayList(reqModel.getConcretizes()),
				FXCollections.observableArrayList(reqModel.getIsConcretizedBy()));

		setPrefixProperties(reqModel.getPrefixProperties());
		this.mainBodyProperty = reqModel.getMainBodyProperty();
		setSuffixProperties(reqModel.getSuffixProperties());
	}

	/**
	 * Checks if the given string is contained in the requirement
	 * 
	 * @param query
	 * @return
	 */
	public boolean contains(String query) {

		if (!prefixProperties.isEmpty()) {
			for (GenericBoilerPlate prefixProperty : prefixProperties) {
				if (prefixProperty != null && prefixProperty.contains(query))
					return true;
			}
		}
		if (!suffixProperties.isEmpty()) {
			for (GenericBoilerPlate suffixProperty : suffixProperties) {
				if (suffixProperty != null && suffixProperty.contains(query))
					return true;
			}
		}
		if (mainBodyProperty.get() != null && mainBodyProperty.get().contains(query))
			return true;
		if (artifact_id.getValue().contains(query))
			return true;
		if (category.getValue().getFullName().contains(query))
			return true;
		if (absLevel.getValue().getFullName().contains(query))
			return true;
		if (statusModelProperty.get().currentStatus.get().contains(query))
			return true;
		return false;
	}

	public String generateReqID() {
		String absLevelName = absLevel.get().getShortName();
		String catName = category.get().getShortName();

		if (absLevelName != null && absLevelName.length() > 0 && catName != null && catName.length() > 0) {
			String baseReqID = (absLevel.get().getShortName() + "-" + catName).replaceAll("[a-z]*", "") + "-";
			// Keep only capital letters
			ArrayList<String> matches = new ArrayList<>();
			Memory.getRequirementsInMemory().forEach((RequirementModel reqMdl) -> {
				if (reqMdl.getReqID().startsWith(baseReqID) && !reqMdl.getReqID().equals(artifact_id.get()))
					matches.add(reqMdl.getReqID());
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

	/**
	 * Invokes validation of prefix, main, and suffix.
	 * @return
	 */
	public ArrayList<LogEntryModel> validate() {
		ArrayList<LogEntryModel> errors = new ArrayList<>();

		for (GenericBoilerPlate prefixProperty : prefixProperties) {
			if (prefixProperty != null)
				errors.addAll(prefixProperty.validate());
		}

		if (mainBodyProperty.get() != null)
			errors.addAll(mainBodyProperty.get().validate());
		
		for (GenericBoilerPlate suffixProperty : suffixProperties) {
			if (suffixProperty != null)
				errors.addAll(suffixProperty.validate());
		}
		return errors;
	}

	/* ------------------------------- */
	/* ---- Getter / Setter land ---- */
	/* ------------------------------ */

	public String getReqID() {
		return artifact_id.get();
	}

	public String getReqIDNoNS() {
		if (artifact_id.get().contains(":"))
			return artifact_id.get().split(":", 2)[1].replace(" ", "");
		return artifact_id.get();
	}

	public RequirementModel setReqID(String reqID) {
		this.artifact_id.set(reqID);
		return this;
	}

	public OntologyWordModel getCatID() {
		return category.get();
	}

	public RequirementModel setCatID(OntologyWordModel catID) {
		this.category.set(catID);
		return this;
	}

	public OntologyWordModel getAbsLevel() {
		return absLevel.get();
	}

	public RequirementModel setAbsLevel(OntologyWordModel absLevel) {
		this.absLevel.set(absLevel);
		return this;
	}

	public RequirementStatusView getPropertyStatusView() {
		return statusView.get();
	}

	public RequirementModel setStatusView(RequirementStatusView statusView) {
		this.statusView.set(statusView);
		return this;
	}

	public AbstractStatusModel getStatusModel() {
		return statusModelProperty.get();
	}

	public RequirementModel setStatusModel(RequirementStatusModel statusModel) {
		this.statusModelProperty.setValue(statusModel);
		return this;
	}

	public GenericBoilerPlate getMainBody() {
		return mainBodyProperty.get();
	}

	public RequirementModel setMainBody(GenericBoilerPlate mainBody) {
		if (mainBody == null)
			return this;
		mainBodyProperty.set(mainBody);
		updateDisplayForm();
		return this;
	}

	protected void updateDisplayForm() {
		String total = "";
		for (GenericBoilerPlate prefixProperty : prefixProperties) {
			if (prefixProperty != null)
				total += prefixProperty.toString();
		}

		if (mainBodyProperty.get() != null)
			total += mainBodyProperty.get().toString();

		for (GenericBoilerPlate suffixProperty : suffixProperties) {
			if (suffixProperty != null)
				total += suffixProperty.toString();
		}

		displayForm.set(total);
	}

	public RequirementModel setPrefix(GenericBoilerPlate prefix) {
		if (prefix == null)
			return this;
		prefixProperties.clear();
		prefixProperties.add(prefix);
		updateDisplayForm();
		return this;
	}

	public RequirementModel setSuffix(GenericBoilerPlate suffix) {
		if (suffix == null)
			return this;
		suffixProperties.clear();
		suffixProperties.add(suffix);
		updateDisplayForm();
		return this;
	}

	public String getDisplayForm() {
		return displayForm.get();
	}

	public void setDisplayForm(String displayForm) {
		this.displayForm.set(displayForm);
	}

	public Button getEditButton() {
		return editButton.get();
	}

	public void setEditButton(Button editButton) {
		this.editButton.set(editButton);
	}

	public SimpleObjectProperty<AbstractStatusModel> getStatusModelProperty() {
		return statusModelProperty;
	}

	public ObservableList<GenericBoilerPlate> getPrefixProperties() {
		return prefixProperties;
	}

	public SimpleObjectProperty<GenericBoilerPlate> getMainBodyProperty() {
		return mainBodyProperty;
	}

	public ObservableList<GenericBoilerPlate> getSuffixProperties() {
		return suffixProperties;
	}

	public ObservableList<RequirementModel> getRefines() {
		return refines;
	}

	public ObservableList<RequirementModel> getIsRefinedBy() {
		return isRefinedFrom;
	}

	public void setRefines(ObservableList<RequirementModel> refines) {
		this.refines = refines;
	}

	public void setIsRefinedFrom(ObservableList<RequirementModel> isRefinedFrom) {
		this.isRefinedFrom = isRefinedFrom;
	}

	public ObservableList<RequirementModel> getConcretizes() {
		return concretizes;
	}

	public void setConcretizes(ObservableList<RequirementModel> concretizes) {
		this.concretizes = concretizes;
	}

	public ObservableList<RequirementModel> getIsConcretizedBy() {
		return isConcretizedFrom;
	}

	public void setIsConcretizedBy(ObservableList<RequirementModel> isConcretizedFrom) {
		this.isConcretizedFrom = isConcretizedFrom;
	}

	public void setRefinesByIDs(ArrayList<String> refinesIDs, HashMap<String, RequirementModel> reqMap) {
		refinesIDs.forEach((id) -> {
			if (reqMap.containsKey(id)) {
				this.refines.add(reqMap.get(id));
			}
		});
	}

	public void setIsRefinedFromByIDs(ArrayList<String> isRefinedFrom, HashMap<String, RequirementModel> reqMap) {
		isRefinedFrom.forEach((id) -> {
			if (reqMap.containsKey(id)) {
				this.isRefinedFrom.add(reqMap.get(id));
			}
		});
	}

	public void setConcretizesByIDs(ArrayList<String> concretizesIDs, HashMap<String, RequirementModel> reqMap) {
		concretizesIDs.forEach((id) -> {
			if (reqMap.containsKey(id)) {
				this.concretizes.add(reqMap.get(id));
			}
		});
	}

	public void setIsConcretizedFromByIDs(ArrayList<String> isConcretizedFrom,
			HashMap<String, RequirementModel> reqMap) {
		isConcretizedFrom.forEach((id) -> {
			if (reqMap.containsKey(id)) {
				this.isConcretizedFrom.add(reqMap.get(id));
			}
		});
	}

	public Collection<AbstractBoilerPlate> saveToList(Collection<AbstractBoilerPlate> allBlps) {
		if (!prefixProperties.isEmpty()) {
			for (GenericBoilerPlate prefixProperty : prefixProperties) {
				if (prefixProperty != null)
					prefixProperty.saveToList(allBlps);
			}
		}
		if (mainBodyProperty.get() != null)
			mainBodyProperty.get().saveToList(allBlps);
		if (!suffixProperties.isEmpty()) {
			for (GenericBoilerPlate suffixProperty : suffixProperties) {
				if (suffixProperty != null)
					suffixProperty.saveToList(allBlps);
			}
		}
		return allBlps;
	}

	public void setPrefixProperties(ObservableList<GenericBoilerPlate> prefixProperties) {
		this.prefixProperties = prefixProperties;
	}

	public void setSuffixProperties(ObservableList<GenericBoilerPlate> suffixProperties) {
		this.suffixProperties = suffixProperties;
	}

	public ArrayList<String> getAbstract() {
		ArrayList<String> abstractObjects = new ArrayList<>();
		for (GenericBoilerPlate pref : prefixProperties)
			abstractObjects.addAll(pref.getAbstract());
		if (mainBodyProperty.get() != null)
			abstractObjects.addAll(mainBodyProperty.get().getAbstract());
		;
		for (GenericBoilerPlate suff : suffixProperties)
			abstractObjects.addAll(suff.getAbstract());
		return abstractObjects;
	}

}
