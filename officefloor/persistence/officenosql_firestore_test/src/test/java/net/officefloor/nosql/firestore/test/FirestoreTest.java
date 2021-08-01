package net.officefloor.nosql.firestore.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.frame.test.FileTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure can run emulator and write/read data.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class FirestoreTest {

	private final FileTestSupport file = new FileTestSupport();

	@Test
	public void startEmulator() throws Exception {

		final int port = 8081;

		// Ensure build image
		final String imageName = "officefloor-firestore:emulator";
		File buildDir = this.file.findFile(this.getClass(), "Dockerfile").getParentFile();
		OfficeFloorDockerUtil.ensureImageAvailable(imageName, buildDir);

		// Ensure running
		final String containerName = "officefloor-firestore";
		try (DockerContainerInstance container = OfficeFloorDockerUtil.ensureContainerAvailable("officefloor-firestore",
				"officefloor-firestore:emulator", (docker) -> {
					final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(
							new PortBinding(Binding.bindIpAndPort("0.0.0.0", port), ExposedPort.tcp(8080)));
					return docker.createContainerCmd(imageName).withName(containerName).withHostConfig(hostConfig);
				})) {

			// Create connection
			FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setEmulatorHost("localhost:" + port)
					.build();
			Firestore firestore = firestoreOptions.getService();

			// Add data
			for (int i = 1; i < 10; i++) {
				DocumentReference docRef = firestore.collection("test").document(String.valueOf(i));
				Map<String, Object> data = new HashMap<>();
				data.put("first", "Daniel");
				data.put("last", "Sagenschneider");
				data.put("born", 1978 + i);
				docRef.set(data).get();
			}

			// List all data
			int maxId = 0;
			for (QueryDocumentSnapshot snapshot : firestore.collection("test").get().get().getDocuments()) {
				System.out.println("DOC: " + snapshot.getId() + ", " + snapshot.getString("first") + " "
						+ snapshot.getString("last") + " " + snapshot.getLong("born"));
				int id = Integer.parseInt(snapshot.getId());
				maxId = Math.max(id, maxId);
			}
		}
	}
}