package viewWrappers;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MinimizeWrapper extends StackPane {
	protected Node content;
	protected Button button;
	protected BorderPane miniPane;
	protected static final String minimizeText = "-";
	protected final String maximizeText; // default
	
	public MinimizeWrapper(Node content, String text){
		
		maximizeText = (text !=null && !text.isEmpty()) ? text : "+";
		this.content = content;
		this.getChildren().add(content);
		this.getChildren().add(miniPane = minimizerPane());
	}
	
	private BorderPane minimizerPane(){
		
		BorderPane minimizerPane = new BorderPane();
		minimizerPane.setPickOnBounds(false);
		button = new Button(minimizeText);
		button.setOnAction(aEvent -> minimize());
		minimizerPane.setRight(button);
		
		return minimizerPane;
	}
	

	public void addContent() {
		this.getChildren().add(0,content);
		miniPane.getChildren().clear();
		miniPane.setRight(button);
	}

	public void removeContent() {
		this.getChildren().remove(content);
		miniPane.getChildren().clear();
		miniPane.setRight(button);
	}
	
	public void minimize(){
		button = new Button(maximizeText);
		removeContent();
		button.setOnAction(actionEvent -> {
			button = new Button(minimizeText);
			addContent();
			button.setOnAction(aEvent -> minimize());
		});
	}
	
}
