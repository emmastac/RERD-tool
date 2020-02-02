package containerViews;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import models.OntologyWordModel;
import utils.ExtStrings;
import viewWrappers.MinimizeWrapper;

public class DictionaryTab extends Tab {
	private static final String ONT_BROWSER_TITLE = ExtStrings.getString("OntBrowser");
	private static final String ONT_EDIT_PANEL_TITLE = ExtStrings.getString("OntEditPanel");
	public static SimpleObjectProperty<OntologyWordModel> selectedClass;
	public static SimpleObjectProperty<OntologyWordModel> selectedInstance;
	private OntologyBrowser ontObjectBrowser;
	private OntologyEditPanel ontObjectEditingPanel;
	private VBox dictionaryPane;
	private static final double BROWSER_HEIGHT = ExtStrings.getInt("BrowserHeight");
	private static final double INFO_PANEL_HEIGHT = ExtStrings.getInt("InfoPanelHeight");

	public DictionaryTab() {

		init();
	}

	protected void init() {

		selectedClass = new SimpleObjectProperty<OntologyWordModel>();
		selectedInstance = new SimpleObjectProperty<OntologyWordModel>();

		this.setText("Dictionary");

		dictionaryPane = new VBox(100);
		dictionaryPane.getChildren()
				.add(new MinimizeWrapper(ontObjectBrowser = new OntologyBrowser(), ONT_BROWSER_TITLE));
		dictionaryPane.getChildren()
				.add(new MinimizeWrapper(ontObjectEditingPanel = new OntologyEditPanel(), ONT_EDIT_PANEL_TITLE));
		ontObjectBrowser.prefHeightProperty().bind(dictionaryPane.heightProperty().multiply(BROWSER_HEIGHT));
		ontObjectEditingPanel.prefHeightProperty().bind(dictionaryPane.heightProperty().multiply(INFO_PANEL_HEIGHT));

		selectedClass.addListener(new ChangeListener<OntologyWordModel>() {

			@Override
			public void changed(ObservableValue<? extends OntologyWordModel> arg0, OntologyWordModel arg1,
					OntologyWordModel arg2) {
				if (arg2 != null) {
					ontObjectBrowser.updatePanels();
					ontObjectEditingPanel.drawClassView();
				}
			}
		});

		selectedInstance.addListener(new ChangeListener<OntologyWordModel>() {

			@Override
			public void changed(ObservableValue<? extends OntologyWordModel> arg0, OntologyWordModel arg1,
					OntologyWordModel arg2) {
				if (arg2 != null) {
					// ontObjectBrowser.updatePanels();
					ontObjectEditingPanel.drawInstanceView();
				}
			}
		});

		dictionaryPane.setPadding(new Insets(20));

		this.setContent(dictionaryPane);
	}
}
