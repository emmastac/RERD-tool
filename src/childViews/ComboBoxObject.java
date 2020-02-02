package childViews;

import javafx.scene.control.ComboBox;
import utils.ExtStrings;

public class ComboBoxObject extends ComboBox<String> implements SaveableObject {
	private static final long serialVersionUID = 1L;
	protected String[] rawItems;
	public static final String MIDDLE_DELIMITER = ExtStrings.getString("SerializeBLPMiddleDelimiter"); //$NON-NLS-1$
	public static final String BOTTOM_DELIMITER = ExtStrings.getString("SerializeBLPBottomDelimiter"); //$NON-NLS-1$

	/**
	 * Adds all given items to the dropDown The first one will be selected
	 * 
	 * @param items
	 */
	public ComboBoxObject(String... items) {

		super();
		rawItems = items;
		getItems().addAll(items);
		if (items.length > 0)
			selectionModelProperty().get().select(0);
	}

	public ComboBoxObject(ComboBoxObject copyObject) {
		this(copyObject.rawItems);
		selectionModelProperty().get().select(copyObject.getSelectionModel().getSelectedItem());
	}

	public ComboBoxObject(String serialized) {
		this(fromBytes(serialized));
	}

	@Override
	public String toString() {
		return getSelectionModel().getSelectedItem()+" ";
	}

	@Override
	public String toBytes() {

		String serializedForm = "";
		serializedForm += getItems().get(0);
		for (int i = 1; i < getItems().size(); i++) {
			serializedForm += " | " + getItems().get(i);
		}
		// after a bottom level object, we place a delimiter
		serializedForm += MIDDLE_DELIMITER + getSelectionModel().getSelectedItem() + BOTTOM_DELIMITER;
		return serializedForm;
	}

	private static ComboBoxObject fromBytes(String serialized) {
		String[] contents_selection = serialized.split(MIDDLE_DELIMITER);
		String[] items = contents_selection[0].split("\\|");
		for (int i = 0; i < items.length; i++) {
			items[i] = items[i].trim();
		}
		ComboBoxObject comboBoxObject = new ComboBoxObject(items);
		if (contents_selection.length > 1) {
			comboBoxObject.selectionModelProperty().get().select(contents_selection[1]);
		} else {
			comboBoxObject.selectionModelProperty().get().select(0);
		}
		return comboBoxObject;
	}

	public String getContents() {
		if (getItems().size()>0){
			return getSelectionModel().getSelectedItem();
		}
		else return "";
	}
}
