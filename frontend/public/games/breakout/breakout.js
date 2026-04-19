;(function () {
  var W = 400,
    H = 300
  var canvas = document.getElementById('cv')
  var ctx = canvas.getContext('2d')
  var bricks,
    px,
    py,
    pw = 72,
    ph = 10,
    bx,
    by,
    bvx,
    bvy,
    br = 6,
    running,
    raf,
    last,
    score,
    lives

  function size() {
    var nw = Math.min(520, window.innerWidth - 24)
    W = nw
    H = (nw * 0.68) | 0
    canvas.width = W
    canvas.height = H
    buildBricks()
    if (!running) resetBall()
  }

  function emit() {
    var hi = MOYU.setHi(score)
    MOYU.stats('最高分 ' + hi + ' · 得分 ' + score + ' · 命 ' + lives)
  }

  function buildBricks() {
    bricks = []
    var cols = 10
    var bw = (W - 32) / cols
    var bh = Math.max(14, (H * 0.06) | 0)
    for (var r = 0; r < 5; r++) {
      for (var c = 0; c < cols; c++) {
        bricks.push({
          x: 16 + c * bw,
          y: 36 + r * (bh + 4),
          w: bw - 3,
          h: bh,
          alive: true,
          hue: 190 + r * 16,
        })
      }
    }
  }

  function resetBall() {
    px = W / 2 - pw / 2
    py = H - 28
    bx = W / 2
    by = H - 44
    bvx = 3 * (Math.random() > 0.5 ? 1 : -1)
    bvy = -3.2
  }

  function init() {
    size()
    score = 0
    lives = 3
    running = true
    resetBall()
    last = performance.now()
    MOYU.post({ type: 'playing' })
    emit()
  }

  function loseLife() {
    lives--
    if (lives <= 0) {
      running = false
      MOYU.setHi(score)
      MOYU.gameover({ score: score })
      emit()
    } else resetBall()
  }

  function dark() {
    return document.documentElement.classList.contains('moyu-dark')
  }

  function loop(t) {
    raf = requestAnimationFrame(loop)
    var dt = Math.min(32, t - last)
    last = t
    if (!running) {
      draw()
      return
    }
    bx += bvx * (dt / 16)
    by += bvy * (dt / 16)
    if (bx < br || bx > W - br) bvx *= -1
    if (by < br) bvy *= -1
    if (by > H + 16) loseLife()
    if (by > py - br && by < py + ph && bx > px && bx < px + pw) {
      bvy = -Math.abs(bvy)
      var hit = (bx - (px + pw / 2)) / (pw / 2)
      bvx += hit * 1.1
    }
    for (var i = 0; i < bricks.length; i++) {
      var b = bricks[i]
      if (!b.alive) continue
      if (bx + br > b.x && bx - br < b.x + b.w && by + br > b.y && by - br < b.y + b.h) {
        b.alive = false
        score += 10
        MOYU.setHi(score)
        var ox = Math.min(bx + br - b.x, b.x + b.w - (bx - br))
        var oy = Math.min(by + br - b.y, b.y + b.h - (by - br))
        if (ox < oy) bvx *= -1
        else bvy *= -1
        emit()
        break
      }
    }
    if (!bricks.some(function (b) { return b.alive })) {
      buildBricks()
      resetBall()
      score += 50
      MOYU.setHi(score)
      emit()
    }
    draw()
  }

  function draw() {
    ctx.fillStyle = dark() ? '#020617' : '#f1f5f9'
    ctx.fillRect(0, 0, W, H)
    for (var i = 0; i < bricks.length; i++) {
      var b = bricks[i]
      if (!b.alive) continue
      ctx.fillStyle = 'hsl(' + b.hue + ' 75% ' + (dark() ? 42 : 52) + '%)'
      ctx.fillRect(b.x, b.y, b.w, b.h)
    }
    ctx.fillStyle = '#38bdf8'
    ctx.fillRect(px, py, pw, ph)
    ctx.fillStyle = '#f472b6'
    ctx.beginPath()
    ctx.arc(bx, by, br, 0, Math.PI * 2)
    ctx.fill()
    ctx.fillStyle = dark() ? '#e2e8f0' : '#0f172a'
    ctx.font = '13px system-ui'
    ctx.fillText('得分 ' + score + ' · 生命 ' + lives, 10, 20)
    if (!running && lives <= 0) {
      ctx.fillStyle = 'rgba(0,0,0,0.5)'
      ctx.fillRect(0, 0, W, H)
    }
  }

  function moveToClientX(clientX) {
    if (!running) {
      if (lives <= 0) init()
      return
    }
    var r = canvas.getBoundingClientRect()
    var x = ((clientX - r.left) / r.width) * W
    px = Math.max(0, Math.min(W - pw, x - pw / 2))
  }

  canvas.addEventListener('mousemove', function (e) {
    moveToClientX(e.clientX)
  })
  canvas.addEventListener(
    'touchmove',
    function (e) {
      moveToClientX(e.touches[0].clientX)
    },
    { passive: true }
  )
  canvas.addEventListener('click', function () {
    if (!running && lives <= 0) init()
  })

  document.getElementById('l').onclick = function () {
    if (running) px = Math.max(0, px - 28)
  }
  document.getElementById('r').onclick = function () {
    if (running) px = Math.min(W - pw, px + 28)
  }

  init()
  raf = requestAnimationFrame(loop)
})()
