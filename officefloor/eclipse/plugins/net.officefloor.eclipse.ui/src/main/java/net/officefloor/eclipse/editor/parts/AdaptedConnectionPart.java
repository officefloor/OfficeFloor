/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.fx.nodes.Connection;

import com.google.common.collect.SetMultimap;

import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.model.ConnectionModel;

public class AdaptedConnectionPart<C extends ConnectionModel>
		extends AbstractAdaptedModelPart<C, AdaptedConnection<C>, Connection> {

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected Connection doCreateVisual() {
		return new Connection();
	}

	@Override
	protected void doRefreshVisual(Connection visual) {
		// TODO Auto-generated method stub

	}

}