package childViews;

import java.util.ArrayList;
import java.util.HashSet;


//import org.stringtemplate.v4.compiler.STParser.ifstat_return;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import models.LogEntryModel;
import models.OntologyWordModel;
import models.ValidatableNode;
import utils.ExtStrings;
import utils.Memory;
import utils.Utils;

/**
 * This class is a pair of a class name (called [type]) and an instance name
 * (called [name])
 * 
 * Visually, it is usually just a textbox where you see the instance name
 * 
 * The class name controls the accepted values in the text box Allowed input
 * should match an instance named [name], belonging to a class named [type]
 * 
 * @author alkoclick
 *
 */

public class BottomLevelObject extends VBox implements SaveableObject, ValidatableNode {

	private static final long serialVersionUID = 1L;
	// private String type; // This is the class name that the instance above
	// should belong to
	private AutocompleteTextField editableText;
	private TextFlow highlightedText;
	private String plainText; // This is the content of the textBox.
	private ArrayList<String> ontologyObjects;
	private ArrayList<String> unknownObjects;
	private ArrayList<String> classObjects;

	private OntologyWordModel type;
	public static Color INSTANCECOLOR = Color.BLUE;
	public static Color PREPWORDCOLOR = Color.GREEN;
	private static Color PROBLEMCOLOR = Color.RED;
	private static Color CLASSCOLOR = Color.GOLD;
	private static Color COMPLEXINSTANCECOLOR = Color.MAGENTA;
	public static final String BOTTOM_DELIMITER = ExtStrings.getString("SerializeBLPBottomDelimiter"); //$NON-NLS-1$
	public static final String MIDDLE_DELIMITER = ExtStrings.getString("SerializeBLPMiddleDelimiter"); //$NON-NLS-1$
	private static final String PREPWORDFILENAME = ExtStrings.getString("PrepWordsFile");
	private static final Border ERROR_BORDER = new Border(
			new BorderStroke(PROBLEMCOLOR, BorderStrokeStyle.SOLID, null, null));
	private String defaultText;
	private Node typeLabel;

	public BottomLevelObject(OntologyWordModel type, String name) {

		this.type = type;
		if (name.length() > 0) {
			this.defaultText = name;
		} else {
			this.defaultText = "Example";
		}

		this.plainText = this.defaultText; // $NON-NLS-1$
		this.setAlignment(Pos.BASELINE_CENTER);
		this.setSpacing(5);
		switchToEditableText();
		switchToHighlightedText();
	}

	public BottomLevelObject(String serialized) {
		this(fromBytes(serialized));
	}

	public BottomLevelObject(BottomLevelObject copyObject) {
		this(copyObject.type, copyObject.plainText);
	}

	private void switchToEditableText() {

		this.getChildren().clear();
		this.getChildren().add(createTypeLabelAndContextMenu());
		this.getChildren().add(editableText = createTextField());
	}

	private Node createTypeLabelAndContextMenu() {
		typeLabel = new Label(type.getShortName() + ":");
		return typeLabel;
	}

	private AutocompleteTextField createTextField() {

		AutocompleteTextField editableText;
		editableText = new AutocompleteTextField(plainText);
		editableText.reCalculateSize();
		editableText.getEntries().addAll(getInstances());

		editableText.focusedProperty().addListener(new FocusChangeListener());
		editableText.setOnKeyTyped((KeyEvent) -> {
			plainText = editableText.getText();
		});
		return editableText;
	}

	public void switchToHighlightedText() {

		if (this.plainText.equals("")) {
			return;
		}
		this.getChildren().clear();
		this.getChildren().add(createTypeLabelAndContextMenu()); // $NON-NLS-1$
		this.getChildren().add(highlightedText = findAndHighlightKnownInstances());
	}
	
	private TextFlow findAndHighlightKnownInstances() {
		
		TextFlow highlightFlow = new TextFlow();
		highlightFlow.focusedProperty().addListener(new FocusChangeListener());
		highlightFlow.setOnMouseClicked((MouseEvent) -> switchToEditableText());

		HashSet<String> knownWords = getInstances();
		HashSet<String> classes = getClassNames();
		ontologyObjects = new ArrayList<>();
		unknownObjects = new ArrayList<>();
		classObjects = new ArrayList<>();
		if (plainText.length() == 0) {
			return highlightFlow;
		}
		String word = plainText.trim();
		Text wordText = new Text(word + " "); //$NON-NLS-1$
		if (classes.contains(word)) {
			wordText.setFill(CLASSCOLOR);
			if (word.contains("."))
				classObjects.add(word.split("\\.", 2)[1]);
			else 
				classObjects.add(word);

		} else if (knownWords.contains(word)) {
			wordText.setFill(INSTANCECOLOR);
			ontologyObjects.add(word);
		} else {
			unknownObjects.add(word);
			wordText.setFill(PROBLEMCOLOR);
		}
		highlightFlow.getChildren().add(wordText);		
			
		return highlightFlow;
	}

