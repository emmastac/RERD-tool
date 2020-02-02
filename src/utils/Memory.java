package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import models.AbstractBoilerPlate;
import models.Architecture;
import models.ArchitectureStyle;
import models.GenericBoilerPlate;
import models.OntologyWordModel;
import models.PropertyModel;
import models.PropertyPattern;
import models.RequirementModel;

/**
 * This class is used to share information globally in the program
 * 
 * @author alkoclick
 *
 */

public class Memory {

	public static Preferences preferences = new Preferences();
	public static final String PREFERENCES_FILE = ExtStrings.getString( "PreferencesFile" );
	public static final String REQ_BLP_FILE = ExtStrings.getString( "ReqBlpFile" );
	public static final String PRP_PTRN_FILE = ExtStrings.getString( "PrpPatrnFile" );
	public static final String ARCH_FILE = ExtStrings.getString( "ArchSaveFile" );
	static final String ID_SEPARATOR = ExtStrings.getString( "IDSep" );
	static final String COMP_SEPARATOR = ExtStrings.getString( "CompSep" );
	static final String ARCH_DELIMITER = ExtStrings.getString( "ArchDelimiter" );
	static final String BOTTOM_DELIMITER = ExtStrings.getString( "SerializeBLPBottomDelimiter" );
	static final String MIDDLE_DELIMITER = ExtStrings.getString( "SerializeBLPMiddleDelimiter" );
	private static final String CSV_REQ_HEADER = ExtStrings.getString( "RequirementCSVHeader" );
	private static final String CSV_PRP_HEADER = ExtStrings.getString( "PropertiesCSVHeader" );
	private static final String CSV_CTL_HEADER = ExtStrings.getString( "CTLFormulasCSVHeader" );
	private static final Logger logger = Logger.getLogger( Memory.class.getName( ) );

	// All boilerPlate prefix, main and suffix parts currently loaded
	private static HashMap < String , String [ ] > prefixTemplates;
	private static HashMap < String , String [ ] > bodyTemplates;
	private static HashMap < String , String [ ] > suffixTemplates;

	// Abstraction levels and categories
	private static HashMap < OntologyWordModel , ArrayList < OntologyWordModel >> abstractionLevelsAndCategoriesTopLevel;
	private static HashMap < OntologyWordModel , ArrayList < OntologyWordModel >> abstractionLevelsAndCategoriesFlat;

	// Helper sets to avoid excessive IOs
	private static HashSet < RequirementModel > newReqs;
	private static HashSet < PropertyModel > newProps;
	private static HashSet < RequirementModel > reqsForDelete;
	private static HashSet < PropertyModel > propsForDelete;

	// The main model of ontology loaded into memory
	private static ObservableList < RequirementModel > requirementsInMemory;
	// private static ObservableList<RequirementModel>
	// requirementsLoadedFromOnt;
	private static ObservableList < PropertyModel > propertiesInMemory;
	private static HashMap < String , String > boilerplatesInMemory; // BlpId -
																		// SerializedFrom
	private static HashMap < String , String > patternsInMemory;
	// Bottom Level Object Type (with namespace) - Suggestions (loaded from ont)
	private static HashMap < String , HashSet < String >> suggestionsInMemory;

	// ideally these should move to a new Memory class?
	private static HashMap < String , PropertyPattern > patternTemplates;
	private static HashMap < String , PropertyPattern > archPatternTemplates;
	private static HashMap < String , ArrayList < AbstractBoilerPlate >> syntax;

	// Architectures
	private static HashMap < String , Architecture > architectures;
	private static HashMap < String , ArchitectureStyle > baseArchIDs;
	private static int ArchitecturesCounter = 1;
	private static HashSet flatClasses;


	public static int nextArchitecturesCounter( ) {

		return ArchitecturesCounter;
	}


	/**
	 * when loading architectures, update the ArchitecturesCounter to the next
	 * value of the maximum counter of already instantiated Architectures.
	 * 
	 * @return
	 */
	public static void updateArchitecturesCounter( int counterFound ) {

		ArchitecturesCounter = Math.max( ArchitecturesCounter , counterFound + 1 );
	}


