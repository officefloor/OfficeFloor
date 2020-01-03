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