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

package net.officefloor.plugin.xml.unmarshall.tree;

import java.util.Stack;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * State of the XML unmarshalling.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlState {

	/**
	 * Initial {@link XmlContext} in which to unmarshall.
	 */
	protected final XmlContext initialContext;

	/**
	 * Stack of previous {@link XmlContext}.
	 */
	protected final Stack<XmlContextState> contextStack;

	/**
	 * Current {@link XmlContext} in which to unmarshall.
	 */
	protected XmlContext currentContext;

	/**
	 * Current target object.
	 */
	protected Object currentTargetObject = null;

	/**
	 * XML element name to end the current context.
	 */
	protected String endElementName = null;

	/**
	 * Initiate with the initial {@link XmlContext}.
	 * 
	 * @param initialContext
	 *            Initial {@link XmlContext} in which to unmarshall.
	 */
	public XmlState(XmlContext initialContext) {
		// Store state
		this.initialContext = initialContext;
		this.contextStack = new Stack<XmlContextState>();

		// Current context is the initial context
		this.currentContext = this.initialContext;
	}

	/**
	 * Specifies the current target object.
	 * 
	 * @param targetObject
	 *            Target object.
	 */
	public void setTargetObject(Object targetObject) {
		this.currentTargetObject = targetObject;
	}

	/**
	 * Resets the state.
	 */
	public void reset() {
		// Clear stack and context
		this.contextStack.clear();
		this.endElementName = null;

		// Clear the target object
		this.currentTargetObject = null;

		// Reset initial context and current context
		this.currentContext = this.initialContext;
	}

	/**
	 * Obtains the current {@link XmlContext}.
	 * 
	 * @return Current {@link XmlContext}.
	 */
	public XmlContext getCurrentContext() {
		return this.currentContext;
	}

	/**
	 * Obtains the current target object.
	 * 
	 * @return Target object to load values/objects.
	 */
	public Object getCurrentTargetObject() {
		return this.currentTargetObject;
	}

	/**
	 * Obtains the XML element name to end the current context.
	 * 
	 * @return XML element name to end the current context.
	 */
	public String getEndElementName() {
		return this.endElementName;
	}

	/**
	 * Makes the input target object and {@link XmlContext} the current for XML
	 * unmarshalling.
	 * 
	 * @param elementName
	 *            Name of the element/attribute.
	 * @param targetObject
	 *            Target object.
	 * @param context
	 *            New {@link XmlContext}.
	 */
	public void pushContext(String elementName, Object targetObject,
			XmlContext context) {
		// Push current context onto the stack
		this.contextStack.push(new XmlContextState(this.endElementName,
				this.currentContext, this.currentTargetObject));

		// Set the current focus
		this.endElementName = elementName;
		this.currentContext = context;
		this.currentTargetObject = targetObject;
	}

	/**
	 * Pops the previous {@link XmlContext} off the stack and makes it the
	 * current {@link XmlContext} for XML unmarshalling.
	 * 
	 * @throws XmlMarshallException
	 *             If there is no previous {@link XmlContext}.
	 */
	public void popContext() throws XmlMarshallException {

		// Pop the previous state
		XmlContextState poppedState = this.contextStack.pop();

		// Ensure have previous state
		if (poppedState == null) {
			throw new XmlMarshallException("No previous context");
		}

		// Have previous context therefore make it current
		this.endElementName = poppedState.endElementName;
		this.currentContext = poppedState.getContext();
		this.currentTargetObject = poppedState.getTargetObject();
	}

}

/**
 * State of the {@link net.officefloor.plugin.xml.unmarshall.tree.XmlContext}.
 * 
 * @author Daniel Sagenschneider
 */
class XmlContextState {

	/**
	 * XML element name indicating the end of the context.
	 */
	protected final String endElementName;

	/**
	 * {@link XmlContext} of the state.
	 */
	protected final XmlContext context;

	/**
	 * Target object of the state.
	 */
	protected final Object targetObject;

	/**
	 * Initiate with state.
	 * 
	 * @param endElementName
	 *            XML element name indicating the end of the context.
	 * @param context
	 *            {@link XmlContext} of state.
	 * @param targetObject
	 *            Target object of state.
	 */
	public XmlContextState(String endElementName, XmlContext context,
			Object targetObject) {
		// Store state
		this.endElementName = endElementName;
		this.context = context;
		this.targetObject = targetObject;
	}

	/**
	 * Obtains the XML element name to end the current context.
	 * 
	 * @return XML element name to end the current context.
	 */
	public String getEndElementName() {
		return this.endElementName;
	}

	/**
	 * Obtains the {@link XmlContext} of the state.
	 * 
	 * @return {@link XmlContext} of the state.
	 */
	public XmlContext getContext() {
		return this.context;
	}

	/**
	 * Obtains the target object of the state.
	 * 
	 * @return Target object of the state.
	 */
	public Object getTargetObject() {
		return this.targetObject;
	}
}
