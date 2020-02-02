package architectures;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import architectures.ArchitecturePreparator.Operand;
import architectures.BufferManagementPreparator.ConsumerOperand;
import architectures.BufferManagementPreparator.ProdConOperand;
import architectures.BufferManagementPreparator.ProducerOperand;
import models.AbstractBoilerPlate;
import models.PropertyModel;

//import org.antlr.stringtemplate.StringTemplate;

public class Modes2Preparator extends ArchitecturePreparator {

	public Modes2Preparator() throws FileNotFoundException {

		super( "ModeManagement" );

	}

	class ModesOperand extends Operand {

		public ArrayList < String [ ] > m1Actions;
		public ArrayList < String [ ] > m2Actions;


		public ModesOperand() {

			m1Actions = new ArrayList < String [ ] >( );
			m2Actions = new ArrayList < String [ ] >( );
		}

	}


	protected Object prepareForTemplate( Operand operand ) {

		return operand;
	}


	protected Map < String , Map < String , Operand >> parseOperandsInfo( ArrayList < PropertyModel > properties ) {

		String patternStatic = "MD";
		Map < String , Map < String , Operand >> operands = new HashMap < String , Map < String , Operand >>( );
		Map < String , Operand > operandsP = new HashMap < String , Operand >( );
		Map < String , Operand > operandsG = new HashMap < String , Operand >( );
		Map < String , Operand > operandsB = new HashMap < String , Operand >( );
		operands.put( "operandsB" , operandsB );

		for ( PropertyModel prop : properties ) {
			AbstractBoilerPlate absBoil = prop.getPattern( );
			// parse instances of pattern MX1
			if ( absBoil.getIDNoNS( ).startsWith( patternStatic + "1" ) ) {
				// find the putPorts and endPorts
				int count = 0;
				for ( String part : absBoil.getRawComponents( ) ) {

					AbstractBoilerPlate subComponent = AbstractBoilerPlate.parseToBoilerplate( part );
					if ( subComponent != null ) {
						// System.out.println( "a " + subComponent );
						int index = 0;
						for ( String compPort : subComponent.getRawComponents( ) ) {
							// compPort is complex
							AbstractBoilerPlate portRef = AbstractBoilerPlate.parseToBoilerplate( compPort );

//							for ( String portRefComp : portRef.getRawComponents( ) ) {
//
//								String portName = getRawPartsContent( portRefComp , "Port" );
//								// System.out.println( "c " + portRefComp );
//								if ( !portName.equals( "" ) ) {
//									String compName = getRawPartsContent( portRef.getRawComponents( )[ index - 1 ] , "Component" );
//									System.out.println( "b " + compName );
//
//									ModesOperand op = new ModesOperand( );
//									op.componentName = compName;
//									op.componentType = getComponentType( compName );
//									op.operandClass = "B";
//									operandsP.put( compName , op );
//
//								}
//								index++;
//							}

						}
					}
					count++;
				}
			}
		}

		return operands;
	}

}
