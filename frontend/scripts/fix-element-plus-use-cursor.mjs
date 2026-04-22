import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const root = path.resolve(__dirname, '..')

const targets = [
  path.join(root, 'node_modules/element-plus/es/hooks/use-cursor/index.mjs'),
  path.join(root, 'node_modules/element-plus/lib/hooks/use-cursor/index.js'),
  path.join(root, 'node_modules/element-plus/es/hooks/use-cursor/index.d.ts'),
  path.join(root, 'node_modules/element-plus/lib/hooks/use-cursor/index.d.ts'),
]

const jsContent = `const noop = () => {};
export const useCursor = () => [noop, noop];
`

const dtsContent = `import type { ShallowRef } from 'vue';
export declare const useCursor: (
  input: ShallowRef<HTMLInputElement | undefined>
) => [() => void, () => void];
`

for (const target of targets) {
  const exists = fs.existsSync(target)
  if (exists) {
    const current = fs.readFileSync(target, 'utf8')
    // Only rewrite previously generated shim placeholders.
    if (!current.includes('const noop = () => {}')) {
      continue
    }
  }

  fs.mkdirSync(path.dirname(target), { recursive: true })
  fs.writeFileSync(
    target,
    target.endsWith('.d.ts') ? dtsContent : jsContent,
    'utf8'
  )
  console.log(
    `[postinstall] ${exists ? 'updated' : 'created'}: ${path.relative(root, target)}`
  )
}
