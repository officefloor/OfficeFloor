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

import net.officefloor.eclipse.util.EclipseUtil;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Source attachment entry.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceAttachmentEntry {

	/**
	 * {@link IClasspathEntry} {@link IPath#toPortableString()}.
	 */
	private String classpathPath = "";

	/**
	 * Source attachment path.
	 */
	private String sourceAttachmentPath = "";

	/**
	 * Source attachment root path.
	 */
	private String sourceAttachmentRootPath = "";

	/**
	 * Default constructor for retrieving.
	 */
	public SourceAttachmentEntry() {
	}

	/**
	 * Initiate.
	 * 
	 * @param classpathPath
	 *            {@link IClasspathEntry} {@link IPath#toPortableString()}.
	 * @param sourceAttachmentPath
	 *            Source attachment path.
	 * @param sourceAttachmentRootPath
	 *            Source attachment root path.
	 */
	public SourceAttachmentEntry(String classpathPath,
			String sourceAttachmentPath, String sourceAttachmentRootPath) {
		this.classpathPath = classpathPath;
		this.sourceAttachmentPath = sourceAttachmentPath;
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
	}

	/**
	 * Initiate.
	 * 
	 * @param classpathPath
	 *            {@link IClasspathEntry#getPath()}.
	 * @param sourceAttachmentPath
	 *            {@link IClasspathEntry#getSourceAttachmentPath()}.
	 * @param sourceAttachmentRootPath
	 *            {@link IClasspathEntry#getSourceAttachmentRootPath()}.
	 */
	public SourceAttachmentEntry(IPath classpathPath,
			IPath sourceAttachmentPath, IPath sourceAttachmentRootPath) {
		this.classpathPath = classpathPath.toPortableString();
		this.sourceAttachmentPath = (sourceAttachmentPath != null ? sourceAttachmentPath
				.toString()
				: null);
		this.sourceAttachmentRootPath = (sourceAttachmentRootPath != null ? sourceAttachmentRootPath
				.toString()
				: null);
	}

	/**
	 * Obtains the {@link IClasspathEntry} {@link IPath#toPortableString()}.
	 * 
	 * @return {@link IClasspathEntry} {@link IPath#toPortableString()}.
	 */
	public String getClasspathPath() {
		return this.classpathPath;
	}

	/**
	 * Specifies the {@link IClasspathEntry} {@link IPath#toPortableString()}.
	 * 
	 * @param classpathPath
	 *            {@link IClasspathEntry} {@link IPath#toPortableString()}.
	 */
	public void setClasspathPath(String classpathPath) {
		this.classpathPath = classpathPath;
	}

	/**
	 * Obtains the source attachment path.
	 * 
	 * @return Source attachment path.
	 */
	public String getSourceAttachmentPath() {
		return this.sourceAttachmentPath;
	}

	/**
	 * Obtains the source attachment {@link IPath}.
	 * 
	 * @return Source attachment {@link IPath}.
	 */
	public IPath getSourceAttachmentIPath() {
		return (EclipseUtil.isBlank(this.sourceAttachmentPath) ? null
				: new Path(this.sourceAttachmentPath));
	}

	/**
	 * Specifies the source attachment path.
	 * 
	 * @param sourceAttachmentPath
	 *            Source attachment path.
	 */
	public void setSourceAttachmentPath(String sourceAttachmentPath) {
		this.sourceAttachmentPath = sourceAttachmentPath;
	}

	/**
	 * Obtains the source attachment root path.
	 * 
	 * @return Source attachment root path.
	 */
	public String getSourceAttachmentRootPath() {
		return this.sourceAttachmentRootPath;
	}

	/**
	 * Obtains the source attachment root {@link IPath}.
	 * 
	 * @return Source attachment root {@link IPath}.
	 */
	public IPath getSourceAttachmentRootIPath() {
		return (EclipseUtil.isBlank(this.sourceAttachmentRootPath) ? null
				: new Path(this.sourceAttachmentRootPath));
	}

	/**
	 * Specifies the source attachment root path.
	 * 
	 * @param sourceAttachmentRootPath
	 *            Source attachment root path.
	 */
	public void setSourceAttachmentRootPath(String sourceAttachmentRootPath) {
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
	}

}