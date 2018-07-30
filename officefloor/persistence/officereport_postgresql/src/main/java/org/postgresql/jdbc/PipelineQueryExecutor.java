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
package org.postgresql.jdbc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;

import org.postgresql.PGProperty;
import org.postgresql.core.PGStream;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.QueryExecutorBase;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.v3.QueryExecutorImpl;

/**
 * Pipeline {@link QueryExecutor}.
 * 
 * @author Daniel Sagenschneider
 */
public class PipelineQueryExecutor extends QueryExecutorImpl {

	private static PGStream extractPGStream(QueryExecutorImpl executor) throws SQLException {
		try {
			Field field = QueryExecutorBase.class.getDeclaredField("pgStream");
			field.setAccessible(true);
			return (PGStream) field.get(executor);
		} catch (Exception ex) {
			throw new SQLException("Failure setting up " + PipelineQueryExecutor.class.getSimpleName(), ex);
		}
	}

	private static int extractCancelSignalTimeout(Properties info) throws SQLException {
		return PGProperty.CANCEL_SIGNAL_TIMEOUT.getInt(info) * 1000;
	}

	/**
	 * {@link ResultsProcessor}.
	 */
	private final ResultsProcessor resultsProcessor;

	public PipelineQueryExecutor(QueryExecutorImpl executor, Properties info) throws SQLException, IOException {
		super(extractPGStream(executor), executor.getUser(), executor.getDatabase(), extractCancelSignalTimeout(info),
				info);
		this.resultsProcessor = new ResultsProcessor();

		// Start processing
		Thread processor = new Thread(this.resultsProcessor);
		processor.setDaemon(true);
		processor.start();
	}

	@Override
	public void readStartupMessages() throws IOException, SQLException {
		// Already read startup messages
	}

	@Override
	protected void processResults(ResultHandler handler, int flags) throws IOException {

		// Register the handler for results
		this.resultsProcessor.addResults(new Results(handler, flags));
	}

	protected void processResults(Results results) throws IOException {
		super.processResults(results.handler, results.flags);
	}

	/**
	 * Results.
	 */
	private static class Results {

		/**
		 * {@link ResultHandler}.
		 */
		private final ResultHandler handler;

		/**
		 * Flags.
		 */
		private final int flags;

		private Results(ResultHandler handler, int flags) {
			this.handler = handler;
			this.flags = flags;
		}
	}

	/**
	 * Processes the results.
	 */
	private class ResultsProcessor implements Runnable {

		private final Deque<Results> resultsQueue = new ArrayDeque<>();

		private synchronized void addResults(Results results) {
			this.resultsQueue.add(results);
			this.notify(); // wake up immediately to process
		}

		@Override
		public void run() {
			while (!PipelineQueryExecutor.this.isClosed()) {

				// Obtain the results
				Results results = null;
				try {
					synchronized (this) {
						results = this.resultsQueue.pollFirst();
						while (results == null) {
							this.wait(1000);
							results = this.resultsQueue.pollFirst();
						}
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					return;
				}

				// Process results
				try {
					PipelineQueryExecutor.this.processResults(results);

				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}