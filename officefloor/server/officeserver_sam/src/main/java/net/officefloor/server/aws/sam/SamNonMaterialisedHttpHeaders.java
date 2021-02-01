package net.officefloor.server.aws.sam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;

/**
 * AWS SAM {@link NonMaterialisedHttpHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamNonMaterialisedHttpHeaders implements NonMaterialisedHttpHeaders {

	/**
	 * {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers;

	/**
	 * Instantiate.
	 * 
	 * @param samHeaders SAM headers.
	 */
	public SamNonMaterialisedHttpHeaders(Map<String, List<String>> samHeaders) {

		// Create listing of headers
		this.headers = new ArrayList<>();
		samHeaders.forEach((name, values) -> {
			for (String value : values) {
				HttpHeader header = new SerialisableHttpHeader(name, value);
				this.headers.add(new NonMaterialisedHttpHeader() {

					@Override
					public CharSequence getName() {
						return header.getName();
					}

					@Override
					public HttpHeader materialiseHttpHeader() {
						return header;
					}
				});
			}
		});
	}

	/*
	 * =================== NonMaterialisedHttpHeaders ====================
	 */

	@Override
	public Iterator<NonMaterialisedHttpHeader> iterator() {
		return this.headers.iterator();
	}

	@Override
	public int length() {
		return this.headers.size();
	}

}
