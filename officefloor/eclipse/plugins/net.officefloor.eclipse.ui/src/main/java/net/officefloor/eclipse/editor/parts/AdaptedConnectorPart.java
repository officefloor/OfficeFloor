/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import net.officefloor.eclipse.editor.models.AdaptedConnector;

/**
 * {@link IContentPart} for the {@link AdaptedConnector}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectorPart extends AbstractContentPart<GeometryNode<?>> {

	@Override
	public AdaptedConnector<?> getContent() {
		return (AdaptedConnector<?>) super.getContent();
	}

	@Override
	public void setContent(Object content) {
		if (content != null && !(content instanceof AdaptedConnector)) {
			throw new IllegalArgumentException("Only " + AdaptedConnector.class.getSimpleName() + " supported.");
		}
		super.setContent(content);
	}

	@Override
	protected void doAttachToContentAnchorage(Object contentAnchorage, String role) {
		// TODO Auto-generated method stub
		super.doAttachToContentAnchorage(contentAnchorage, role);
	}

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected void doDetachFromContentAnchorage(Object contentAnchorage, String role) {
		// TODO Auto-generated method stub
		super.doDetachFromContentAnchorage(contentAnchorage, role);
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected GeometryNode<?> doCreateVisual() {
		return this.getContent().getGeometryNode();
	}

	@Override
	protected void doRefreshVisual(GeometryNode<?> visual) {
	}

}