	/**
	 * This method fills all maps with entries for main, prefix, suffix
	 * templates. Also it fills the maps which associate templates to specific
	 * categories and bodies.
	 */
	public static void load( ) {

		initVars( );
		loadClassNames( );

		// Load Syntax Files
		loadRequirementBoilerPlateFile( );
		loadPropertyPatternFile( );
		loadArchitectureFile( );

		// Load contents
		loadComplexObjects( );
		loadBlpInstances( );
		loadPatterns( );
		preferences.importPreferencesFromFile( );

		requirementsInMemory.addAll( OntologyIO.loadRequirementsFromOntology( ) );
		propertiesInMemory.addAll( OntologyIO.loadPropertiesFromOntology( ) );

		loadAbstractionLevelsAndCategories( );

		requirementsInMemory.addListener( new ListChangeListener < RequirementModel >( ) {

			@Override
			public void onChanged( ListChangeListener.Change < ? extends RequirementModel > c ) {

				c.next( );
				c.getAddedSubList( ).forEach( ( reqMdl ) -> {
					newReqs.add( reqMdl );
				} );
				c.getRemoved( ).forEach( ( reqMdl ) -> {
					reqsForDelete.add( reqMdl );
				} );
			}
		} );
		propertiesInMemory.addListener( new ListChangeListener < PropertyModel >( ) {

			@Override
			public void onChanged( ListChangeListener.Change < ? extends PropertyModel > c ) {

				c.next( );
				c.getAddedSubList( ).forEach( propMdl -> {
					newProps.add( propMdl );
				} );
				c.getRemoved( ).forEach( propMdl -> {
					propsForDelete.add( propMdl );
				} );
			}
		} );
	}


	private static void loadClassNames( ) {

		for ( OntologyWordModel ontModel : OntologyIO.getClassesFlat( ) ) {
			flatClasses.add( ontModel.getFullName( ).replaceAll( ":" , "." ) );
		}
	}


	protected static void initVars( ) {

		// Initialize all vars
		prefixTemplates = new HashMap <>( ); // Read from text input
		bodyTemplates = new HashMap <>( ); // Read from text input
		suffixTemplates = new HashMap <>( ); // Read from text input
		patternTemplates = new HashMap <>( ); // Read from text input
		archPatternTemplates = new HashMap <>( ); // Read from text input
		syntax = new HashMap <>( ); // Read from text input
		requirementsInMemory = FXCollections.observableArrayList( );
		// requirementsLoadedFromOnt = FXCollections.observableArrayList();
		propertiesInMemory = FXCollections.observableArrayList( );
		boilerplatesInMemory = new HashMap <>( );
		patternsInMemory = new HashMap <>( );
		suggestionsInMemory = new HashMap <>( );
		architectures = new HashMap <>( );
		newReqs = new HashSet <>( );
		newProps = new HashSet <>( );
		reqsForDelete = new HashSet <>( );
		propsForDelete = new HashSet <>( );
		baseArchIDs = new HashMap <>( );
		flatClasses = new HashSet( );
	}


	/**
	 * Adds a new Item to a HashMap of HashSet<String>. Useful for adding items
	 * to HashMaps like category2Prefixes, bodies2Suffixes etc. In case the
	 * category doesn't exist, it creates a new entry for that category in the
	 * HashMap and initializes the HashSet, then adds the item. In any other
	 * case it will just add the item normally, like you would get by calling
	 * inputHashMap.get(category).add(newItemID)
	 * 
	 * @param inputHashMap
	 * @param category
	 * @param newItemID
	 */
	public static void addTo( HashMap < String , HashSet < String >> inputHashMap , String category , String newItemID ) {

		if ( !inputHashMap.containsKey( category ) ) {
			inputHashMap.put( category , new HashSet <>( ) );
		}
		inputHashMap.get( category ).add( newItemID );
	}


	private static void loadBlpInstances( ) {

		boilerplatesInMemory = OntologyIO.getBlpInstances( );
	}


	private static void loadPatterns( ) {

		patternsInMemory = OntologyIO.getPatternInstances( );
	}


