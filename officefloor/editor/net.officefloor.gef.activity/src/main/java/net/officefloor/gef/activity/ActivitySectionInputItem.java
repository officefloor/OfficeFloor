package net.officefloor.gef.activity;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityInputToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityProcedureNextToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityProcedureOutputToActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel.ActivitySectionInputEvent;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.activity.model.ActivitySectionOutputToActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionModel.ActivitySectionEvent;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;

/**
 * Configuration for the {@link ActivitySectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivitySectionInputItem extends
		AbstractItem<ActivityModel, ActivityChanges, ActivitySectionModel, ActivitySectionEvent, ActivitySectionInputModel, ActivitySectionInputEvent> {

	@Override
	public ActivitySectionInputModel prototype() {
		return new ActivitySectionInputModel("Input", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getInputs(), ActivitySectionEvent.ADD_INPUT,
				ActivitySectionEvent.REMOVE_INPUT);
	}

	@Override
	public void loadToParent(ActivitySectionModel parentModel, ActivitySectionInputModel itemModel) {
		parentModel.addInput(itemModel);
	}

	@Override
	public Pane visual(ActivitySectionInputModel model,
			AdaptedChildVisualFactoryContext<ActivitySectionInputModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, ActivityInputToActivitySectionInputModel.class,
						ActivityExceptionToActivitySectionInputModel.class,
						ActivityProcedureNextToActivitySectionInputModel.class,
						ActivityProcedureOutputToActivitySectionInputModel.class,
						ActivitySectionOutputToActivitySectionInputModel.class).getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivitySectionInputName(),
				ActivitySectionInputEvent.CHANGE_ACTIVITY_SECTION_INPUT_NAME);
	}

}