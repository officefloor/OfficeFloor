/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.bayeux;

import java.util.HashMap;
import java.util.Map;

import org.cometd.bayeux.Message;
import org.junit.Assert;

/**
 * Validates the {@link Message}.
 * 
 * @author Daniel Sagenschneider
 */
public class MessageValidateUtil {

	/**
	 * Asserts the {@link Message} is as expected.
	 * 
	 * @param message
	 *            {@link Message} to validate.
	 * @param nameValuePairs
	 *            Name/value pairs expected on the {@link Message}.
	 */
	@SuppressWarnings("unchecked")
	public static void assertMessage(Message message, String... nameValuePairs) {

		// Ensure correct number of values
		Assert.assertEquals("Incorrect number of values",
				(nameValuePairs.length / 2), message.size());

		// Ensure the values are as expected
		for (int i = 0; i < nameValuePairs.length; i += 2) {

			// Obtain the expected name and value
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];

			// Determine matching for value
			if (value.startsWith("[")) {
				// Validate as an array

				// Ensure is an array value
				Object actual = message.get(name);
				Assert.assertTrue("'" + name + "' should be an array value ["
						+ actual.getClass().getName() + "]",
						(actual instanceof String[]));
				String[] arrayActual = (String[]) actual;

				// Validate as array value
				value = value.substring("[".length(),
						(value.length() - "]".length()));
				String[] arrayExpected = value.split(",");
				Assert.assertEquals("Incorrect number of array values for '"
						+ name + "'", arrayExpected.length, arrayActual.length);
				for (int e = 0; e < arrayExpected.length; e++) {
					Assert.assertEquals("Incorrect array value " + e + " for '"
							+ name + "'", arrayExpected[e], arrayActual[e]);
				}

			} else if (value.startsWith("{")) {
				// Validate as name/value pairs

				// Ensure is a map value
				Object actual = message.get(name);
				Assert.assertTrue("'" + name + "' shoudl be a map value ["
						+ actual.getClass().getName() + "]",
						(actual instanceof Map));
				Map<String, String> actualMap = (Map<String, String>) actual;

				// Validate as map value
				value = value.substring("{".length(),
						(value.length() - "}".length()));
				String[] mapEntries = value.split(",");
				Map<String, String> expectedMap = new HashMap<String, String>();
				for (String mapEntry : mapEntries) {
					String[] entryNameValue = mapEntry.split("=");
					Assert.assertEquals(
							"Inalid test data, as should always have two (name=value)",
							2, entryNameValue.length);
					expectedMap.put(entryNameValue[0], entryNameValue[1]);
				}
				Assert.assertEquals("Incorrect number of map values for '"
						+ name + "'", expectedMap.size(), actualMap.size());
				for (String mapName : expectedMap.keySet()) {
					Assert.assertEquals("Incorrect value for map '" + name
							+ "." + mapName + "'", expectedMap.get(mapName),
							actualMap.get(mapName));
				}

			} else {
				// Validate as single value
				Assert.assertEquals("Incorrect value for '" + name + "'",
						value, message.get(name));
			}
		}
	}

	/**
	 * All access via static methods.
	 */
	private MessageValidateUtil() {
	}

}