	private static void loadComplexObjects( ) {

		OntologyIO.loadBlpsComplex( ).forEach( cmplx -> {
			addSyntax( new AbstractBoilerPlate( cmplx ) );
		} );
		OntologyIO.loadPtrnsComplex( ).forEach( cmplx -> {
			addSyntax( new AbstractBoilerPlate( cmplx ) );
		} );
	}


	protected static void loadArchitectureFile( ) {

		List < String > lines = Utils.reader( ARCH_FILE );

		if ( !lines.isEmpty( ) ) {
			baseArchParser( lines );
		}
	}


	private static void baseArchParser( List < String > textInput ) {

		ArchitectureStyle archStyle;

		for ( String line : textInput ) {
			archStyle = new ArchitectureStyle( );
			String [ ] tokens = line.split( "," );
			archStyle.archID.set( tokens[ 0 ] );
			if ( tokens.length > 0 ) {
				archStyle.definition = tokens[ 1 ];
			}

			baseArchIDs.put( archStyle.archID.getValue( ) , archStyle );
		}
	}


	public static void loadRequirementBoilerPlateFile( ) {

		List < String > lines = Utils.reader( REQ_BLP_FILE );

		if ( !lines.isEmpty( ) ) {
			prefixTemplates = new HashMap < String , String [ ] >( ); // Read
																		// from
																		// text
																		// input
			bodyTemplates = new HashMap < String , String [ ] >( ); // Read from
																	// text
																	// input
			suffixTemplates = new HashMap < String , String [ ] >( ); // Read
																		// from
																		// text
																		// input
			reqBlpParser( lines );
		}
	}


	private static void reqBlpParser( List < String > textInput ) {

		final String PREFIXES_TITLE = ExtStrings.getString( "BlpPrfxHeader" );
		final String BODIES_TITLE = ExtStrings.getString( "BlpMnBdHeader" );
		final String SUFFIXES_TITLE = ExtStrings.getString( "BlpSffxHeader" );
		final String SNTX_TITLE = ExtStrings.getString( "SntxTitle" );
		String currentEdit = "";

		if ( textInput.size( ) == 0 )
			return;

		for ( String line : textInput ) {
			if ( line.equals( PREFIXES_TITLE ) || line.equals( BODIES_TITLE ) || line.equals( SUFFIXES_TITLE ) || line.equals( SNTX_TITLE ) ) {
				currentEdit = line;
			}
			if ( line.contains( ID_SEPARATOR ) ) {
				String [ ] tokens = line.split( ID_SEPARATOR , 2 );
				String id = tokens[ 0 ].trim( );
				String content = tokens[ 1 ].trim( );

				if ( SNTX_TITLE.equals( currentEdit ) ) {
					logger.log( Level.FINER , new StringBuilder( ).append( "Adding BLP " ).append( line ).append( " to syntax" ).toString( ) );
					ArrayList < AbstractBoilerPlate > definitions = syntax.containsKey( id ) ? syntax.get( id ) : new ArrayList <>( );
					definitions.add( new AbstractBoilerPlate( id , splitAndClean( content ) ) );
					syntax.put( id , definitions );
				} else if ( PREFIXES_TITLE.equals( currentEdit ) ) {
					loadPrefix( id , splitAndClean( content ) );
				} else if ( BODIES_TITLE.equals( currentEdit ) ) {
					loadBody( id , splitAndClean( content ) );
				} else if ( SUFFIXES_TITLE.equals( currentEdit ) ) {
					loadSuffix( id , splitAndClean( content ) );
				}
			}
		}
	}


	private static String [ ] splitAndClean( String input ) {

		if ( !input.contains( COMP_SEPARATOR ) ) // Handle inputs with only one
													// component
			return new String [ ] { input };
		String [ ] tokens = input.split( COMP_SEPARATOR );
		for ( int i = 0 ; i < tokens.length ; i++ ) {
			tokens[ i ] = tokens[ i ].trim( );
		}
		return tokens;
	}


	public static void loadPropertyPatternFile( ) {

		List < String > lines = Utils.reader( PRP_PTRN_FILE );

		if ( lines.size( ) > 0 ) {
			propPtrnParser( lines );
		}
	}


