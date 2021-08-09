/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Factory for the creation of {@link HttpObjectResponder} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectResponderFactory {

	/**
	 * Obtains the <code>Content-Type</code> supported by the create
	 * {@link HttpObjectResponder} instances.
	 * 
	 * @return <code>Content-Type</code>.
	 */
	String getContentType();

	/**
	 * Creates the {@link HttpObjectResponder} for the {@link Object} type.
	 *
	 * @param <T>
	 *            Object type.
	 * @param objectType
	 *            {@link Object} type.
	 * @return {@link HttpObjectResponder} for the {@link Object} type.
	 */
	<T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType);

	/**
	 * Creates the {@link HttpObjectResponder} for the {@link Escalation} type.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @param escalationType
	 *            {@link Escalation} type.
	 * @return {@link HttpObjectResponder} for the {@link Escalation} type.
	 */
	<E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType);

}
