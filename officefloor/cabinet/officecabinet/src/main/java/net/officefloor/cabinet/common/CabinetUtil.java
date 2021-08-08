/*-
 * #%L
 * OfficeFloor Filing Cabinet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