	private static void propPtrnParser( List < String > lines ) {

		final String BEH_TITLE = ExtStrings.getString( "PtrnBhvrTitle" );
		final String ARCH_TITLE = ExtStrings.getString( "PtrnArchTitle" );
		final String SNTX_TITLE = ExtStrings.getString( "SntxTitle" );

		String currentEdit = "";

		if ( lines.isEmpty( ) )
			return;

		for ( String line : lines ) {

			if ( line.equals( BEH_TITLE ) || line.equals( ARCH_TITLE ) || line.equals( SNTX_TITLE ) ) {
				currentEdit = line;
			}

			if ( line.contains( ID_SEPARATOR ) ) {
				String [ ] tokens = line.split( ID_SEPARATOR , 2 );
				String id = tokens[ 0 ].trim( );
				String content = tokens[ 1 ].trim( );

				if ( SNTX_TITLE.equals( currentEdit ) ) {
					logger.log( Level.FINER , new StringBuilder( ).append( "Adding PTRN " ).append( line ).append( " to syntax" ).toString( ) );
					ArrayList < AbstractBoilerPlate > definitions = syntax.containsKey( id ) ? syntax.get( id ) : new ArrayList <>( );
					definitions.add( new AbstractBoilerPlate( id , splitAndClean( content ) ) );
					syntax.put( id , definitions );
				} else if ( BEH_TITLE.equals( currentEdit ) ) {
					patternTemplates.put( id , new PropertyPattern( id , splitAndClean( content ) ) );
				} else if ( ARCH_TITLE.equals( currentEdit ) ) {
					archPatternTemplates.put( id , new PropertyPattern( id , splitAndClean( content ) ) );
				}
			}
		}
	}


	public static void loadAbstractionLevelsAndCategories( ) {

		abstractionLevelsAndCategoriesTopLevel = new HashMap <>( );
		abstractionLevelsAndCategoriesFlat = new HashMap <>( );

		for ( OntologyWordModel absLevel : OntologyIO.getAbstractionLevels( ) ) {
			ArrayList < OntologyWordModel > categories = OntologyIO.getCategoriesForAbstractionLevelTopLevel( absLevel.getFullName( ) );
			abstractionLevelsAndCategoriesTopLevel.put( absLevel , categories );
			ArrayList < OntologyWordModel > flatCategories = OntologyIO.getCategoriesForAbstractionLevel( absLevel.getFullName( ) );
			flatCategories.sort( new Comparator < OntologyWordModel >( ) {

				@Override
				public int compare( OntologyWordModel o1 , OntologyWordModel o2 ) {

					return o1.getShortName( ).compareTo( o2.getShortName( ) );
				}
			} );
			abstractionLevelsAndCategoriesFlat.put( absLevel , flatCategories );
		}
	}


	/**
	 * Adds a body template to all the maps that it's supposed to be added.
	 * 
	 * @param id
	 * @param rawComponents
	 */
	public static void loadBody( String id , String... rawComponents ) {

		bodyTemplates.put( id , rawComponents );
	}


	/**
	 * Adds a prefix template to all the maps that it's supposed to be added.
	 * 
	 * @param id
	 * @param rawComponents
	 */
	public static void loadPrefix( String id , String... rawComponents ) {

		prefixTemplates.put( id , rawComponents );
	}


	/**
	 * Adds a suffix template to all the maps that it's supposed to be added.
	 * 
	 * @param id
	 * @param rawComponents
	 */
	public static void loadSuffix( String id , String... rawComponents ) {

		suffixTemplates.put( id , rawComponents );
	}


	public static void export( ) throws IOException {

		flushPatternsAndWriteToOnt( );
		flushBoilerPlatesAndWriteToOnt( );
		saveAllPropertiesToOntology( );
		saveAllRequirementsToOntology( );
		OntologyIO.write( );
	}


	public static void flushBoilerPlatesAndWriteToOnt( ) {

		boilerplatesInMemory.clear( );
		ArrayList < AbstractBoilerPlate > allBlps = new ArrayList <>( );
		if ( !requirementsInMemory.isEmpty( ) ) {
			for ( RequirementModel reqModel : requirementsInMemory ) {
				reqModel.saveToList( allBlps );
			}
		}
		// We do this so we can continue editing normally in this session
		allBlps.forEach( blp -> addBoilerPlate( blp ) );
		OntologyIO.addAllBoilerplatesToOntology( allBlps );
	}


