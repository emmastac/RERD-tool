package containerViews;

import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import models.PropertyModel;
import models.PropertyPattern;
import utils.ExtStrings;
import viewWrappers.MinimizeWrapper;
import viewWrappers.PropertyBrowserPanelWrapper;
import viewWrappers.PropertyEditViewWrapper;

public class PropertyTab extends Tab {

	protected static VBox propertyPane;
	protected static PropertyEditViewWrapper editPanel;
	protected static final String 
		PTTRN_SELECTOR_TITLE = ExtStrings.getString("PTTRNSelector"),
		PRP_EDIT_PANEL_TITLE = ExtStrings.getString("PrpEditPanel"),
		PROP_BROWSER_TITLE = ExtStrings.getString("PropBrowser");

	public PropertyTab(){
		
		init();
	}
	
	protected void init() {
		
		this.setText("Property Formalization" );
		
		propertyPane = new VBox();
		propertyPane.getChildren().add(new MinimizeWrapper(new PropertyPatternBrowser(),PTTRN_SELECTOR_TITLE));
		propertyPane.getChildren().add(new MinimizeWrapper(editPanel = new PropertyEditViewWrapper(),PRP_EDIT_PANEL_TITLE));
		propertyPane.getChildren().add(new MinimizeWrapper(new PropertyBrowserPanelWrapper(),PROP_BROWSER_TITLE));
		
		this.setContent(propertyPane);
	}
	
	public static void sendPatternForEditing(PropertyPattern propertyPattern) {
		editPanel.sendPatternForEditing(propertyPattern);
	}

	public static void sendForEditing(PropertyModel propertyModel) {
		editPanel.sendForEditing(propertyModel);
	}

	public static void reloadPanel() {
		propertyPane.getChildren().remove(2);
		propertyPane.getChildren().add(2,new MinimizeWrapper(new PropertyBrowserPanelWrapper(),PROP_BROWSER_TITLE));
	}
	
//	private HBox topPanel() {
//		HBox topPane = new HBox();
//		topPane.setBorder(Border.EMPTY);
//		
//		String firstEntity = "entity:simple-entity:this;";
//		String state = "state:" + firstEntity + "in:;mode:Connected;";
//		String prefix = "prefix:simple-prefix:p1:ifwhile:if:;event:error occurs;";
//		// String prefix = "";
//		String main = "main:m2:subject:system:TCManager;maymode:may not:;verb:allow;" + state;
//		TemplateBoilerPlate msample3 = new TemplateBoilerPlate( "m2" , "boilerplate:" + prefix + main );
//
//		HBox referReqPanel = new HBox( );
//		referReqPanel.getChildren().add( new Label("Referenced Requirement: ") );
//		referReqPanel.getChildren().addAll(msample3.getChildren());
//		//referReqPanel.add( msample3 );
////			for(Node c : msample3.getChildren()){
////
////				referReqPanel.getChildren().add(c);
////				referReqPanel.getChildren().add(new Label("   "));
////			}
//		topPane.getChildren().add( referReqPanel  );
//		
//		topPane.setSpacing(10);
//		topPane.setPadding(new Insets(10,10,10,10));
//		topPane.setBorder(Border.EMPTY);
//		topPane.setAlignment(Pos.CENTER);
////		topPane.setBackground(color); TODO Add css here
//		return topPane;
//	}
}
