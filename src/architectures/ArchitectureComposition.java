package architectures;

import java.io.IOException;

import ch.epfl.risd.archman.composer.ArchitectureComposer;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.model.ArchitectureInstance;


public class ArchitectureComposition {
	
	public static void composeArchitectures(String pathToConfFile1, String pathToConfFile2){
		
		
		try {
			ArchitectureInstance instance1 = new ArchitectureInstance(pathToConfFile1, true);
			ArchitectureInstance instance2 = new ArchitectureInstance(pathToConfFile2, true);

			String systemName = "MutualExclusion123";
			String rootTypeName = "Mutex";
			String rootInstanceName = "mutex";

			String pathToSaveBIPFile = "/home/vladimir/Architecture_examples/Compose/MutualExclusion123.bip";
			String pathToSaveConfFile = "/home/vladimir/Architecture_examples/Compose/ConfFile.txt";

			ArchitectureComposer.compose(instance1, instance2, systemName, rootTypeName, rootInstanceName,
					pathToSaveBIPFile, pathToSaveConfFile);

		} catch (ConfigurationFileException | ArchitectureExtractorException | InvalidComponentNameException
				| InvalidConnectorTypeNameException | InvalidPortParameterNameException
				| IllegalPortParameterReferenceException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
