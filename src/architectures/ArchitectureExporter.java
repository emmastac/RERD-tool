package architectures;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.Architecture;
import models.PropertyModel;
import utils.Memory;
import ch.epfl.risd.archman.builder.ArchitectureInstantiator;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.BIPFileModel;

public class ArchitectureExporter {



	public void exportArchitecture( Architecture expArch ) throws IOException, ConfigurationFileException, ArchitectureExtractorException, ArchitectureBuilderException {

		String stringArchID = expArch.archID.toString( );

		// get all properties in Memory with this ArchID

		List < PropertyModel > props = Memory.getPropertiesInMemory( );
		ArrayList < PropertyModel > linkedProps = new ArrayList < PropertyModel >( );
		for ( PropertyModel prop : props ) {
			if ( prop.getArchitecture( ).toString( ).equals( expArch.toString( ) ) ) {
				// System.out.println("no "+ absBoil.getRawComponents( )[0]);
				linkedProps.add( prop );
			}
		}
		ArchitecturePreparator archPrep = null;
		switch ( expArch.getArchID( ) ) {
		case "MutualExclusion" :
			archPrep = exportMutex( linkedProps , expArch );
			break;
		case "BufferManagement" :
			archPrep = exportBufferManagement( linkedProps , expArch );
			break;
		case "ModeManagement" :
			archPrep = exportModeManagement( linkedProps , expArch );
			break;
		default :
			System.out.println( "Architecture not found " );
		}

		if ( archPrep != null ) {
			//invokeAMtool
			instantiateArchitecture(archPrep);
		}
		

	}

	public String[] instantiateArchitecture(ArchitecturePreparator archPrep) throws ConfigurationFileException, ArchitectureExtractorException, ArchitectureBuilderException, IOException{
		
		String archStylePath = archPrep.architDefinPath+"/utils/AEConf.txt";
		ArchitectureStyle architectureStyle = new ArchitectureStyle(archStylePath);
		
		String archOperandsPath = archPrep.outputPath+"/AEConf-instance.txt";
		ArchitectureOperands architectureOperands = new ArchitectureOperands(archOperandsPath);
;

		String pathToSaveBIPFile = archPrep.outputPath+"/example.bip";

		String pathToSaveConfFile = archPrep.outputPath+"/exampleconf.txt";
		try {
			ArchitectureInstance instance = ArchitectureInstantiator.createArchitectureInstance(architectureStyle,
					architectureOperands, archPrep.archName, archPrep.archName,  archPrep.archName.toLowerCase( ), pathToSaveBIPFile, pathToSaveConfFile);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return new String[]{ pathToSaveBIPFile,  pathToSaveConfFile};
	}

	private ArchitecturePreparator exportMutex( ArrayList < PropertyModel > linkedProps , Architecture arch ) throws IOException {

		MutexPreparator mxPrep = new MutexPreparator( );
		mxPrep.createArchitectureFiles( linkedProps , arch );
		return mxPrep;
	}
	
	private ArchitecturePreparator exportBufferManagement( ArrayList < PropertyModel > linkedProps , Architecture arch ) throws IOException {

		BufferManagementPreparator mxPrep = new BufferManagementPreparator( );
		mxPrep.createArchitectureFiles( linkedProps , arch );
		return mxPrep;
	}
	
	
	private ArchitecturePreparator exportModeManagement( ArrayList < PropertyModel > linkedProps , Architecture arch ) throws IOException {

		Modes2Preparator mxPrep = new Modes2Preparator( );
		mxPrep.createArchitectureFiles( linkedProps , arch );
		return mxPrep;
	}


}
