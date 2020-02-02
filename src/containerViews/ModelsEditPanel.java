package containerViews;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.geometry.Insets;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import utils.Memory;
import utils.OntologyIO;
import viewWrappers.MinimizeWrapper;

public class ModelsEditPanel extends BorderPane {

	TextArea descriptionField;


	public ModelsEditPanel ( ) {

		init( );
		this.setCenter( classPanel( ) );
	}


	private void init ( ) {

		this.setPadding( new Insets( 10 ) );
		this.setBorder( new Border( new BorderStroke( Color.BLACK , BorderStrokeStyle.DASHED , new CornerRadii( 10.0 ) , new BorderWidths( 1 ) ) ) );
	}


	private static String getCodeForComponent ( String name ) {
		if ( name == null || name.equals( "" ) )
			return "";
		try {
			return new String( Files.readAllBytes( Paths.get( Memory.preferences.getComponentsCodePath( ) + "/" + name + ".txt" ) ) , StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
	}
	

	private static String getPortsCodeForComponent ( String name ) {

		if ( name == null || name.equals( "" ) )
			return "";
		try {
			return new String( Files.readAllBytes( Paths.get( Memory.preferences.getComponentsCodePath( ) + "/" + name + "_ports.txt" ) ) , StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
	}
	
	private static String getGlobalConnectorsForComponent ( String name ) {
		
		if ( name == null || name.equals( "" ) )
			return "";
		try {
			String selectedLines = "";
			File globalConns = new File( Memory.preferences.getComponentsCodePath( ) + "/globalConns.txt" );
			BufferedReader br = new BufferedReader( new FileReader( globalConns ) );
			String line = "";
			while((line = br.readLine( ))!=null){
				if (!line.contains( "(" ))
					continue;
			
				String[] tokens = line.split( "[()\\.\\s]" );
				for( String tok : tokens){
					System.out.println(tok);
					if(tok.trim().equals( name )){
						selectedLines += line+'\n';
						continue;
					}
				}
			}
			br.close( );
			return selectedLines;
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

		String fullName = ModelsTab.selectedModel.get( );

		TextField label = new TextField( fullName );
		label.setEditable( false );
		label.setBackground( Background.EMPTY );
		
		TextField label1 = new TextField( "Behaviour" );
		label1.setEditable( false );
		label1.setBackground( Background.EMPTY );

		String descriptionStr = getCodeForComponent( fullName );

		
		TextArea descriptionField = new TextArea( descriptionStr );
		descriptionField.setEditable( false );
		descriptionField.setPrefRowCount( 27 );

		TextField label2 = new TextField( "Port Types");
		label2.setEditable( false );
		label2.setBackground( Background.EMPTY );
		String descriptionPortStr = getPortsCodeForComponent( fullName );

		TextArea descriptionPortField = new TextArea( descriptionPortStr );
		descriptionPortField.setEditable( false );
		descriptionPortField.setPrefRowCount( 3 );
		
		String globalConnsStr = getGlobalConnectorsForComponent( fullName );
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
