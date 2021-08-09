/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
