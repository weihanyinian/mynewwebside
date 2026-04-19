/** 参考 dionyziz/canvas-tetris — 简化原生 Canvas */
;(function () {
  var W = 10
  var H = 20
  var SHAPES = [
    [[1, 1, 1, 1]],
    [
      [1, 1],
      [1, 1],
    ],
    [
      [0, 1, 0],
      [1, 1, 1],
    ],
    [
      [1, 0, 0],
      [1, 1, 1],
    ],
    [
      [0, 0, 1],
      [1, 1, 1],
    ],
    [
      [1, 1, 0],
      [0, 1, 1],
    ],
    [
      [0, 1, 1],
      [1, 1, 0],
    ],
  ]
  var COLORS = ['', '#38bdf8', '#4ade80', '#fbbf24', '#f472b6', '#a78bfa', '#fb7185']

  var canvas = document.getElementById('cv')
  var ctx = canvas.getContext('2d')
  var board,
    piece,
    runScore,
    over,
    timer,
    cell = 20

  function emit() {
    var hi = MOYU.setHi(runScore)
    MOYU.stats('最高分 ' + hi + ' · 得分 ' + runScore)
  }

  function emptyBoard() {
    return Array.from({ length: H }, function () {
      return Array(W).fill(0)
    })
  }

  function collide(shape, x, y) {
    for (var r = 0; r < shape.length; r++) {
      for (var c = 0; c < shape[r].length; c++) {
        if (!shape[r][c]) continue
        var nx = x + c
        var ny = y + r
        if (nx < 0 || nx >= W || ny >= H) return true
        if (ny >= 0 && board[ny][nx]) return true
      }
    }
    return false
  }

  function spawn() {
    var shape = SHAPES[(Math.random() * SHAPES.length) | 0].map(function (r) {
      return r.slice()
    })
    var c = ((Math.random() * 6) | 0) + 1
    var x = ((W - shape[0].length) / 2) | 0
    if (collide(shape, x, 0)) {
      over = true
      piece = null
      if (timer) clearInterval(timer)
      timer = null
      MOYU.gameover({ score: runScore })
      emit()
      draw()
      return
    }
    piece = { shape: shape, x: x, y: 0, c: c }
  }

  function mergePiece() {
    var p = piece
    for (var r = 0; r < p.shape.length; r++) {
      for (var c = 0; c < p.shape[r].length; c++) {
        if (!p.shape[r][c]) continue
        var y = p.y + r
        var x = p.x + c
        if (y >= 0) board[y][x] = p.c
      }
    }
    var cleared = 0
    var nb = board.filter(function (row) {
      if (row.every(function (x) { return x > 0 })) {
        cleared++
        return false
      }
      return true
    })
    while (nb.length < H) nb.unshift(Array(W).fill(0))
    board = nb
    runScore += cleared * 100 + cleared * cleared * 20
    MOYU.setHi(runScore)
    piece = null
    if (!over) spawn()
    emit()
  }

  function softDrop() {
    if (!piece || over) return
    if (!collide(piece.shape, piece.x, piece.y + 1)) piece.y++
    else mergePiece()
    draw()
  }

  function moveH(dx) {
    if (!piece || over) return
    if (!collide(piece.shape, piece.x + dx, piece.y)) piece.x += dx
    draw()
  }

  function rotate() {
    if (!piece || over) return
    var p = piece
    var s = p.shape
    var rows = s.length
    var cols = s[0].length
    var ns = Array.from({ length: cols }, function (_, c) {
      return Array.from({ length: rows }, function (_, r) {
        return s[rows - 1 - r][c]
      })
    })
    if (!collide(ns, p.x, p.y)) piece = { shape: ns, x: p.x, y: p.y, c: p.c }
    draw()
  }

  function draw() {
    var g = board.map(function (r) {
      return r.slice()
    })
    if (piece) {
      for (var r = 0; r < piece.shape.length; r++) {
        for (var c = 0; c < piece.shape[r].length; c++) {
          if (!piece.shape[r][c]) continue
          var y = piece.y + r
          var x = piece.x + c
          if (y >= 0 && y < H && x >= 0 && x < W) g[y][x] = piece.c
        }
      }
    }
    ctx.fillStyle = '#0f172a'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    for (var rr = 0; rr < H; rr++) {
      for (var cc = 0; cc < W; cc++) {
        var v = g[rr][cc]
        ctx.strokeStyle = 'rgba(255,255,255,0.08)'
        ctx.strokeRect(cc * cell, rr * cell, cell, cell)
        if (v) {
          ctx.fillStyle = COLORS[v % COLORS.length]
          ctx.fillRect(cc * cell + 1, rr * cell + 1, cell - 2, cell - 2)
        }
      }
    }
    if (over) {
      ctx.fillStyle = 'rgba(0,0,0,0.55)'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
    }
  }

  function sizeCanvas() {
    var maxW = Math.min(340, window.innerWidth - 28)
    cell = Math.floor(maxW / W)
    cell = Math.max(14, Math.min(24, cell))
    canvas.width = W * cell
    canvas.height = H * cell
  }

  function init() {
    if (timer) clearInterval(timer)
    sizeCanvas()
    board = emptyBoard()
    runScore = 0
    over = false
    MOYU.post({ type: 'playing' })
    spawn()
    timer = setInterval(softDrop, 580)
    emit()
    draw()
  }

  document.getElementById('keys').addEventListener('click', function (e) {
    var el = e.target && e.target.closest && e.target.closest('[data-a]')
    if (!el) return
    var a = el.getAttribute('data-a')
    if (a === 'left') moveH(-1)
    if (a === 'right') moveH(1)
    if (a === 'rot') rotate()
    if (a === 'down') softDrop()
  })

  window.addEventListener('keydown', function (e) {
    if (over) return
    if (e.key === 'ArrowLeft') moveH(-1)
    if (e.key === 'ArrowRight') moveH(1)
    if (e.key === 'ArrowDown') softDrop()
    if (e.key === 'ArrowUp') rotate()
    e.preventDefault()
  })

  var sx,
    sy,
    st = 0
  canvas.addEventListener('touchstart', function (e) {
    var t = e.touches[0]
    sx = t.clientX
    sy = t.clientY
    st = Date.now()
  })
  canvas.addEventListener(
    'touchend',
    function (e) {
      var t = e.changedTouches[0]
      var dx = t.clientX - sx
      var dy = t.clientY - sy
      if (Date.now() - st > 400) return
      if (Math.abs(dx) < 14 && Math.abs(dy) < 14) return
      if (Math.abs(dx) > Math.abs(dy)) moveH(dx > 0 ? 1 : -1)
      else if (dy > 0) softDrop()
      else rotate()
    },
    { passive: true }
  )

  window.addEventListener('orientationchange', function () {
    setTimeout(function () {
      sizeCanvas()
      draw()
    }, 280)
  })

  init()
})()
