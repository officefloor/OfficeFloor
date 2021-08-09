/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

// Start the Cosmos DB server
const { default: cosmosServer } = require("@zeit/cosmosdb-server")
var port = process.env.PORT /// must run on same port exposed from docker, otherwise hangs
if (!port) {
	console.log('Must specify environment variable PORT')
	process.exit(1)
}
cosmosServer().listen(port, () => {
  console.log(`Cosmos DB server running at https://localhost:${port}`)
});
