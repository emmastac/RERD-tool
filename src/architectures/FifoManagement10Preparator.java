package architectures;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import models.PropertyModel;

//import org.antlr.stringtemplate.StringTemplate;

public class FifoManagement10Preparator extends ArchitecturePreparator {

	public FifoManagement10Preparator() throws FileNotFoundException {

		super( "FifoManagement10" );

	}


//	protected void parseOperandsInfo( Map < String , List < Map < String , String >>> operands ) {
//
//		parseOperandsInfo1( operands );
//
//	}
	protected Object prepareForTemplate( Operand operand ){
		return operand;
	}

//	private void parseOperandsInfo1( Map < String , List < Map < String , String >>> operands ) {
//
//		// for each operand we have its modes, with a list of begin ports
//		// Map<String, List<String>> modes = new HashMap<String,
//		// List<String>>();
//		// modes.put( "mode1", new ArrayList<String>() );
//		// modes.put( "mode2", new ArrayList<String>() );
//		// // add all the allowed ports in the associated mode
//		// modes.get( "mode1" ).add( "saveToMem" );
//		// modes.get( "mode2" ).add( "sendToGround" );
//
//		operands.put( "B" , new ArrayList < Map < String , String >>( ) );
//
//		//addOperandComponent( operands , "G" , new String [ ] [ ] { { "name" , "TCmanagement" } , { "get" , "getGround" }, { "put" , "put" }  } );
//
//	}
	
	protected Map  parseOperandsInfo( ArrayList<PropertyModel> properties  ){
		return null;
		
	}

}
