package containerViews;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import popupWindowViews.PreferencesPopup;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import utils.Memory;
import utils.OntologyIO;
import viewWrappers.MinimizeWrapper;

public class ArchitecturesEditPanel extends BorderPane {

	TextArea descriptionField;


	public ArchitecturesEditPanel ( ) {

		init( );
		this.setCenter( classPanel( ) );
	}


	private void init ( ) {

		this.setPadding( new Insets( 10 ) );
		this.setBorder( new Border( new BorderStroke( Color.BLACK , BorderStrokeStyle.DASHED , new CornerRadii( 10.0 ) , new BorderWidths( 1 ) ) ) );
	}


	private static String getCodeForArchitecture ( String name ) {
		if ( name == null || name.equals( "" ) )
			return "";
		try {
			return new String( Files.readAllBytes( Paths.get( Memory.preferences.getArchitecturesInstancesPath( ) + "/" + name + "/"+"example.txt" ) ) , StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
	}
	
	private String getCodeForCoordinatorComponent( String name ){
		//System.out.println(Memory.preferences.getArchitecturesInstancesPath( )+" "+name );
		if ( name == null || name.equals( "" ) )
			return "";
		try {
			return new String( Files.readAllBytes( Paths.get( Memory.preferences.getArchitecturesInstancesPath( ) + "/" + name + "/Coordinator.txt" ) ) , StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
	}
	
	private String getCodeForOperandComponents( String name ){
		if ( name == null || name.equals( "" ) )
			return "";
		try {
			return new String( Files.readAllBytes( Paths.get( Memory.preferences.getArchitecturesInstancesPath( ) + "/" + name + "/Operands.bip" ) ) , StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
	}

	private static String getGlobalConnectorsForArchitecture ( String name ) {
		
		if ( name == null || name.equals( "" ) )
			return "";
		try {
			return new String( Files.readAllBytes( Paths.get( Memory.preferences.getArchitecturesInstancesPath( ) + "/" + name + "/Connectors.txt" ) ) , StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
		
	}


	private ScrollPane classPanel ( ) {

		VBox propertiesBox = new VBox( 10 );
		ScrollPane scrollPane = new ScrollPane( propertiesBox );
		scrollPane.setPadding( new Insets( 10 ) );
		// scrollPane.setFitToWidth(true);

		String fullName = ModelsTab.selectedArch.get( );
		if(fullName!=null && !fullName.equals( "" )){
			fullName = fullName.split( "\\." )[1].trim();
		}

		TextField label = new TextField( fullName );
		label.setEditable( false );
		label.setBackground( Background.EMPTY );
		
		TextField label1 = new TextField( "Architectures' Operands" );
		label1.setEditable( false );
		label1.setBackground( Background.EMPTY );

		String descriptionStr = getCodeForOperandComponents( fullName );

		
		TextArea descriptionField = new TextArea( descriptionStr );
		descriptionField.setEditable( false );
		descriptionField.setPrefRowCount( 27 );

		TextField label2 = new TextField( "Coordinator");
		label2.setEditable( false );
		label2.setBackground( Background.EMPTY );
		String descriptionPortStr = getCodeForCoordinatorComponent( fullName );

		TextArea descriptionPortField = new TextArea( descriptionPortStr );
		descriptionPortField.setEditable( false );
		descriptionPortField.setPrefRowCount( 3 );
		
		String globalConnsStr = getGlobalConnectorsForArchitecture( fullName );
		TextField label3 = new TextField( "Connectors");
		label3.setEditable( false );
		label3.setBackground( Background.EMPTY );
		
		TextArea globalConnsField = new TextArea( globalConnsStr );
		globalConnsField.setEditable( false );
		globalConnsField.setPrefRowCount( 5 );
		
		


		propertiesBox.getChildren( ).addAll( label , label1, descriptionField, label2, descriptionPortField , label3, globalConnsField);

		return scrollPane;
	}

	

	public void drawClassView ( ) {

		this.setCenter( classPanel( ) );
	}
}
