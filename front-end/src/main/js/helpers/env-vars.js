//add error handling, if that's even possible in js...
async function envVarsDefined(filePath){
    // unused module, keeping it as a reference

    const fs = require("fs");
    const readline = require('readline');
    const { once } = require('events');

    let useEnvVars = false
    let envVarDefined = line => !line.startsWith("#")

    const rl = readline.createInterface({
        input: fs.createReadStream(filePath),
        output: process.stderr,
        terminal: false
    });

    rl.on('line', (line) => {
        if (line && envVarDefined(line.trim())){
            useEnvVars = true
        }
    })

    await once(rl, 'close')
    return useEnvVars
}

// the front-end.fastopt.js module is invoked twice (internally while creating the bundle).
// Unfortunately the first time it's invoked `env` is undefined. Therefore we cannot load the
// dotenv-safe package conditionally. This means we will always have to load it and validate the .env file
// even if we set `ignoreEnvVarFile` to true. (This means that the .env file always needs to be correct: e.g. we cannot
// have missing env vars. Refer to .env.sample).
// Note: The second invocation of front-end.fastopt.js instead will have the correct values.
// The return value of the function will be set correctly (TODO dicover why this is invoked twice)
function setEnvVars(ignoreEnvVarFile){
    if(ignoreEnvVarFile){
        return JSON.stringify({})
    }
    else{
        const { envFileLocation, envSampleFileLocation } = require('./common');
        const config = { path: envFileLocation, example: envSampleFileLocation, allowEmptyValues: true };
        const dotenv = require("dotenv-safe").config(config);
        return JSON.stringify(dotenv.parsed)
    }
}

// logging as error to avoid module.exports to fail
// (it is probably used as output of a shell command...)
function logDevServerEnvVars(env) {
    console.error(
        env ? `[INFO] webpack-dev-server env: ${JSON.stringify(env)}`
            : `[INFO] webpack-dev-server env not defined: do not worry, it is not an ERROR.`
    );
}

module.exports = {
    envVarsDefined,
    setEnvVars,
    logDevServerEnvVars
}
