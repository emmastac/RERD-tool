package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import childViews.BottomLevelObject;
import childViews.ComboBoxObject;
import childViews.LabelObject;
import childViews.SaveableObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import utils.ExtStrings;
import utils.Memory;

public class AbstractBoilerPlate extends HBox {
	protected String[] rawComponents;
	protected SimpleStringProperty iD, category, displayForm;
	public HashMap<OntologyWordModel, ArrayList<String>> contentMap;
	private ContextMenu context;
	public static final OntologyWordModel COMBO_BOX_NAME = new OntologyWordModel("ComboBox");
	public static final String BOTTOM_DELIMITER = ExtStrings.getString("SerializeBLPBottomDelimiter");
	public static final String COMPLEX_DELIMITER_START = ExtStrings.getString("SerializeBLPComplexDelimiterStart");
	public static final String COMPLEX_DELIMITER_END = ExtStrings.getString("SerializeBLPComplexDelimiterEnd");
	private static final Logger logger = Logger.getLogger(AbstractBoilerPlate.class.getName());

	public AbstractBoilerPlate(String category, String... rawComponents) {

		super();
		this.category = new SimpleStringProperty(category);
		this.iD = new SimpleStringProperty(category);
		this.rawComponents = rawComponents;

		loadComponents();
		this.init();
		// genID();
	}

	public AbstractBoilerPlate(AbstractBoilerPlate abstractBoilerPlate) {
		this(abstractBoilerPlate.getCat(), abstractBoilerPlate.getRawComponents().clone());
	}

	public AbstractBoilerPlate(String serializedBoilerPlate) {
		this(deserialize(serializedBoilerPlate));
	}

	private void setSelf(AbstractBoilerPlate aBlp) {
		rawComponents = aBlp.rawComponents;
		category = aBlp.category;
		iD = aBlp.iD;
		loadComponents();
		init();
	}

	private void loadComponents() {

		this.getChildren().clear();

		for (String rawString : rawComponents) {
			Node component = this.parse(rawString);
			if (component != null) {
				this.getChildren().add(component);
			}
		}
	}

	private static Node parse(String rawString) {

		logger.log(Level.FINE, "Parsing " + rawString);
		if (rawString.contains("|")) {
			// Process as DropDown
			return new ComboBoxObject(rawString);
		} else if (rawString.contains("<")) {
			// Process as complex object
			String item = rawString.substring(1, rawString.length() - 1);
			logger.log(Level.FINER, "Parsing " + item + " as Ablp");
			if (Memory.getSyntax().containsKey(item))
				return new AbstractBoilerPlate(Memory.getSyntax().get(item).get(0));
			else {
				logger.log(Level.WARNING, "Object " + item + " was not found in any syntax file");
				return null;
			}
		} else if (rawString.contains("::")) {
			// Process as textField
			Memory.addToSuggestions(rawString);
			return new BottomLevelObject(rawString);
		} else {
			// Process as StaticField
			return new LabelObject(new OntologyWordModel(rawString));
		}
	}

	public static AbstractBoilerPlate parseToBoilerplate(String rawString) {

		logger.log(Level.FINE, "Parsing " + rawString);
		if (rawString.contains("<")) {
			// Process as complex object
			String item = rawString.substring(1, rawString.length() - 1);
			logger.log(Level.FINER, "Parsing " + item + " as Ablp");
			if (Memory.getSyntax().containsKey(item)) {
				System.out.println("in " + item + " " + Memory.getSyntax().get(item).size() + " "
						+ Memory.getSyntax().get(item).get(0));
				return new AbstractBoilerPlate(Memory.getSyntax().get(item).get(0));
			} else {
				logger.log(Level.WARNING, "Object " + item + " was not found in any syntax file");
				return null;
			}
		} else {
			// Process as StaticField
			return null;
		}
	}

