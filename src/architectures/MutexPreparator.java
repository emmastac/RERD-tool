package architectures;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.AbstractBoilerPlate;
import models.PropertyModel;

public class MutexPreparator extends ArchitecturePreparator {

	public MutexPreparator() throws FileNotFoundException {

		super( "Mutex" );
	}


//	protected void parseOperandsInfo( Map < String , List < Map < String , String >>> operands ) {
//
//		operands.put( "B" , new ArrayList < Map < String , String >>( ) );
//
//		addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" , "CMP1" } , { "begin" , "beg1" } , { "finish" , "fin1" } } );
//		addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" , "CMP2" } , { "begin" , "beg2" } , { "finish" , "fin2" } } );
//		addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" , "CMP3" } , { "begin" , "beg3" } , { "finish" , "fin3" } } );
//
//	}

	public class MutexOperand extends Operand {
		public String beginPort;
		public String finishPort;
		
		public MutexOperand(){
			super();
		}
	}


	protected Map parseOperandsInfo( ArrayList<PropertyModel> properties  ) {

		String patternStatic = "MX";
		Map < String , MutexOperand > operandsB = new HashMap < String , MutexOperand >( );
		Map < String , Map < String , MutexOperand > > operands = new HashMap < String , Map < String , MutexOperand > >( );
		operands.put( "operandsB" , operandsB );

		for ( PropertyModel prop : properties ) {
			AbstractBoilerPlate absBoil = prop.getPattern( );

			// parse instances of pattern MX1
			if ( absBoil.getIDNoNS( ).startsWith( patternStatic + "1" ) ) {
				// find the operand components
				for ( String part : absBoil.getRawComponents( ) ) {
					String compName = getRawPartsContent( part , "Component" );
					if ( !compName.equals( "" ) ) {
						MutexOperand op = new MutexOperand( );
						op.componentName = compName;
						// componentType should be retrieved from a Component instances' file.
						op.componentType = compName.toLowerCase( ); 
						operandsB.put( compName , op );
					}
				}
			}

			// parse instances of pattern MX1
			else if ( absBoil.getIDNoNS( ).startsWith( patternStatic + "2" ) ) {
				// find the beginPort and endPort
				int count = 0;
				for ( String part : absBoil.getRawComponents( ) ) {
					AbstractBoilerPlate subComponent = AbstractBoilerPlate.parseToBoilerplate( part );
					if ( subComponent != null ) {
						// System.out.println( "a " + subComponent );
						int index = 0;
						for ( String compPort : subComponent.getRawComponents( ) ) {
							String portName = getRawPartsContent( compPort , "Port" );
							// System.out.println( "a " + compPort );
							if ( !portName.equals( "" ) ) {
								String compName = getRawPartsContent( subComponent.getRawComponents( )[ index - 1 ] , "Component" );
								// System.out.println( "a "
								// +subComponent.getRawComponents( )[ index - 1
								// ]+" "+compName );

								if ( count == 0 ) {
									((MutexOperand) operandsB.get( compName )).beginPort = portName;
									count++;
								} else {
									((MutexOperand) operandsB.get( compName )).finishPort = portName;
								}
							}
							index++;

						}
					}
				}
			}
		}
		
//		operands.put( "B" , new ArrayList < Map < String , String >>( ) );
//
//		addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" , "CMP1" } , { "begin" , "beg1" } , { "finish" , "fin1" } } );
//		addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" , "CMP2" } , { "begin" , "beg2" } , { "finish" , "fin2" } } );
//		addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" , "CMP3" } , { "begin" , "beg3" } , { "finish" , "fin3" } } );
		return operands;
	}
	
	protected Object prepareForTemplate( Operand operand ){
		return operand;
	}

}
