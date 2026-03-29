/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.structure;

/**
 * Item copied out of a {@link LinkedListSet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedListSetItem<I> {

	/**
	 * Obtains the {@link LinkedListSetEntry} copied out of the
	 * {@link LinkedListSet}.
	 * 
	 * @return {@link LinkedListSetEntry} copied out of the
	 *         {@link LinkedListSet}.
	 */
	I getEntry();

	/**
	 * Obtains the next {@link LinkedListSetItem} copied out of the
	 * {@link LinkedListSet}.
	 * 
	 * @return Next {@link LinkedListSetItem} copied out of the
	 *         {@link LinkedListSet}.
	 */
	LinkedListSetItem<I> getNext();

}
