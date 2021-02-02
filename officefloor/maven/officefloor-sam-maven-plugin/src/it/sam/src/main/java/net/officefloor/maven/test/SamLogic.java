package net.officefloor.maven.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Logic of SAM application.
 * 
 * @author Daniel Sagenschneider
 */
public class SamLogic {

	public static void get(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("GET");
	}

	public static void post(ServerHttpConnection connection) throws IOException {
		try (Reader reader = new InputStreamReader(connection.getRequest().getEntity())) {
			try (Writer writer = connection.getResponse().getEntityWriter()) {
				for (int character = reader.read(); character != -1; character = reader.read()) {
					writer.write(character);
				}
			}
		}
	}

}