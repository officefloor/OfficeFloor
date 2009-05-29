/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk;

import org.eclipse.gef.requests.CreationFactory;

/**
 * Implementation of the {@link CreationFactory} that returns the tag from the
 * initiation.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInstigationTagFactory implements CreationFactory {

	/**
	 * Tag.
	 */
	private final Object tag;

	/**
	 * Initiate.
	 * 
	 * @param tag
	 *            Tag to be returned from {@link #getNewObject()}.
	 */
	public FlowInstigationTagFactory(Object tag) {
		this.tag = tag;
	}

	/*
	 * ==================== CreationFactory ===========================
	 */
	
	@Override
	public Object getNewObject() {
		return this.tag;
	}

	@Override
	public Object getObjectType() {
		return this.tag.getClass();
	}

}