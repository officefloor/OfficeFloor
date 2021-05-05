/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.editor.internal.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.geometry.planar.BezierCurve;
import org.eclipse.gef.mvc.fx.parts.DefaultHoverIntentHandlePartFactory;
import org.eclipse.gef.mvc.fx.parts.IHandlePart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import javafx.scene.Node;
import net.officefloor.gef.editor.internal.models.AdaptedAction;
import net.officefloor.gef.editor.internal.models.AdaptedActions;
import net.officefloor.model.Model;

public class OfficeFloorHoverIntentHandlePartFactory extends DefaultHoverIntentHandlePartFactory {

	@Inject
	private Injector injector;

	@Override
	@SuppressWarnings("unchecked")
	protected List<IHandlePart<? extends Node>> createHoverHandlePartsForPolygonalOutline(
			IVisualPart<? extends Node> target, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {

		// Obtain the adapted actions
		AdaptedActions<Model, Object, Model> actions = target.getAdapter(AdaptedActions.class);
		if ((actions == null) || (actions.getAdaptedActions().size() == 0)) {
			return Collections.emptyList();
		}

		// Create the adapted action handle parts
		List<IHandlePart<? extends Node>> handles = new ArrayList<>();

		// Create root handle part
		HoverHandleContainerPart rootHandler = new HoverHandleContainerPart();
		injector.injectMembers(rootHandler);
		handles.add(rootHandler);

		// Load the adapter actions
		for (AdaptedAction<Model, Object, Model> action : actions.getAdaptedActions()) {

			// Create the handler part for action
			AdaptedActionHandlePart<Model, Object, Model> handlePart = this.injector
					.getInstance(AdaptedActionHandlePart.class);
			handlePart.setAdaptedAction(action);

			// Add to root handler
			rootHandler.addChild(handlePart);
		}

		// Return the handles
		return handles;
	}

	@Override
	protected List<IHandlePart<? extends Node>> createHoverHandlePartsForRectangularOutline(
			IVisualPart<? extends Node> target, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {
		return createHoverHandlePartsForPolygonalOutline(target, contextMap, segmentsProvider);
	}
}
