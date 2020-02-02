package models;

import javafx.beans.property.SimpleStringProperty;

public class ArchitectureStyle {

	public SimpleStringProperty archID;
	public String definition;


	public ArchitectureStyle ( ) {
		this.archID = new SimpleStringProperty( );
	}
	
	public void parse(String text){

		String [ ] tokens = text.split( "," );
			this.archID.set( tokens [ 0 ] );
			if ( tokens.length > 0 ) {
				this.definition = tokens [ 1 ];
			}
	}

}
