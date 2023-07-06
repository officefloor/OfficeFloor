package net.officefloor.server.google.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;

/**
 * Google Function {@link NonMaterialisedHttpHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleFunctionNonMaterialisedHttpHeaders implements NonMaterialisedHttpHeaders {

	/**
	 * {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers;

	/**
	 * Instantiate.
	 * 
	 * @param googleFunctionHeaders Google {@link HttpFunction} headers.
	 */
	public GoogleFunctionNonMaterialisedHttpHeaders(Map<String, List<String>> googleFunctionHeaders) {

		// Create listing of headers
		this.headers = new ArrayList<>();
		googleFunctionHeaders.forEach((name, values) -> {
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
