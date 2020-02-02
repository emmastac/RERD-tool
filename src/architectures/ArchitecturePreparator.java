package architectures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import models.Architecture;
import models.PropertyModel;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import utils.Memory;

public abstract class ArchitecturePreparator {

	public String architDefinPath;
	public String buildFile;
	public String archName;
	public String outputPath;
	public StringTemplateGroup group;

	private static String COMPONENTS_HOME = Memory.preferences.getComponentsCodePath( );
	private static String ARCH_HOME = Memory.preferences.getArchitecturesDefinPath( );
	private static String ARCH_OUT_HOME = Memory.preferences.getArchitecturesInstancesPath( );

	public static Map <String , String> instantiatedComponentNames = new HashMap <String , String>( );
	public static Map <Integer , String[]> instantiatedArchitectures = new HashMap <Integer , String[]>( );


	public ArchitecturePreparator ( String archName ) throws FileNotFoundException {

		this.archName = archName;
		// templateFile = "architectureDefinitions/" + getArchitectureName() +
		// "/BIParchitecture.stg";
		architDefinPath = ARCH_HOME + "/" + this.archName;
		group = new StringTemplateGroup( new FileReader( architDefinPath + "/BIParchitecture.stg" ) , DefaultTemplateLexer.class );
	}


	public static void loadComponents(){
		instantiatedComponentNames = new HashMap <String , String>( );
		try {
			BufferedReader br = new BufferedReader( new FileReader( COMPONENTS_HOME + "/global.txt" ) );

			String line = "";

			while ( ( line = br.readLine( ) ) != null ) {
				if ( line.startsWith( "component " ) ) {
					String [ ] parts = line.split( "\\s+" );
					instantiatedComponentNames.put( parts [ 2 ] , parts [ 1 ] );
				}
			}
			br.close( );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}
	
	
	private static void saveArchitectures(){
		ArrayList<Integer> sort = new ArrayList<Integer>(instantiatedArchitectures.keySet( ));
		Collections.sort( sort );
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter( COMPONENTS_HOME + "/modelingActions.txt" ) );

			for(int i : sort){
				String line = "ArchitectureInstantiation;;;"+instantiatedArchitectures.get( i )[1];
				line += ";;;"+instantiatedArchitectures.get( i )[2]; 
				bw.write( line+'\n' );
			}
			
			
			bw.close( );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}
	
	public static void addArchitecture(String name, String path1, String path2){
		for( String[] entry: instantiatedArchitectures.values() ){
			if( name.equals( entry[0] ) ){
				return;
			}
		}
		instantiatedArchitectures.put(instantiatedArchitectures.keySet( ).size( )+1, new String[]{name, path1,path2});
		
		saveArchitectures();
	}
	
	public static void removeArchitecture(int index ){
			instantiatedArchitectures.remove( index );
			saveArchitectures();
	}
	
