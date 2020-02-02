package containerViews;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import models.OntologyWordModel;
import utils.OntologyIO;

public class CategoryDescriptionView extends HBox {

//	private HashMap <String , String> absLevelsLabels2IDs;
//
//	protected static final double DESCRIPTION_WIDTH = 0.35;
//	protected static final double PARENT_LIST_WIDTH = 0.25;
//	protected static final double RADIO_LIST_WIDTH = 0.25;
//	private Label catNameLabel;
//	private Label catDescriptionLabel;
//	private final Label superClassLabel = new Label( "Superclasses: " );
//	private final Label subClassLabel = new Label( "Subclasses: " );
//	private Button showBLPs;
//	private OntologyWordModel category;
//	private VBox categoryBox;
//	private VBox superClassBox;
//	private VBox subClassBox;
//
//
//	public CategoryDescriptionView ( ) {
//
//		absLevelsLabels2IDs = new HashMap <String , String>( );
//		absLevelsLabels2IDs.put( "System spacecraft level" , "OoSSA:SystemSpacecraft" );
//		absLevelsLabels2IDs.put( "Avionics level" , "OoSSA:Avionics" );
//		absLevelsLabels2IDs.put( "TS level" , "OoSSA:TS" );
//		absLevelsLabels2IDs.put( "RB level" , "OoSSA:RB" );
//
//		category = new OntologyWordModel( "" ); // For safety
//		catNameLabel = new Label( "No category selected" );
//		catDescriptionLabel = new Label( "Choose a category to see its description here" );
//		showBLPs = new Button( "Show these boilerplates" );
//
//		catNameLabel.setFont( new Font( 16 ) );
//		catNameLabel.setWrapText( true );
//		catDescriptionLabel.setFont( new Font( 14 ) );
//		catDescriptionLabel.setWrapText( true );
//		superClassLabel.setFont( new Font( 16 ) );
//		superClassLabel.setWrapText( true );
//		subClassLabel.setFont( new Font( 16 ) );
//		subClassLabel.setWrapText( true );
//		superClassLabel.prefWidthProperty( ).bind( this.prefWidthProperty( ).multiply( RADIO_LIST_WIDTH ) );
//		subClassLabel.prefWidthProperty( ).bind( this.prefWidthProperty( ).multiply( RADIO_LIST_WIDTH ) );
//
//		showBLPs.setOnAction( ( ActionEvent ) -> {
//			RequirementTab.switchToBLPNavigator( category );
//		} );
//
//		this.setSpacing( 25 );
//		this.setPadding( new Insets( 10 , 10 , 10 , 30 ) );
//		getChildren( ).addAll( categoryBox = new VBox( 15 , catNameLabel , catDescriptionLabel , showBLPs ) , superClassBox = new VBox( 5 , superClassLabel , buildParentList( ) ) ,
//				subClassBox = new VBox( 5 , subClassLabel , buildChildrenList( ) ) );
//
//		categoryBox.prefWidthProperty( ).bind( this.prefWidthProperty( ).multiply( DESCRIPTION_WIDTH ) );
//	}
//
//
//	public OntologyWordModel getCategory ( ) {
//
//		return category;
//	}
//
//
//	public void setCategory ( OntologyWordModel category ) {
//
//		this.category = category;
//		catNameLabel.setText( category.getShortName( ) );
//		String allLevelCategoryDesc = OntologyIO.getDescriptionForRequirementCategory( category.getFullName( ) );
//		String selectedLevelsCatDesc = allLevelCategoryDesc.replace( "\"" , "" );
//		String [ ] categoryDescPerLevel = allLevelCategoryDesc.split( "\\\\r\\\\n\\\\r\\\\n" );
//		boolean found = false;
//		for ( String part : categoryDescPerLevel ) {
//			String part2 = part.trim( );
//			part2 = part2.replace( "\"" , "" );
//
//			for ( String absLevelText : absLevelsLabels2IDs.keySet( ) ) {
//				if ( part2.startsWith( absLevelText ) && RequirementTab.getAbsLevel( ).getFullName( ).equals( absLevelsLabels2IDs.get( absLevelText ) ) ) {
//					part2 = part2.replaceAll( "\\\\r\\\\n" , "\n" );
//					selectedLevelsCatDesc = part2;
//					found = true;
//					break;
//				}
//			}
//			if ( found ) {
//				catDescriptionLabel.setText( selectedLevelsCatDesc );
//				break;
//			} else {
//				catDescriptionLabel.setText( "No description was found for the selected Category at this Abstraction Level." );
//			}
//		}
//
//		superClassBox.getChildren( ).clear( );
//		superClassBox.getChildren( ).addAll( superClassLabel , buildParentList( ) );
//		Node childrenList = buildChildrenList( );
//		// if ( childrenList != null ) {
//		subClassBox.getChildren( ).clear( );
//		subClassBox.getChildren( ).addAll( subClassLabel , childrenList );
//		getChildren( ).clear( );
//		getChildren( ).addAll( categoryBox , superClassBox , subClassBox );
//		// }
//	}
//
//
//	private Node buildParentList ( ) {
//
//		if ( category == null || category.getFullName( ) == null | category.getFullName( ).isEmpty( ) )
//			return new ScrollPane( new VBox( ) );
//		System.out.println( "Category: " + category.getFullName( ) );
//		ArrayList <OntologyWordModel> parents = OntologyIO.getSuperClassOfRequirementCategory( category.getFullName( ) );
//		parents.sort( new Comparator <OntologyWordModel>( ) {
//
//			@Override
//			public int compare ( OntologyWordModel o1 , OntologyWordModel o2 ) {
//
//				return o1.getShortName( ).compareTo( o2.getShortName( ) );
//			}
//		} );
//
//		if ( parents.size( ) == 0 )
//			return new Label( "No parents found for this category" );
//
//		VBox parentBox = new VBox( );
//		ArrayList <RadioButton> tempList = new ArrayList <>( );
//		ToggleGroup parentGroup = new ToggleGroup( );
//		for ( OntologyWordModel parent : parents ) {
//			RadioButton rButton = new RadioButton( parent.getShortName( ) );
//			rButton.setToggleGroup( parentGroup );
//			tempList.add( rButton );
//			rButton.setOnAction( ( ActionEvent ) -> {
//				CategoryDescriptionView.this.setCategory( parent );
//			} );
//		}
//		parentBox.getChildren( ).addAll( tempList );
//
//		ScrollPane scrollableList = new ScrollPane( parentBox );
//		scrollableList.setFitToWidth( true );
//		return scrollableList;
//	}
//
//
//	private Node buildChildrenList ( ) {
//
//		if ( category == null || category.getFullName( ) == null | category.getFullName( ).isEmpty( ) )
//			return new ScrollPane( new VBox( ) );
//		ArrayList <OntologyWordModel> children = OntologyIO.getSubclassesOfCategoryTopLevel( category.getFullName( ) );
//		children.sort( new Comparator <OntologyWordModel>( ) {
//			
//			@Override
//			public int compare ( OntologyWordModel o1 , OntologyWordModel o2 ) {
//
//				return o1.getShortName( ).compareTo( o2.getShortName( ) );
//			}
//		} );
//		if ( children.size( ) == 0 )
//				//return null;
//		 return new Label( "No children found for this category" );
//
//		VBox childrenBox = new VBox( );
//		ArrayList <RadioButton> tempList = new ArrayList <>( );
//		ToggleGroup childrenGroup = new ToggleGroup( );
//		for ( OntologyWordModel child : children ) {
//			RadioButton rButton = new RadioButton( child.getShortName( ) );
//			rButton.setToggleGroup( childrenGroup );
//			tempList.add( rButton );
//			rButton.setOnAction( ( ActionEvent ) -> {
//				CategoryDescriptionView.this.setCategory( child );
//			} );
//		}
//		childrenBox.getChildren( ).addAll( tempList );
//
//		ScrollPane scrollableList = new ScrollPane( childrenBox );
//		scrollableList.setFitToWidth( true );
//		scrollableList.prefWidthProperty( ).bind( this.prefWidthProperty( ).multiply( RADIO_LIST_WIDTH ) );
//		return scrollableList;
//	}
}
