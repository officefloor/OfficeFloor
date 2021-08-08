/*-
 * #%L
 * HTTP Server
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