	public static void loadArchitectures(){
		instantiatedArchitectures = new HashMap <Integer , String[]>( );
		try {
			BufferedReader br = new BufferedReader( new FileReader( COMPONENTS_HOME + "/modelingActions.txt" ) );

			String line = "";

			while ( ( line = br.readLine( ) ) != null ) {
				if ( line.startsWith( "ArchitectureInstantiation" ) ) {
					String [ ] parts = line.split( ";;;" );
					String name = parts[1];
					name = name.replace( Memory.preferences.getArchitecturesInstancesPath( )+"/" , "" );
					name = name.split( "/" )[0];
					instantiatedArchitectures.put( instantiatedArchitectures.keySet( ).size( )+1 , new String[]{ name ,parts [ 1 ] , parts [ 2 ]} );
				}
			}
			br.close( );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}
	
	public static String getComponentType ( String name ) {
		loadComponents();
		return instantiatedComponentNames.get( name );

	}

	public class Operand {

		public String componentName;
		public String componentType;
		public String operandClass = "";
		public String code = "";
	}


	public void createArchitectureFiles ( ArrayList <PropertyModel> properties , Architecture arch ) throws IOException {

		outputPath = ARCH_OUT_HOME + "/" + arch;
		createDirectoriesForInstance( );

		HashSet <String> ports = new HashSet <String>( );

		Map <String , Map <String , Operand>> operands = parseOperandsInfo( properties );

		// create each Operand file
		readOperandsCode( COMPONENTS_HOME , operands );

		readOperandPortDefinitions( COMPONENTS_HOME , operands , ports );
		// create combined Operands file String ruleName = "Operands";
		String operandsFilePath = createOperandsComposition( outputPath , operands , ports , archName );

		// create
		createAEConfInstance( outputPath , operands , operandsFilePath );

	}


	/**
	 * Returns the content of the serialized raw String if the raw String is of
	 * type partType. The search of a partType may or may be NS-agnostic.
	 * 
	 * @param raw
	 * @param partType
	 * @return raw's content
	 */
	protected String getRawPartsContent ( String raw , String partType ) {

		if ( !raw.contains( ":" ) ) {
			return "";
		}

		// if query has not NS, then remove it from raw
		if ( !partType.contains( ":" ) ) {
			String [ ] parts = raw.split( ":" , 2 );
			raw = parts [ 1 ];
		}

		if ( !raw.startsWith( partType + "::" ) ) {
			// System.out.println( "get " + partType + " " + raw );
			return "";
		}

		return raw.replace( partType + "::" , "" ).trim( );
	}


	protected String createOperandsComposition ( String outputPath , Map <String , Map <String , Operand>> operands , HashSet <String> ports , String archName ) {

		outputPath = outputPath + "/" + archName + "Operands.bip";
		StringTemplate template = getTemplate( "OperandsComposition" );

		HashSet <String> operands_added = new HashSet <String>( );

		for ( String operandClass : operands.keySet( ) ) {
			for ( String operandName : operands.get( operandClass ).keySet( ) ) {
				// template.setAttribute( "operands" + operandName ,
				// operands.get(
				// operandName ) );
				if ( operands_added.add( operandName ) ) {
					template.setAttribute( "operands" , operands.get( operandClass ).get( operandName ) );
				}
			}
		}

		for ( String port : ports ) {
			template.setAttribute( "ports" , port );
		}

		String out = template.toString( );
		printToFile( out , outputPath );
		return outputPath;
	}


	/**
	 * Creates new directories for putting the files generated for architecture
	 * instantiation.
	 * 
	 * @throws IOException
	 */
	private void createDirectoriesForInstance ( ) throws IOException {

		File archInstancePath = new File( outputPath );
		// Delete previously created BIP architecture
		if ( !archInstancePath.exists( ) ) {
			try {
				Files.createDirectory( archInstancePath.toPath( ) );
			} catch ( IOException e ) {
				e.printStackTrace( );
			}
		}
	}


	// /**
	// * Creates a map with an operand's attributes and adds it to the map of
	// * operands.
	// *
	// * @param operands
	// * @param compName
	// * @param attrValues
	// */
	// protected void addOperandComponent( Map < String , List < Map < String ,
	// String >>> operands , String compName , String [ ][ ] attrValues ) {
	//
	// Map < String , String > newOperandsMap = new HashMap < String , String >(
	// );
	// for ( String [ ] pair : attrValues ) {
	// newOperandsMap.put( pair[ 0 ] , pair[ 1 ] );
	// }
	// operands.get( compName ).add( newOperandsMap );
	//
	// }

	protected void readOperandsCode ( String componentsPath , Map <String , Map <String , Operand>> operands ) {

		HashSet <String> operands_added = new HashSet <String>( );

		for ( Map <String , Operand> operandClassMap : operands.values( ) ) {
			for ( Operand operand : operandClassMap.values( ) ) {

				// e.g. name="CMP1"
				String name = operand.componentName;

				// read the operand's bip code and add it to template
				try {
					if ( operands_added.add( name ) ) {
						String modelCode = new String( Files.readAllBytes( Paths.get( componentsPath + "/" + name + ".txt" ) ) , StandardCharsets.UTF_8 );
						operand.code = modelCode;
						System.out.println( Paths.get( " a " + componentsPath + "/" + name + ".txt" ) );
						System.out.println( " b " + modelCode );
					}

				} catch ( IOException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace( );
				}
			}
		}

	}


	/**
	 * Prints a string to a file.
	 * 
	 * 
	 * @param out
	 *            the string to be printed
	 * @param outputPath
	 *            the path of the file to which the string is written
	 */
	protected void printToFile ( String out , String outputPath ) {

		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter( outputPath ) );
			bw.write( out );
			bw.close( );
		} catch ( IOException e ) {
			e.printStackTrace( );
		}
	}


	protected StringTemplate getTemplate ( String tmplName ) {

		return group.getInstanceOf( tmplName );
	}


	protected void readOperandPortDefinitions ( String componentsPath , Map <String , Map <String , Operand>> operands , HashSet <String> ports ) {

		for ( Map <String , Operand> operandClassMap : operands.values( ) ) {
			for ( Operand operand : operandClassMap.values( ) ) {

				// e.g. name="CMP1"
				String name = operand.componentName;

				// read the operand's bip code and add it to template

				BufferedReader br;
				try {
					br = new BufferedReader( new FileReader( componentsPath + "/" + name + "_ports.txt" ) );

					String line = "";

					while ( ( line = br.readLine( ) ) != null ) {
						line = line.trim( );
						if ( line.equals( "" ) ) {
							continue;
						}

						ports.add( line );
					}

				} catch ( FileNotFoundException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace( );
				} catch ( IOException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace( );
				}

			}
		}

	}


	// protected abstract void parseOperandsInfo( Map < String , List < Map <
	// String , String >>> operands );

	protected abstract Map parseOperandsInfo ( ArrayList <PropertyModel> properties );


	protected abstract Object prepareForTemplate ( Operand operand );


	protected void createAEConfInstance ( String outputPath , Map <String , Map <String , Operand>> operands , String operandsFilePath ) {

		outputPath = outputPath + "/AEConf-instance.txt";
		// File portConnTypesFile = new File(portConnTypesPath);
		StringTemplate template = getTemplate( "AEConfInstance" );
		for ( Map <String , Operand> operandClassMap : operands.values( ) ) {
			for ( Operand operand : operandClassMap.values( ) ) {
				// template.setAttribute( "operands" + operandName ,
				// operands.get(
				// operandName ) );
				template.setAttribute( "operands" + operand.operandClass , this.prepareForTemplate( operand ) );
			}
		}
		template.setAttribute( "path" , operandsFilePath );
		String out = template.toString( );

		printToFile( out , outputPath );
	}

}
