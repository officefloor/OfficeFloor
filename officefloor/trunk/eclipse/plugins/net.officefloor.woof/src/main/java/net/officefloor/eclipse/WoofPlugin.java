/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse;

import net.officefloor.eclipse.skin.WoofSkin;
import net.officefloor.eclipse.skin.standard.StandardWoofSkin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The plug-in class for WoOF.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofPlugin extends AbstractUIPlugin {

	/**
	 * Plug-in Id for the {@link WoofPlugin}.
	 */
	public static final String PLUGIN_ID = "net.officefloor.woof";

	/**
	 * Shared instance.
	 */
	private static WoofPlugin plugin;

	/**
	 * {@link WoofSkin}.
	 */
	private static WoofSkin skin;

	/**
	 * Initiate.
	 */
	public WoofPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// TODO obtain skin via extension

		// No skin specified by extension so use standard
		skin = new StandardWoofSkin();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static WoofPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
				"officefloor_eclipse", path);
	}

	/**
	 * Obtains the {@link WoofSkin}.
	 * 
	 * @return {@link WoofSkin}.
	 */
	public static WoofSkin getSkin() {
		return skin;
	}

}