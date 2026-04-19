/** 玩法参考 CodeExplainedRepo/Snake-Game — Canvas 原生 */
;(function () {
  var COLS = 20
  var ROWS = 20
  var CELL = 16
  var canvas = document.getElementById('cv')
  var ctx = canvas.getContext('2d')
  var snake,
    dir,
    nextDir,
    food,
    score,
    over,
    paused,
    timer,
    tickMs

  function sizeCanvas() {
    var w = Math.min(360, Math.floor(window.innerWidth - 24))
    var g = Math.floor(w / COLS)
    g = Math.max(12, g)
    CELL = g
    canvas.width = COLS * CELL
    canvas.height = ROWS * CELL
  }

  function randFood() {
    for (;;) {
      var x = (Math.random() * COLS) | 0
      var y = (Math.random() * ROWS) | 0
      if (!snake.some(function (s) { return s.x === x && s.y === y })) {
        food = { x: x, y: y }
        return
      }
    }
  }

  function emit() {
    var hi = MOYU.setHi(score)
    MOYU.stats('最高分 ' + hi + ' · 得分 ' + score + (paused ? ' · 暂停' : ''))
  }

  function end() {
    over = true
    if (timer) clearInterval(timer)
    timer = null
    MOYU.setHi(score)
    MOYU.gameover({ score: score })
    emit()
    draw()
  }

  function tick() {
    if (over || paused) return
    dir = nextDir
    var head = { x: snake[0].x + dir.x, y: snake[0].y + dir.y }
    if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) return end()
    if (snake.some(function (s) { return s.x === head.x && s.y === head.y })) return end()
    snake.unshift(head)
    if (head.x === food.x && head.y === food.y) {
      score += 10
      emit()
      randFood()
    } else snake.pop()
    draw()
  }

  function dark() {
    return document.documentElement.classList.contains('moyu-dark')
  }

  function draw() {
    ctx.fillStyle = dark() ? '#020617' : '#f1f5f9'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = dark() ? 'rgba(148,163,184,0.5)' : 'rgba(51,65,85,0.35)'
    for (var x = 0; x <= COLS; x++) {
      ctx.beginPath()
      ctx.moveTo(x * CELL, 0)
      ctx.lineTo(x * CELL, ROWS * CELL)
      ctx.stroke()
    }
    for (var y = 0; y <= ROWS; y++) {
      ctx.beginPath()
      ctx.moveTo(0, y * CELL)
      ctx.lineTo(COLS * CELL, y * CELL)
      ctx.stroke()
    }
    ctx.fillStyle = dark() ? '#f472b6' : '#db2777'
    ctx.fillRect(food.x * CELL + 2, food.y * CELL + 2, CELL - 4, CELL - 4)
    snake.forEach(function (s, i) {
      ctx.fillStyle = i === 0 ? '#38bdf8' : '#22d3ee'
      ctx.fillRect(s.x * CELL + 1, s.y * CELL + 1, CELL - 2, CELL - 2)
    })
    if (over) {
      ctx.fillStyle = 'rgba(0,0,0,0.5)'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
    } else if (paused) {
      ctx.fillStyle = 'rgba(0,0,0,0.35)'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
    }
  }

  function setDir(dx, dy) {
    if (over) return
    if (dx === -dir.x && dy === -dir.y) return
    nextDir = { x: dx, y: dy }
  }

  function resetTimer() {
    if (timer) clearInterval(timer)
    tickMs = parseInt(document.getElementById('speed').value, 10) || 120
    timer = setInterval(tick, tickMs)
  }

  function init() {
    sizeCanvas()
    snake = [
      { x: 4, y: 10 },
      { x: 3, y: 10 },
      { x: 2, y: 10 },
    ]
    dir = { x: 1, y: 0 }
    nextDir = { x: 1, y: 0 }
    score = 0
    over = false
    paused = false
    MOYU.post({ type: 'playing' })
    randFood()
    emit()
    resetTimer()
    draw()
  }

  document.getElementById('restart').onclick = init
  document.getElementById('pause').onclick = function () {
    if (over) return
    paused = !paused
    emit()
    draw()
  }
  document.getElementById('speed').onchange = function () {
    if (!over) resetTimer()
  }

  document.querySelectorAll('[data-d]').forEach(function (btn) {
    btn.onclick = function () {
      var p = btn.getAttribute('data-d').split(',')
      setDir(parseInt(p[0], 10), parseInt(p[1], 10))
    }
  })

  window.addEventListener('keydown', function (e) {
    if (e.key === 'ArrowLeft') setDir(-1, 0)
    if (e.key === 'ArrowRight') setDir(1, 0)
    if (e.key === 'ArrowUp') setDir(0, -1)
    if (e.key === 'ArrowDown') setDir(0, 1)
    if (e.key === 'p' || e.key === 'P') {
      if (!over) {
        paused = !paused
        emit()
        draw()
      }
    }
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
      if (Date.now() - st > 500) return
      if (Math.abs(dx) < 20 && Math.abs(dy) < 20) return
      if (Math.abs(dx) > Math.abs(dy)) setDir(dx > 0 ? 1 : -1, 0)
      else setDir(0, dy > 0 ? 1 : -1)
    },
    { passive: true }
  )

  window.addEventListener('resize', function () {
    if (!over) {
      sizeCanvas()
      draw()
    }
  })

  init()
})()
