package containerViews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import models.GenericBoilerPlate;
import models.LogEntryModel;
import models.OntologyWordModel;
import models.RequirementModel;
import models.RequirementStatusModel;
import popupWindowViews.DoubleListPopup;
import utils.ExtStrings;
import utils.Memory;

public class RequirementView extends BorderPane {

	protected static final String REFINES_BUTTON_TEXT = ExtStrings.getString("RefinesButton"),
			REFINED_BY_BUTTON_TEXT = ExtStrings.getString("RefinedByButton"),
			CONCRETIZES_BUTTON_TEXT = ExtStrings.getString("ConcretizesButton"),
			CONCRETIZED_BY_BUTTON_TEXT = ExtStrings.getString("ConcretizedByButton");

	protected ComboBox<OntologyWordModel> absLevelsBox;
	protected ComboBox<OntologyWordModel> categoryBox;
	protected ComboBox<String> statusModelBox;
	protected TextField reqIDField;
	protected RequirementModel reqModel;
	protected RequirementModel reqModelOriginal;
	protected VBox childrenBox;
	protected VBox prefixesPanel, mainPanel, suffixesPanel;
	// protected GenericBoilerPlate prefix, main, suffix;
	protected DoubleListPopup<RequirementModel> refinesPopup, refinedByPopup;
	protected DoubleListPopup<RequirementModel> concretizesPopup, concretizedFromPopup;

	public RequirementView() {

		init();
	};

	protected void init() {
		setReqModel(new RequirementModel("", new OntologyWordModel(""), new OntologyWordModel(""),
				new RequirementStatusModel(), null, null, null, null));
	}

	public RequirementView(RequirementModel reqModel) {

		setReqModel(reqModel);
	}

