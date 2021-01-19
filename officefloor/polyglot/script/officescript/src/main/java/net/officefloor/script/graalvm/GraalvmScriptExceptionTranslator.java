/*-
 * #%L
 * PolyglotScript
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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
