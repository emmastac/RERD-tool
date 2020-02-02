package containerViews;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import models.Architecture;
import models.LogEntryModel;
import models.LogLevel;
import models.OntologyWordModel;
import models.PropertyModel;
import models.PropertyPattern;
import models.PropertyStatusModel;
import models.RequirementModel;
import models.RequirementStatusModel;
import popupWindowViews.ArchitecturesPopup;
import popupWindowViews.DoubleListPopup;
import utils.ExtStrings;
import utils.Memory;
import architectures.ArchitectureExporter;
import architectures.ArchitecturePreparator;

public class PropertyView extends BorderPane {

	private static final String DERIVED_BY_BUTTON_TEXT = ExtStrings.getString( "DerivedByButton" );

	protected PropertyModel propModel , propModelOriginal;
	private ComboBox < String > statusModelBox;
	private TextField propIDField;
	// private ComboBox<String> categoryField;
	private Label categoryField;
	private DoubleListPopup<RequirementModel> derivePopup;
	private ArchitecturesPopup architecturePopup;
	private Label architectureLabel;
	private int index;


	public PropertyView() {

		init( );
	}


	protected void init( ) {

		setPropModel( new PropertyModel( "" , PropertyModel.DEFAULT_CATEGORY , new PropertyStatusModel( ) , null , null ) );
	}


	public PropertyView( PropertyModel propertyModel ) {

		setPropModel( propertyModel );
	}


	public VBox getOtherElements( ) {

		VBox elementBox = new VBox( 10 );
		elementBox.setPadding( new Insets( 10 ) );
		elementBox.setMaxWidth( 250 );

		propIDField = new TextField( propModel.generatePropID( ) );
		Tooltip hoverText = new Tooltip( "Property ID" );
		Tooltip.install( propIDField , hoverText );

		categoryField = new Label( propModel.getCategory( ).toString( ) );

		statusModelBox = new ComboBox <>( PropertyStatusModel.USER_DEFINED_STATES );
		statusModelBox.getSelectionModel( ).select( propModel.getStatusModel( ).getCurrentStatus( ) );

		propModel.getPropIDProperty( ).addListener( new ChangeListener < String >( ) {

			@Override
			public void changed( ObservableValue < ? extends String > arg0 , String arg1 , String arg2 ) {

				propIDField.setText( arg2 );
			}
		} );
		// propModel.getCategoryProperty().bind(categoryField.selectionModelProperty().get().selectedItemProperty());

		elementBox.getChildren( ).addAll( propIDField , categoryField , statusModelBox , createArchitecturesPanel( ) , createDerivePanel( ) );
		// elementBox.getChildren().addAll(propIDField, statusModelBox,
		// createDerivePanel());

		return elementBox;
	}


	private VBox createDerivePanel( ) {

		VBox derivePanelFull = new VBox( 10 );

		Button derivedFromButton = new Button( DERIVED_BY_BUTTON_TEXT );
		derivedFromButton.setOnAction( ( ActionEvent ) -> {
			derivePopup = new DoubleListPopup<RequirementModel>( getScene( ).getWindow( ) , derivedFromButton.getText( ) , 
					Memory.getRequirementsInMemory( ), propModel.getDerivedBy( ), new RequirementModel[] {} );
		} );

		derivePanelFull.getChildren( ).addAll( derivedFromButton );

		return derivePanelFull;
	}


