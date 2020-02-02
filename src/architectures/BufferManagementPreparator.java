package architectures;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import models.AbstractBoilerPlate;
import models.PropertyModel;

public class BufferManagementPreparator extends ArchitecturePreparator {

	public BufferManagementPreparator() throws FileNotFoundException {

		super( "BufferManagement" );
	}

	public class ProducerOperand extends Operand {

		public String producePort;


		public ProducerOperand() {

			super( );
		}
	}

	public class ConsumerOperand extends Operand {

		public String consumePort;


		public ConsumerOperand() {

			super( );
		}
	}
	
	public class ProdConOperand extends Operand {

		public String consumePort;
		public String producePort;


		public ProdConOperand() {

			super( );
		}
	}


	protected Map < String , Map < String , Operand >> parseOperandsInfo( ArrayList < PropertyModel > properties ) {

		String patternStatic = "BM";
		Map < String , Map < String , Operand >> operands = new HashMap < String , Map < String , Operand >>( );
		Map < String , Operand > operandsP = new HashMap < String , Operand >( );
		Map < String , Operand > operandsG = new HashMap < String , Operand >( );
		Map < String , Operand > operandsB = new HashMap < String , Operand >( );
		operands.put( "operandsP" , operandsP );
		operands.put( "operandsG" , operandsG );
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

							for ( String portRefComp : portRef.getRawComponents( ) ) {

								String portName = getRawPartsContent( portRefComp , "Port" );
								//System.out.println( "c " + portRefComp );
								if ( !portName.equals( "" ) ) {
									String compName = getRawPartsContent( portRef.getRawComponents( )[ index - 1 ] , "Component" );
									System.out.println( "b " + compName );

									if ( count == 0 ) {
										ProducerOperand op = new ProducerOperand( );
										op.componentName = compName;
										op.componentType = getComponentType(compName);
										op.producePort = portName;
										op.operandClass = "P";
										operandsP.put( compName , op );

									} else {
										ConsumerOperand op = new ConsumerOperand( );
										op.componentName = compName;
										op.componentType = getComponentType(compName);
										op.consumePort = portName;
										op.operandClass = "G";
										operandsG.put( compName , op );
									}
								}
								index++;
							}

						}
					}
					count++;
				}
			}
		}
		
		// if same operand in P and G place them in B
		
		HashSet<String> bothNames = new HashSet<String>(operandsG.keySet( ));
		bothNames.retainAll( operandsP.keySet( ) );
		
		for( String name : bothNames ){
			ConsumerOperand consumer = (ConsumerOperand) operandsG.remove( name );
			ProducerOperand producer = (ProducerOperand) operandsP.remove( name );
			
			ProdConOperand both = new ProdConOperand();
			both.componentName = name;
			both.componentType = consumer.componentType;
			both.consumePort = consumer.consumePort;
			both.producePort = producer.producePort;
			both.operandClass = "B";
			operandsB.put( name , both );
		}

//		for ( Operand opx : operandsG.values( ) ) {
//			ConsumerOperand op = ( ConsumerOperand ) opx;
//			System.out.println( " G : " + op.componentName + " " + op.operandClass + " " + op.consumePort );
//		}
//
//		for ( Operand opx : operandsP.values( ) ) {
//			ProducerOperand op = ( ProducerOperand ) opx;
//			System.out.println( " P : " + op.componentName + " " + op.operandClass + " " + op.producePort );
//		}

		// operands.put( "B" , new ArrayList < Map < String , String >>( ) );
		//
		// addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" ,
		// "CMP1" } , { "begin" , "beg1" } , { "finish" , "fin1" } } );
		// addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" ,
		// "CMP2" } , { "begin" , "beg2" } , { "finish" , "fin2" } } );
		// addOperandComponent( operands , "B" , new String [ ] [ ] { { "name" ,
		// "CMP3" } , { "begin" , "beg3" } , { "finish" , "fin3" } } );
		return operands;
	}


	protected Object prepareForTemplate( Operand operand ) {

		return operand;
	}

}
