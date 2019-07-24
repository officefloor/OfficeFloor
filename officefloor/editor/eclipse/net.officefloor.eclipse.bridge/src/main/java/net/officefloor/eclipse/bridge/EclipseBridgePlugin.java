/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.bridge;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLStreamHandlerService;

import net.officefloor.eclipse.editor.AdaptedEditorStyle;
import net.officefloor.eclipse.editor.style.AbstractStyleRegistry;

/**
 * {@link Plugin} for bridging to Eclipse.
 * 
 * @author Daniel Sagenschneider
 */
public class EclipseBridgePlugin extends AbstractUIPlugin {

	/**
	 * Obtains the {@link EclipseBridgePlugin} singleton.
	 * 
	 * @return {@link EclipseBridgePlugin} singleton.
	 */
	public EclipseBridgePlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Singleton.
	 */
	private static EclipseBridgePlugin INSTANCE;

	/**
	 * {@link ServiceRegistration} for the {@link OsgiURLStreamHandlerService}.
	 */
	private ServiceRegistration<?> styleUrlHandler;

	/**
	 * Instantiate.
	 */
	public EclipseBridgePlugin() {
		INSTANCE = this;
	}

	/*
	 * =============== AbstractUIPlugin =========================
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// Register the URL handler for styling
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put("url.handler.protocol", AbstractStyleRegistry.PROTOCOL);
		this.styleUrlHandler = context.registerService(URLStreamHandlerService.class.getName(),
				new OsgiURLStreamHandlerService(), properties);

		// Flag URL location active
		AdaptedEditorStyle.urlStyleLocationActive();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		// Unregister style URL handler
		if (this.styleUrlHandler != null) {
			context.ungetService(this.styleUrlHandler.getReference());
			this.styleUrlHandler = null;
		}
	}

}