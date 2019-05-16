/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.server.filesystem;

import java.nio.file.FileSystems;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Provides safe {@link FileAttribute} support.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorFileAttributes {

	/**
	 * Indicates if posix is supported.
	 * 
	 * @return <code>true</code> if posix is supported.
	 */
	public static boolean isSupportPosix() {
		return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
	}

	/**
	 * Obtains the default {@link FileAttribute} instances for a directory.
	 * 
	 * @return Default {@link FileAttribute} instances for a directory.
	 */
	public static FileAttribute<?>[] getDefaultDirectoryAttributes() {
		return isSupportPosix()
				? new FileAttribute[] {
						PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---")) }
				: new FileAttribute[0];
	}

	/**
	 * Obtains the default {@link FileAttribute} instances for a file.
	 * 
	 * @return Default {@link FileAttribute} instances for a file.
	 */
	public static FileAttribute<?>[] getDefaultFileAttributes() {
		return isSupportPosix()
				? new FileAttribute[] {
						PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r-----")) }
				: new FileAttribute[0];
	}

}