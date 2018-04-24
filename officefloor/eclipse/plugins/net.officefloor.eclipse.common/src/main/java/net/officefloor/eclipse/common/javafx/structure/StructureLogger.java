/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.javafx.structure;

import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Logs the structure of the JavaFx application.
 * 
 * @author Daniel Sagenschneider
 */
public class StructureLogger {

	/**
	 * Logs the full structure that the {@link Node} is involved in.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to log.
	 */
	public static void logFull(Node node, Appendable output) throws IOException {
		log(node.getScene().getRoot(), output);
	}

	/**
	 * Logs the {@link Node} and all it's descendants.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to log.
	 */
	public static void log(Node node, Appendable output) throws IOException {
		log(node, 0, output);
	}

	/**
	 * Recursively logs the structure.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param depth
	 *            Depth.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to log.
	 */
	private static void log(Node node, int depth, Appendable output) throws IOException {

		// Indent
		for (int i = 0; i < (depth * 2); i++) {
			output.append(" ");
		}

		// Log details of the node
		output.append(node.toString());
		output.append(System.lineSeparator());

		// Load possible children
		if (node instanceof Parent) {
			Parent parent = (Parent) node;
			for (Node child : parent.getChildrenUnmodifiable()) {
				log(child, depth + 1, output);
			}
		}
	}

	/**
	 * All access via static methods.
	 */
	private StructureLogger() {
	}

}