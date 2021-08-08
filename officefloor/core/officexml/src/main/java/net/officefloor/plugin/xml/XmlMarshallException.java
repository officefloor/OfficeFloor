/*-
 * #%L
 * OfficeXml
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

package net.officefloor.plugin.xml;

import java.io.IOException;

/**
 * Indicates failure to marshall/unmarshall XML.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMarshallException extends IOException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Enforce reason.
	 * 
	 * @param reason Reason.
	 */
	public XmlMarshallException(String reason) {
		super(reason);
	}

	/**
	 * Enforce reason and allow cause.
	 * 
	 * @param reason Reason.
	 * @param cause  Cause.
	 */
	public XmlMarshallException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