	public static void flushPatternsAndWriteToOnt( ) {

		patternsInMemory.clear( );
		ArrayList < AbstractBoilerPlate > allPtrns = new ArrayList <>( );
		if ( !propertiesInMemory.isEmpty( ) ) {
			for ( PropertyModel propertyModel : propertiesInMemory ) {
				propertyModel.saveToList( allPtrns );
			}
		}
		// We do this so we can continue editing normally in this session
		allPtrns.forEach( ptrn -> addPattern( ptrn ) );
		OntologyIO.addAllPatternsToOntology( allPtrns );
	}


	public static void saveAllPropertiesToOntology( ) {

		// Newly created properties
		for ( PropertyModel propModel : newProps ) {
			try {
				exportPropertyToOntology( propModel );
			} catch ( Exception e ) {
				logger.log( Level.WARNING , "Error writing property " + propModel.getPropID( ) + " to ontology" , e );
			}
		}

		// Properties for deletion
		for ( PropertyModel reqModel : propsForDelete ) {
			OntologyIO.removePropertyFromOntology( reqModel.getPropIDNoNS( ) );
		}

		// Properties for update
		propertiesInMemory.removeAll( newProps );
		if ( propertiesInMemory != null && !propertiesInMemory.isEmpty( ) ) {
			for ( PropertyModel propModel : propertiesInMemory ) {
				try {
					OntologyIO.removePropertyFromOntology( propModel.getPropIDNoNS( ) );
					exportPropertyToOntology( propModel );
				} catch ( Exception e ) {
					logger.log( Level.WARNING , "Error updating property " + propModel.getPropIDNoNS( ) + " in ontology" , e );
				}
				;
			}
		}
	}


	public static void saveAllRequirementsToOntology( ) {

		// Newly created requirements
		for ( RequirementModel reqModel : newReqs ) {
			try {
				exportRequirementToOntology( reqModel );
			} catch ( Exception e ) {
				logger.log( Level.WARNING , "Error writing requirement " + reqModel.getReqID( ) + " to ontology" );
			}
		}

		// Requirements for deletion
		for ( RequirementModel reqModel : reqsForDelete ) {
			try {
				OntologyIO.removeRequirementFromOntology( reqModel.getReqIDNoNS( ) );
			} catch ( IOException e ) {
				logger.log( Level.WARNING , "Error removing requirement " + reqModel.getReqIDNoNS( ) + " from ontology" );
			}
		}

		// Requirements for update
		requirementsInMemory.removeAll( newReqs );
		if ( requirementsInMemory != null && !requirementsInMemory.isEmpty( ) ) {
			for ( RequirementModel reqModel : requirementsInMemory ) {
				try {
					OntologyIO.removeRequirementFromOntology( reqModel.getReqIDNoNS( ) );
					exportRequirementToOntology( reqModel );
				} catch ( Exception e ) {
					e.printStackTrace( );
					logger.log( Level.WARNING , "Error updating requirement " + reqModel.getReqIDNoNS( ) + " in ontology" );
				}
				;
			}
		}
	}


	protected static void exportPropertyToOntology( PropertyModel propModel ) throws IOException {

		String propID = propModel.getPropIDNoNS( );
		String patternName = "";
		if ( propModel.getPattern( ) != null ) {
			PropertyPattern main = propModel.getPattern( );
			main.genPropID( );
			patternName = main.getIDNoNS( );
			if ( !patternName.endsWith( propID ) )
				patternName += "-" + propID;
			OntologyIO.addPatternToOntology( patternName , main.getCatNoNS( ) , main.getContentMap( ) , main.serialize( ) );
		}
		OntologyIO.addPropertyToOntology( patternName , propID , propModel.getCatNoNS( ) , propModel.getStatusModel( ).getCurrentStatus( ) ,
				propModel.getDerivedBy( ) , propModel.getArchitecture( ).serialize( ) );
	}


