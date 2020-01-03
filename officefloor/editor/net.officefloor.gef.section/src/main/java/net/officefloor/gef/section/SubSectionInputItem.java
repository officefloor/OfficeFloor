package net.officefloor.gef.section;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;

/**
 * Configuration for the {@link SubSectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionInputItem extends
		AbstractItem<SectionModel, SectionChanges, SubSectionModel, SubSectionEvent, SubSectionInputModel, SubSectionInputEvent> {

	@Override
	public SubSectionInputModel prototype() {
		return new SubSectionInputModel("Input", null, false, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getSubSectionInputs(), SubSectionEvent.ADD_SUB_SECTION_INPUT,
				SubSectionEvent.REMOVE_SUB_SECTION_INPUT);
	}

	@Override
	public void loadToParent(SubSectionModel parentModel, SubSectionInputModel itemModel) {
		parentModel.addSubSectionInput(itemModel);
	}

	@Override
	public Pane visual(SubSectionInputModel model, AdaptedChildVisualFactoryContext<SubSectionInputModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, FunctionToNextSubSectionInputModel.class,
						FunctionFlowToSubSectionInputModel.class, FunctionEscalationToSubSectionInputModel.class)
						.getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getSubSectionInputName(),
				SubSectionInputEvent.CHANGE_PUBLIC_INPUT_NAME);
	}

}