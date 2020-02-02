package childViews;

import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.AbstractStatusModel;

public abstract class AbstractStatusView extends Circle {
	
	protected static HashMap<String, Color> colors;
	protected AbstractStatusModel statusModel;
	protected Tooltip hoverText;
	
	public AbstractStatusView(AbstractStatusModel statusModel) {
		
		this.statusModel = statusModel;

		colors = new HashMap<>();
		colors.put(null, Color.BLACK);	
		
		setRadius(10);
		
		hoverText = new Tooltip();
		hoverText.textProperty().bind(statusModel.currentStatus);
		Tooltip.install(this, hoverText);
		
		this.statusModel.currentStatus.addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				fillProperty().setValue(colors.get(arg2));
			}
		});
	}

	public AbstractStatusModel getStatusModel() {
		return statusModel;
	}

	public void setStatusModel(AbstractStatusModel statusModel) {
		this.statusModel = statusModel;
	}
}
