/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.ldap;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.name.LdapDN;

/**
 * Embedded LDAP for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class EmbeddedLdap {

	/**
	 * {@link DirectoryService}.
	 */
	private final DirectoryService directory = new DefaultDirectoryService();

	/**
	 * {@link LdapServer}.
	 */
	private final LdapServer ldap = new LdapServer();

	/**
	 * Obtains the {@link DirectoryService}.
	 * 
	 * @return {@link DirectoryService}.
	 */
	public DirectoryService getDirectoryService() {
		return this.directory;
	}

	/**
	 * Adds a {@link Partition}.
	 * 
	 * @param partitionId
	 *            Id of the {@link Partition}.
	 * @param partitionDn
	 *            Domain name for {@link Partition}.
	 * @param attributes
	 *            Attributes for indexing of {@link Partition}.
	 * @throws Exception
	 *             If fails to add {@link Partition}.
	 */
	public void addPartition(String partitionId, String partitionDn,
			String... attributes) throws Exception {

		// Create the partition
		JdbmPartition partition = new JdbmPartition();
		partition.setId(partitionId);
		partition.setSuffix(partitionDn);
		this.directory.addPartition(partition);

		// Ensure have attributes
		if (attributes.length == 0) {
			return; // no attributes
		}

		// Specifies the index for the partition
		Set<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();
		for (String attribute : attributes) {
			indexedAttributes
					.add(new JdbmIndex<String, ServerEntry>(attribute));
		}
		partition.setIndexedAttributes(indexedAttributes);
	}

	/**
	 * Starts the LDAP server on the input port.
	 * 
	 * @param port
	 *            Port.
	 * @throws Exception
	 *             If fails to start.
	 */
	public void start(int port) throws Exception {

		// Specify to use temporary directory
		String tempDirectory = System.getProperty("java.io.tmpdir");
		String userName = System.getProperty("user.name");
		File workingDirectory = new File(tempDirectory, userName
				+ "/EmbeddedLdap-" + port + "-" + System.currentTimeMillis());
		TestCase.assertFalse("Need new working directory", workingDirectory
				.exists());
		workingDirectory.mkdirs();
		this.directory.setWorkingDirectory(workingDirectory);

		// Initialise the directory service
		this.directory.getChangeLog().setEnabled(false);
		this.directory.setDenormalizeOpAttrsEnabled(true);

		// Initialise the LDAP server
		this.ldap.setDirectoryService(this.directory);
		this.ldap.setTransports(new TcpTransport(port));

		// Start the service
		System.out.print("Starting LDAP server ... ");
		System.out.flush();
		this.directory.startup();
		this.ldap.start();
		System.out.println("Started");
	}

	/**
	 * Stops the LDAP server.
	 * 
	 * @throws Exception
	 *             If fails to stop.
	 */
	public void stop() throws Exception {
		// Stop the service
		System.out.print("Stopping LDAP server ... ");
		System.out.flush();
		try {
			this.ldap.stop();
		} finally {
			this.directory.shutdown();
		}
		System.out.println("Stopped");
	}

	/**
	 * Creates a new {@link ServerEntry}.
	 * 
	 * @param partitionDn
	 *            Domain name for {@link Partition}.
	 * @return New {@link ServerEntry}.
	 * @throws Exception
	 *             If fails to create {@link ServerEntry}.
	 */
	public ServerEntry newEntry(String partitionDn) throws Exception {
		return this.directory.newEntry(new LdapDN(partitionDn));
	}

	/**
	 * Binds the {@link ServerEntry}.
	 * 
	 * @param entry
	 *            {@link ServerEntry}.
	 * @throws Exception
	 *             If fails to bind the {@link ServerEntry}.
	 */
	public void bindEntry(ServerEntry entry) throws Exception {
		this.directory.getAdminSession().add(entry);
	}

}