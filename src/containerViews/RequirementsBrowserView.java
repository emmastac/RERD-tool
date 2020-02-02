package containerViews;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Border;
import javafx.util.Callback;
import models.RequirementModel;
import utils.Memory;
import childViews.RequirementStatusView;

public class RequirementsBrowserView extends TableView<RequirementModel> {

	private static final long serialVersionUID = 1L;
	private static ObservableList<RequirementModel> contents;


	public RequirementsBrowserView () {

		reBuildTable();
		this.setBorder(Border.EMPTY);
	}
	
	public void reBuildTable(){
		
		TableColumn<RequirementModel,String> reqIDColumn = new TableColumn<>("Req. ID");
		reqIDColumn.setCellValueFactory(new PropertyValueFactory<>("reqIDNoNS"));
		
		TableColumn<RequirementModel,Button> editColumn = new TableColumn<>("Edit");
		editColumn.setCellValueFactory(new PropertyValueFactory<RequirementModel,Button>("editButton"));
		
		TableColumn<RequirementModel,RequirementStatusView> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusView"));
		
		TableColumn<RequirementModel,String> textColumn = new TableColumn<>("Text");
		textColumn.setCellValueFactory(new PropertyValueFactory<>("displayForm"));
		
		TableColumn<RequirementModel,String> catIDColumn = new TableColumn<>("Category");
		catIDColumn.setCellValueFactory(new PropertyValueFactory<>("catID"));
		
		TableColumn<RequirementModel,String> absLevelColumn = new TableColumn<>("AbsLevel");
		absLevelColumn.setCellValueFactory(new PropertyValueFactory<>("absLevel"));
		
		
		TableColumn<RequirementModel, Boolean> deleteColumn = new TableColumn<>("Delete");
		deleteColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RequirementModel,Boolean>, ObservableValue<Boolean>>() {

			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<RequirementModel, Boolean> arg0) {
				return new SimpleBooleanProperty(arg0.getValue() != null);
			}
		});
		
		deleteColumn.setCellFactory(new Callback<TableColumn<RequirementModel,Boolean>, TableCell<RequirementModel,Boolean>>() {
			
			@Override
			public TableCell<RequirementModel, Boolean> call(TableColumn<RequirementModel, Boolean> arg0) {
				return new DeleteCell();
			}
		});
		
		getColumns().addAll(reqIDColumn, statusColumn, textColumn, catIDColumn, absLevelColumn, editColumn, deleteColumn);
		
		initContents();
	}
	
	public void initContents(){
		
		this.getItems().clear();
		contents = Memory.getRequirementsInMemory(); 
		this.getItems().addAll(contents);
	}
	
	/**
	 * Replaces all currently shown requirements with requirements that contain the search string.
	 * @param searchString
	 */
	public void refreshContents(String searchString){

		this.getItems().clear();
		//TODO: why do we need to create an extra list?
		ObservableList<RequirementModel> filteredItems = FXCollections.observableArrayList();
		for (RequirementModel reqMod : contents){
			if (searchString.length()==0 || reqMod.contains(searchString)){
				filteredItems.add(reqMod);
			}
		}
			
		this.getItems().addAll(filteredItems);
	}
	
	private class DeleteCell extends TableCell<RequirementModel, Boolean>{
		final Button delbutton = new Button("Delete");
		
		public DeleteCell() {
			delbutton.setOnAction((ActionEvent) -> {
					RequirementModel thisBp = DeleteCell.this.getTableView().getItems().get(DeleteCell.this.getIndex());
					RequirementsBrowserView.this.getItems().remove(thisBp);
					RequirementsBrowserView.contents.remove(thisBp);
				});
		}

		@Override
		protected void updateItem(Boolean t, boolean empty){
			super.updateItem(t, empty);
			if (!empty) {
				setGraphic(delbutton);
			}
			else {
				setGraphic(null);
			}
		}
	}
	
	
	
	

	/*
	private String createCommonInterval(int left, boolean leftClosed, int right, boolean rightClosed ){
		String s = "interval:interval-left:";
		if(left==-1){
			s += "(..:;";
		}else{
			String leftbracket = (leftClosed)? "left-bracket:[:;" : "left-bracket:(:;";
			s += "inter-left-eval:"+leftbracket+"evaluation:literal-value:"+left+";";
		}
		s+=",:;interval-right:";
		if(right==-1){
			s += "..):;";
		}else{
			String rightbracket = (rightClosed)? "right-bracket:]:;" : "right-bracket:):;";
			s += "inter-right-eval:evaluation:literal-value:"+right+";"+rightbracket;
		}
		return s;
	}
	
	private void loadPropertiesExample ( ) {
		String intervalX = createCommonInterval(5,true,7,true);
		String proposition="proposition:logicExpressHolds:logic-expression:evalQuantity:evaluation:variable:variableHolder:G:;.:;variableName:XVariable;in:;quant-interval:"+intervalX+"holds:;";
		TemplatePropertyPattern sample1 = new TemplatePropertyPattern( "p1" ,
				"p1:Whenever:;behPart:beh:simple-beh:simple-beh2:"+proposition+", then:;behPart:beh:simple-beh:simple-beh2:proposition:componInMode:component:SMU2;is in:;mode:Xmode2;occursMode:does-not-occur:;" );
		contents.add( sample1 );
		
		String event="event:eventType:compPort:component:TcAcq;.:;port:ackTC;occursMode:occurs:;";
		String interval = createCommonInterval(8,true,-1,false);
		String intervalTimes="intervalTimes:"+interval+"times:;";
		String duringTime="duringTime:during:;time-interval:"+createCommonInterval(1,true,1,true)+"time-unit:s:;";
		String always="p2:Always:;behPart:beh:simple-beh:simple-beh1:"+event+intervalTimes+duringTime;
		TemplatePropertyPattern sample2 = new TemplatePropertyPattern( "p1" ,
				always );
		contents.add( sample2 );
		
		
//		String simplebeh1 = "simple-beh:simple-beh1:event:eventType:compPort:component:TcAcq;.:;port:acqTC;occursMode:occurs:;letPart:let:;localDefinition:variableName:t1;:=:;evaluation:variable:variableHolder:component:OBT;.:;variableName:time;";
//		String simplebeh2 = "simple-beh:simple-beh1:event:eventType:compPort:component:TcAcq;.:;port:acqTC;occursMode:occurs:;letPart:let:;localDefinition:variableName:t2;:=:;evaluation:variable:variableHolder:component:OBT;.:;variableName:time;";
//		String simplebeh3 = "simple-beh:simple-beh1:event:eventType:compPort:component:TcAcq;.:;port:acqTC;occursMode:occurs:;letPart:let:;localDefinition:variableName:t3;:=:;evaluation:variable:variableHolder:component:OBT;.:;variableName:time;";
//		String complexbeh2 = "complex-beh:beh:"+simplebeh2+"beh-operator:SEQ:;beh:"+simplebeh3;
//		String complexbeh1= "complex-beh:beh:"+simplebeh1+"beh-operator:SEQ:;beh:"+complexbeh2;
//		String wheneverX = "Whenever:;behPart:beh:"+complexbeh1;
//		String thenX = ", then:;behPart:beh:simple-beh:simple-beh:simple-beh2:proposition:logicExpressHolds:logic-expression:compl-eval:evaluation:expression:t3-t2;comparator:==:;evaluation:expression:t2-t1;holds:;";
//		String prop3 = "p1:"+wheneverX+thenX;
//		TemplatePropertyPattern sample3 = new TemplatePropertyPattern( "p1" , prop3 );
//		contents.add( sample3 );
	}*/

}
