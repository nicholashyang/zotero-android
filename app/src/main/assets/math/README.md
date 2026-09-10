MathJax 3.2.2, `es5/tex-svg.js`, from the pinned `mathjax` npm package.
Apache 2.0 license is included in LICENSE. The fixed local SVG bundle includes fonts;
no network resource is needed. Only the base and AMS TeX packages are enabled.
All item text is inserted as text nodes and navigation/network requests are blocked.

Despite its name, the upstream `es5` bundle includes modern dependency syntax.
`scripts/mathjax/build.cjs` transpiles it to ES5 and prepends core-js 3.41.0
polyfills for the original Android 6 WebView. Its MIT license is LICENSE-core-js.
Regenerate with `cd scripts/mathjax && pnpm install --frozen-lockfile && pnpm build`.
The output must pass Acorn's ES5 parser; device tests additionally require real SVG output.
The generated bundle is committed so Android builds require no Node or network access.
