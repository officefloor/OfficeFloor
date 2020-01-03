package net.officefloor.gef.editor.internal.parts;

import java.util.List;
import java.util.Map;

import org.eclipse.gef.mvc.fx.parts.DefaultHoverFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.IFeedbackPart;
import org.eclipse.gef.mvc.fx.parts.IFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import javafx.scene.Node;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link IFeedbackPartFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHoverFeedbackPartFactory extends DefaultHoverFeedbackPartFactory {

	@Override
	public List<IFeedbackPart<? extends Node>> createFeedbackParts(List<? extends IVisualPart<? extends Node>> targets,
			Map<Object, Object> contextMap) {
		return OfficeFloorContentPartFactory.createFeedbackParts(targets,
				(parts) -> super.createFeedbackParts(parts, contextMap));
	}

}
