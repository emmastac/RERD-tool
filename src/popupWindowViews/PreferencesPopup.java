package popupWindowViews;

import javax.security.auth.Refreshable;

import models.Preferences;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

/**
 * Creates the Preferences Popup Window.
 *
 */
public class PreferencesPopup extends Stage {

	private TextField componentsCodeField;
	private TextField architecturesDefinField;
	private TextField architecturesInstancesField;
	private final static int WIDTH = 800;
	private final static int HEIGHT = 300;
	private static final String PREF_TITLE = ExtStrings.getString( "PreferencesPropPopupTitle" );
	private static final int WRAPPER_PADDING = 20;


	public PreferencesPopup ( Window owner ) {

		setTitle( PREF_TITLE );
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


	private VBox optionsPanel ( ) {

		VBox vertPanel = new VBox( 20 );
		vertPanel.setPadding( new Insets( WRAPPER_PADDING ) );

		Label componentsCodeFieldLabel = new Label( "Path to components' code : " );
		componentsCodeField = new TextField( Memory.preferences.getComponentsCodePath( ));
		componentsCodeField.setMinWidth( 500 );
		Label architecturesDefinFieldLabel = new Label( "Path to architectures' definition : " );
		architecturesDefinField = new TextField(Memory.preferences.getArchitecturesDefinPath( ) );
		architecturesDefinField.setMinWidth( 500 );
		Label architecturesInstancesFieldLabel = new Label( "Path to architecture instances : " );
		architecturesInstancesField = new TextField(Memory.preferences.getArchitecturesInstancesPath( ) );
		architecturesInstancesField.setMinWidth( 500 );

		HBox h1Panel = new HBox( );
		//h1Panel.setPadding( new Insets( WRAPPER_PADDING ) );
		h1Panel.getChildren( ).add( componentsCodeFieldLabel );
		h1Panel.getChildren( ).add( componentsCodeField );

		HBox h2Panel = new HBox( );
		//h2Panel.setPadding( new Insets( WRAPPER_PADDING ) );
		h2Panel.getChildren( ).add( architecturesDefinFieldLabel );
		h2Panel.getChildren( ).add( architecturesDefinField );

		HBox h3Panel = new HBox( );
		//h3Panel.setPadding( new Insets( WRAPPER_PADDING ) );
		h3Panel.getChildren( ).add( architecturesInstancesFieldLabel );
		h3Panel.getChildren( ).add( architecturesInstancesField );

		vertPanel.getChildren( ).add( h1Panel );
		vertPanel.getChildren( ).add( h2Panel );
		vertPanel.getChildren( ).add( h3Panel );

		return vertPanel;
	}


	private HBox buttonBox ( ) {

		HBox buttonBox = new HBox( );
		buttonBox.setPadding( new Insets( WRAPPER_PADDING , 0 , 0 , 0 ) );
		Button saveButton = new Button( "Save" );
		saveButton.setOnAction( ( ActionEvent ) -> {
			String componentsCodeFieldText = componentsCodeField.getText( ).trim();
			String architecturesDefinFieldText = architecturesDefinField.getText( ).trim( );
			String architecturesInstancesFieldText = architecturesInstancesField.getText( ).trim( );
			Memory.preferences.setComponentsCodePath( componentsCodeFieldText );
			Memory.preferences.setArchitecturesDefinPath( architecturesDefinFieldText );
			Memory.preferences.setArchitecturesInstancesPath( architecturesInstancesFieldText );
			Memory.preferences.exportPreferencesToFile( componentsCodeFieldText, architecturesDefinFieldText , architecturesInstancesFieldText );
			PreferencesPopup.this.close( );
		} );
		Button cancelButton = new Button( "Cancel" );
		cancelButton.setOnAction( ( ActionEvent ) -> PreferencesPopup.this.close( ) );
		saveButton.setFont( new Font( 20 ) );
		cancelButton.setFont( new Font( 20 ) );
		
		Button setDefault = new Button( "Set default" );
		setDefault.setOnAction( ( ActionEvent ) -> {
			this.resetInput();
		} );
		setDefault.setFont( new Font( 20 ) );
		setDefault.setFont( new Font( 20 ) );

		Region spacer1 = new Region( );
		HBox.setHgrow( spacer1 , Priority.ALWAYS );

		Region spacer2 = new Region( );
		HBox.setHgrow( spacer2 , Priority.ALWAYS );
		
		buttonBox.getChildren( ).addAll( setDefault, spacer1, cancelButton , spacer2 , saveButton );

		return buttonBox;
	}
	
	private void resetInput(  ){
		this.componentsCodeField.setText( Memory.preferences.getDefaultComponentsCodePath( ) );
		this.architecturesDefinField.setText( Memory.preferences.getDefaultArchitecturesDefinPath( ) );
		this.architecturesInstancesField.setText( Memory.preferences.getDefaultArchitecturesInstancesPath( ) );
		
	}


	

	
}
