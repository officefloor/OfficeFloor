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

package net.officefloor.frame.api.profile;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Profiled execution of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProfiledManagedFunction {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the time stamp in milliseconds when the {@link ManagedFunction}
	 * was started.
	 * 
	 * @return Time stamp in milliseconds when the {@link ManagedFunction} was
	 *         started.
	 */
	long getStartTimestampMilliseconds();

	/**
	 * Obtains the time stamp in nanoseconds when the {@link ManagedFunction}
	 * was started.
	 * 
	 * @return Time stamp in nanoseconds when the {@link ManagedFunction} was
	 *         started.
	 */
	long getStartTimestampNanoseconds();

	/**
	 * Obtains the name of the executing {@link Thread}.
	 * 
	 * @return Name of the executing {@link Thread}.
	 */
	String getExecutingThreadName();

}
