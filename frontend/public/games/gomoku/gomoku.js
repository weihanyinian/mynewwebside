;(function () {
  var N = 15
  var EMPTY = 0,
    HUM = 1,
    AI = 2
  var canvas = document.getElementById('cv')
  var ctx = canvas.getContext('2d')
  var board,
    over,
    wins,
    cell,
    msgEl = document.getElementById('msg')

  var dirs = [
    [1, 0],
    [0, 1],
    [1, 1],
    [1, -1],
  ]

  function emit() {
    var hi = MOYU.setHi(wins)
    MOYU.stats('连胜纪录 ' + hi + ' · 当前连胜 ' + wins)
  }

  function sizeCanvas() {
    var side = Math.min(window.innerWidth - 28, 480)
    cell = side / (N + 1)
    canvas.width = side
    canvas.height = side
  }

  function checkWin(x, y, p) {
    for (var d = 0; d < dirs.length; d++) {
      var dx = dirs[d][0],
        dy = dirs[d][1]
      var c = 1
      for (var s = 1; s < 5; s++) {
        var nx = x + dx * s,
          ny = y + dy * s
        if (nx < 0 || ny < 0 || nx >= N || ny >= N || board[ny][nx] !== p) break
        c++
      }
      for (s = 1; s < 5; s++) {
        nx = x - dx * s
        ny = y - dy * s
        if (nx < 0 || ny < 0 || nx >= N || ny >= N || board[ny][nx] !== p) break
        c++
      }
      if (c >= 5) return true
    }
    return false
  }

  function scorePoint(x, y, p) {
    var sc = 0
    for (var d = 0; d < dirs.length; d++) {
      var dx = dirs[d][0],
        dy = dirs[d][1]
      var a = 0,
        b = 0
      for (var s = 1; s < 5; s++) {
        var nx = x + dx * s,
          ny = y + dy * s
        if (nx < 0 || ny < 0 || nx >= N || ny >= N) break
        if (board[ny][nx] === p) a++
        else break
      }
      for (s = 1; s < 5; s++) {
        nx = x - dx * s
        ny = y - dy * s
        if (nx < 0 || ny < 0 || nx >= N || ny >= N) break
        if (board[ny][nx] === p) b++
        else break
      }
      var t = a + b + 1
      if (t >= 5) sc += 1e5
      else if (t === 4) sc += 5e3
      else if (t === 3) sc += 200
      else if (t === 2) sc += 20
    }
    return sc
  }

  function aiMove() {
    var best = -1,
      bx = 7,
      by = 7
    for (var y = 0; y < N; y++) {
      for (var x = 0; x < N; x++) {
        if (board[y][x] !== EMPTY) continue
        var atk = scorePoint(x, y, AI)
        var def = scorePoint(x, y, HUM)
        var s = atk * 1.05 + def
        if (s > best) {
          best = s
          bx = x
          by = y
        }
      }
    }
    board[by][bx] = AI
    if (checkWin(bx, by, AI)) {
      over = true
      wins = 0
      msgEl.textContent = 'AI 获胜 · 连胜已清零'
      MOYU.gameover({})
      emit()
    }
    draw()
  }

  function draw() {
    var dark = document.documentElement.classList.contains('moyu-dark')
    ctx.fillStyle = dark ? '#451a03' : '#fde68a'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    var pad = cell / 2
    var gs = (canvas.width - cell) / N
    for (var i = 0; i < N; i++) {
      ctx.strokeStyle = dark ? 'rgba(250,250,250,0.25)' : 'rgba(0,0,0,0.2)'
      ctx.beginPath()
      ctx.moveTo(pad + i * gs, pad)
      ctx.lineTo(pad + i * gs, pad + (N - 1) * gs)
      ctx.stroke()
      ctx.beginPath()
      ctx.moveTo(pad, pad + i * gs)
      ctx.lineTo(pad + (N - 1) * gs, pad + i * gs)
      ctx.stroke()
    }
    var r = gs * 0.42
    for (var y = 0; y < N; y++) {
      for (var x = 0; x < N; x++) {
        var cx = pad + x * gs
        var cy = pad + y * gs
        if (board[y][x] === HUM) {
          ctx.fillStyle = '#0f172a'
          ctx.beginPath()
          ctx.arc(cx, cy, r, 0, Math.PI * 2)
          ctx.fill()
          ctx.strokeStyle = 'rgba(255,255,255,0.35)'
          ctx.stroke()
        } else if (board[y][x] === AI) {
          ctx.fillStyle = '#f8fafc'
          ctx.beginPath()
          ctx.arc(cx, cy, r, 0, Math.PI * 2)
          ctx.fill()
          ctx.strokeStyle = 'rgba(0,0,0,0.25)'
          ctx.stroke()
        }
      }
    }
  }

  function getCell(clientX, clientY) {
    var rect = canvas.getBoundingClientRect()
    var mx = clientX - rect.left
    var my = clientY - rect.top
    var pad = cell / 2
    var gs = (canvas.width - cell) / N
    var x = Math.round((mx - pad) / gs)
    var y = Math.round((my - pad) / gs)
    if (x < 0 || x >= N || y < 0 || y >= N) return null
    var cx = pad + x * gs
    var cy = pad + y * gs
    if (Math.hypot(mx - cx, my - cy) > gs * 0.48) return null
    return { x: x, y: y }
  }

  function tryHuman(px, py) {
    if (over) return
    var p = getCell(px, py)
    if (!p) return
    if (board[p.y][p.x] !== EMPTY) return
    board[p.y][p.x] = HUM
    if (checkWin(p.x, p.y, HUM)) {
      over = true
      wins++
      MOYU.setHi(wins)
      msgEl.textContent = '你赢了！'
      MOYU.gameover({ win: true })
      emit()
      draw()
      return
    }
    aiMove()
    emit()
  }

  var lastTap = 0
  canvas.addEventListener(
    'click',
    function (e) {
      var now = Date.now()
      if (now - lastTap < 280) return
      lastTap = now
      tryHuman(e.clientX, e.clientY)
    },
    false
  )
  canvas.addEventListener(
    'touchend',
    function (e) {
      e.preventDefault()
      var t = e.changedTouches[0]
      var now = Date.now()
      if (now - lastTap < 320) return
      lastTap = now
      tryHuman(t.clientX, t.clientY)
    },
    { passive: false }
  )

  function initBoard() {
    board = Array.from({ length: N }, function () {
      return Array(N).fill(EMPTY)
    })
    over = false
    msgEl.textContent = '你执黑先行'
    draw()
  }

  function init() {
    wins = 0
    MOYU.getHi()
    sizeCanvas()
    initBoard()
    MOYU.post({ type: 'playing' })
    emit()
  }

  document.getElementById('again').onclick = function () {
    initBoard()
    MOYU.post({ type: 'playing' })
    emit()
  }
  document.getElementById('resetHi').onclick = function () {
    wins = 0
    localStorage.setItem('moyu-hi-' + MOYU.id, '0')
    emit()
  }

  window.addEventListener('resize', function () {
    sizeCanvas()
    draw()
  })
  init()
})()
