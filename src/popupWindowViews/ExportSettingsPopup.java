package popupWindowViews;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import utils.ExtStrings;
import utils.Memory;

public class ExportSettingsPopup extends Stage {

	private TextField fileNameField;
	private ComboBox <String> separatorBox;
	private ToggleGroup exportLanguage;
	private static final String EXPORT_TEXT = "Text";
	private static final String EXPORT_CTL = "CTL formulas";
	private final static int WIDTH = 500;
	private final static int HEIGHT = 300;
	private static final String FILE_EXTENSION = ExtStrings.getString( "FileExtension" );
	private static final String REQ_TITLE = ExtStrings.getString( "ExportSettingsReqPopupTitle" );
	private static final String PRP_TITLE = ExtStrings.getString( "ExportSettingsPropPopupTitle" );
	private static final int WRAPPER_PADDING = 20;


	public ExportSettingsPopup ( Window owner , String title ) {

		setTitle( title );
		initModality( Modality.APPLICATION_MODAL );
		initOwner( owner );

		setScene( new Scene( mainContents( ) , WIDTH , HEIGHT ) );

		show( );
	}


	private Parent mainContents ( ) {

		BorderPane mainPane = new BorderPane( );

		mainPane.setCenter( optionsPanel( ) );
		mainPane.setBottom( buttonBox( ) );
		mainPane.setPadding( new Insets( WRAPPER_PADDING ) );

		return mainPane;
	}


	private HBox radioButtons ( ) {

		HBox horPanel = new HBox( 30 );
		horPanel.setPadding( new Insets( WRAPPER_PADDING ) );

		RadioButton firstButton = new RadioButton( EXPORT_TEXT );
		firstButton.setSelected( true );

		RadioButton secondButton = new RadioButton( EXPORT_CTL );
		exportLanguage = new ToggleGroup( );
		firstButton.setToggleGroup( exportLanguage );
		secondButton.setToggleGroup( exportLanguage );

		horPanel.getChildren( ).add( new Label( "Export as: " ) );
		horPanel.getChildren( ).add( firstButton );
		horPanel.getChildren( ).add( secondButton );

		return horPanel;
	}


	private VBox optionsPanel ( ) {

		VBox vertPanel = new VBox( 20 );
		vertPanel.setPadding( new Insets( WRAPPER_PADDING ) );

		fileNameField = new TextField( );

		separatorBox = new ComboBox <>( );
		separatorBox.getItems( ).add( "," );
		separatorBox.getItems( ).add( "|" );
		separatorBox.getItems( ).add( ":" );
		separatorBox.getItems( ).add( ";" );
		separatorBox.getSelectionModel( ).select( 0 );

		vertPanel.getChildren( ).add( fileNameField );
		vertPanel.getChildren( ).add( separatorBox );
		vertPanel.getChildren( ).add( radioButtons( ) );

		return vertPanel;
	}


	private HBox buttonBox ( ) {

		HBox buttonBox = new HBox( );
		buttonBox.setPadding( new Insets( WRAPPER_PADDING , 0 , 0 , 0 ) );
		Button confirm = new Button( "Finish exporting" );
		confirm.setOnAction( ( ActionEvent ) -> {
			if ( ExportSettingsPopup.this.getTitle( ).equals( REQ_TITLE ) ) {
				Memory.exportReqsToFile( getFilename( ) , getSeparator( ) );
			} else if ( ExportSettingsPopup.this.getTitle( ).equals( PRP_TITLE ) ) {
				String selectedExportLanguage = ( ( RadioButton ) exportLanguage.getSelectedToggle( ) ).getText( );
				if ( selectedExportLanguage.equals( EXPORT_TEXT ) ) {
					Memory.exportPropsToFile( getFilename( ) , getSeparator( ) );
				} else if ( selectedExportLanguage.equals( EXPORT_CTL ) ) {
					Memory.exportCTLPropsToFile( getFilename( ) , getSeparator( ) );
				}
			}
			ExportSettingsPopup.this.close( );
		} );
		Button cancel = new Button( "Cancel" );
		cancel.setOnAction( ( ActionEvent ) -> ExportSettingsPopup.this.close( ) );
		confirm.setFont( new Font( 20 ) );
		cancel.setFont( new Font( 20 ) );

		Region spacer = new Region( );
		HBox.setHgrow( spacer , Priority.ALWAYS );

		buttonBox.getChildren( ).addAll( cancel , spacer , confirm );

		return buttonBox;
	}


	public String getFilename ( ) {

		return fileNameField.getText( ) + FILE_EXTENSION;
	}


	public String getSeparator ( ) {

		return separatorBox.getSelectionModel( ).getSelectedItem( );
	}
}
