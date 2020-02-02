package childViews;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AutocompleteTextField extends TextField {
	private final SortedSet<String> entries;
	private ContextMenu entriesPopup;

	// Based on:
	// https://gist.github.com/floralvikings/10290131

	public AutocompleteTextField(String plainText) {
		super(plainText);
		entries = new TreeSet<>();
		init();
	}

	public AutocompleteTextField() {
		super();
		entries = new TreeSet<>();
		init();
	}

	private void init() {

		entriesPopup = new ContextMenu();

		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {

				if (getText().length() == 0) {
					entriesPopup.hide();
				} else {

					Platform.runLater(new Runnable() {
						@Override
						public void run() {

							reCalculateSize();
							LinkedList<String> searchResult = new LinkedList<>();
							String text = getText();

//							int start = getStartOfWord(text);
//							int end = getEndOfWord(text);
//							String word = text.substring(start, end).trim();
							String word = text.trim();

							// Create a subset of result entries containing all
							// words starting with "word"
							searchResult.addAll(entries.subSet(word, word + Character.MAX_VALUE));
							if (entries.size() > 0) {
								populatePopup(searchResult);
								if (!entriesPopup.isShowing()) {
									entriesPopup.show(AutocompleteTextField.this, Side.BOTTOM,
											new Double(getCaretPosition() * 7.3 + 7).intValue(), 0);
								}
							} else {
								entriesPopup.hide();
							}

						}
					});

				}
			}
		});

		focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean,
					Boolean aBoolean2) {
				entriesPopup.hide();
			}
		});

	}

//	private int getStartOfWord(String text) {
//		@SuppressWarnings("unused")
//		char letter;
//
//		int start = getCaretPosition() - 1;
//		String prefixText = text.substring(0, start);
//
//		if (prefixText.contains("\"")) {
//			// If there is an odd number of preceding quotes
//			if ((prefixText.length() - prefixText.replaceAll("\"", "").length()) % 2 == 1) {
//				return prefixText.lastIndexOf("\"") + 1;
//			}
//		}
//		while (start >= 0 && (letter = text.charAt(start)) != ' ') {
//			start--;
//		}
//
//		return Math.max(start + 1, 0);
//	}

//	private int getEndOfWord(String text) {
//		@SuppressWarnings("unused")
//		char letter;
//
//		int end = getCaretPosition();
//		String suffixText = text.substring(getCaretPosition());
//
//		if (suffixText.contains("\"")) {
//			// If there is an odd number of following quotes
//			if ((suffixText.length() - suffixText.replaceAll("\"", "").length()) % 2 == 1) {
//				return getCaretPosition() + suffixText.indexOf("\"");
//			}
//		}
//
//		while (end < text.length() && (letter = text.charAt(end)) != ' ') {
//			end++;
//		}
//
//		return end;
//	}

	/**
	 * Get the existing set of autocomplete entries.
	 * 
	 * @return The existing autocomplete entries.
	 */
	public SortedSet<String> getEntries() {
		return entries;
	}

	/**
	 * Populate the entry set with the given search results. Display is limited
	 * to 10 entries, for performance.
	 * 
	 * @param searchResult
	 *            The set of matching strings.
	 */
	private void populatePopup(List<String> searchResult) {
		List<CustomMenuItem> menuItems = new LinkedList<>();
		int maxEntries = 8;
		int count = Math.min(searchResult.size(), maxEntries);
		for (int i = 0; i < count; i++) {
			final String result = searchResult.get(i);
			Label entryLabel = new Label(result);
			CustomMenuItem item = new CustomMenuItem(entryLabel, true);

			item.setOnAction((ActionEvent) -> {
//				String prefix = getText().substring(0, startOfWord);

				String finalResult = result.trim();

//				String suffix = getText().substring(endofWord);

				// Add missing quotes
//				if (finalResult.contains(" ")) {
//					if (!prefix.endsWith("\""))
//						finalResult = "\"" + finalResult;
//					if (!suffix.startsWith("\""))
//						finalResult = finalResult + "\"";
//				}

				// Add space left if we are not in the start of the sentence
//				if (!prefix.endsWith(" ") && !prefix.endsWith("\"") && prefix.length() > 0)
//					prefix = prefix + " "; // TODO Check if we can simplify this
				// Add space right
//				if (!suffix.startsWith(" ") && !suffix.startsWith("\""))
//					suffix = " " + suffix;

				setText(finalResult);
				positionCaret(finalResult.length() + 2);
				entriesPopup.hide();
			});
			menuItems.add(item);
		}
		entriesPopup.getItems().clear();
		entriesPopup.getItems().addAll(menuItems);

	}

	/**
	 * Readjust size of textfield based on text contained
	 */
	public void reCalculateSize() {
		setPrefColumnCount(calcOptimalSize());
	}

	protected int calcOptimalSize() {
		return new Double(getText().length() * 0.65 + 1).intValue();
	}
}
