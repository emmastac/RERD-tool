package viewWrappers;

import models.GenericBoilerPlate;
import models.RequirementModel;
import containerViews.EditViewWrapper;
import containerViews.RequirementTab;
import containerViews.RequirementView;

public class RequirementEditViewWrapper extends EditViewWrapper {
	RequirementView centerPane;
	
	public RequirementEditViewWrapper() {

		super();
	}

	protected void init() {

		super.init();
		this.setCenter(centerPane = new RequirementView());
		createButtonListeners();
	}

	protected void createButtonListeners() {
		
		saveButton.setOnAction(ActionEvent -> {
			console.getItems().clear();
			if (centerPane.getReqModel() != null)
				console.getItems().addAll(centerPane.save());
			RequirementTab.reloadPanel();
		});

		checkButton.setOnAction(ActionEvent -> {
			console.getItems().clear();
			if (centerPane.getReqModel() != null)
				console.getItems().addAll(centerPane.validateSelf());
		});

		clearButton.setOnAction(ActionEvent -> {
			centerPane.clear();
			console.getItems().clear();
		});

		newButton.setOnAction(ActionEvent -> {
//			if (centerPane.getReqModel() != null)
//				RequirementTab.switchToBLPNavigator(centerPane.getReqModel().getCatID());
			console.getItems().clear();
			centerPane.clear();
		});
	}

	public void sendMainForEditing(GenericBoilerPlate genBP) {

		centerPane.sendMainForEditing(genBP);
	}

	public void sendPrefixForEditing(GenericBoilerPlate prefix) {

		centerPane.sendPrefixForEditing(prefix);
	}

	public void sendSuffixForEditing(GenericBoilerPlate suffix) {

		centerPane.sendSuffixForEditing(suffix);
	}

	public void sendForEditing(RequirementModel reqModel) {

		console.getItems().clear();
		centerPane.setReqModel(reqModel);
	}
}
