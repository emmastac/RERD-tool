package containerViews;

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
import utils.OntologyIO;
import viewWrappers.MinimizeWrapper;

public class OntologyEditPanel extends BorderPane {

	public OntologyEditPanel() {
		init();
	}

	private void init() {

		this.setPadding(new Insets(10));
		this.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(10.0), new BorderWidths(1))));
	}

	private ScrollPane classPanel() {

		VBox propertiesBox = new VBox(10);
		ScrollPane scrollPane = new ScrollPane(propertiesBox);
		scrollPane.setPadding(new Insets(10));
		scrollPane.setFitToWidth(true);

		String fullName = DictionaryTab.selectedClass.get().getFullName();

		TextField label = new TextField(fullName);
		label.setEditable(false);
		label.setBackground(Background.EMPTY);

		String descriptionStr = "";
		for (String descString : OntologyIO.getDescriptionForClass(fullName)) {
			descriptionStr += descString;
		}
		TextArea descriptionField = new TextArea(descriptionStr != null && !descriptionStr.isEmpty()
				? descriptionStr.replace("\\r\\n\\r\\n", "\n\n") : "");
		descriptionField.setEditable(false);
		descriptionField.setPrefRowCount(3);

		propertiesBox.getChildren().addAll(label, descriptionField);

		OntologyIO.getParentClassesForClass(fullName).forEach(l -> {
			VBox parentBox = new VBox(5);
			parentBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(5.0),
					new BorderWidths(0.5))));
			parentBox.getChildren().add(new Label("Inherited from parent : " + l.getFullName()));
			parentBox.getChildren().add(new Label("Domains:"));
			OntologyIO.getRangePropertiesForClass(l.getFullName()).forEach(l2 -> {
				parentBox.getChildren().add(new Label(l2.getFullName()));
			});
			parentBox.getChildren().add(new Label("Ranges:"));
			OntologyIO.getDomainPropertiesForClass(l.getFullName()).forEach(l2 -> {
				parentBox.getChildren().add(new Label(l2.getFullName()));
			});
			propertiesBox.getChildren().add(new MinimizeWrapper(parentBox, l.getFullName()));
		});

		return scrollPane;
	}

	private ScrollPane instancePanel() {

		VBox propertiesBox = new VBox(10);
		ScrollPane scrollPane = new ScrollPane(propertiesBox);
		scrollPane.setPadding(new Insets(10));
		scrollPane.setFitToWidth(true);

		String fullName = DictionaryTab.selectedInstance.get().getFullName();

		Label label = new Label("Instance : " + fullName);

		Label classLabel = new Label("Class : " + DictionaryTab.selectedClass.get().getFullName());

		String serialized = OntologyIO.getSerializedFor(DictionaryTab.selectedInstance.get().getFullName());
		Label ser = new Label();
		if (serialized != null && !serialized.isEmpty())
			ser.setText("Serialized : " + serialized);
		else
			ser.setText("Serialized : ");

		propertiesBox.getChildren().addAll(label, classLabel, ser);

		OntologyIO.getParentClassesForClass(DictionaryTab.selectedClass.get().getFullName()).forEach(l -> {
			VBox parentBox = new VBox(5);
			parentBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(5.0),
					new BorderWidths(0.5))));
			parentBox.getChildren().add(new Label("Inherited from parent : " + l.getFullName()));
			parentBox.getChildren().add(new Label("Domains:"));
			OntologyIO.getRangePropertiesForClass(l.getFullName()).forEach(l2 -> {
				parentBox.getChildren().add(new Label(l2.getFullName()));
			});
			parentBox.getChildren().add(new Label("Ranges:"));
			OntologyIO.getDomainPropertiesForClass(l.getFullName()).forEach(l2 -> {
				parentBox.getChildren().add(new Label(l2.getFullName()));
			});
			propertiesBox.getChildren().add(new MinimizeWrapper(parentBox, l.getFullName()));
		});

		return scrollPane;
	}

	public void drawClassView() {

		this.setCenter(classPanel());
	}

	public void drawInstanceView() {

		this.setCenter(instancePanel());
	}
}
