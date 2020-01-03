package net.officefloor.polyglot.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Web.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebTypes {

	private String pathParameter;

	private String queryParameter;

	private String headerParameter;

	private String cookieParameter;

	private MockHttpParameters httpParameters;

	private MockHttpObject httpObject;

	private JavaObject object;

}