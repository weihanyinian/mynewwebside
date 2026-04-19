;(function () {
  var W = 360
  var H = 480
  var canvas = document.getElementById('cv')
  var ctx = canvas.getContext('2d')
  var birdY, vy, pipes, running, over, raf, last, score

  function size() {
    var nw = Math.min(420, window.innerWidth - 20)
    var nh = Math.min(560, (window.innerHeight * 0.62) | 0)
    W = nw
    H = Math.max(280, nh)
    canvas.width = W
    canvas.height = H
  }

  function emit() {
    var hi = MOYU.setHi(score)
    MOYU.stats('最高分 ' + hi + ' · 管道 ' + score)
  }

  function reset() {
    birdY = H / 2
    vy = 0
    pipes = []
    score = 0
    running = false
    over = false
    MOYU.getHi()
  }

  function start() {
    if (running && !over) return
    if (over) reset()
    running = true
    over = false
    vy = -4.5
    last = performance.now()
    MOYU.post({ type: 'playing' })
  }

  function flap() {
    if (!running) {
      start()
      return
    }
    vy = -6.8
  }

  function spawnPipe() {
    var lastX = pipes.length ? pipes[pipes.length - 1].x : W
    if (lastX < W - 150) {
      var gapY = 110 + Math.random() * (H - 240)
      pipes.push({ x: W + 20, gapY: gapY, passed: false })
    }
  }

  function loop(t) {
    raf = requestAnimationFrame(loop)
    var dt = Math.min(32, t - last)
    last = t
    if (!running || over) {
      draw()
      return
    }
    vy += 0.35 * (dt / 16)
    birdY += vy * (dt / 16)
    spawnPipe()
    for (var i = 0; i < pipes.length; i++) {
      var p = pipes[i]
      p.x -= 2.8 * (dt / 16)
      if (!p.passed && p.x + 40 < 72) {
        p.passed = true
        score++
        MOYU.setHi(score)
        emit()
      }
    }
    pipes = pipes.filter(function (p) {
      return p.x > -60
    })
    var bx = 64
    var by = birdY
    var br = Math.min(15, W * 0.038)
    var gap = Math.min(100, H * 0.18)
    if (by - br < 0 || by + br > H) end()
    for (var j = 0; j < pipes.length; j++) {
      var p2 = pipes[j]
      var topH = p2.gapY - gap / 2
      var botT = p2.gapY + gap / 2
      if (bx + br > p2.x && bx - br < p2.x + 48) {
        if (by - br < topH || by + br > botT) end()
      }
    }
    draw()
  }

  function end() {
    over = true
    running = false
    MOYU.setHi(score)
    MOYU.gameover({ score: score })
    emit()
  }

  function dark() {
    return document.documentElement.classList.contains('moyu-dark')
  }

  function draw() {
    var d = dark()
    var g1 = d ? '#0c4a6e' : '#38bdf8'
    var g2 = d ? '#020617' : '#bae6fd'
    var grd = ctx.createLinearGradient(0, 0, 0, H)
    grd.addColorStop(0, g1)
    grd.addColorStop(1, g2)
    ctx.fillStyle = grd
    ctx.fillRect(0, 0, W, H)
    ctx.fillStyle = d ? '#14532d' : '#86efac'
    ctx.fillRect(0, H - 40, W, 40)
    var gap = Math.min(100, H * 0.18)
    for (var i = 0; i < pipes.length; i++) {
      var p = pipes[i]
      var topH = p.gapY - gap / 2
      var botT = p.gapY + gap / 2
      ctx.fillStyle = d ? '#16a34a' : '#15803d'
      ctx.fillRect(p.x, 0, 48, topH)
      ctx.fillRect(p.x, botT, 48, H - botT)
    }
    var br = Math.min(15, W * 0.038)
    ctx.fillStyle = d ? '#fbbf24' : '#eab308'
    ctx.beginPath()
    ctx.arc(64, birdY, br, 0, Math.PI * 2)
    ctx.fill()
    if (d) {
      ctx.strokeStyle = 'rgba(0,0,0,0.45)'
      ctx.lineWidth = 2
      ctx.stroke()
    }
    ctx.fillStyle = d ? '#fff' : '#0f172a'
    ctx.font = 'bold ' + Math.max(16, W * 0.055) + 'px system-ui'
    ctx.fillText(String(score), W / 2 - 6, 36)
    if (!running && !over) {
      ctx.fillStyle = 'rgba(0,0,0,0.4)'
      ctx.fillRect(0, H / 2 - 36, W, 72)
      ctx.fillStyle = '#fff'
      ctx.font = '14px system-ui'
      ctx.textAlign = 'center'
      ctx.fillText('点按开始', W / 2, H / 2)
      ctx.textAlign = 'left'
    }
    if (over) {
      ctx.fillStyle = 'rgba(0,0,0,0.55)'
      ctx.fillRect(0, 0, W, H)
    }
  }

  canvas.addEventListener('click', flap)
  window.addEventListener('keydown', function (e) {
    if (e.code === 'Space') {
      flap()
      e.preventDefault()
    }
  })
  size()
  reset()
  last = performance.now()
  raf = requestAnimationFrame(loop)
  emit()
})()
