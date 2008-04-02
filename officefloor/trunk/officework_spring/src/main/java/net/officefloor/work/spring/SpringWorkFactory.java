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
package net.officefloor.work.spring;

import org.springframework.beans.factory.BeanFactory;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.work.clazz.ClassWork;
import net.officefloor.work.clazz.ClassWorkFactory;

/**
 * {@link WorkFactory} for a Spring bean.
 * 
 * @author Daniel
 */
public class SpringWorkFactory extends ClassWorkFactory {

	/**
	 * {@link BeanFactory}.
	 */
	private final BeanFactory beanFactory;

	/**
	 * Name of the bean.
	 */
	private final String beanName;

	/**
	 * Initiate.
	 * 
	 * @param beanFactory
	 *            {@link BeanFactory}.
	 * @param beanName
	 *            Name of the bean.
	 */
	public SpringWorkFactory(BeanFactory beanFactory, String beanName) {
		super(null);
		this.beanFactory = beanFactory;
		this.beanName = beanName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.clazz.ClassWorkFactory#createWork()
	 */
	@Override
	public ClassWork createWork() {

		// Create the bean
		Object bean = this.beanFactory.getBean(this.beanName);

		// Return the work
		return new ClassWork(bean);
	}

}
