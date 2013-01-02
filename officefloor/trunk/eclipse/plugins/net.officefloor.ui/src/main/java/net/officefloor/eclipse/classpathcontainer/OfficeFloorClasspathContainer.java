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
package net.officefloor.eclipse.classpathcontainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.xml.XmlMarshaller;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshallerFactory;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * {@link IClasspathContainer} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorClasspathContainer implements IClasspathContainer {

	/**
	 * {@link IClasspathContainer} path Id.
	 */
	public static String CONTAINER_ID = "net.officefloor.eclipse.OFFICE_FLOOR";

	/**
	 * Listing of core {@link Class} instances for OfficeFloor.
	 */
	private static final Class<?>[] CORE_CLASSES = new Class<?>[] {
			OfficeFrame.class, OfficeFloorCompiler.class,
			XmlUnmarshaller.class, OfficeBuilding.class };

	/**
	 * {@link IPath} for this {@link OfficeFloorClasspathContainer}.
	 */
	private final IPath containerPath;

	/**
	 * Listing of the {@link SourceAttachmentEntry} instances.
	 */
	private final List<SourceAttachmentEntry> sourceAttachmentEntries = new LinkedList<SourceAttachmentEntry>();

	/**
	 * Listing of the {@link ExtensionClasspathProviderEntry} instances.
	 */
	private final List<ExtensionClasspathProviderEntry> extensionEntries = new LinkedList<ExtensionClasspathProviderEntry>();

	/**
	 * Initiate.
	 * 
	 * @param containerPath
	 *            {@link IPath}.
	 */
	public OfficeFloorClasspathContainer(IPath containerPath) {
		this.containerPath = containerPath;
	}

	/**
	 * Loads this {@link OfficeFloorClasspathContainer} from configuration.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws Exception
	 *             If fails to load configuration.
	 */
	public void load(InputStream configuration) throws Exception {

		// Clear this container
		this.sourceAttachmentEntries.clear();
		this.extensionEntries.clear();

		// Obtain the unmarshal configuration
		final String configurationFileName = "UnmarshalConfiguration.xml";
		InputStream unmarshalConfiguration = this.getClass()
				.getResourceAsStream(configurationFileName);
		if (unmarshalConfiguration == null) {
			throw new FileNotFoundException("Can not find file "
					+ configurationFileName);
		}

		// Obtain the unmarshaler
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
				.createUnmarshaller(unmarshalConfiguration);

		// Load this container
		unmarshaller.unmarshall(configuration, this);
	}

	/**
	 * Stores this {@link OfficeFloorClasspathContainer} to configuration.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws Exception
	 *             If fails to store.
	 */
	public void store(OutputStream configuration) throws Exception {

		// Obtain the marshal configuration
		final String configurationFileName = "MarshalConfiguration.xml";
		InputStream marshalConfiguration = this.getClass().getResourceAsStream(
				configurationFileName);
		if (marshalConfiguration == null) {
			throw new FileNotFoundException("Can not find file "
					+ configurationFileName);
		}

		// Obtain the marshaler
		XmlMarshaller marshaler = TreeXmlMarshallerFactory.getInstance()
				.createMarshaller(marshalConfiguration);

		// Store this container
		final Writer writer = new OutputStreamWriter(configuration);
		marshaler.marshall(this, new XmlOutput() {
			@Override
			public void write(String content) throws IOException {
				writer.write(content);
			}
		});
		writer.flush();
	}

	/**
	 * Updates the state of this {@link IClasspathContainer} from the input
	 * suggested {@link IClasspathContainer}.
	 * 
	 * @param containerSuggestion
	 *            Suggested {@link IClasspathContainer}.
	 */
	public void updateState(IClasspathContainer containerSuggestion) {

		// Clear the source attachment entries
		this.sourceAttachmentEntries.clear();

		// Reload the source attachment entries from suggested container
		for (IClasspathEntry entry : containerSuggestion.getClasspathEntries()) {

			// Obtain the details about the entry
			IPath path = entry.getPath();
			IPath sourceAttachmentPath = entry.getSourceAttachmentPath();
			IPath sourceAttachmentRootPath = entry
					.getSourceAttachmentRootPath();

			// Include entry if has source attachments
			if ((sourceAttachmentPath != null)
					|| (sourceAttachmentRootPath != null)) {

				// Create and register the source attachment entry
				SourceAttachmentEntry sourceAttachmentEntry = new SourceAttachmentEntry(
						path, sourceAttachmentPath, sourceAttachmentRootPath);
				this.sourceAttachmentEntries.add(sourceAttachmentEntry);
			}
		}
	}

	/**
	 * Obtains the {@link SourceAttachmentEntry} for the {@link IClasspathEntry}
	 * {@link IPath}.
	 * 
	 * @param path
	 *            {@link IClasspathEntry} {@link IPath}.
	 */
	public SourceAttachmentEntry getSourceAttachmentEntry(IPath path) {

		// Obtain the portable path
		String portablePath = path.toPortableString();

		// Obtain the source attachment entry
		for (SourceAttachmentEntry entry : this.sourceAttachmentEntries) {
			if (portablePath.equals(entry.getClasspathPath())) {
				// Found the source attachment entry
				return entry;
			}
		}

		// As here, no source attachment entry
		return null;
	}

	/**
	 * Adds the {@link ExtensionClasspathProvider} class name.
	 * 
	 * @param extensionClassName
	 *            {@link ExtensionClasspathProvider} class name.
	 */
	public void addExtensionClasspathProvider(String extensionClassName) {

		// Determine if extension already added
		for (ExtensionClasspathProviderEntry entry : this.extensionEntries) {
			if (extensionClassName.equals(entry.getExtensionClassName())) {
				return; // Already added
			}
		}

		// Add the extension provider entry
		this.extensionEntries.add(new ExtensionClasspathProviderEntry(
				extensionClassName));
	}

	/**
	 * Obtains the {@link SourceAttachmentEntry} instances.
	 * 
	 * @return {@link SourceAttachmentEntry} instances.
	 */
	public List<SourceAttachmentEntry> getSourceAttachmentEntries() {
		return this.sourceAttachmentEntries;
	}

	/**
	 * Adds the {@link SourceAttachmentEntry}.
	 * 
	 * @param entry
	 *            {@link SourceAttachmentEntry}.
	 */
	public void addSourceAttachmentEntry(SourceAttachmentEntry entry) {
		this.sourceAttachmentEntries.add(entry);
	}

	/**
	 * Obtains the {@link ExtensionClasspathProviderEntry} instances.
	 * 
	 * @return {@link ExtensionClasspathProviderEntry} instances.
	 */
	public List<ExtensionClasspathProviderEntry> getExtensionClasspathProviderEntries() {
		return this.extensionEntries;
	}

	/**
	 * Adds the {@link ExtensionClasspathProviderEntry}.
	 * 
	 * @param entry
	 *            {@link ExtensionClasspathProviderEntry}.
	 */
	public void addExtensionClasspathProviderEntry(
			ExtensionClasspathProviderEntry entry) {
		this.extensionEntries.add(entry);
	}

	/*
	 * ======================= IClasspathContainer =============================
	 */

	@Override
	public String getDescription() {
		return "OfficeFloor";
	}

	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	@Override
	public IPath getPath() {
		return this.containerPath;
	}

	@Override
	public IClasspathEntry[] getClasspathEntries() {

		// Obtain the mapping of extension class name to extension
		Map<String, ExtensionClasspathProvider> providers = ExtensionUtil
				.createClasspathProvidersByExtensionClassNames();

		// Create the listing of class path entries
		List<IClasspathEntry> classpathEntries = new LinkedList<IClasspathEntry>();

		// Add the core classes first
		for (Class<?> coreClass : CORE_CLASSES) {
			this.addClasspathEntry(ClasspathUtil.createClasspathEntry(
					coreClass, this), classpathEntries);
		}

		// Add the extension class path providers next
		for (ExtensionClasspathProviderEntry providerEntry : this.extensionEntries) {
			for (IClasspathEntry classpathEntry : providerEntry
					.getClasspathEntries(providers, this)) {
				this.addClasspathEntry(classpathEntry, classpathEntries);
			}
		}

		// Return the class path entries
		return classpathEntries.toArray(new IClasspathEntry[0]);
	}

	/**
	 * Adds the {@link IClasspathEntry} to the list if not in the list.
	 * 
	 * @param entry
	 *            {@link IClasspathEntry}. May be <code>null</code>.
	 * @param list
	 *            List to add the {@link IClasspathEntry}.
	 */
	private void addClasspathEntry(IClasspathEntry entry,
			List<IClasspathEntry> list) {

		// Do not add if null
		if (entry == null) {
			return;
		}

		// Determine if entry in list
		for (IClasspathEntry item : list) {
			// Check matching on kind, content and path
			if ((entry.getEntryKind() == item.getEntryKind())
					&& (entry.getContentKind() == item.getContentKind())
					&& (entry.getPath().equals(item.getPath()))) {
				// Already included, so do not add
				return;
			}
		}

		// As here not in list, so add
		list.add(entry);
	}

}