	/**
	 * Fills the Requirement's Editor with the dialogues to fill in for a selected requirement. 
	 * @param reqModel
	 */
	public void setReqModel(RequirementModel reqModel) {

		this.getChildren().clear();
		this.setPadding(new Insets(10));
		
		// Save the reqModel as a reqModelOriginal
		this.reqModelOriginal = reqModel;

		// This is a copy of the requirement which will be saved into the original, when save is clicked.
		this.reqModel = new RequirementModel(reqModel);

		childrenBox = new VBox(10);
		prefixesPanel = new VBox(5);
		prefixesPanel.setBorder(new Border(
				new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, new CornerRadii(10.0), new BorderWidths(1))));
		mainPanel = new VBox(5);
		suffixesPanel = new VBox(5);
		suffixesPanel.setBorder(new Border(
				new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, new CornerRadii(10.0), new BorderWidths(1))));

		// Show dialogues for prefix, main and suffix
		if (!reqModel.getPrefixProperties().isEmpty()) {

			for (GenericBoilerPlate genBlp : reqModel.getPrefixProperties()) {
				sendPrefixForEditing(genBlp);
			}
		}

		if (reqModel.getMainBody() != null)
			mainPanel.getChildren().add(reqModel.getMainBody());

		if (!reqModel.getSuffixProperties().isEmpty()) {

			for (GenericBoilerPlate genBlp : reqModel.getSuffixProperties()) {
				sendSuffixForEditing(genBlp);
			}
		}

		childrenBox.getChildren().addAll(prefixesPanel, mainPanel, suffixesPanel);

		this.setCenter(childrenBox);

		this.setRight(getOtherElements());
	}

	/**
	 * This function creates a VBox with the additional requirement data.
	 * @return
	 */
	public VBox getOtherElements() {

		VBox elementBox = new VBox();
		elementBox.setSpacing(10);
		elementBox.setPadding(new Insets(10));
		elementBox.setMaxWidth(250);
		Set<OntologyWordModel> absLevels = Memory.getAbstractionLevelsAndCategoriesTopLevel().keySet();
		HashMap<OntologyWordModel, ArrayList<OntologyWordModel>> absToAllCats = Memory
				.getAbstractionLevelsAndCategoriesFlat();

		// show requirement id text input field
		reqIDField = new TextField(reqModel.getReqID());
		Tooltip hoverText = new Tooltip("Requirement ID");
		Tooltip.install(reqIDField, hoverText);

		// show 'generate req id' button
		Button autoGenReqID = new Button("Generate Req ID");
		autoGenReqID.setOnAction((ActionEvent) -> reqModel.setReqID(reqModel.generateReqID()));

		// show abstraction level and category lists
		absLevelsBox = new ComboBox<>();
		categoryBox = new ComboBox<>();
		
		// When absLevel selection changes, update category box
		absLevelsBox.selectionModelProperty().get().selectedItemProperty()
				.addListener(new ChangeListener<OntologyWordModel>() {
					@Override
					public void changed(ObservableValue<? extends OntologyWordModel> observable,
							OntologyWordModel oldValue, OntologyWordModel newValue) {
						categoryBox.getItems().clear();
						if (newValue == null)
							return;
						// Load the flat one here because it has all categories
						// for an absLevel

						if (absToAllCats.containsKey(newValue)) {
							categoryBox.getItems().addAll(absToAllCats.get(newValue));
							selectCat();
						}
					}
				});

		// Load this one because it's smaller and for absLevels it's the same
		absLevelsBox.getItems().addAll(absLevels);
		if (!absLevels.isEmpty()) {
			if (reqModel.getAbsLevel() != null && !reqModel.getAbsLevel().getShortName().isEmpty()) {
				absLevelsBox.getSelectionModel().select(reqModel.getAbsLevel());
			} else {
				absLevelsBox.getSelectionModel().select(0);
			}
		}

//		if (absLevelsBox.getSelectionModel().getSelectedItem() != null) {
			
//			categoryBox.getItems().clear();
//			categoryBox.selectionModelProperty().get().clearSelection();
		
			OntologyWordModel selectedAbsLevel = absLevelsBox.getSelectionModel().getSelectedItem();
			if (absToAllCats.containsKey(selectedAbsLevel)) {
				categoryBox.getItems().addAll(absToAllCats.get(selectedAbsLevel));
			}

			selectCat();
//		}

		statusModelBox = new ComboBox<>(RequirementStatusModel.USER_DEFINED_STATES);
		statusModelBox.getSelectionModel().select(reqModel.getStatusModel().getCurrentStatus());

		reqModel.artifact_id.addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				reqIDField.setText(arg2);
			}
		});
		reqModel.absLevel.bind(absLevelsBox.selectionModelProperty().get().selectedItemProperty());
		reqModel.category.bind(categoryBox.selectionModelProperty().get().selectedItemProperty());

		elementBox.getChildren().addAll(reqIDField, autoGenReqID, absLevelsBox, categoryBox, statusModelBox,
				createRefinesAndConcretizesPanel());

		return elementBox;
	}

	/**
	 * Selects the requirements' category if it exists, or a default item
	 * otherwise
	 */
	protected void selectCat() {
		OntologyWordModel catId = reqModel.getCatID();
		// if requirement has a category and the category box is not empty
		if (catId != null && !categoryBox.getItems().isEmpty()) {

			// If category's short name is not empty and is in the set, select it
			if (!catId.getShortName().isEmpty() && categoryBox.getItems().contains(catId)) {
				categoryBox.getSelectionModel().select(catId);
			} else { // Select a default
				categoryBox.getSelectionModel().select(0);
			}

		}
	}

	private VBox createRefinesAndConcretizesPanel() {

		VBox outerWrapper = new VBox(5);

		HBox refinesPanel = new HBox(10);
		Button refinesButton = new Button(REFINES_BUTTON_TEXT);
		Button refinedFromButton = new Button(REFINED_BY_BUTTON_TEXT);
		
		RequirementModel[] excludedArray = new RequirementModel[] {this.reqModel};
		
		refinesButton.setOnAction(ActionEvent -> {
			refinesPopup = new DoubleListPopup<RequirementModel>(getScene().getWindow(), refinesButton.getText(),
					Memory.getRequirementsInMemory(), reqModel.getRefines(), excludedArray);
		});
		refinedFromButton.setOnAction(ActionEvent -> {
			refinedByPopup = new DoubleListPopup<RequirementModel>(getScene().getWindow(), refinedFromButton.getText(),
					Memory.getRequirementsInMemory(), reqModel.getIsRefinedBy(), excludedArray);
		});
		refinesPanel.getChildren().addAll(refinesButton, refinedFromButton);

		HBox concretizesPanel = new HBox(10);
		Button concretizesButton = new Button(CONCRETIZES_BUTTON_TEXT);
		Button concretizedFromButton = new Button(CONCRETIZED_BY_BUTTON_TEXT);
		concretizesButton.setOnAction(ActionEvent -> {
			concretizesPopup = new DoubleListPopup<RequirementModel>(getScene().getWindow(), concretizesButton.getText(),
					Memory.getRequirementsInMemory(), reqModel.getConcretizes(), excludedArray);
		});
		concretizedFromButton.setOnAction(ActionEvent -> {
			concretizedFromPopup = new DoubleListPopup<RequirementModel>(getScene().getWindow(), concretizesButton.getText(),
					Memory.getRequirementsInMemory(), reqModel.getIsConcretizedBy(), excludedArray);
		});
		concretizesPanel.getChildren().addAll(concretizesButton, concretizedFromButton);

		outerWrapper.getChildren().addAll(refinesPanel, concretizesPanel);

		return outerWrapper;
	}

	/**
	 * Validates and saves this requirement. The saving will happen even if
	 * validation fails
	 * 
	 * @return ArrayList<LogEntry> All the output of the saving and validation,
	 *         to be put in the req console
	 */
	public ArrayList<LogEntryModel> save() {

		if (reqModel == null)
			return new ArrayList<>();
		
		// Check for ReqID conflicts
		reqModel.setReqID(reqIDField.getText());
		for (RequirementModel reqMdl : Memory.getRequirementsInMemory()) {
			if (reqMdl != reqModelOriginal && reqMdl.getReqID().equals(reqModel.getReqID())) {
//				valResults.add(new LogEntryModel(LogLevel.ERROR, LogEntryModel.ID_CONFLICT));
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle(LogEntryModel.ID_CONFLICT);
				alert.setHeaderText(null);
				alert.setContentText("Please select another ID.");

				alert.showAndWait();
				return new ArrayList<>();
			}
		}
		
		// Copy data of reqModel to reqModelOriginal
		reqModelOriginal.setAbsLevel(reqModel.getAbsLevel());
		reqModelOriginal.setCatID(reqModel.getCatID());
		reqModelOriginal.setReqID(reqIDField.getText());

		if (!reqModel.getPrefixProperties().isEmpty())
			reqModelOriginal.setPrefixProperties(reqModel.getPrefixProperties());
		if (reqModel.getMainBody() != null)
			reqModelOriginal.setMainBody(reqModel.getMainBody());
		if (!reqModel.getSuffixProperties().isEmpty())
			reqModelOriginal.setSuffixProperties(reqModel.getSuffixProperties());
		
		// If our requirement is not in memory, add it
		Memory.addRequirement(reqModelOriginal);


		ArrayList<LogEntryModel> errors = new ArrayList<>();
		errors.addAll(validateSelf());

		reqModelOriginal.getStatusModel().setCurrentStatus((statusModelBox.getSelectionModel().getSelectedItem()));

		updateRefines();
		reqModelOriginal.setRefines(reqModel.getRefines());
		updateIsRefinedBy();
		reqModelOriginal.setIsRefinedFrom(reqModel.getIsRefinedBy());

		updateConcretizes();
		reqModelOriginal.setConcretizes(reqModel.getConcretizes());
		updateIsConcretizedBy();
		reqModelOriginal.setIsConcretizedBy(reqModel.getIsConcretizedBy());

		return errors;
	}

	protected void updateRefines() {
		ObservableList<RequirementModel> removedRefines = FXCollections.observableArrayList();
		removedRefines.addAll(reqModelOriginal.getRefines());
		removedRefines.removeAll(reqModel.getRefines());
		ObservableList<RequirementModel> addedRefines = FXCollections.observableArrayList();
		addedRefines.addAll(reqModel.getRefines());
		addedRefines.removeAll(reqModelOriginal.getRefines());
		addedRefines.forEach(reqMdl -> {
			if (!reqMdl.getIsRefinedBy().contains(reqModelOriginal)) {
				reqMdl.getIsRefinedBy().add(reqModelOriginal);
			}
		});
		removedRefines.forEach(reqMdl -> {
			if (reqMdl.getIsRefinedBy().contains(reqModelOriginal)) {
				reqMdl.getIsRefinedBy().remove(reqModelOriginal);
			}
		});
	}

	protected void updateConcretizes() {
		ObservableList<RequirementModel> removedConcretizes = FXCollections.observableArrayList();
		removedConcretizes.addAll(reqModelOriginal.getConcretizes());
		removedConcretizes.removeAll(reqModel.getConcretizes());
		ObservableList<RequirementModel> addedConcretizes = FXCollections.observableArrayList();
		addedConcretizes.addAll(reqModel.getConcretizes());
		addedConcretizes.removeAll(reqModelOriginal.getConcretizes());
		addedConcretizes.forEach(reqMdl -> {
			if (!reqMdl.getIsConcretizedBy().contains(reqModelOriginal)) {
				reqMdl.getIsConcretizedBy().add(reqModelOriginal);
			}
		});
		removedConcretizes.forEach(reqMdl -> {
			if (reqMdl.getIsConcretizedBy().contains(reqModelOriginal)) {
				reqMdl.getIsConcretizedBy().remove(reqModelOriginal);
			}
		});
	}

	protected void updateIsRefinedBy() {
		ObservableList<RequirementModel> removedRefines = FXCollections.observableArrayList();
		removedRefines.addAll(reqModelOriginal.getIsRefinedBy());
		removedRefines.removeAll(reqModel.getIsRefinedBy());
		ObservableList<RequirementModel> addedRefines = FXCollections.observableArrayList();
		addedRefines.addAll(reqModel.getIsRefinedBy());
		addedRefines.removeAll(reqModelOriginal.getIsRefinedBy());
		addedRefines.forEach(reqMdl -> {
			if (!reqMdl.getRefines().contains(reqModelOriginal)) {
				reqMdl.getRefines().add(reqModelOriginal);
			}
		});
		removedRefines.forEach(reqMdl -> {
			if (reqMdl.getRefines().contains(reqModelOriginal)) {
				reqMdl.getRefines().remove(reqModelOriginal);
			}
		});
	}

	protected void updateIsConcretizedBy() {
		ObservableList<RequirementModel> removedConcretizes = FXCollections.observableArrayList();
		removedConcretizes.addAll(reqModelOriginal.getIsConcretizedBy());
		removedConcretizes.removeAll(reqModel.getIsConcretizedBy());
		ObservableList<RequirementModel> addedConcretizes = FXCollections.observableArrayList();
		addedConcretizes.addAll(reqModel.getIsConcretizedBy());
		addedConcretizes.removeAll(reqModelOriginal.getIsConcretizedBy());
		addedConcretizes.forEach(reqMdl -> {
			if (!reqMdl.getConcretizes().contains(reqModelOriginal)) {
				reqMdl.getConcretizes().add(reqModelOriginal);
			}
		});
		removedConcretizes.forEach(reqMdl -> {
			if (reqMdl.getConcretizes().contains(reqModelOriginal)) {
				reqMdl.getConcretizes().remove(reqModelOriginal);
			}
		});
	}

	public ArrayList<LogEntryModel> validateSelf() {

		ArrayList<LogEntryModel> errors = reqModelOriginal.validate();

		// If validation returned with errors, set status model to Invalid, 
		// else if requirement had default status, set it to not yet covered.
		if (!errors.isEmpty()) {
			statusModelBox.getSelectionModel().select(RequirementStatusModel.INVALID);
		}
		else if (reqModel.getStatusModel().getCurrentStatus().equals(RequirementStatusModel.DEFAULTSTATUS.get())) {
			statusModelBox.getSelectionModel().select(RequirementStatusModel.NOTYETCOVERED);
		}

		return errors;
	}

	public void clear() {

		this.getChildren().clear();
		init();
	}

	public RequirementModel getReqModel() {
		return reqModel;
	}

	public void sendMainForEditing(GenericBoilerPlate genBP) {

		reqModel.setMainBody(genBP);
		mainPanel.getChildren().clear();
		mainPanel.getChildren().add(genBP);
	}

	public void sendPrefixForEditing(GenericBoilerPlate prefix) {

		Button removalButton = new Button("-");
		RemoveBLPWrapper removeWrapper = new RemoveBLPWrapper(removalButton, prefix);
		
		removalButton.setOnAction(ae -> {
			reqModel.getPrefixProperties().remove(prefix);
			prefixesPanel.getChildren().remove(removeWrapper);
		});
		
		reqModel.setPrefix(prefix);
		// remove previous prefix (only one is allowed)
		prefixesPanel.getChildren().clear();
		prefixesPanel.getChildren().add(removeWrapper);
	}

	public void sendSuffixForEditing(GenericBoilerPlate suffix) {

		Button removalButton = new Button("-");
		RemoveBLPWrapper removeWrapper = new RemoveBLPWrapper(removalButton, suffix);
		removalButton.setOnAction(ae -> {
			reqModel.getSuffixProperties().remove(suffix);
			suffixesPanel.getChildren().remove(removeWrapper);
		});
		reqModel.setSuffix(suffix);
		// remove previous suffix (only one is allowed)
		suffixesPanel.getChildren().clear();
		suffixesPanel.getChildren().add(removeWrapper);
	}

	public class RemoveBLPWrapper extends HBox {
		private Button removalButton;
		private GenericBoilerPlate mainItem;

		public RemoveBLPWrapper(Button removalButton, GenericBoilerPlate mainItem) {
			super(10, removalButton, mainItem);
			this.mainItem = mainItem;
			this.removalButton = removalButton;
			setAlignment(Pos.CENTER_LEFT);
		}
	}
}
