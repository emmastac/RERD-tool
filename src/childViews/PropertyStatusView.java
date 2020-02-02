package childViews;

import javafx.scene.paint.Color;
import models.AbstractStatusModel;
import models.PropertyStatusModel;

public class PropertyStatusView extends AbstractStatusView {

	public PropertyStatusView(AbstractStatusModel abstractStatusModel) {
		super(abstractStatusModel);
		
		colors.put(PropertyStatusModel.MISSING, Color.YELLOW);
		colors.put(PropertyStatusModel.INVALID, Color.RED);
		colors.put(PropertyStatusModel.NOTYETDISCHARGED, Color.MEDIUMPURPLE);
		colors.put(PropertyStatusModel.VERIFIED, Color.DODGERBLUE);
		
		fillProperty().setValue(colors.get(this.statusModel.currentStatus.get()));
	}
	
}
