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
package net.officefloor.plugin.jndi.ldap;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.factory.DefaultDirectoryServiceFactory;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.ldap.handlers.sasl.digestMD5.DigestMd5MechanismHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;

/**
 * Embedded LDAP for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class EmbeddedLdap {

	/**
	 * Allow for running.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to start.
	 */
	public static void main(String... args) throws Exception {

		final int LDAP_PORT = 63636;

		EmbeddedLdap ldap = new EmbeddedLdap();
		ldap.addPartition("OfficeFloor", "dc=officefloor,dc=net");
		ldap.start(LDAP_PORT);

		// Populate the ldap server
		ldap.addCredentialStoreEntries();
	}

	/**
	 * {@link DirectoryService}.
	 */
	private final DirectoryService directoryService;

	/**
	 * {@link LdapServer}.
	 */
	private final LdapServer ldapServer = new LdapServer();

	/**
	 * URL to the {@link LdapServer}.
	 */
	private String ldapUrl;

	/**
	 * Initiate.
	 * 
	 * @throws Exception
	 *             If fails to construct.
	 */
	public EmbeddedLdap() throws Exception {

		// Obtain details for setup of directory
		String userName = System.getProperty("user.name");
		String instanceDirectory = this.getClass().getName().replace('.', '_');

		// Ensure directory is available
		String tempDirectory = System.getProperty("java.io.tmpdir");
		File workingDirectory = new File(tempDirectory, userName + "/"
				+ instanceDirectory);
		workingDirectory.mkdirs();

		// Create the directory service
		DefaultDirectoryServiceFactory factory = new DefaultDirectoryServiceFactory();
		factory.init("Test");
		this.directoryService = factory.getDirectoryService();
		this.directoryService.getChangeLog().setEnabled(false);
		this.directoryService.setShutdownHookEnabled(true);
		this.directoryService.setAllowAnonymousAccess(true);
		this.directoryService.setInstanceLayout(new InstanceLayout(
				workingDirectory));
	}

	/**
	 * Obtains the {@link DirectoryService}.
	 * 
	 * @return {@link DirectoryService}.
	 */
	public DirectoryService getDirectoryService() {
		return this.directoryService;
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

		// Add in memory partition to the service
		AvlPartition partition = new AvlPartition(
				this.directoryService.getSchemaManager());
		partition.setId(partitionId);
		partition.setSuffixDn(new Dn(this.directoryService.getSchemaManager(),
				partitionDn));
		partition.initialize();
		this.directoryService.addPartition(partition);

		// Ensure have attributes
		if (attributes.length == 0) {
			return; // no attributes
		}

		// Specifies the index for the partition
		Set<Index<?, String>> indexedAttributes = new HashSet<>();
		for (String attribute : attributes) {
			indexedAttributes.add(new JdbmIndex<>(attribute, false));
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

		// Initialise the LDAP server
		this.ldapServer.setDirectoryService(this.directoryService);
		this.ldapServer.setTransports(new TcpTransport(port));

		// Provide SASL for MD5 login
		this.ldapServer.setSaslHost("localhost");
		this.ldapServer.setSaslRealms(Arrays.asList("officefloor"));
		this.ldapServer.setSearchBaseDn("ou=People,dc=officefloor,dc=net");
		this.ldapServer.addSaslMechanismHandler("DIGEST-MD5",
				new DigestMd5MechanismHandler());

		// Specify the URL for server
		this.ldapUrl = "ldap://localhost:" + port;

		// Start the service
		System.out.print("Starting LDAP server ... ");
		System.out.flush();
		this.directoryService.startup();
		this.ldapServer.start();
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
			this.ldapServer.stop();
		} finally {
			this.directoryService.shutdown();
		}
		System.out.println("Stopped");
	}

	/**
	 * Obtains the JNDI {@link DirContext} to the {@link EmbeddedLdap} instance.
	 * 
	 * @return {@link DirContext}.
	 * @throws Exception
	 *             If fails to obtain the {@link DirContext}.
	 */
	protected DirContext getDirContext() throws Exception {

		// Create the context
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.setProperty(Context.PROVIDER_URL, this.ldapUrl);
		DirContext context = new InitialDirContext(env);

		// Return the context
		return context;
	}

	/**
	 * Obtains the logged in JNDI {@link DirContext} to the {@link EmbeddedLdap}
	 * instance.
	 * 
	 * @param authentication
	 *            Means of authentication.
	 * @param userId
	 *            User Id.
	 * @param credentials
	 *            Credentials.
	 * @param additionalProperties
	 *            Additional property name/value pairs.
	 * @return {@link DirContext}.
	 * @throws Exception
	 *             If fails to obtain the {@link DirContext}.
	 */
	protected DirContext getDirContext(String authentication, String userId,
			String credentials, String... additionalProperties)
			throws Exception {

		// Create the context
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.setProperty(Context.PROVIDER_URL, this.ldapUrl);
		env.setProperty(Context.SECURITY_AUTHENTICATION, authentication);
		env.setProperty(Context.SECURITY_PRINCIPAL, userId);
		env.setProperty(Context.SECURITY_CREDENTIALS, credentials);
		for (int i = 0; i < additionalProperties.length; i += 2) {
			String name = additionalProperties[i];
			String value = additionalProperties[i + 1];
			env.setProperty(name, value);
		}
		DirContext context = new InitialDirContext(env);

		// Return the context
		return context;
	}

	/**
	 * Creates a new {@link Entry}.
	 * 
	 * @param partitionDn
	 *            Domain name for {@link Partition}.
	 * @return New {@link Entry}.
	 * @throws Exception
	 *             If fails to create {@link Entry}.
	 */
	public Entry newEntry(String partitionDn) throws Exception {
		return this.directoryService.newEntry(new Dn(this.directoryService
				.getSchemaManager(), partitionDn));
	}

	/**
	 * Binds the {@link Entry}.
	 * 
	 * @param entry
	 *            {@link Entry}.
	 * @throws Exception
	 *             If fails to bind the {@link Entry}.
	 */
	public void bindEntry(Entry entry) throws Exception {
		this.directoryService.getAdminSession().add(entry);
	}

	/**
	 * Adds a {@link Entry}.
	 * 
	 * @param dn
	 *            Path for {@link Entry}.
	 * @param attributeNameValues
	 *            Attribute name and value pairs. Multiple values may be
	 *            semi-colon &quot;;&quot; separated.
	 * @throws Exception
	 *             If fails to add {@link Entry}.
	 */
	public void addEntry(String dn, String... attributeNameValues)
			throws Exception {
		Entry entry = this.newEntry(dn);
		for (int i = 0; i < attributeNameValues.length; i += 2) {
			String attributeName = attributeNameValues[i];
			String attributeValues = attributeNameValues[i + 1];

			// Add the attribute
			if ("userPassword".equalsIgnoreCase(attributeName)) {
				// Add the password (must be binary entry)
				entry.add(attributeName,
						attributeValues.getBytes(Charset.forName("ASCII")));
			} else {
				// Add the attribute values
				String[] values = attributeValues.split(";");
				entry.add(attributeName, values);
			}
		}
		this.bindEntry(entry);
	}

	/**
	 * Adds the {@link Entry} instances to use as a Credential Store.
	 * 
	 * @throws Exception
	 *             If fails to add {@link Entry} instances.
	 */
	public void addCredentialStoreEntries() throws Exception {

		// Digest MD5 of 'daniel:officefloor:password'
		final String digestMd5Password = "{MD5}msu723GSLovbwuaPnaLcnQ==";
		final String plainPassword = "password";

		this.addEntry("dc=officefloor,dc=net", "objectClass",
				"top;domain;extensibleObject", "dc", "officefloor");
		this.addEntry("ou=People,dc=officefloor,dc=net", "objectClass",
				"top;organizationalUnit", "ou", "People");
		this.addEntry("uid=daniel,ou=People,dc=officefloor,dc=net",
				"objectClass", "top;person;organizationalPerson;inetOrgPerson",
				"uid", "daniel", "cn", "Daniel", "sn", "Sagenschneider",
				"userPassword", plainPassword, "userPassword",
				digestMd5Password);
		this.addEntry("uid=melanie,ou=People,dc=officefloor,dc=net",
				"objectClass", "top;person;organizationalPerson;inetOrgPerson",
				"uid", "melanie", "cn", "Melanie", "sn", "Sagenschneider",
				"userPassword", plainPassword, "userPassword",
				digestMd5Password);
		this.addEntry("ou=Groups,dc=officefloor,dc=net", "objectClass",
				"top;organizationalUnit", "ou", "Groups");
		this.addEntry(
				"cn=developers,ou=Groups,dc=officefloor,dc=net",
				"objectClass",
				"top;groupOfNames",
				"cn",
				"developers",
				"ou",
				"developer",
				"member",
				"uid=daniel,ou=People,dc=officefloor,dc=net;uid=melanie,ou=People,dc=officefloor,dc=net");
		this.addEntry("cn=committers,ou=Groups,dc=officefloor,dc=net",
				"objectClass", "top;groupOfNames", "cn", "committers", "ou",
				"committer", "member",
				"uid=daniel,ou=People,dc=officefloor,dc=net");
	}

}