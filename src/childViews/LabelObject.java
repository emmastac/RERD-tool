package childViews;

import javafx.scene.control.Label;
import models.OntologyWordModel;
import utils.ExtStrings;

/**
 * This class is a pair of a class name (called [type]) and an instance name (called [name])
 * 
 * Visually, it is usually just a textbox where you see the instance name
 * 
 * The class name controls the accepted values in the text box 
 * Allowed input should match an instance named [name], belonging to a class named [type]
 * @author alkoclick
 *
 */
public class LabelObject extends Label implements SaveableObject{
	private static final long serialVersionUID = 1L;
	protected OntologyWordModel type;
	public static final String bottomDelimiter = ExtStrings.getString("SerializeBLPBottomDelimiter"); //$NON-NLS-1$
	
	public LabelObject(OntologyWordModel type) {
		super(type.getShortName());
		this.type = type;
	}


	@Override
	public String toString() {
		return this.type.getShortName()+" ";
	}

	@Override
	public String toBytes() {
		String serializedForm = "";
		// after a bottom level object, we place a delimiter 
		serializedForm += this.type + bottomDelimiter;
		return serializedForm;
	}


	public OntologyWordModel getType() {
		return type;
	}

}
