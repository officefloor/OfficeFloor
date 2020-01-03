package net.officefloor.gef.editor.style;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * {@link URLStreamHandler} for the {@link SystemStyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return AbstractStyleRegistry.openConnection(url);
	}

}