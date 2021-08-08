/*-
 * #%L
 * OfficeFloor Filing Cabinet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.cabinet.common;

import java.util.UUID;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Document.DocumentNameContext;
import net.officefloor.cabinet.Document.DocumentNamer;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;

/**
 * Utility methods for {@link OfficeCabinet} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetUtil {

	/**
	 * Obtains the {@link Document} name for type.
	 * 
	 * @param documentType {@link Document} type.
	 * @return Name of {@link Document} type.
	 * @throws Exception If fails to obtain name.
	 */
	public static String getDocumentName(Class<?> documentType) throws Exception {

		// Obtain the annotation
		Document document = documentType.getAnnotation(Document.class);

		// Obtain the table name
		Class<? extends DocumentNamer> documentNamerClass = document.documentNamer();
		DocumentNamer documentNamer = documentNamerClass.getConstructor().newInstance();
		return documentNamer.getName(new DocumentNameContext() {

			@Override
			public Document getDocument() {
				return document;
			}

			@Override
			public Class<?> getDocumentClass() {
				return documentType;
			}
		});
	}

	/**
	 * Obtains new {@link Key} value.
	 * 
	 * @return New {@link Key} value.
	 */
	public static String newKey() {
		return UUID.randomUUID().toString();
	}

	/**
	 * All access via static methods.
	 */
	private CabinetUtil() {
	}
}
