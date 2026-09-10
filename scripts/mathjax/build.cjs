// Rebuild the checked-in offline asset with: pnpm install --frozen-lockfile && pnpm build.
const fs = require('node:fs');
const path = require('node:path');
const babel = require('@babel/core');
const terser = require('terser');
const acorn = require('acorn');

async function build() {
  // MathJax's "es5" distribution still contains modern syntax in bundled dependencies.
  // Android 6's original WebView also needs the runtime polyfills (Map, Symbol, etc.).
  const input = fs.readFileSync(require.resolve('mathjax/es5/tex-svg.js'), 'utf8');
  const transformed = babel.transformSync(input, {
    configFile: false, babelrc: false, sourceType: 'script',
    presets: [[require.resolve('@babel/preset-env'), { targets: { ie: '11' }, modules: false }]],
  }).code;
  const polyfills = fs.readFileSync(require.resolve('core-js-bundle/minified.js'), 'utf8');
  const minified = await terser.minify(polyfills + '\n' + transformed, {
    ecma: 5, compress: false, mangle: false, format: { comments: /^!/ },
  });
  // Fail the regeneration if a dependency reintroduces unsupported syntax.
  acorn.parse(minified.code, { ecmaVersion: 5 });
  const out = path.resolve(__dirname, '../../app/src/main/assets/math');
  fs.writeFileSync(path.join(out, 'tex-svg.js'), minified.code + '\n');
  fs.copyFileSync(require.resolve('core-js-bundle/LICENSE'), path.join(out, 'LICENSE-core-js'));
}
build().catch(error => { console.error(error); process.exitCode = 1; });
