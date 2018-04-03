const fs = require("fs");
const resolveFrom = require("resolve-from");
const path = require("path");
const escapeStringRegexp = require("escape-string-regexp");
const { optimize: { CommonsChunkPlugin }} = require("webpack");
const { DependencyCheckWebpackPlugin } = require("@coremedia/dependency-check");
const { workspace: { getThemeConfig } } = require("@coremedia/tool-utils");
const deepMerge = require("./utils/deepMerge");

const themeConfig = getThemeConfig();

const include = [
  path.resolve(".")
];

const exclude = [
  // All modules but CoreMedia specific modules
  new RegExp(escapeStringRegexp(path.sep + "node_modules" + path.sep) + "((?!@coremedia).)*$"),
  new RegExp(escapeStringRegexp(path.sep + "legacy" + path.sep)),
  new RegExp(escapeStringRegexp(path.sep + "vendor" + path.sep)),
];

const DEFAULT_ENTRY_NAME = themeConfig.name;
const PREVIEW_ENTRY_NAME = "preview";

const entry = {};

// check if a javascript entry point exists for the module
let mainJsPath = null;
let previewJsPath = null;
try {
  mainJsPath = resolveFrom(themeConfig.path, ".");
  previewJsPath = path.resolve(path.dirname(mainJsPath), "preview.js");
  if (!fs.existsSync(previewJsPath)) {
    previewJsPath = null;
  }
} catch (e) {
  // the theme provides no entry point for JavaScript, so use an empty index.js (otherwise webpack will not run)
  mainJsPath = require.resolve("./emptyIndex");
}

entry[DEFAULT_ENTRY_NAME] = [ mainJsPath ];
if (previewJsPath) {
  entry[PREVIEW_ENTRY_NAME] = [ previewJsPath ];
}

module.exports = () => config => deepMerge(config,
        {
          entry: entry,
          output: {
            filename: path.join("js", "[name].js")
          },
          module: {
            rules: [
              {
                test: /\.js$/,
                loader: 'eslint-loader',
                options: {
                  cache: true
                },
                enforce: "pre",
                include: include,
                exclude: exclude
              },
              {
                test: /\.js$/,
                use: [
                  {
                    loader: 'babel-loader',
                    // babel < 7 does not support the .babelrc.js yet, so import it explicitly here
                    // remove this explicit merge when we are upgrading to babel 7
                    options: deepMerge(require("../.babelrc"), {
                      // babel-loader specific options
                      cacheDirectory: true
                    })
                  }
                ],
                exclude: exclude,
              },
            ]
          },
          plugins: [
            new DependencyCheckWebpackPlugin({
              exclude: exclude
            }),
            // preview entry is meant to be loaded after the default entry has been loaded
            // so common chunks can be moved to the default entry
            new CommonsChunkPlugin({
              name: DEFAULT_ENTRY_NAME,
              chunks: [
                DEFAULT_ENTRY_NAME,
                PREVIEW_ENTRY_NAME
              ]
            })
          ]
        }
);
