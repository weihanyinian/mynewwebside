/** 玩法参考 gabrielecirulli/2048 — 纯原生实现 */
;(function () {
  var SIZE = 4
  var grid = []
  var score = 0
  var over = false
  var won = false
  var undoStack = []
  var boardEl = document.getElementById('board')

  var COLORS = {
    2: ['#e0f2fe', '#0f172a'],
    4: ['#bae6fd', '#0f172a'],
    8: ['#7dd3fc', '#0f172a'],
    16: ['#38bdf8', '#fff'],
    32: ['#0ea5e9', '#fff'],
    64: ['#0284c7', '#fff'],
    128: ['#fbbf24', '#0f172a'],
    256: ['#f59e0b', '#fff'],
    512: ['#ea580c', '#fff'],
    1024: ['#dc2626', '#fff'],
    2048: ['#7c3aed', '#fff'],
  }

  function emitStats() {
    var hi = MOYU.setHi(score)
    MOYU.stats('最高分 ' + hi + ' · 得分 ' + score + (won ? ' · 已达2048!' : ''))
  }

  function emptyCells() {
    var e = []
    for (var i = 0; i < 16; i++) if (grid[i] === 0) e.push(i)
    return e
  }

  function addRandom() {
    var e = emptyCells()
    if (!e.length) return
    var i = e[(Math.random() * e.length) | 0]
    grid[i] = Math.random() < 0.9 ? 2 : 4
  }

  function cloneG() {
    return grid.slice()
  }

  function pushUndo() {
    undoStack.push({ g: cloneG(), score: score })
    if (undoStack.length > 12) undoStack.shift()
  }

  function init() {
    grid = Array(16).fill(0)
    score = 0
    over = false
    won = false
    undoStack = []
    addRandom()
    addRandom()
    MOYU.post({ type: 'playing' })
    emitStats()
    draw()
  }

  function slideLine(line) {
    var filtered = line.filter(function (x) {
      return x !== 0
    })
    var out = []
    var gained = 0
    for (var i = 0; i < filtered.length; i++) {
      if (i < filtered.length - 1 && filtered[i] === filtered[i + 1]) {
        var v = filtered[i] * 2
        out.push(v)
        gained += v
        if (v === 2048) won = true
        i++
      } else out.push(filtered[i])
    }
    while (out.length < SIZE) out.push(0)
    return { line: out, gained: gained }
  }

  function move(dir) {
    if (over) return
    var prev = cloneG()
    var ps = score
    var g = grid.slice()
    var moved = false
    var gained = 0
    if (dir === 'l' || dir === 'r') {
      for (var r = 0; r < SIZE; r++) {
        var lineH = []
        for (var c = 0; c < SIZE; c++) lineH.push(g[r * SIZE + c])
        if (dir === 'r') lineH.reverse()
        var RH = slideLine(lineH)
        gained += RH.gained
        var nlH = RH.line
        if (dir === 'r') nlH.reverse()
        for (c = 0; c < SIZE; c++) {
          if (g[r * SIZE + c] !== nlH[c]) moved = true
          g[r * SIZE + c] = nlH[c]
        }
      }
    } else {
      for (c = 0; c < SIZE; c++) {
        var lineV = []
        for (r = 0; r < SIZE; r++) lineV.push(g[r * SIZE + c])
        if (dir === 'd') lineV.reverse()
        var RV = slideLine(lineV)
        gained += RV.gained
        var nlV = RV.line
        if (dir === 'd') nlV.reverse()
        for (r = 0; r < SIZE; r++) {
          if (g[r * SIZE + c] !== nlV[r]) moved = true
          g[r * SIZE + c] = nlV[r]
        }
      }
    }
    if (!moved) return
    pushUndo()
    grid = g
    score += gained
    MOYU.setHi(score)
    addRandom()
    emitStats()
    if (!emptyCells().length && !canMove()) {
      over = true
      MOYU.gameover({ score: score })
    }
    draw()
  }

  function canMove() {
    for (var r = 0; r < SIZE; r++) {
      for (var c = 0; c < SIZE; c++) {
        var v = grid[r * SIZE + c]
        if (v === 0) return true
        if (c < SIZE - 1 && grid[r * SIZE + c + 1] === v) return true
        if (r < SIZE - 1 && grid[(r + 1) * SIZE + c] === v) return true
      }
    }
    return false
  }

  function draw() {
    boardEl.innerHTML = ''
    for (var i = 0; i < 16; i++) {
      var v = grid[i]
      var cell = document.createElement('div')
      cell.className = 'cell'
      var pair = COLORS[v] || ['#4c1d95', '#fff']
      cell.style.background = v ? pair[0] : 'transparent'
      cell.style.color = pair[1]
      cell.textContent = v ? String(v) : ''
      boardEl.appendChild(cell)
    }
  }

  function undo() {
    if (over || !undoStack.length) return
    var u = undoStack.pop()
    grid = u.g
    score = u.score
    over = false
    emitStats()
    draw()
  }

  document.getElementById('btn-new').onclick = function () {
    init()
  }
  document.getElementById('btn-undo').onclick = undo

  window.addEventListener(
    'keydown',
    function (e) {
      if (e.key === 'ArrowLeft') {
        move('l')
        e.preventDefault()
      }
      if (e.key === 'ArrowRight') {
        move('r')
        e.preventDefault()
      }
      if (e.key === 'ArrowUp') {
        move('u')
        e.preventDefault()
      }
      if (e.key === 'ArrowDown') {
        move('d')
        e.preventDefault()
      }
    },
    { passive: false }
  )

  var sx,
    sy,
    st = 0
  boardEl.addEventListener(
    'touchstart',
    function (e) {
      var t = e.changedTouches[0]
      sx = t.clientX
      sy = t.clientY
      st = Date.now()
    },
    { passive: true }
  )
  boardEl.addEventListener(
    'touchend',
    function (e) {
      var t = e.changedTouches[0]
      var dx = t.clientX - sx
      var dy = t.clientY - sy
      if (Date.now() - st > 450) return
      if (Math.abs(dx) < 24 && Math.abs(dy) < 24) return
      if (Math.abs(dx) > Math.abs(dy)) move(dx > 0 ? 'r' : 'l')
      else move(dy > 0 ? 'd' : 'u')
      e.preventDefault()
    },
    { passive: false }
  )

  init()
})()
