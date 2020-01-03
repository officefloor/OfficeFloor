package net.officefloor.gef.editor.style;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

/**
 * OfficeFloorStyle {@link URLStreamHandlerProvider}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorStyleURLStreamHandlerProvider extends URLStreamHandlerProvider {

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		return new Handler();
	}

}