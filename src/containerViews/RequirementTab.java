package containerViews;

import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import models.GenericBoilerPlate;
import models.RequirementModel;
import utils.ExtStrings;
import viewWrappers.MinimizeWrapper;
import viewWrappers.RequirementEditViewWrapper;
import viewWrappers.RequirementsBrowserPanelWrapper;

public class RequirementTab extends Tab {
	
	private static VBox requirementPane;
// removed apriori category selection
//	private static RequirementTopPane categorySelector;
	private static RequirementEditViewWrapper mainEditingPanel;
	private static BoilerPlateNavigatorView bLPNavigator;
	private static RequirementsBrowserPanelWrapper reqBrowser;
	private static MinimizeWrapper blPNavigatorWrapper;
	private static MinimizeWrapper categorySelectorWrapper;
	private static MinimizeWrapper reqBrowserWrapper;

	protected static final String 
		CAT_SELECTOR_TITLE = ExtStrings.getString("CatSelector"),
		BLP_SELECTOR_TITLE = ExtStrings.getString("BLPSelector"),
		REQ_EDIT_PANEL_TITLE = ExtStrings.getString("ReqEditPanel"),
		REQ_BROWSER_TITLE = ExtStrings.getString("ReqBrowser");

	public RequirementTab(){
		
		init();
	}
	
	protected void init() {
		
		this.setText("Requirement Editing" );
		
		requirementPane = new VBox();
//		requirementPane.getChildren().add(categorySelectorWrapper = new MinimizeWrapper(categorySelector = new RequirementTopPane(),CAT_SELECTOR_TITLE));
		requirementPane.getChildren().add(blPNavigatorWrapper = new MinimizeWrapper(bLPNavigator =  new BoilerPlateNavigatorView(""),BLP_SELECTOR_TITLE));
		requirementPane.getChildren().add(new MinimizeWrapper(mainEditingPanel = new RequirementEditViewWrapper(),REQ_EDIT_PANEL_TITLE));
		requirementPane.getChildren().add(reqBrowserWrapper = new MinimizeWrapper(reqBrowser = new RequirementsBrowserPanelWrapper(),REQ_BROWSER_TITLE));
		
		this.setContent(requirementPane);
	}
	
//	public static void switchToCategorySelection(){
//
//		if (requirementPane.getChildren().contains(blPNavigatorWrapper))
//			requirementPane.getChildren().remove(blPNavigatorWrapper);
//		else
//			requirementPane.getChildren().remove(categorySelectorWrapper);
//		requirementPane.getChildren().add(0, categorySelectorWrapper);
//	}
	
//	public static void switchToBLPNavigator(OntologyWordModel category){
//		
//		bLPNavigator.setCategory(category);
//		if (requirementPane.getChildren().contains(categorySelectorWrapper))
//			requirementPane.getChildren().remove(categorySelectorWrapper);
//		else
//			requirementPane.getChildren().remove(blPNavigatorWrapper);
//		requirementPane.getChildren().add(0, blPNavigatorWrapper);
//	}
	
	public static void sendMainForEditing(GenericBoilerPlate genBP){
		
		mainEditingPanel.sendMainForEditing(genBP);
	}
	
	public static void sendForEditing(RequirementModel reqModel){
		
		mainEditingPanel.sendForEditing(reqModel);
	}

	public static void sendPrefixForEditing(GenericBoilerPlate prefix) {

		mainEditingPanel.sendPrefixForEditing(prefix);
	}

	public static void sendSuffixForEditing(GenericBoilerPlate suffix) {
		
		mainEditingPanel.sendSuffixForEditing(suffix);
	}

	public static void reloadPanel() {
		
//		requirementPane.getChildren().remove(reqBrowserWrapper);
//		requirementPane.getChildren().add(2,reqBrowserWrapper = new MinimizeWrapper(reqBrowser = new RequirementsBrowserPanelWrapper(),REQ_BROWSER_TITLE));
		reqBrowser.refreshEditingPanel();
	}
	
//	public static void setAbsLevel(OntologyWordModel newAbsLevel){
//		
//		bLPNavigator.setAbsLevel(newAbsLevel);
//	}
//	
//	public static void setAbsLevel(){
//		
//		String shortName = categorySelector.getSelectionModel().getSelectedItem().getText();
//		OntologyWordModel categoryWithNamespace = new OntologyWordModel("");
//		HashMap<OntologyWordModel, ArrayList<OntologyWordModel>> absLevelsAndCatsTemp = Memory.getAbstractionLevelsAndCategoriesTopLevel();
//		absLevelsAndCatsTemp.keySet().forEach((v) -> {
//			if (v.getShortName().equals(shortName));
//				categoryWithNamespace.setSelf(v);
//		});
//		bLPNavigator.setAbsLevel(categoryWithNamespace);
//	}
//	
//	public static void setAbsLevel(String shortName){
//		
//		OntologyWordModel absWithNameSpace = new OntologyWordModel("");
//		HashMap<OntologyWordModel, ArrayList<OntologyWordModel>> absLevelsAndCatsTemp = Memory.getAbstractionLevelsAndCategoriesTopLevel();
//		absLevelsAndCatsTemp.keySet().forEach((v) -> {
//			if (v.getShortName().equals(shortName)){
//				absWithNameSpace.setSelf(v);
//			}
//		});
//		bLPNavigator.setAbsLevel(absWithNameSpace);
//	}
//
//	public static void setCategory(OntologyWordModel category) {
//		
//		bLPNavigator.setCategory(category);
//	}
//	
//	public static OntologyWordModel getCategory(){
//		return bLPNavigator.getCategoryBox().getCategoryName();
//	}
//	
//	public static OntologyWordModel getAbsLevel(){
//		return bLPNavigator.getCategoryBox().getAbsLevel();
//	}
//	
	
}
