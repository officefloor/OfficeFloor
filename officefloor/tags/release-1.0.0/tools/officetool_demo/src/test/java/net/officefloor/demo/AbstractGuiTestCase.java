/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.demo;

import java.awt.GraphicsEnvironment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import net.officefloor.frame.test.OfficeFrameTestCase;

import junit.framework.TestCase;

/**
 * Abstract {@link TestCase} for handling headless builds (i.e. not running
 * graphical tests).
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGuiTestCase extends OfficeFrameTestCase {

	/*
	 * ==================== TestCase =========================
	 */

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface GuiTest {
	}

	@Override
	public void runBare() throws Throwable {

		// Determine if a graphical test
		try {
			Method testMethod = this.getClass().getMethod(this.getName());
			if (testMethod.getAnnotation(GuiTest.class) != null) {
				// Determine if headed environment (i.e. can run graphical test)
				if (GraphicsEnvironment.isHeadless()) {
					System.out.println("NOT RUNNING GUI TEST "
							+ this.getClass().getSimpleName() + "."
							+ this.getName());
					return;
				}
			}
		} catch (Throwable ex) {
			// Ignore and not consider a graphical test
		}

		// Run the test
		super.runBare();
	}

}