package containerViews;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import utils.ExtStrings;
import viewWrappers.MinimizeWrapper;
import childViews.LogConsole;

public abstract class EditViewWrapper extends BorderPane {
	private static final double CONSOLE_WIDTH = ExtStrings.getInt("ConsoleWidth");
	protected Node centerPane;
	protected HBox optionBox;
	protected Button saveButton,checkButton,clearButton,newButton;
	protected LogConsole console;
	protected static MinimizeWrapper consoleWrapper;
	protected static final String SAVE_BUTTON_TEXT = ExtStrings.getString("SaveButton");
	protected static final String VAL_BUTTON_TEXT = ExtStrings.getString("ValidateButton");
	protected static final String CLEAR_BUTTON_TEXT = ExtStrings.getString("ClearButton");
	protected static final String NEW_BUTTON_TEXT = ExtStrings.getString("NewButton");
	
	public EditViewWrapper() {

		init();
	}
	
	protected void init() {

		this.setBottom(optionBox = drawBottomPanel());

		this.setPadding(new Insets(10));
		
		this.setLeft(consoleWrapper = new MinimizeWrapper(console = new LogConsole(),"Console"));
		console.prefWidthProperty().bind(this.widthProperty().multiply(CONSOLE_WIDTH));
		consoleWrapper.minimize();

		this.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null)));
		
		createButtonListeners();
	}
	
	protected abstract void createButtonListeners();
	
	protected HBox drawBottomPanel() {
		HBox optionPanel = new HBox();
		optionPanel.setSpacing(10);
		optionPanel.setPadding(new Insets(10));

		saveButton = new Button(SAVE_BUTTON_TEXT);

		checkButton = new Button(VAL_BUTTON_TEXT);

		clearButton = new Button(CLEAR_BUTTON_TEXT);

		newButton = new Button(NEW_BUTTON_TEXT);
		
		optionPanel.getChildren().addAll(saveButton, checkButton, clearButton, newButton);

		return optionPanel;
	}
	
	public LogConsole getConsole() {
		return console;
	}
}
