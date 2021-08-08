// Correct bug as per https://github.com/vercel/cosmosdb-query/pull/13/files
const fs = require('fs')
const fixFilePath = 'node_modules/@zeit/cosmosdb-query/lib/contains-partition-keys.js'
const rawContents = fs.readFileSync(fixFilePath)
const translatedContents = String(rawContents).replace('${node.property.name}', '${node.property.name || node.property.value}')
if (rawContents != translatedContents) {
    fs.writeFileSync(fixFilePath, translatedContents)
}