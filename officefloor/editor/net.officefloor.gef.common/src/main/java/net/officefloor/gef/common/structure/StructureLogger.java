/*-
 * #%L
 * [bundle] OfficeFloor Common
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

package net.officefloor.gef.common.structure;

import java.io.IOException;

import javafx.css.CssMetaData;
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
	 * Logs the {@link Node} and all its descendants.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to log.
	 */
	public static void log(Node node, Appendable output) throws IOException {
		log(node, 0, -1, false, output);
	}

	/**
	 * Logs the {@link Node} including its {@link CssMetaData} and all its
	 * descendants.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param maxDepth
	 *            Maximum depth to recurse before stopping. Note that CSS output is
	 *            quite large, hence need to limit depth. Use <code>-1</code> for
	 *            unlimited depth.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to log.
	 */
	public static void logCss(Node node, int maxDepth, Appendable output) throws IOException {
		log(node, 0, maxDepth, true, output);
	}

	/**
	 * Recursively logs the structure.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param depth
	 *            Depth.
	 * @param maxDepth
	 *            Maximum depth. <code>-1</code> for unlimited depth.
	 * @param isIncludeCss
	 *            Indicates to include the {@link CssMetaData}.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to log.
	 */
	private static void log(Node node, int depth, int maxDepth, boolean isIncludeCss, Appendable output)
			throws IOException {

		// Determine if reaached max depth
		if ((maxDepth >= 0) && (depth >= maxDepth)) {
			indent(depth, output);
			output.append("...");
			output.append(System.lineSeparator());
			return;
		}

		// Log details of the node
		indent(depth, output);
		output.append(node.toString());
		output.append(System.lineSeparator());

		// Include CSS meta-data
		if (isIncludeCss) {
			for (CssMetaData<?, ?> metaData : node.getCssMetaData()) {
				indent(depth, output);
				output.append("-");
				output.append(metaData.toString());
				output.append(System.lineSeparator());
			}
		}

		// Load possible children
		if (node instanceof Parent) {
			Parent parent = (Parent) node;
			for (Node child : parent.getChildrenUnmodifiable()) {
				log(child, depth + 1, maxDepth, isIncludeCss, output);
			}
		}
	}

	/**
	 * Indents the depth.
	 * 
	 * @param depth
	 *            Depth.
	 * @param output
	 *            {@link Appendable}.
	 * @throws IOException
	 *             If fails to indent.
	 */
	private static void indent(int depth, Appendable output) throws IOException {
		for (int i = 0; i < (depth * 2); i++) {
			output.append(" ");
		}
	}

	/**
	 * All access via static methods.
	 */
	private StructureLogger() {
	}

}
