/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.integrate.escalation;

import java.util.Map;

import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.impl.AbstractMockManagedObjectSource;
import net.officefloor.frame.impl.PassByReference;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Invokes handler with an {@link EscalationHandler}.
 * 
 * @author Daniel
 */
public class EscalationManagedObjectSource
		extends
		AbstractMockManagedObjectSource<None, EscalationManagedObjectSource.Handlers>
		implements HandlerFactory<Indexed>, Handler<Indexed>, ManagedObject,
		EscalationHandler {

	/**
	 * Instance.
	 */
	private static EscalationManagedObjectSource INSTANCE;

	/**
	 * Invokes processing.
	 */
	public static void invokeProcessing() {
		// Invoke processing
		INSTANCE.handlerContext.invokeProcess(0, null, INSTANCE, INSTANCE);
	}

	/**
	 * Throws the escalation if handled by the {@link ManagedObjectSource}
	 * {@link EscalationHandler}.
	 * 
	 * @throws Throwable
	 *             Escalation.
	 */
	public static void throwPossibleEscalation() throws Throwable {
		if (INSTANCE.escalation != null) {
			throw INSTANCE.escalation;
		}
	}

	/**
	 * Store the instance.
	 */
	protected void init() throws Exception {
		INSTANCE = this;
	}

	/**
	 * {@link HandlerContext}.
	 */
	private HandlerContext<Indexed> handlerContext;

	/**
	 * Escalation.
	 */
	private Throwable escalation = null;

	/**
	 * Initiate handlers.
	 * 
	 * @param handlerKeys
	 *            Handler.
	 * @param handlers
	 *            Handlers.
	 */
	@Override
	protected void initHandlers(PassByReference<Class<Handlers>> handlerKeys,
			Map<Handlers, Class<?>> handlers) {
		handlerKeys.setValue(Handlers.class);
		handlers.put(Handlers.ESCALATE, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerFactory#createHandler()
	 */
	@Override
	public Handler<Indexed> createHandler() {
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor.frame.api.execute.HandlerContext)
	 */
	@Override
	public void setHandlerContext(HandlerContext<Indexed> context)
			throws Exception {
		this.handlerContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
	 */
	@Override
	public Object getObject() throws Exception {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.EscalationHandler#handleEscalation(java.lang.Throwable)
	 */
	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		this.escalation = escalation;
	}

	/**
	 * Handlers.
	 */
	public static enum Handlers {
		ESCALATE
	}

}
