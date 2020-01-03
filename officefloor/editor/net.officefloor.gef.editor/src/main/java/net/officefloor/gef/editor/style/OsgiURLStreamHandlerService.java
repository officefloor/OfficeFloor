package net.officefloor.gef.editor.style;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * OSGi {@link URLStreamHandlerService}.
 * 
 * @author Daniel Sagenschneider
 */
public class OsgiURLStreamHandlerService extends AbstractURLStreamHandlerService {

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return DefaultStyleRegistry.openConnection(url);
	}

}