	private TextFlow findAndHighlightKnownInstancesOld() {

		TextFlow highlightFlow = new TextFlow();
		highlightFlow.focusedProperty().addListener(new FocusChangeListener());
		highlightFlow.setOnMouseClicked((MouseEvent) -> switchToEditableText());

		HashSet<String> knownWords = getInstances();
		ArrayList<String> prepWords = getPrepWords();
		HashSet<String> classes = getClassNames();
		ontologyObjects = new ArrayList<>();
		unknownObjects = new ArrayList<>();
		classObjects = new ArrayList<>();
		if (plainText.length() == 0) {
			return highlightFlow;
		}
		String[] quoteSplits = plainText.split("\""); //$NON-NLS-1$
		int inQuotes = plainText.startsWith("\"") ? 0 : 1; //$NON-NLS-1$
		for (int i = 0; i < quoteSplits.length; ++i) {
			String phrase = quoteSplits[i];
			if (i % 2 == inQuotes) {
				Text wordText = new Text(phrase + " "); //$NON-NLS-1$
				if (knownWords.contains(phrase.trim())) {
					wordText.setFill(COMPLEXINSTANCECOLOR);
					ontologyObjects.add(phrase);
				}
				highlightFlow.getChildren().add(wordText);
			} else { // Outside of quotes, process every word
				String[] words = phrase.split(" "); //$NON-NLS-1$
				for (String word : words) {
					Text wordText = new Text(word + " "); //$NON-NLS-1$
					if (classes.contains(word)) {
						wordText.setFill(CLASSCOLOR);
						if (word.contains("."))
							classObjects.add(word.split("\\.", 2)[1]);
						else 
							classObjects.add(word);

					} else if (!knownWords.isEmpty() && knownWords.contains(word)) {
						wordText.setFill(INSTANCECOLOR);
						ontologyObjects.add(word);
					} else if (prepWords.contains(word.toLowerCase())) {
						wordText.setFill(PREPWORDCOLOR);
					} else {
						unknownObjects.add(word);
						wordText.setFill(PROBLEMCOLOR);
					}
					highlightFlow.getChildren().add(wordText);
				}
			}
		}	
			
		return highlightFlow;
	}

	public class FocusChangeListener implements ChangeListener<Boolean> {

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

			if (!newValue) { // Focus lost
				plainText = (editableText.getText().equals("")) ? defaultText : editableText.getText();
				switchToHighlightedText();
			}
		}
	}

	public TextField getContents() {

		return editableText;
	}

	private HashSet<String> getClassNames() {
		return Memory.getClassesFlat();
	}

	private HashSet<String> getInstances() {
		return Memory.getSuggestionsFor(type.getFullName());
	}

	private ArrayList<String> getPrepWords() {

		ArrayList<String> prepWordsList = new ArrayList<>();
		prepWordsList.addAll(Utils.reader(PREPWORDFILENAME));
		return prepWordsList;
	}

	@Override
	public String toString() {

		String contentDump = ""; //$NON-NLS-1$
		if (editableText != null)
			contentDump += editableText.getText() + " "; //$NON-NLS-1$
		return "<" + type + "> " + contentDump; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String toBytes() {

		String serializedForm = ""; //$NON-NLS-1$
		// after a bottom level object, we place a delimiter
		serializedForm += this.type.getFullName() + MIDDLE_DELIMITER + editableText.getText() + BOTTOM_DELIMITER;
		return serializedForm;
	}

	@Override
	public void select() {

		switchToHighlightedText();
		highlightedText.borderProperty().setValue(ERROR_BORDER);
	}

	public ArrayList<String> getOntologyObjects() {

		switchToHighlightedText();
		ontologyObjects.addAll(unknownObjects);
		return ontologyObjects;
	}

	public ArrayList<String> getUnknownObjects() {

		switchToHighlightedText();
		return unknownObjects;
	}

	public ArrayList<LogEntryModel> validate() {

		switchToHighlightedText();
		ArrayList<LogEntryModel> errors = new ArrayList<>();
		if (unknownObjects.size() > 0) {
			for (String unknownString : unknownObjects) {
				errors.add(new LogEntryModel().setAsUnknownObject(unknownString, this));
			}
		}
		return errors;
	}

	public OntologyWordModel getType() {

		return type;
	}

	public static BottomLevelObject fromBytes(String serialized) {
		return serialized.split("::").length > 1
				? new BottomLevelObject(new OntologyWordModel(serialized.split("::")[0].trim()),
						serialized.split("::")[1].trim())
				: new BottomLevelObject(new OntologyWordModel(serialized.split("::")[0].trim()), "");
	}

	public ArrayList<String> getAbstract() {
		switchToHighlightedText();
		return classObjects;
	}

}
