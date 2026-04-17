#!/usr/bin/env node
/**
 * 按文件内容 SHA-256 分组，列出仓库内重复文件（相同内容、可能不同路径）。
 * 用法（在仓库根目录）：node scripts/find-duplicate-files.mjs
 * 默认跳过：node_modules、.git、dist、build、target、.vite、coverage、.npm_cache
 */
import { createHash } from 'node:crypto'
import { readdir, readFile, stat } from 'node:fs/promises'
import path from 'node:path'

const ROOT = process.argv[2] ? path.resolve(process.argv[2]) : process.cwd()
const SKIP_DIRS = new Set([
  'node_modules',
  '.git',
  'dist',
  'build',
  'target',
  '.vite',
  'coverage',
  '.npm_cache',
  '.idea',
  '.vscode',
])
const MAX_BYTES = 80 * 1024 * 1024

/** @type {Map<string, string[]>} */
const byHash = new Map()

async function walk(dir) {
  let entries
  try {
    entries = await readdir(dir, { withFileTypes: true })
  } catch {
    return
  }
  for (const e of entries) {
    const full = path.join(dir, e.name)
    if (e.isDirectory()) {
      if (SKIP_DIRS.has(e.name)) continue
      await walk(full)
      continue
    }
    if (!e.isFile()) continue
    let st
    try {
      st = await stat(full)
    } catch {
      continue
    }
    if (st.size > MAX_BYTES) continue
    let buf
    try {
      buf = await readFile(full)
    } catch {
      continue
    }
    const h = createHash('sha256').update(buf).digest('hex')
    const rel = path.relative(ROOT, full).split(path.sep).join('/')
    let list = byHash.get(h)
    if (!list) {
      list = []
      byHash.set(h, list)
    }
    list.push(rel)
  }
}

await walk(ROOT)

let groups = 0
for (const [, files] of byHash) {
  if (files.length < 2) continue
  groups++
  files.sort()
  console.log(`\n--- 相同内容 (${files.length} 个文件) ---`)
  for (const f of files) console.log(f)
}

if (groups === 0) {
  console.log('未发现内容完全相同的重复文件（在扫描范围内）。')
} else {
  console.log(`\n共 ${groups} 组重复内容。请人工确认后删除多余副本。`)
}
