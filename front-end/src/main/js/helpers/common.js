const path = require("path");
const root = path.resolve(__dirname, "../../../../..");

module.exports = {
    path: path,
    root: root,
    envFileLocation: `${root}/.env`,
    envSampleFileLocation: `${root}/.env.sample`,
}