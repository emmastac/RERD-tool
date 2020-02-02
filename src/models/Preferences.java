package models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import utils.ExtStrings;

public class Preferences {

	private static final String PREF_FILE_NAME = ExtStrings.getString( "PreferencesFile" );
	private String componentsCodePath;
	private String architecturesDefinPath;
	private String architecturesInstancesPath;


	public Preferences ( ) {

		componentsCodePath = ExtStrings.getString( "DefaultComponentsCodeHome" );
		architecturesDefinPath = ExtStrings.getString( "DefaultArchitecturesDefinHome" );
		architecturesInstancesPath = ExtStrings.getString( "DefaultArchitecturesInstancesHome" );
	}


	public String getArchitecturesInstancesPath ( ) {

		return architecturesInstancesPath;
	}


	public String getDefaultArchitecturesInstancesPath ( ) {

		return ExtStrings.getString( "DefaultArchitecturesInstancesHome" );
	}


	public void setArchitecturesInstancesPath ( String architecturesInstancesPath ) {

		this.architecturesInstancesPath = architecturesInstancesPath;
	}


	public String getComponentsCodePath ( ) {

		return componentsCodePath;
	}


	public String getDefaultComponentsCodePath ( ) {

		return ExtStrings.getString( "DefaultComponentsCodeHome" );
	}


	public void setComponentsCodePath ( String componentsCodePath ) {

		this.componentsCodePath = componentsCodePath;
	}


	public String getArchitecturesDefinPath ( ) {

		return architecturesDefinPath;
	}


	public String getDefaultArchitecturesDefinPath ( ) {

		return ExtStrings.getString( "DefaultArchitecturesDefinHome" );
	}


	public void setArchitecturesDefinPath ( String architecturesDefinPath ) {

		this.architecturesDefinPath = architecturesDefinPath;
	}


	public void exportPreferencesToFile ( String componentsCodeFieldText , String architecturesDefinFieldText , String architecturesInstancesFieldText ) {

		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter( getFilename( ) ) );

			bw.write( componentsCodeFieldText + getHardSeparator( ) );
			bw.write( architecturesDefinFieldText + getHardSeparator( ) );
			bw.write( architecturesInstancesFieldText + getHardSeparator( ) );
			bw.close( );

		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}


	public void importPreferencesFromFile ( ) {

		File prefFile = new File( getFilename( ) );
		if( !prefFile.exists( ) )
			return;
		
		try {
			BufferedReader br = new BufferedReader( new FileReader( getFilename( ) ) );

			String line = br.readLine( );
			String [ ] prefParts = line.split( getHardSeparator( ) );

			setComponentsCodePath( prefParts [ 0 ] );
			setArchitecturesDefinPath( prefParts [ 1 ] );
			setArchitecturesInstancesPath( prefParts [ 2 ] );
			br.close( );

		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

	}


	public String getFilename ( ) {
		return PREF_FILE_NAME;
	}


	public String getHardSeparator ( ) {

		return ";;;";
	}


	public String getSoftSeparator ( ) {

		return ";;";
	}

}
