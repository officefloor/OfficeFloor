/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.benchmark;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;

/**
 * Tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ TestSuite.SpringJsonTest.class, DbTest.class, QueriesTest.class, FortunesTest.class, UpdateTest.class,
		TestSuite.SpringPlaintextTest.class })
public class TestSuite {

	public static class SpringJsonTest extends JsonTest {

		@ClassRule
		public static PostgreSqlRule dataSource = BenchmarkEnvironment.createPostgreSqlRule();
	}

	public static class SpringPlaintextTest extends PlaintextTest {

		@ClassRule
		public static PostgreSqlRule dataSource = BenchmarkEnvironment.createPostgreSqlRule();
	}

}