	private VBox createArchitecturesPanel( ) {

		VBox architecturesPanelFull = new VBox( 10 );
		if ( propModel.getCategory( ).getFullName( ).equals( "CSSP:EnforceableProperty" ) ) {

			architectureLabel = new Label( );
			architectureLabel.textProperty( ).bind( propModel.architectureProperty.asString( ) );
			Tooltip hoverText = new Tooltip( "The currently selected architecture" );
			Tooltip.install( architectureLabel , hoverText );
			architecturesPanelFull.getChildren( ).add( architectureLabel );

			Button selectArchitectureButton = new Button( "Select Architecture" );
			selectArchitectureButton.setOnAction( ( ActionEvent ) -> {
				architecturePopup = new ArchitecturesPopup( getScene( ).getWindow( ) , architectureLabel , propModel );
			} );
			architecturesPanelFull.getChildren( ).add( selectArchitectureButton );

			// if ( propModel.getArchitecture( ) != null &&
			// !propModel.getArchitecture( ).archID.equals( "" ) ) {

			// if instantiated
//			boolean isInstantiated = false;
//			for ( Integer i : ArchitecturePreparator.instantiatedArchitectures.keySet( ) ) {
//				if ( propModel.getArchitecture( ).archID.get().equals( ArchitecturePreparator.instantiatedArchitectures.get( i )[ 0 ] ) ) {
//					index = i;
//					isInstantiated = true;
//					break;
//				}
//			}
//
//			if ( isInstantiated == false ) {
				Button exportArchitectureButton = new Button( "Export Architecture" );
				exportArchitectureButton.setOnAction( ( ActionEvent ) -> {
					ArchitectureExporter architTest = new ArchitectureExporter( );
					try {
						architTest.exportArchitecture( propModel.getArchitecture( ) );
							//validateSelf( );
					} catch ( Exception e ) {
						// TODO Auto-generated catch block
						e.printStackTrace( );
					}
					propModel.getArchitecture( ).outputCodePath = new SimpleStringProperty( Memory.preferences.getArchitecturesInstancesPath( ) + "/" + propModel.getArchitecture( ) + "/example.bip" );
					propModel.getArchitecture( ).outputConfPath = new SimpleStringProperty( Memory.preferences.getArchitecturesInstancesPath( ) + "/" + propModel.getArchitecture( ) + "/exampleconf.txt" );
					ArchitecturePreparator.addArchitecture( propModel.getArchitecture( ).toString( ) ,
							propModel.getArchitecture( ).outputCodePath.get( ) , propModel.getArchitecture( ).outputConfPath.get( ) );
					//System.out.println("a "+propModel.getArchitecture( ).toString( )+" b "+propModel.getArchitecture( ).outputCodePath.get( )+" c "+ propModel.getArchitecture( ).outputConfPath.get( ) );
				
				} );
				architecturesPanelFull.getChildren( ).add( exportArchitectureButton );
//			} else {
//				Button undoArchitectureButton = new Button( "Remove Architecture" );
//				undoArchitectureButton.setOnAction( ( ActionEvent ) -> {
//					ArchitecturePreparator.removeArchitecture( index );
//					validateSelf( );
//				} );
//				architecturesPanelFull.getChildren( ).add( undoArchitectureButton );
//			}

			Label outputCodePathLink = new Label( );
			if ( architectureLabel.getText( ).equals( "" ) ) {
				outputCodePathLink.textProperty( ).bind( new SimpleStringProperty( "" ) );
			} else {
				outputCodePathLink.textProperty( ).bind( new SimpleStringProperty( "Browse Archit. Code" ) );

				String outputCodePath = Memory.preferences.getArchitecturesInstancesPath( ) + "/" + propModel.getArchitecture( ) + "/example.bip";
				Tooltip.install( outputCodePathLink , new Tooltip( "Architecture's code file is in the path: " + outputCodePath ) );
				// To indicate the the link is clickable
				outputCodePathLink.setCursor( Cursor.CLOSED_HAND );

				outputCodePathLink.setOnMouseClicked( new EventHandler < MouseEvent >( ) {

					@Override
					public void handle( MouseEvent mouseEvent ) {

						// System.out.println("architectureInstances/" +
						// propModel.getArchitecture( ) + "/example.bip" );
						openFile( outputCodePath );

					}
				} );
			}
			architecturesPanelFull.getChildren( ).add( outputCodePathLink );

			Label outputCodeConfLink = new Label( );
			if ( architectureLabel.getText( ).equals( "" ) ) {
				outputCodePathLink.textProperty( ).bind( new SimpleStringProperty( "" ) );
			} else{
				outputCodeConfLink.textProperty( ).bind( new SimpleStringProperty( "Browse Archit. Configuration" ) );
				String outputConfPath = Memory.preferences.getArchitecturesInstancesPath( ) + "/" + propModel.getArchitecture( ) + "/exampleconf.txt";
				Tooltip.install( outputCodeConfLink , new Tooltip( "Architecture's configuration file is in the path:" + outputConfPath ) );

				// To indicate the the link is clickable
				outputCodeConfLink.setCursor( Cursor.CLOSED_HAND );

				outputCodeConfLink.setOnMouseClicked( new EventHandler < MouseEvent >( ) {

					@Override
					public void handle( MouseEvent mouseEvent ) {

						openFile( outputConfPath );
					}
				} );

				architecturesPanelFull.getChildren( ).add( outputCodeConfLink );
			}
		}

		return architecturesPanelFull;
	}


