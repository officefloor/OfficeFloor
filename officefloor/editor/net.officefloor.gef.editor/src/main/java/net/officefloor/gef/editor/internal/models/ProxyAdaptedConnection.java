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

package net.officefloor.gef.editor.internal.models;

import java.util.List;

import org.eclipse.gef.mvc.fx.parts.IBendableContentPart.BendPoint;

import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Proxy {@link AdaptedConnection} to enable creating connections.
 * 
 * @author Daniel Sagenschneider
 */
public class ProxyAdaptedConnection<R extends Model, O> implements AdaptedConnection<ConnectionModel> {

	/**
	 * Source {@link AdaptedConnector}.
	 */
	private final AdaptedConnector<?> sourceConnector;

	/**
	 * {@link BendPoint} instances.
	 */
	private List<BendPoint> bendPoints = null;

	/**
	 * Instantiate.
	 * 
	 * @param sourceConnector
	 *            Source {@link AdaptedConnector}.
	 */
	public ProxyAdaptedConnection(AdaptedConnector<?> sourceConnector) {
		this.sourceConnector = sourceConnector;
	}

	/**
	 * Obtains the source {@link AdaptedConnector}.
	 * 
	 * @return Source {@link AdaptedConnector}.
	 */
	public AdaptedConnector<?> getSourceAdaptedConnector() {
		return this.sourceConnector;
	}

	/**
	 * Specifies the {@link BendPoint} instances.
	 * 
	 * @param bendPoints
	 *            {@link BendPoint} instances.
	 */
	public void setBendPoints(List<BendPoint> bendPoints) {
		this.bendPoints = bendPoints;
	}

	/**
	 * Obtains the target {@link BendPoint}.
	 * 
	 * @return Target {@link BendPoint}.
	 */
	public BendPoint getTargetBendPoint() {
		if (this.bendPoints.size() >= 2) {
			return this.bendPoints.get(this.bendPoints.size() - 1);
		}
		return null;
	}

	/*
	 * ================ AdaptedConnection ====================
	 */

	@Override
	public ConnectionModel getModel() {
		return null;
	}

	@Override
	public AdaptedModel<?> getParent() {
		return null; // No parent for connections
	}

	@Override
	public AdaptedErrorHandler getErrorHandler() {
		return this.sourceConnector.getParentAdaptedConnectable().getErrorHandler();
	}

	@Override
	public AdaptedChild<?> getSource() {
		throw new IllegalStateException("Should not obtain source for " + this.getClass().getSimpleName());
	}

	@Override
	public AdaptedChild<?> getTarget() {
		throw new IllegalStateException("Should not obtain target for " + this.getClass().getSimpleName());
	}

	@Override
	public boolean canRemove() {
		return true; // as creating, visually appear able to delete
	}

	@Override
	public void remove() {
		throw new IllegalStateException("Should not remove " + this.getClass().getSimpleName());
	}

}
