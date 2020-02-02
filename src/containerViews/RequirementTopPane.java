package containerViews;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.OntologyWordModel;
import utils.Memory;

public class RequirementTopPane extends TabPane {
	
//	private static HashMap<String, Integer> absLevelOrders = new HashMap<String,Integer>();
//
//	private static final long serialVersionUID = 1L;
//	protected static final double RADIOPANE_WIDTH = 0.25;
//	protected static final double CATEGORY_DESCRIPTION_WIDTH = 0.75;
//	protected Button createNewButton;
//	protected static Button boilerPlateTemplateScreenButton;
//
//	protected VBox boilerPlateTemplates;
//	protected HashMap < OntologyWordModel , ArrayList < OntologyWordModel >> abstractionLevelsAndCategories;
//
//	protected HBox content;
//	private ScrollPane scrollPane;
//
//
//	public RequirementTopPane() {
//
//		init( );
//	}
//
//
//	protected void init( ) {
//		absLevelOrders = new HashMap<String, Integer>();
//		absLevelOrders.put( "OoSSA:SystemSpacecraft" , 0 );
//		absLevelOrders.put( "OoSSA:Avionics" , 1 );
//		absLevelOrders.put( "OoSSA:RB" , 2 );
//		absLevelOrders.put( "OoSSA:TS" , 3 );
//
//		
//		abstractionLevelsAndCategories = Memory.getAbstractionLevelsAndCategoriesTopLevel( );
//
//		this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
//		this.getTabs().addAll(absLevelTabs(abstractionLevelsAndCategories.keySet()));
//		this.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<Tab>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Tab> arg0, Tab arg1, Tab arg2) {
////				RequirementTab.setAbsLevel(arg2.getText());
//			}
//		});
////		RequirementTab.setAbsLevel(this.getTabs().get( 0 ).getText( ));
//	}
//
//
//	protected ArrayList < Tab > absLevelTabs( Set < OntologyWordModel > tabNames ) {
//
//		ArrayList < Tab > tabs = new ArrayList <>( );
//		for ( OntologyWordModel ontWord : tabNames ) {
//			Tab absLevelTab = new Tab( ontWord.getShortName( ) );
//			absLevelTab.setContent( tabContents( ontWord ) );
//			tabs.add( absLevelTab );
//		}
//		tabs.sort( new Comparator < Tab >( ) {
//
//			@Override
//			public int compare( Tab o1 , Tab o2 ) {
//
//				if(absLevelOrders.get( "OoSSA:"+o1.getText( ) ) > absLevelOrders.get( "OoSSA:"+o2.getText( ) )){
//					return 1;
//				}
//				return -1;
//			}
//		} );
//		return tabs;
//	}
//
//
//	protected BorderPane tabContents( OntologyWordModel ontWord ) {
//
//		BorderPane mainPane = new BorderPane( );
//
//		CategoryDescriptionView catDescriptionView = new CategoryDescriptionView( );
//		catDescriptionView.prefWidthProperty( ).bind( this.widthProperty( ).multiply( CATEGORY_DESCRIPTION_WIDTH ) );
//
//		FlowPane radioPane = new FlowPane( );
//		radioPane.setHgap( 5 );
//		radioPane.setVgap( 5 );
//		radioPane.setOrientation( Orientation.VERTICAL );
//		radioPane.getChildren( ).addAll( buildRadioButtonGroup( "" , ontWord , catDescriptionView ) );
//		radioPane.prefWidthProperty( ).bind( this.widthProperty( ).multiply( RADIOPANE_WIDTH ) );
//
//		scrollPane = new ScrollPane( radioPane );
//		scrollPane.setPadding( new Insets( 10 ) );
//		scrollPane.setFitToWidth( true );
//		scrollPane.setMinWidth( 100 );
//
//		mainPane.setPadding( new Insets( 20 ) );
//		mainPane.setTop( searchBar( ontWord , radioPane , catDescriptionView ) );
//		mainPane.setLeft( scrollPane );
//		mainPane.setCenter( catDescriptionView );
//
//		return mainPane;
//	}
//
//
//	protected HBox searchBar( OntologyWordModel tabName , FlowPane radioPane , CategoryDescriptionView catDescriptionView ) {
//
//		HBox searchBarPanel = new HBox( 10 );
////		searchBarPanel.setPadding( new Insets( 10 ) );
////
////		TextField searchField = new TextField( );
////		searchField.setTooltip( new Tooltip( "Search" ) );
////		searchField.setPromptText( "Search all categories..." );
////
////		searchField.textProperty( ).addListener( new ChangeListener < String >( ) {
////
////			@Override
////			public void changed( ObservableValue < ? extends String > arg0 , String arg1 , String arg2 ) {
////
////				radioPane.getChildren( ).clear( );
////				radioPane.getChildren( ).addAll( buildRadioButtonGroup( searchField.getText( ) , tabName , catDescriptionView ) );
////			}
////		} );
////		
////		Button showAllBLPs = new Button( "Show All Boilerplates" );
////		showAllBLPs.setOnAction( ( ActionEvent ) -> {
////			RequirementTab.setCategory( new OntologyWordModel( "" ) );
////			RequirementTab.switchToBLPNavigator( new OntologyWordModel( "" ) );
////		} );
////
////		searchBarPanel.getChildren( ).addAll( searchField , showAllBLPs);
//
//		return searchBarPanel;
//	}
//
//
//	protected ArrayList<RadioButton> buildRadioButtonGroup(String filter, OntologyWordModel tabName, CategoryDescriptionView catDescriptionView) {
//		ArrayList<RadioButton> buttons = new ArrayList<>();
//		ToggleGroup categoryGroup = new ToggleGroup();
//
//		ArrayList<OntologyWordModel> categories = 
//				(filter.length() == 0) ? 
//						Memory.getAbstractionLevelsAndCategoriesTopLevel().get(tabName) :
//						Memory.getAbstractionLevelsAndCategoriesFlat().get(tabName);
//		categories.sort(new Comparator<OntologyWordModel>() {
//
//			@Override
//			public int compare(OntologyWordModel o1, OntologyWordModel o2) {
//				return o1.getShortName().compareTo(o2.getShortName());
//			}
//		});
//		
//		
//		if (categories.size() != 0)
//			for (OntologyWordModel categoryWord : categories) {
//				String category = categoryWord.getShortName();
//				
//				if (filter.length() == 0 || category.contains(filter)) {
//					RadioButton rb;
//					rb = new RadioButton(category);
//					rb.setToggleGroup(categoryGroup);
//					buttons.add(rb);
//					rb.setOnAction((ActionEvent) -> {
//							RequirementTab.setCategory(categoryWord);
//							catDescriptionView.setCategory(categoryWord);
//						});
//				}
//			}
//
//		return buttons;
//	}
}