	private void init() {

		this.setSpacing(10);
		this.setPadding(new Insets(0, 10, 10, 10));
		this.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(10.0), new BorderWidths(1))));

		this.displayForm = new SimpleStringProperty(this.toString());

		// Add the ability to right click to change to another syntax for this
		// item, by showing a context menu
		this.setOnMouseClicked(mE -> {
			context = buildContextMenu();
			context.hide();
			mE.consume();
			// To avoid parents also displaying their menus, consume this
			if (mE.getButton().equals(MouseButton.SECONDARY)) // If right click
				// Show on these coordinates
				context.show(AbstractBoilerPlate.this, mE.getScreenX(), mE.getScreenY());
		});
		Tooltip hoverText = new Tooltip();
		hoverText.textProperty().bind(category);
		Tooltip.install(this, hoverText);
	}

	private ContextMenu buildContextMenu() {
		ArrayList<MenuItem> suggestions = new ArrayList<>();

		// Ensure we have a proper ID
		genID();

		// Add this item's category to the list
		MenuItem catItem = new MenuItem("Type: " + getCat());
		ContextMenu entriesPopup = new ContextMenu();
		suggestions.add(catItem);

		if (Memory.getSyntax().containsKey(getCat())) {
			Memory.getSyntax().get(getCat()).forEach(aBlp -> {
				MenuItem temp = new MenuItem(aBlp.getTemplateForm());
				suggestions.add(temp);
				temp.setOnAction(aE -> this.setSelf(aBlp));
			});
		} else {
			MenuItem noOtherSuggestions = new MenuItem("No other suggestions found");
			suggestions.add(noOtherSuggestions);
		}

		entriesPopup.getItems().addAll(suggestions);
		entriesPopup.setAutoHide(true);
		return entriesPopup;
	}

	/**
	 * This method is used, also, for displaying boilerplate instances in the
	 * Editing Panel
	 */
	@Override
	public String toString() {

		String total = new String("");
		for (Node component : this.getChildren()) {
			if (component.toString().contains(">"))
				total += component.toString().split(">")[1];
			else
				total += component.toString();
		}
		return total;
	}

	public String serialize() {
		String serialized = "" + iD.get() + BOTTOM_DELIMITER;
		logger.log(Level.FINE, "Serializing " + iD.get());
		if (!this.getChildren().isEmpty())
			for (Node object : this.getChildren()) {
				if (object instanceof AbstractBoilerPlate) {
					AbstractBoilerPlate absBlp = (AbstractBoilerPlate) object;
					absBlp.genID();
					absBlp.serialize();
					serialized += COMPLEX_DELIMITER_START + absBlp.getID() + COMPLEX_DELIMITER_END;
					serialized += BOTTOM_DELIMITER;
					// Memory.addSyntax(absBlp);
				} else {
					serialized += ((SaveableObject) object).toBytes();
				}
			}
		if (!Memory.getSyntax().keySet().contains(this.getID()))
			Memory.addSyntax(this);
		return serialized;
	}

	protected static AbstractBoilerPlate deserialize(String serializedForm) {
		if (serializedForm == null || serializedForm.isEmpty())
			return new AbstractBoilerPlate("", "");
		String[] tokens = serializedForm.split(BOTTOM_DELIMITER);
		if (tokens.length < 2)
			return null;
		else {
			return new AbstractBoilerPlate(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
		}
	}

	public void genID(Set<String> keyset) {
		String iDPrefix = category.get();
		if (iDPrefix != null && !iDPrefix.isEmpty() && iDPrefix.contains("-")) {
			iD.set(iDPrefix);
			category.set(iDPrefix.substring(0, iDPrefix.lastIndexOf("-")));
		} else {
			int count = 1;
			while (keyset.contains(iDPrefix + "-" + count)) {
				count++;
			}
			iD.set(iDPrefix + "-" + count);
		}
	}

	/**
	 * Generates a new ID for the object if it doesn't have one already
	 */
	public void genID() {
		String iDPrefix = category.get();
		String iDContent = iD.get();
		// If the id is all in the category already, just fix things
		if (iDPrefix != null && !iDPrefix.isEmpty() && iDPrefix.matches(".*-\\d+")) {
			iD.set(iDPrefix);
			// set category to exclude ending
			category.set(iDPrefix.substring(0, iDPrefix.lastIndexOf("-")));
		} // If the existing ID is proper
		else if (iDContent != null && !iDContent.isEmpty() && iDContent.matches(".*-\\d+")) {
			// Do nothing
		} else {
			forceGenID();
		}
	}

	protected void forceGenID() {
		Set<String> keyset = Memory.getSyntax().keySet();
		String iDPrefix = category.get();
		int count = 1;
		while (keyset.contains(iDPrefix + "-" + count)) {
			count++;
		}
		iD.set(iDPrefix + "-" + count);
		// Memory.addSyntax(this);
	}

	public ArrayList<LogEntryModel> validate() {
		
		ArrayList<LogEntryModel> errors = new ArrayList<>();
		
		for (Node object : this.getChildren()) {
			if (object.getClass().equals(BottomLevelObject.class)) {
				errors.addAll(((BottomLevelObject) object).validate());
			} else if (object.getClass().equals(AbstractBoilerPlate.class)) {
				errors.addAll(((AbstractBoilerPlate) object).validate());
			}	
		}
		return errors;
	}

	public void saveToList(Collection<AbstractBoilerPlate> blpList) {
		if (!this.getChildren().isEmpty())
			for (Node object : this.getChildren()) {
				if (object instanceof AbstractBoilerPlate) {
					AbstractBoilerPlate absBlp = (AbstractBoilerPlate) object;
					blpList.add(absBlp);
					absBlp.saveToList(blpList);
				}
			}
	}

	public Boolean contains(String query) {

		if (rawComponents != null)
			for (String rawString : rawComponents) {
				if (rawString.contains(query))
					return true;
			}
		if (displayForm.getValue().contains(query))
			return true;
		if (iD.getValue().contains(query))
			return true;
		return false;
	}

	public String getID() {
		return iD.get();
	}

	public String getCat() {
		return category.get();
	}

	public String getTemplateForm() {
		String templateString = "";
		for (String str : rawComponents) {
			templateString += templateParse(str);
		}
		return templateString;
	}

	private String templateParse(String rawString) {
		if (rawString.contains("::")) {
			// Process as textField
			String prefixString = rawString.split("::")[0];
			if (prefixString.contains(":"))
				return prefixString.split(":")[1] + " : [ ... ] ";
			else
				return prefixString + " : [ ... ] ";
		} else if (rawString.contains("|")) {
			// Process as DropDown
			return " " + rawString + " ";
		} else if (rawString.contains("<")) {
			// Process as complexObject
			return " " + rawString + " ";
		} else {
			// Process as StaticField
			return " " + rawString + " ";
		}
	}

	public String getDisplayForm() {
		return displayForm.get();
	}

	public SimpleStringProperty getDisplayFormProperty() {
		return displayForm;
	}

	public void setDisplayForm(String input) {
		displayForm.set(input);
	}

	public String[] getRawComponents() {
		return rawComponents;
	}

	public String getCatNoNS() {
		if (category.get().contains(":"))
			return category.get().split(":")[1].replace(" ", "");
		else
			return category.get().replace(" ", "");
	}

	public String getIDNoNS() {
		if (iD.get().contains(":"))
			return iD.get().split(":")[1].replace(" ", "");
		else
			return iD.get().replace(" ", "");
	}

	public HashMap<OntologyWordModel, ArrayList<String>> getContentMap() {
		contentMap = new HashMap<>();

		if (!this.getChildren().isEmpty())
			for (Node object : this.getChildren()) {
				if (object.getClass().equals(BottomLevelObject.class)) {
					BottomLevelObject bottObject = (BottomLevelObject) object;
					if (!contentMap.containsKey(bottObject.getType())) {
						contentMap.put(bottObject.getType(), new ArrayList<>());
					}
					contentMap.get(bottObject.getType()).add(bottObject.getContents().getText().replace(" ", "_"));
				} else if (object.getClass().equals(ComboBoxObject.class)) {
					ComboBoxObject combObject = (ComboBoxObject) object;
					if (!contentMap.containsKey(COMBO_BOX_NAME)) {
						contentMap.put(COMBO_BOX_NAME, new ArrayList<>());
					}
					contentMap.get(COMBO_BOX_NAME).add(combObject.getContents().replace(" ", "_"));
				} else if (object.getClass().equals(AbstractBoilerPlate.class)) {
					AbstractBoilerPlate complexObject = (AbstractBoilerPlate) object;
					OntologyWordModel catOfObject = new OntologyWordModel(complexObject.getCat());
					if (!contentMap.containsKey(catOfObject)) {
						contentMap.put(catOfObject, new ArrayList<>());
					}
					complexObject.genID();
					contentMap.get(catOfObject).add(complexObject.getID());
				}
			}

		return contentMap;
	}

	public ArrayList<String> getAbstract() {
		ArrayList<String> abstractObjects = new ArrayList<>();
		if (!this.getChildren().isEmpty())
			for (Node object : this.getChildren()) {
				if (object.getClass().equals(BottomLevelObject.class)) {
					BottomLevelObject bottObject = (BottomLevelObject) object;
					abstractObjects.addAll(bottObject.getAbstract());
				} else if (object.getClass().equals(AbstractBoilerPlate.class)) {
					AbstractBoilerPlate complexObject = (AbstractBoilerPlate) object;
					abstractObjects.addAll(complexObject.getAbstract());
				}
			}
		return abstractObjects;
	}
}