	protected static void exportRequirementToOntology( RequirementModel reqModel ) throws IOException {

		String reqID = reqModel.getReqIDNoNS( );
		String mainName = "";
		ArrayList < String > prefixes = new ArrayList <>( );
		ArrayList < String > suffixes = new ArrayList <>( );
		if ( !reqModel.getPrefixProperties( ).isEmpty( ) ) {
			for ( GenericBoilerPlate pref : reqModel.getPrefixProperties( ) ) {
				pref.genBlpID( );
				String prefixName = "";
				prefixName = pref.getIDNoNS( );
				if ( !prefixName.endsWith( reqID ) )
					prefixName += "-" + reqID;
				OntologyIO.addPrefixToOntology( prefixName , pref.getCatNoNS( ) , pref.getContentMap( ) , pref.serialize( ) );
				prefixes.add( prefixName );
			}

		}
		if ( reqModel.getMainBody( ) != null ) {
			GenericBoilerPlate main = reqModel.getMainBody( );
			main.genBlpID( );
			mainName = main.getIDNoNS( );
			if ( !mainName.endsWith( reqID ) )
				mainName += "-" + reqID;
			OntologyIO.addMainToOntology( mainName , main.getCatNoNS( ) , main.getContentMap( ) , main.serialize( ) , reqModel.getAbstract( ) );
		}
		if ( !reqModel.getSuffixProperties( ).isEmpty( ) ) {
			for ( GenericBoilerPlate suffix : reqModel.getSuffixProperties( ) ) {
				suffix.genBlpID( );
				String suffixName = "";
				suffixName = suffix.getIDNoNS( );
				if ( !suffixName.endsWith( reqID ) )
					suffixName += "-" + reqID;
				OntologyIO.addSuffixToOntology( suffixName , suffix.getCatNoNS( ) , suffix.getContentMap( ) , suffix.serialize( ) );
				suffixes.add( suffixName );
			}
		}
		OntologyIO.addRequirementToOntology( prefixes , mainName , suffixes , reqID , reqModel.getCatID( ).getShortName( ) , reqModel.getAbsLevel( )
				.getShortName( ) , reqModel.getStatusModel( ).getCurrentStatus( ) , reqModel.getRefines( ) , reqModel.getIsRefinedBy( ) , reqModel
				.getConcretizes( ) , reqModel.getIsConcretizedBy( ) , reqModel.getAbstract( ) );
	}

	/**
	 * Adds a requirement in the memory requirements, if it does not exist already
	 * @param reqModel
	 */
	public static void addRequirement( RequirementModel reqModel ) {

		if ( !requirementsInMemory.contains( reqModel ) ) {
			requirementsInMemory.add( reqModel );
		}
		if ( !reqModel.getPrefixProperties( ).isEmpty( ) ) {
			for ( GenericBoilerPlate prefix : reqModel.getPrefixProperties( ) ) {
				addBoilerPlate( prefix );
			}
		}

		if ( reqModel.getMainBody( ) != null )
			addBoilerPlate( reqModel.getMainBody( ) );

		if ( !reqModel.getSuffixProperties( ).isEmpty( ) ) {
			for ( GenericBoilerPlate suffix : reqModel.getSuffixProperties( ) ) {
				addBoilerPlate( suffix );
			}
		}
	}


	public static void addProperty( PropertyModel propModel ) {

		if ( !propertiesInMemory.contains( propModel ) ) {
			propertiesInMemory.add( propModel );
		}

		if ( propModel.getPattern( ) != null )
			addPattern( propModel.getPattern( ) );
	}


	public static void addBoilerPlate( AbstractBoilerPlate genBlp ) {

		if ( !boilerplatesInMemory.containsKey( genBlp.getID( ) ) )
			boilerplatesInMemory.put( genBlp.getID( ) , genBlp.serialize( ) );
	}


	public static void addPattern( AbstractBoilerPlate pattern ) {

		if ( !patternsInMemory.containsKey( pattern.getID( ) ) )
			patternsInMemory.put( pattern.getID( ) , pattern.serialize( ) );
	}


