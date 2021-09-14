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

import java.lang.reflect.Field;
import java.util.UUID;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Document.DocumentNameContext;
import net.officefloor.cabinet.Document.DocumentNamer;
import net.officefloor.cabinet.common.key.DocumentKey;
import net.officefloor.cabinet.common.key.FieldDocumentKey;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.Key;

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
	 * Obtains the {@link DocumentKey} for the {@link Document}.
	 * 
	 * @param documentType {@link Document} type.
	 * @return {@link DocumentKey}.
	 * @throws Exception If fails to obtain the {@link DocumentKey}.
	 */
	public static <D> DocumentKey<D> getDocumentKey(Class<D> documentType) throws Exception {

		// Search out the key
		Field keyField = null;
		Class<?> interrogate = documentType;
		do {

			// Load the attributes
			for (Field field : interrogate.getDeclaredFields()) {

				// Determine if key
				Key key = field.getAnnotation(Key.class);
				if (key != null) {

					// Ensure only one key
					if (keyField != null) {
						throw new IllegalStateException("More than one " + Key.class.getSimpleName() + " ("
								+ keyField.getName() + ", " + field.getName() + ") on class " + documentType.getName());
					}

					// Capture the key
					keyField = field;
				}
			}

			// Interrogate parent
			interrogate = interrogate.getSuperclass();
		} while (interrogate != null);

		// Ensure have key
		if (keyField == null) {
			throw new IllegalStateException("Must annotate at one field with " + Key.class.getSimpleName()
					+ " for document type " + documentType.getName());
		}

		// Ensure the field is a String
		Class<?> keyFieldType = keyField.getType();
		if (!String.class.equals(keyFieldType)) {
			throw new IllegalStateException(
					"Field " + keyField.getName() + " must be a " + String.class.getSimpleName() + " as annotated with "
							+ Key.class.getSimpleName() + " for document type " + documentType.getName());
		}

		// Return the document key
		return new FieldDocumentKey<>(keyField);
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
	 * Processor of {@link Field}.
	 */
	@FunctionalInterface
	public static interface FieldProcessor {

		/**
		 * Process the {@link Field}.
		 * 
		 * @param context {@link FieldProcessorContext}.
		 * @throws Exception If fails processing {@link Field}.
		 */
		void processField(FieldProcessorContext context) throws Exception;
	}

	/**
	 * Context for {@link FieldProcessor}.
	 */
	public static interface FieldProcessorContext {

		/**
		 * Obtains the {@link Field}.
		 * 
		 * @return {@link Field}.
		 */
		Field getField();

		/**
		 * Obtains whether is a {@link Key}.
		 * 
		 * @return <code>true</code> if {@link Key}.
		 */
		boolean isKey();
	}

	/**
	 * Process the {@link Field} of the {@link Document} type.
	 * 
	 * @param documentType   {@link Document} type.
	 * @param fieldProcessor {@link FieldProcessor}.
	 * @throws Exception If fails processing the {@link Field} instances.
	 */
	public static void processFields(Class<?> documentType, FieldProcessor fieldProcessor) throws Exception {
		Class<?> interrogate = documentType;
		do {

			// Process the fields
			for (Field field : interrogate.getDeclaredFields()) {

				// Determine if key
				Key key = field.getAnnotation(Key.class);
				boolean isKey = key != null;

				// Process the field
				fieldProcessor.processField(new FieldProcessorContext() {

					@Override
					public Field getField() {
						return field;
					}

					@Override
					public boolean isKey() {
						return isKey;
					}
				});
			}

			// Interrogate parent
			interrogate = interrogate.getSuperclass();
		} while (interrogate != null);
	}

	/**
	 * All access via static methods.
	 */
	private CabinetUtil() {
	}
}
