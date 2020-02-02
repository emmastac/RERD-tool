package viewWrappers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import popupWindowViews.ValidationPopup;
import utils.ExtStrings;
import containerViews.RequirementsBrowserView;

public class RequirementsBrowserPanelWrapper extends BorderPane{
	private TextField searchBar;
	private RequirementsBrowserView editingPanel;
	private HBox searchBarPanel;
	private static final String
	searchButtonText = ExtStrings.getString("searchButtonText"),
	searchButtonTooltip = ExtStrings.getString("searchButtonTooltip"),
	ontValButtonText = ExtStrings.getString("ontValButtonText"),
	ontValButtonTooltip = ExtStrings.getString("ontValButtonTooltip");

	public RequirementsBrowserPanelWrapper() {
		this.setTop( searchBarPanel = drawSearchBarPanel());
		this.setCenter(editingPanel = new RequirementsBrowserView() );
	}
	
	private HBox drawSearchBarPanel() {
		searchBar = new TextField();
		searchBar.setPromptText(searchButtonText);
		searchBar.setTooltip(new Tooltip(searchButtonTooltip));
		searchBar.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				editingPanel.refreshContents( searchBar.getText() );
			}
		});
		
//		Button addFiltersButton = new Button("Add Filters");
//		addFiltersButton.setOnAction((ActionEvent) -> {
//			// TODO
//			});
		
		Button validateButton = new Button(ontValButtonText);
		validateButton.setTooltip(new Tooltip(ontValButtonTooltip));
		validateButton.setOnAction((ActionEvent) -> {
				new ValidationPopup(this.getScene().getWindow());
			});
		
		HBox searchPanel = new HBox();
		searchPanel.setSpacing(10);
		searchPanel.setPadding(new Insets(10));
		searchPanel.getChildren().addAll(searchBar,validateButton);
//		searchPanel.getChildren().add(addFiltersButton);
//		searchPanel.getChildren().add(minButton);
		searchPanel.setBorder(Border.EMPTY);
		return searchPanel;
	}
	
	public void refreshEditingPanel(){
		editingPanel.refreshContents( searchBar.getText() );
	}

}
