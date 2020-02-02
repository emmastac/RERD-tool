package childViews;

import javafx.scene.paint.Color;
import models.AbstractStatusModel;
import models.RequirementStatusModel;

public class RequirementStatusView extends AbstractStatusView {

	public RequirementStatusView(AbstractStatusModel abstractStatusModel) {
		super(abstractStatusModel);
		
		colors.put(RequirementStatusModel.MISSING, Color.YELLOW);
		colors.put(RequirementStatusModel.NOTVERIFIABLE, Color.MEDIUMPURPLE);
		colors.put(RequirementStatusModel.INVALID, Color.RED);
		colors.put(RequirementStatusModel.NOTYETCOVERED, Color.DARKORANGE);
		colors.put(RequirementStatusModel.COVERED, Color.LIMEGREEN);
		colors.put(RequirementStatusModel.DISCHARGED, Color.DODGERBLUE);
		
		fillProperty().setValue(colors.get(this.statusModel.currentStatus.get()));
	}
}
