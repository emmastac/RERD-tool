package mainPackage;
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import models.Architecture;
import utils.Memory;
import utils.OntologyIO;
import architectures.ArchitectureComposition;
import architectures.ArchitectureExporter;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;


public class Test extends Application{

	public static void main(String[] args) throws IOException {
		
		
		OntologyIO jtest = new OntologyIO();
		OntologyIO.init(); // Initialize ontology models
		
		Memory.load();
		testArchs();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
	}

	public static void testArchs(){
		try {
			Architecture arch1 = new Architecture( "TCFifo-BufferManagement" );
			new ArchitectureExporter().exportArchitecture( arch1 );
			
			Architecture arch2 = new Architecture( "TCmngtCapacity-ModeManagement" );
			new ArchitectureExporter().exportArchitecture( arch2 );
			
			ArchitectureComposition.composeArchitectures( arch1.outputConfPath.get( ) , arch2.outputConfPath.get( ) );
		} catch ( ConfigurationFileException | ArchitectureExtractorException | ArchitectureBuilderException | IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void TestComp(){
		String pathToConfFile1 = "/home/vladimir/Architecture_examples/Compose/Conf12.txt";
		String pathToConfFile2 = "/home/vladimir/Architecture_examples/Compose/Conf13.txt";
		
		ArchitectureComposition.composeArchitectures( pathToConfFile1 , pathToConfFile2 );

	}
	
}
