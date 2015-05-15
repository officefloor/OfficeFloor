/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.template;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;

/**
 * Interface to write the template content to {@link ServerWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpTemplateWriter {

	/**
	 * Writes the template content to the {@link ServerWriter}.
	 * 
	 * @param writer
	 *            {@link ServerWriter} to receive the template content.
	 * @param isDefaultCharset
	 *            Indicates if the default {@link Charset} for outputting the
	 *            template is being used. While the {@link Charset} may be
	 *            programmatically changed, it is expected in the majority of
	 *            cases to be using the default {@link Charset} configured for
	 *            the template. This flag allows static content to be cached in
	 *            bytes (using default {@link Charset}) for improved
	 *            performance.
	 * @param bean
	 *            Bean to potentially obtain data. May be <code>null</code> if
	 *            template contents does not require a bean.
	 * @param location
	 *            {@link HttpApplicationLocation} to assist in providing web
	 *            location.
	 * @throws IOException
	 *             If fails to write content.
	 */
	void write(ServerWriter writer, boolean isDefaultCharset, Object bean,
			HttpApplicationLocation location) throws IOException;

}