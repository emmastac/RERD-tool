package containerViews;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import models.OntologyWordModel;
import utils.ExtStrings;
import viewWrappers.MinimizeWrapper;

public class ModelsTab extends Tab {

	private static final String ONT_BROWSER_TITLE = "Component models";
	private static final String ONT_EDIT_PANEL_TITLE = "Edit code browser";
	public static SimpleObjectProperty < String > selectedModel;
	public static SimpleObjectProperty < String > selectedArch;
	// /public static SimpleObjectProperty<OntologyWordModel> selectedInstance;
	private ModelsBrowser modelsBrowser;
	private ModelsEditPanel modelsEditingPanel;
	private ArchitecturesBrowser archBrowser;
	private ArchitecturesEditPanel archEditingPanel;
	private HBox modelsPane;
	private static final double BROWSER_HEIGHT = ExtStrings.getInt( "BrowserHeight" );
	private static final double INFO_PANEL_HEIGHT = ExtStrings.getInt( "InfoPanelHeight" );


	public ModelsTab() {

		init( );
	}


	protected void init( ) {

		selectedModel = new SimpleObjectProperty < String >( );
		selectedArch = new SimpleObjectProperty < String >( );

		this.setText( "Models" );

		modelsPane = new HBox( );
		modelsPane.getChildren( ).add( new MinimizeWrapper( modelsBrowser = new ModelsBrowser( ) , ONT_BROWSER_TITLE ) );
		modelsPane.getChildren( ).add( new MinimizeWrapper( modelsEditingPanel = new ModelsEditPanel( ) , ONT_EDIT_PANEL_TITLE ) );
		modelsBrowser.prefHeightProperty( ).bind( modelsPane.heightProperty( ).multiply( BROWSER_HEIGHT ) );
		modelsEditingPanel.prefHeightProperty( ).bind( modelsPane.heightProperty( ).multiply( INFO_PANEL_HEIGHT ) );
		
		modelsPane.getChildren( ).add( new MinimizeWrapper( archBrowser = new ArchitecturesBrowser( ) , ONT_BROWSER_TITLE ) );
		modelsPane.getChildren( ).add( new MinimizeWrapper( archEditingPanel = new ArchitecturesEditPanel( ) , ONT_EDIT_PANEL_TITLE ) );
		archBrowser.prefHeightProperty( ).bind( modelsPane.heightProperty( ).multiply( BROWSER_HEIGHT ) );
		archEditingPanel.prefHeightProperty( ).bind( modelsPane.heightProperty( ).multiply( INFO_PANEL_HEIGHT ) );

		selectedModel.addListener( new ChangeListener < String >( ) {

			@Override
			public void changed( ObservableValue < ? extends String > arg0 , String arg1 , String arg2 ) {

				if ( arg2 != null ) {
					modelsBrowser.updatePanels( );
					modelsEditingPanel.drawClassView( );
				}
			}
		} );

		selectedArch.addListener( new ChangeListener < String >( ) {

			@Override
			public void changed( ObservableValue < ? extends String > arg0 , String arg1 , String arg2 ) {

				if ( arg2 != null ) {
					archBrowser.updatePanels( );
					archEditingPanel.drawClassView( );
				}
			}
		} );

		modelsPane.setPadding( new Insets( 20 ) );

		this.setContent( modelsPane );
	}
}
