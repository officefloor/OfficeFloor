/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
