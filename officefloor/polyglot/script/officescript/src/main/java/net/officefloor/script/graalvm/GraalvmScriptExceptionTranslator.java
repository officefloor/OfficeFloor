/*-
 * #%L
 * PolyglotScript
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

package net.officefloor.script.graalvm;

import javax.script.ScriptException;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import net.officefloor.script.ScriptExceptionTranslator;

/**
 * {@link ScriptExceptionTranslator} for the GraalVM.
 * 
 * @author Daniel Sagenschneider
 */
public class GraalvmScriptExceptionTranslator implements ScriptExceptionTranslator {

	/*
	 * ================= ScriptExceptionTranslator ==================
	 */

	@Override
	public Throwable translate(ScriptException scriptException) {

		// Extract cause
		Throwable cause = scriptException.getCause();
		if (cause != null) {

			// Determine if need to interpret cause
			if (cause instanceof PolyglotException) {
				PolyglotException polyglotEx = (PolyglotException) cause;
				if (polyglotEx.isGuestException()) {
					Object guestObject = polyglotEx.getGuestObject();
					if (guestObject instanceof Value) {
						Value value = (Value) guestObject;
						Object hostObject = value.asHostObject();
						if (hostObject instanceof Throwable) {
							return (Throwable) hostObject;
						}
					}
				} else if (polyglotEx.isHostException()) {
					return polyglotEx.asHostException();
				}
			}
			
			// Just provide the cause
			return cause;
		}

		// No cause, so just propagate script exception
		return scriptException;
	}

}
