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

// Correct bug as per https://github.com/vercel/cosmosdb-query/pull/13/files
const fs = require('fs')
const fixFilePath = 'node_modules/@zeit/cosmosdb-query/lib/contains-partition-keys.js'
const rawContents = fs.readFileSync(fixFilePath)
const translatedContents = String(rawContents).replace('${node.property.name}', '${node.property.name || node.property.value}')
if (rawContents != translatedContents) {
    fs.writeFileSync(fixFilePath, translatedContents)
}
