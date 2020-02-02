package models;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;

public class AbstractSpecificationModel implements Comparable<AbstractSpecificationModel>{
	public SimpleStringProperty artifact_id;
	public SimpleObjectProperty<OntologyWordModel> category;

	protected SimpleStringProperty displayForm;
	protected SimpleObjectProperty<Button> editButton;

	@Override
    public boolean equals(Object other) {
		if( this == other) {
			return true;
		}
		
		if(!(other instanceof AbstractSpecificationModel)) {
			return false;
		}
		
		AbstractSpecificationModel m = (AbstractSpecificationModel) other;
		
		if (this.getClass().equals(m.getClass()) && this.artifact_id.get().equals(m.artifact_id.get())) {
			return true;
		}

		return false;

	}

	@Override
	public int compareTo(AbstractSpecificationModel o) {
		return this.artifact_id.get().compareTo(o.artifact_id.get());
	}
}
