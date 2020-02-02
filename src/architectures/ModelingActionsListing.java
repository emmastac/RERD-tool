package architectures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import utils.Memory;

public class ModelingActionsListing {

	public static String ArchitectureInstantiation_ActionLabel = "ArchitectureInstantiation";
	private static String COMPONENTS_HOME = Memory.preferences.getComponentsCodePath( );
	private static File modelingActionsListingFile = new File( COMPONENTS_HOME + "/modelingActions.txt" );

	static ArrayList < String > modelingActions = new ArrayList < String >( );
	private static BufferedWriter bw;
	
	static{
		loadModelingActions();
	}


	public static void saveArchitectureInstantiationAction( String pathToSaveBIPFile , String pathToSaveConfFile ) {

		try {
			bw = new BufferedWriter( new FileWriter( modelingActionsListingFile, true ) );
			String recordArchitecture = ArchitectureInstantiation_ActionLabel + ";;;" + pathToSaveBIPFile + ";;;" + pathToSaveConfFile;
			modelingActions.add(recordArchitecture);
			bw.append( recordArchitecture+'\n' );
			bw.close( );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}


	public static void loadModelingActions( ) {

		if(!modelingActionsListingFile.exists( ))
			return;
		try {
			BufferedReader br = new BufferedReader( new FileReader( modelingActionsListingFile ) );
			String line = "";
			while((line= br.readLine( ))!=null){
				if(line.equals( "" ))
					continue;
				modelingActions.add(line);
			}
			br.close( );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}

}