	private void openFile( String path ) {

		if ( Desktop.isDesktopSupported( ) ) {
			new Thread( ( ) -> {
				try {
					Desktop.getDesktop( ).browse( new File( path ).toURI( ) );
				} catch ( IOException e1 ) {
					e1.printStackTrace( );
				}
			} ).start( );
		} else {
			// TODO: add UnsupportedDesktop error to the logger
		}

	}


	public PropertyModel getPropModel( ) {

		return propModel;
	}


	public void setPropModel( PropertyModel propModel ) {

		this.getChildren( ).clear( );
		this.setPadding( new Insets( 10 ) );
		this.propModelOriginal = propModel;

		// We create a copy of the property while editing
		// This copy is only saved back into the original when we click save
		this.propModel = new PropertyModel( propModel );

		this.setCenter( propModel.getPattern( ) );

		this.setRight( getOtherElements( ) );
	}


	public void setPattern( PropertyPattern propertyPattern ) {

		propModel.setPattern( propertyPattern );
		propertyPattern.genID( );
		if ( Memory.getArchPatternTemplates( ).containsKey( propertyPattern.getCat( ) ) ) {
			propModel.setCategory( new OntologyWordModel( "CSSP:EnforceableProperty" ) );
		} else {
			propModel.setCategory( new OntologyWordModel( "CSSP:VerifiableProperty" ) );
		}
		this.setCenter( propertyPattern );
		this.setRight( getOtherElements( ) );
	}


	/**
	 * Validates and saves this requirement. The saving will happen even if
	 * validation fails
	 * 
	 * @return ArrayList<LogEntry> All the output of the saving and validation,
	 *         to be put in the req console
	 */
	public ArrayList < LogEntryModel > save( ) {

		if ( propModel == null )
			return new ArrayList <>( );

		// If our requirement is not in memory, add it
		Memory.addProperty( propModelOriginal );

		propModelOriginal.setCategory( propModel.getCategory( ) );
		propModel.setPropID( propIDField.getText( ) );
		propModelOriginal.setPropID( propIDField.getText( ) );

		// If our pattern is not in memory, add it
		// if (!Memory.getPatternsInMemory().containsKey(((PropertyPattern)
		// this.getCenter()).getID()))
		// Memory.addPattern((PropertyPattern) this.getCenter());
		propModelOriginal.setPattern( ( PropertyPattern ) this.getCenter( ) );
		ArrayList < LogEntryModel > errors = new ArrayList <>( );
		errors.addAll( validateSelf( ) );

		propModelOriginal.getStatusModel( ).setCurrentStatus( statusModelBox.getSelectionModel( ).getSelectedItem( ) );
		Architecture arch = propModel.getArchitecture( );
		if ( arch != null ) {
			propModelOriginal.setArchitecture( arch );
			arch.addProperty( propModelOriginal );
			arch.updateStatus( propModelOriginal.getStatusModel( ) );
		}

		// System.out.println(propModel.serialize());
		// System.out.println(propModel.getPattern().serialize());

		return errors;
	}


	public ArrayList < LogEntryModel > validateSelf( ) {

		ArrayList < LogEntryModel > valResults = new ArrayList <>( );

		// Check for ReqID conflicts
		Memory.getPropertiesInMemory( ).forEach( ( propMdl ) -> {
			if ( propMdl != propModelOriginal && propMdl.getPropID( ).equals( propModelOriginal.getPropID( ) ) )
				valResults.add( new LogEntryModel( LogLevel.ERROR , LogEntryModel.ID_CONFLICT ) );
		} );

		// Add the other consistency errors
		valResults.addAll( propModelOriginal.validate( ) );

		if ( !valResults.isEmpty( ) ) {
			// statusModelBox.getSelectionModel().select(PropertyStatusModel.INVALID);
		} else if ( valResults.isEmpty( ) && propModel.getStatusModel( ).getCurrentStatus( ).equals( RequirementStatusModel.DEFAULTSTATUS ) )
			statusModelBox.getSelectionModel( ).select( PropertyStatusModel.NOTYETDISCHARGED );

		return valResults;
	}


	public void clear( ) {

		this.getChildren( ).clear( );
		init( );
	}

}
