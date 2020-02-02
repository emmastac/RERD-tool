package containerViews;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import models.OntologyWordModel;

public class AbsLevelAndCatNameHeaderView extends HBox {
	
		private Label absLevelLabel;
		private Label catNameLabel;
		private OntologyWordModel absLevel,categoryName;
		private final static String absLevelPrefix = "Abstraction Level : ";
		private final static String catPrefix = "Category : ";
		
		public AbsLevelAndCatNameHeaderView(){
			
			absLevelLabel = new Label(absLevelPrefix);
			catNameLabel = new Label(catPrefix);
			absLevel = new OntologyWordModel("");
			categoryName = new OntologyWordModel("");
			
			this.getChildren().addAll(absLevelLabel,catNameLabel);
			this.setPadding(new Insets(5));
			this.setSpacing(5);
		}

		public OntologyWordModel getAbsLevel() {
			return absLevel;
		}

		public void setAbsLevel(OntologyWordModel absLevel) {
			this.absLevel = absLevel;
			absLevelLabel.setText(absLevelPrefix+this.absLevel.getShortName());
		}

		public OntologyWordModel getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(OntologyWordModel categoryName) {
			this.categoryName = categoryName;
			catNameLabel.setText(catPrefix+categoryName.getShortName());
		}
	
}
