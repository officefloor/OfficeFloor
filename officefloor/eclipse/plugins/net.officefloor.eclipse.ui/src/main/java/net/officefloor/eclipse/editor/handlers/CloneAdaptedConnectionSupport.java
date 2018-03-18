/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.handlers;

import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.models.ProxyAdaptedConnection;
import net.officefloor.eclipse.editor.parts.AdaptedConnectionPart;

public class CloneAdaptedConnectionSupport extends AbstractCloneContentSupport {

	@Override
	public Object cloneContent() {
		AdaptedConnection<?> original = getAdaptable().getContent();
		return new ProxyAdaptedConnection(original.getSource());
	}

	@Override
	public AdaptedConnectionPart<?> getAdaptable() {
		return (AdaptedConnectionPart<?>) super.getAdaptable();
	}
}
