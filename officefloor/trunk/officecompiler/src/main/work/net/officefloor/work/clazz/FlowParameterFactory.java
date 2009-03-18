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
package net.officefloor.work.clazz;

import net.officefloor.frame.api.execute.TaskContext;

/**
 * {@link ParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel
 */
public class FlowParameterFactory implements ParameterFactory {

	/**
	 * Index of the {@link Flow}.
	 */
	private final int flowIndex;

	/**
	 * Initiate.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 */
	public FlowParameterFactory(int flowIndex) {
		this.flowIndex = flowIndex;
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object createParameter(TaskContext<?, ?, ?, ?> context) {
		return new FlowImpl(context);
	}

	/**
	 * {@link Flow} implementation.
	 */
	private class FlowImpl<P> implements Flow<P> {

		/**
		 * {@link TaskContext}.
		 */
		private final TaskContext<P, ?, ?, ?> context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link TaskContext}.
		 */
		public FlowImpl(TaskContext<P, ?, ?, ?> context) {
			this.context = context;
		}

		/*
		 * ====================== Flow ====================================
		 */

		@Override
		public void invoke(P parameter) {
			this.context.doFlow(FlowParameterFactory.this.flowIndex, parameter);
		}
	}

}