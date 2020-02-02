package viewWrappers;

import models.PropertyModel;
import models.PropertyPattern;
import popupWindowViews.ValidationPopup;
import containerViews.EditViewWrapper;
import containerViews.PropertyTab;
import containerViews.PropertyView;

public class PropertyEditViewWrapper extends EditViewWrapper {
	PropertyView centerPane;
	
	public PropertyEditViewWrapper() {

		super();
	}

	protected void init() {
		
		super.init();
		this.setCenter(centerPane = new PropertyView());
		createButtonListeners();
	}

	protected void createButtonListeners() {
		
		saveButton.setOnAction((ActionEvent) -> {
			console.getItems().clear();
			if (centerPane.getPropModel() != null)
				console.getItems().addAll(centerPane.save());
			PropertyTab.reloadPanel();
		});
		
		checkButton.setOnAction((ActionEvent) -> {
			console.getItems().clear();
			if (centerPane.getPropModel() != null)
				console.getItems().addAll(centerPane.validateSelf());
			new ValidationPopup(this.getScene().getWindow());
		});
		
		clearButton.setOnAction((ActionEvent) -> {
			centerPane.clear();
			console.getItems().clear();
		});
		
		newButton.setOnAction((ActionEvent) -> {
			centerPane.clear();
			console.getItems().clear();
		});
		
	}

	public void sendForEditing(PropertyModel propertyModel) {
		console.getItems().clear();
		centerPane.setPropModel(propertyModel);
	}

	public void sendPatternForEditing(PropertyPattern propertyPattern) {

		centerPane.setPattern(propertyPattern);
	}
}
