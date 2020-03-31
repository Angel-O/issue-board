const merge = require('webpack-merge');
const webpack = require('webpack');
const { setEnvVars, logDevServerEnvVars } = require('./helpers/env-vars.js');
const core = require('./webpack-core.config.js', { encoding:'utf8', flag:'r' });
const generatedConfig = require("./scalajs.webpack.config.js");

const entries = {};
entries[Object.keys(generatedConfig.entry)[0]] = "scalajs";

module.exports = function(env) {

  logDevServerEnvVars(env);

  return merge(core, {
    devtool: "cheap-module-eval-source-map",
    entry: entries,
    module: {
      noParse: (content) => {
        return content.endsWith("-fastopt.js");
      },
      rules: [
        {
          test: /\-fastopt.js$/,
          use: [ require.resolve('./fastopt-loader.js') ]
        }
      ]
    },
    plugins: [
      new webpack.DefinePlugin({
        'process.env': setEnvVars(env && env.ignoreEnvVarFile)
      })
    ]
  })
};