	public static void addSyntax( AbstractBoilerPlate genBlp ) {

		ArrayList < AbstractBoilerPlate > results = ( syntax.containsKey( genBlp.getID( ) ) ) ? syntax.get( genBlp.getID( ) ) : new ArrayList <>( );
		results.add( genBlp );
		syntax.put( genBlp.getID( ) , results );
		logger.log( Level.INFO , "Added " + genBlp.getID( ) );
	}


	public static Architecture addArchitecture( String name , PropertyModel source ) {

		if ( architectures.containsKey( name ) ) {
			architectures.get( name ).addProperty( source );
		} else {
			Architecture newArch = new Architecture( name );
			architectures.put( name , newArch );
		}
		return architectures.get( name );
	}


	public static Architecture addArchitecture( Architecture architecture ) {

		return addArchitecture( architecture.serialize( ) , null );
	}


	// ///////////////////////// GET, SET ///////////////////////////

	public static HashMap < OntologyWordModel , ArrayList < OntologyWordModel >> getAbstractionLevelsAndCategoriesTopLevel( ) {

		return abstractionLevelsAndCategoriesTopLevel;
	}


	public static HashMap < OntologyWordModel , ArrayList < OntologyWordModel >> getAbstractionLevelsAndCategoriesFlat( ) {

		return abstractionLevelsAndCategoriesFlat;
	}


	public static HashMap < String , String [ ] > getPrefixTemplates( ) {

		return prefixTemplates;
	}


	public static void setPrefixTemplates( HashMap < String , String [ ] > prefixes ) {

		Memory.prefixTemplates = prefixes;
	}


	public static HashMap < String , String [ ] > getBodyTemplates( ) {

		return bodyTemplates;
	}


	public static void setBodyTemplates( HashMap < String , String [ ] > bodies ) {

		Memory.bodyTemplates = bodies;
	}


	public static HashMap < String , String [ ] > getSuffixTemplates( ) {

		return suffixTemplates;
	}


	public static void setSuffixTemplates( HashMap < String , String [ ] > suffixes ) {

		Memory.suffixTemplates = suffixes;
	}


	public static ObservableList < RequirementModel > getRequirementsInMemory( ) {

		return requirementsInMemory;
	}


	public static ObservableList < PropertyModel > getPropertiesInMemory( ) {

		return propertiesInMemory;
	}


	public static HashMap < String , PropertyPattern > getPatternTemplates( ) {

		return patternTemplates;
	}


	public static HashMap < String , PropertyPattern > getArchPatternTemplates( ) {

		return archPatternTemplates;
	}


	public static HashMap < String , String > getBoilerplatesInMemory( ) {

		return boilerplatesInMemory;
	}


	public static String getReqBlpFile( ) {

		return REQ_BLP_FILE;
	}


	public static String getPrpPtrnFile( ) {

		return PRP_PTRN_FILE;
	}


	public static String getIdSeparator( ) {

		return ID_SEPARATOR;
	}


	public static String getCompSeparator( ) {

		return COMP_SEPARATOR;
	}


	public static HashMap < String , String > getPatternsInMemory( ) {

		return patternsInMemory;
	}


	public static HashMap < String , ArrayList < AbstractBoilerPlate >> getSyntax( ) {

		return syntax;
	}


	public static HashMap < String , Architecture > getArchitectures( ) {

		return architectures;
	}


	public static HashMap < String , ArchitectureStyle > getBaseArchIDs( ) {

		return baseArchIDs;
	}


	public static void addToSuggestions( String rawString ) {

		if ( rawString.split( MIDDLE_DELIMITER ).length != 2 )
			return;

		String key = rawString.split( MIDDLE_DELIMITER )[ 0 ];
		String value = rawString.split( MIDDLE_DELIMITER )[ 1 ];

		if ( !suggestionsInMemory.containsKey( key ) ) {
			suggestionsInMemory.put( key , new HashSet <>( ) );
		}
		suggestionsInMemory.get( key ).addAll( Arrays.asList( value ) );
	}


	public static HashSet < String > getSuggestionsFor( String type ) {

		if ( suggestionsInMemory.containsKey( type ) ) {
			return suggestionsInMemory.get( type );
		} else
			return new HashSet <>( );
	}


