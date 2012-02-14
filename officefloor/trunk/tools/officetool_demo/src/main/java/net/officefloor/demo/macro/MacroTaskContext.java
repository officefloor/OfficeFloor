/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.demo.macro;

import java.awt.Point;
import java.awt.Robot;

import javax.swing.JDialog;

/**
 * <p>
 * Context for running a {@link Macro}.
 * <p>
 * The methods invoke an underlying {@link Robot} and handles translating the
 * relative locations to absolute locations.
 * <p>
 * See the corresponding methods of {@link Robot} for details on the methods.
 * 
 * @author Daniel Sagenschneider
 */
public interface MacroTaskContext {

	/**
	 * As per {@link Robot}.
	 * 
	 * @param x
	 *            X location.
	 * @param y
	 *            Y location.
	 */
	void mouseMove(int x, int y);

	/**
	 * As per {@link Robot}.
	 * 
	 * @param buttons
	 *            Buttons.
	 */
	void mousePress(int buttons);

	/**
	 * As per {@link Robot}.
	 * 
	 * @param buttons
	 *            Buttons.
	 */
	void mouseRelease(int buttons);

	/**
	 * Convenience method to do press and release of mouse button.
	 * 
	 * @param buttons
	 *            Buttons as per {@link Robot}.
	 */
	void mouseClick(int buttons);

	/**
	 * As per {@link Robot}.
	 * 
	 * @param buttons
	 *            Wheel amount.
	 */
	void mouseWheel(int wheelAmt);

	/**
	 * As per {@link Robot}.
	 * 
	 * @param keycode
	 *            Key code.
	 */
	void keyPress(int keycode);

	/**
	 * As per {@link Robot}.
	 * 
	 * @param keycode
	 *            Key code.
	 */
	void keyRelease(int keycode);

	/**
	 * Convenience method to do press and release of key.
	 * 
	 * @param keycode
	 *            Key code as per {@link Robot}.
	 */
	void keyStroke(int keycode);

	/**
	 * Convenience method to type in text.
	 * 
	 * @param text
	 *            Text to be typed in.
	 */
	void keyText(String text);

	/**
	 * <p>
	 * Obtains the absolute location for the relative location.
	 * <p>
	 * This is useful for example to display a {@link JDialog} and position
	 * correctly.
	 * 
	 * @param relativeLocation
	 *            Relative location.
	 * @return Absolute location.
	 */
	Point getAbsoluteLocation(Point relativeLocation);

	/**
	 * <p>
	 * Obtains the relative location for the absolute location.
	 * <p>
	 * This is useful for example to find the relative location for a mouse move
	 * where the active window only provides an absolute location.
	 * 
	 * @param absoluteLocation
	 *            Absolute location.
	 * @return Relative location.
	 */
	Point getRelativeLocation(Point absoluteLocation);

}