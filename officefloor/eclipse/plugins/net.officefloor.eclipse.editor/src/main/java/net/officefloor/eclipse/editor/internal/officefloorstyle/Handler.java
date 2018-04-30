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
package net.officefloor.eclipse.editor.internal.officefloorstyle;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import net.officefloor.eclipse.editor.internal.style.AbstractStyleRegistry;
import net.officefloor.eclipse.editor.internal.style.SystemStyleRegistry;

/**
 * {@link URLStreamHandler} for the {@link SystemStyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return AbstractStyleRegistry.openConnection(url);
	}

}