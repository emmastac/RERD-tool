package models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.antlr.stringtemplate.language.StringRef;

import utils.Memory;

public class PropertyPattern extends AbstractBoilerPlate {

	public PropertyPattern( String category , String... rawComponents ) {

		super( category, rawComponents );
	}


	public PropertyPattern( String serializedForm ) {

		super( serializedForm );
		genPropID( );
	}


	public PropertyPattern( AbstractBoilerPlate absBoil ) {

		super( absBoil );
		genPropID( );
	}


	@Override
	public String serialize( ) {

		return super.serialize( );
	}


	public void genPropID( ) {

		Set < String > keyset = Memory.getPatternsInMemory( ).keySet( );
		super.genID( keyset );
		Memory.addPattern( this );
	}

	/*********************************/
	static class CTLTransRule {

		public String id;
		public String structure;
		public String rule;
	}

	protected static HashMap < String , ArrayList < CTLTransRule > > ctlTrans = new HashMap < String , ArrayList < CTLTransRule > >( );


	public static void fillCTLTrans( ) {

		try {
			BufferedReader br = new BufferedReader( new FileReader( "PropertyPatterns2CTL" ) );

			String line = "";

			while ( ( line = br.readLine( ) ) != null ) {
				if ( line.isEmpty( ) || line.startsWith( "#" ) ) {
					continue;
				}

				Pattern p1 = Pattern.compile( "^(\\s*\"[^\"]*\"\\s*,?){3}$" );
				if ( !p1.matcher( line ).matches( ) ) {
					continue;
				}

				Pattern p2 = Pattern.compile( "\"([^\"]*)\"\\s*,?" );
				Matcher m2 = p2.matcher( line );
				CTLTransRule r = new CTLTransRule( );

				m2.find( );
				r.id = m2.group( 1 );

				m2.find( );
				r.structure = m2.group( 1 );

				m2.find( );
				r.rule = m2.group( 1 );

				if ( !ctlTrans.containsKey( r.id ) ) {
					ctlTrans.put( r.id , new ArrayList <>( ) );
				}
				ctlTrans.get( r.id ).add( r );

			}
			br.close( );

		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

	}


	public static String applyRule( CTLTransRule r , String [ ] propParts ) {

		String template = r.rule;
		StringTemplate tmpl = new StringTemplate( template , DefaultTemplateLexer.class );

		if ( template.contains( "$if(" ) ) {
			//System.out.println( "if rule" );
		} else if ( template.contains( "$a\\d$(" ) ) {

			// call inner rule, rule(...), by passing the argument derived from
			// these parts

			// replace the $rule(...)$ with the result
		}

		for ( int i = 0 ; i < propParts.length ; i++ ) {
			// System.out.println( "set "+String.valueOf( i+1 )+" "+ propParts[
			// i ]);
			tmpl.setAttribute( "a" + String.valueOf( i + 1 ) , propParts[ i ] );
		}

		//System.out.println( tmpl.toDebugString( ) );

		String trans = tmpl.toString( );

		return trans;
	}


	public CTLTransRule findRule( ) {

		String partsName = this.getID( );
		partsName = partsName.replaceAll( "-[\\d]+$" , "" );
		String [ ] partValues = this.rawComponents;

		//System.out.println( "find rule for : " + partsName + " " + Arrays.toString( partValues ) );

		CTLTransRule thisRule = null;
		ArrayList < CTLTransRule > rules;

		if ( ( rules = ctlTrans.get( partsName ) ) != null ) {
			thisRule = rules.get( 0 );
			//System.out.println( "thisRule:" + thisRule.structure );
			if ( rules.size( ) > 1 ) {
				// System.out.println( partsName);
				for ( CTLTransRule rule : rules ) {
					//System.out.println( "each Rule:" + rule.structure );
					String [ ] ruleParts = rule.structure.split( "," );
					String [ ] theseParts = partValues;

					boolean thisIsIt = true;
					for ( int i = 0 ; i < ruleParts.length ; i++ ) {
						if(theseParts.length==i){
							break;
						}
						String thisStructPart = theseParts[ i ].trim( );
						if ( thisStructPart.contains( "::" ) ) {
							thisStructPart = thisStructPart.split( "::" )[ 0 ];
							if(!thisStructPart.contains("|")){
								thisStructPart = thisStructPart+ "::";
							}
						}
						thisStructPart = thisStructPart.replaceAll( "-[\\d]+>$" , ">" );
						
						if ( !ruleParts[ i ].trim( ).equals( thisStructPart ) ) {
							//System.out.println( "not it : " + ruleParts[ i ] + " " + thisStructPart );
							thisIsIt = false;
							break;
						}
					}
					if ( thisIsIt ) {
						//System.out.println( "this is it :" );

						thisRule = rule;
						break;
					}

				}
			}

		} else {
			//System.out.println( "problem: no rules defined" );
		}

		return thisRule;

	}


	public String translateToCTL( ) {

		if ( ctlTrans.size( ) == 0 ) {
			fillCTLTrans( );
		}

		AbstractBoilerPlate absBoil = this;
		//System.out.println( "boil: " + absBoil );

		// if has children, translate children (parts)

		String [ ] parts = absBoil.getRawComponents( );

		String [ ] newParts = new String [ parts.length ];

		if ( parts.length > 1 || parts[ 0 ].contains( "<" ) ) {

			for ( int i = 0 ; i < newParts.length ; i++ ) {

				if ( parts[ i ].contains( "<" ) ) {
					String cleanPart = parts[ i ].substring( 1 , parts[ i ].length( ) - 1 );
					//System.out.println( "cleanPart " + cleanPart );
					if ( !Memory.getSyntax( ).containsKey( cleanPart ) ) {
						//System.out.println( "object not found " );
						newParts[ i ] = "";
						//if(cleanPart.startsWith("Simple-Beh-Dur-5")){
						//	newParts[ i ] = "AF";
						//}
						
					} else {
						AbstractBoilerPlate abs = new AbstractBoilerPlate( Memory.getSyntax( ).get( cleanPart ).get( 0 ) );
						newParts[ i ] = new PropertyPattern( abs ).translateToCTL( );

					}

				} else if ( parts[ i ].contains( "::" ) ) {

					newParts[ i ] = parts[ i ].split( "::" )[ 1 ];

				} else {
					newParts[ i ] = "";

				}

			}
		}

		// translate this
		CTLTransRule thisRule = this.findRule( );
		String result = "";
		if ( thisRule != null ) {
			result = applyRule( thisRule , newParts );
			//System.out.println( "rule returned : " + result );
		} else {
			result = absBoil.toString( );
			//System.out.println( "rule null , result: " + result );
		}

		return result;
	}

	/*********************************/
}
