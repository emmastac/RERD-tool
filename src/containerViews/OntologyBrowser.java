package containerViews;

import java.util.ArrayList;
import java.util.Comparator;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import models.OntologyWordModel;
import utils.OntologyIO;

public class OntologyBrowser extends BorderPane {

	private static final String SEARCH_TEXT = "Search";
	private static final String SEARCH_PROMPT_TEXT = "Search all objects...";
	public SimpleStringProperty filter;
	private VBox categoryPanel;
	private VBox parentsPanel;
	private VBox childrenPanel;
	private VBox instancePanel;

	public OntologyBrowser() {
		filter = new SimpleStringProperty();

		this.setPadding(new Insets(10));
		this.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(10.0), new BorderWidths(1))));

		this.setTop(searchBar());
		this.setCenter(browserPanel());

		filter.addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				updatePanels();
			}
		});
	}

	private HBox searchBar() {

		HBox searchBarPanel = new HBox(10);
		searchBarPanel.setPadding(new Insets(10));

		TextField searchField = new TextField();
		searchField.setTooltip(new Tooltip(SEARCH_TEXT));
		searchField.setPromptText(SEARCH_PROMPT_TEXT);

		filter.bind(searchField.textProperty());

		searchBarPanel.getChildren().add(searchField);

		return searchBarPanel;
	}

	private HBox browserPanel() {

		HBox mainPane = new HBox(25);

		initPanels();

		mainPane.setPadding(new Insets(20));
		mainPane.getChildren().addAll(createScrollPanel(categoryPanel), createScrollPanel(parentsPanel),
				createScrollPanel(childrenPanel), createScrollPanel(instancePanel));

		mainPane.getChildren().forEach(panel -> {
			((Region) panel).setBorder(new Border(
					new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
			((Region) panel).setPadding(new Insets(10));
		});

		return mainPane;
	}

	private ScrollPane createScrollPanel(Pane pane) {
		ScrollPane scrollPane = new ScrollPane(pane);
		scrollPane.setPadding(new Insets(10));
		scrollPane.setFitToWidth(true);
		scrollPane.setMinWidth(100);
		scrollPane.setMaxWidth(200);
		return scrollPane;
	}

	private void initPanels() {
		categoryPanel = new VBox(new Label("Category"));
		parentsPanel = new VBox(new Label("Parent Classes"));
		childrenPanel = new VBox(new Label("Children Classes"));
		instancePanel = new VBox(new Label("Instances"));
		updatePanels();
	}

	public void updatePanels() {

		fillPanel(categoryPanel, /*
									 * (filter.get().length() == 0) ?
									 * OntologyIO.getClassesTopLevel() :
									 */ OntologyIO.getClassesFlat(), DictionaryTab.selectedClass);

		if (DictionaryTab.selectedClass.get() != null) {
			fillPanel(parentsPanel,
					OntologyIO.getParentClassesForClass(DictionaryTab.selectedClass.get().getFullName()),
					DictionaryTab.selectedClass);
			fillPanel(childrenPanel,
					OntologyIO.getChildClassesForClass(DictionaryTab.selectedClass.get().getFullName()),
					DictionaryTab.selectedClass);
			fillPanel(instancePanel, OntologyIO.getInstancesForClass(DictionaryTab.selectedClass.get().getFullName()),
					DictionaryTab.selectedInstance);
		}
	}

	private void fillPanel(Pane panel, ArrayList<OntologyWordModel> items,
			SimpleObjectProperty<OntologyWordModel> changeable) {

		Node title = panel.getChildren().get(0);
		panel.getChildren().clear();
		panel.getChildren().add(title);

		ToggleGroup classGroup = new ToggleGroup();
		ArrayList<RadioButton> buttons = new ArrayList<>();

		items.sort(new Comparator<OntologyWordModel>() {

			@Override
			public int compare(OntologyWordModel o1, OntologyWordModel o2) {
				return o1.getShortName().compareTo(o2.getShortName());
			}
		});

		if (items.size() != 0) {
			for (OntologyWordModel item : items) {
				String category = item.getShortName();
				if (!item.getNamespace().equals("DSO"))
					continue;

				if (filter.get().length() == 0 || category.toLowerCase().contains(filter.get().toLowerCase())) {
					RadioButton rb;
					rb = new RadioButton(category);
					rb.setToggleGroup(classGroup);
					buttons.add(rb);
					rb.setOnAction(ae -> {
						changeable.set(item);
					});
					if (DictionaryTab.selectedClass.get() != null
							&& DictionaryTab.selectedClass.get().getFullName().equals(item.getFullName()))
						rb.setSelected(true);
					else if (DictionaryTab.selectedInstance.get() != null
							&& DictionaryTab.selectedInstance.get().getFullName().equals(item.getFullName()))
						rb.setSelected(true);
				}

			}
		}

		panel.getChildren().addAll(buttons);
	}

}