	/**
	 * Creates a popup asking the user for a filename and a separator then
	 * exports all requirements currently in Memory to a csv file with the given
	 * name and divided by the chosen separator
	 */
	public static void exportReqsToFile( String filename , String separator ) {

		ArrayList < String > linesToWrite = new ArrayList <>( );
		linesToWrite.add( CSV_REQ_HEADER.replace( "," , separator ) );
		for ( RequirementModel reqModel : Memory.getRequirementsInMemory( ) ) {
			String stringToWrite = "\"" + reqModel.getReqID( ) + "\"" + separator + reqModel.getAbsLevel( ).getFullName( ) + separator
					+ reqModel.getCatID( ).getFullName( ) + separator + "\"" + reqModel.getDisplayForm( ) + "\"" + separator
					+ reqModel.getStatusModel( ).getCurrentStatus( ) + separator;
			stringToWrite += "\"";
			for ( RequirementModel reqMdl2 : reqModel.getRefines( ) ) {
				stringToWrite += reqMdl2.getReqID( ) + separator;
			}
			stringToWrite += "\"" + separator + "\"";
			for ( RequirementModel reqMdl2 : reqModel.getIsRefinedBy( ) ) {
				stringToWrite += reqMdl2.getReqID( ) + separator;
			}
			stringToWrite += "\"" + separator + "\"";
			for ( RequirementModel reqMdl2 : reqModel.getConcretizes( ) ) {
				stringToWrite += reqMdl2.getReqID( ) + separator;
			}
			stringToWrite += "\"" + separator + "\"";
			for ( RequirementModel reqMdl2 : reqModel.getIsConcretizedBy( ) ) {
				stringToWrite += reqMdl2.getReqID( ) + separator;
			}
			stringToWrite += "\"";
			linesToWrite.add( stringToWrite );
		}
		Utils.writeToFile( filename , linesToWrite );
	}


	/**
	 * Creates a popup asking the user for a filename and a separator then
	 * exports all properties currently in Memory to a csv file with the given
	 * name and divided by the chosen separator
	 */
	public static void exportPropsToFile( String filename , String separator ) {

		ArrayList < String > linesToWrite = new ArrayList <>( );
		linesToWrite.add( CSV_PRP_HEADER.replace( "," , separator ) );
		for ( PropertyModel propModel : Memory.getPropertiesInMemory( ) ) {
			String stringToWrite = "\"" + propModel.getPropID( ) + "\"" + separator + "\"" + propModel.getArchitecture( ).toString( ) + "\""
					+ separator + propModel.getCategory( ).getFullName( ) + separator + "\"" + propModel.getDisplayForm( ) + "\"" + separator
					+ propModel.getStatusModel( ).getCurrentStatus( ) + separator;
			stringToWrite += "\"";
			for ( RequirementModel reqMdl2 : propModel.getDerivedBy( ) ) {
				stringToWrite += reqMdl2.getReqID( ) + separator;
			}
			stringToWrite += "\"";
			linesToWrite.add( stringToWrite );
		}
		Utils.writeToFile( filename , linesToWrite );
	}


	/**
	 * Alternative of exportPropsToFile, where a CTL formula is exported for
	 * each property
	 * 
	 */
	public static void exportCTLPropsToFile( String filename , String separator ) {

		ArrayList < String > linesToWrite = new ArrayList <>( );
		linesToWrite.add( CSV_CTL_HEADER.replace( "," , separator ) );
		for ( PropertyModel propModel : Memory.getPropertiesInMemory( ) ) {
			//System.out.println(propModel.getCategory( ) );
			if ( !propModel.getCategory( ).toString( ).equals( "VerifiableProperty" ) ) {
				continue;
			}
			String stringToWrite = "\"" + propModel.getPropID( ) + "\"" + separator + "\"" + propModel.getPattern().translateToCTL( );
			stringToWrite += "\"";
			linesToWrite.add( stringToWrite );
		}
		 Utils.writeToFile(filename, linesToWrite);

	}


	public static HashSet < String > getClassesFlat( ) {

		return flatClasses;
	}
}
