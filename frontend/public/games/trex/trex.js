;(function () {
  var W = 320
  var H = 160
  var GROUND = 120
  var canvas = document.getElementById('cv')
  var ctx = canvas.getContext('2d')

  var vy,
    y,
    obs,
    speed,
    playing,
    gameOver,
    raf,
    last,
    score
  var maxSpeed = 9

  function resize() {
    var nw = Math.min(560, window.innerWidth - 24)
    W = nw
    canvas.width = W
    canvas.height = H
    GROUND = Math.min(120, H - 36)
  }

  function emit() {
    var hi = MOYU.setHi(score)
    MOYU.stats('最高分 ' + hi + ' · 距离 ' + score)
  }

  function resetRun() {
    vy = 0
    y = GROUND - 40
    obs = []
    speed = 4.2
    score = 0
    gameOver = false
    playing = true
    last = performance.now()
    MOYU.post({ type: 'playing' })
  }

  function init() {
    cancelAnimationFrame(raf)
    resize()
    MOYU.getHi()
    resetRun()
    emit()
    last = performance.now()
    raf = requestAnimationFrame(loop)
  }

  function jump() {
    if (!playing) {
      gameOver = false
      resetRun()
      return
    }
    if (y >= GROUND - 40 - 0.5) vy = -10.5
  }

  function spawnObs() {
    if (!obs.length || obs[obs.length - 1].x < W - 180 - Math.random() * 100) {
      var h = 22 + ((Math.random() * 18) | 0)
      obs.push({ x: W + 20, w: 16, h: h })
    }
  }

  function hit() {
    var px = 70
    var pw = 34
    var ph = 36
    var top = y
    for (var i = 0; i < obs.length; i++) {
      var o = obs[i]
      if (o.x < px + pw && o.x + o.w > px && top + ph > GROUND - o.h) return true
    }
    return false
  }

  function dark() {
    return document.documentElement.classList.contains('moyu-dark')
  }

  function loop(t) {
    raf = requestAnimationFrame(loop)
    var dt = Math.min(40, t - last)
    last = t
    if (!playing) {
      draw()
      return
    }
    score += (speed * (dt / 16)) | 0
    if ((score / 420) | 0 > ((score - (speed * (dt / 16))) / 420) | 0) speed = Math.min(maxSpeed, speed + 0.12)
    vy += 0.52 * (dt / 16)
    y += vy * (dt / 16)
    if (y > GROUND - 40) {
      y = GROUND - 40
      vy = 0
    }
    spawnObs()
    for (var j = 0; j < obs.length; j++) obs[j].x -= speed * (dt / 16)
    obs = obs.filter(function (o) {
      return o.x > -40
    })
    if (hit()) {
      playing = false
      gameOver = true
      MOYU.setHi(score)
      MOYU.gameover({ score: score })
      emit()
    }
    draw()
  }

  function draw() {
    ctx.fillStyle = dark() ? '#020617' : '#e2e8f0'
    ctx.fillRect(0, 0, W, H)
    ctx.fillStyle = dark() ? '#334155' : '#cbd5e1'
    ctx.fillRect(0, GROUND, W, H - GROUND)
    ctx.fillStyle = '#38bdf8'
    ctx.fillRect(70, y, 34, 36)
    ctx.fillStyle = dark() ? '#64748b' : '#475569'
    for (var i = 0; i < obs.length; i++) {
      var o = obs[i]
      ctx.fillRect(o.x, GROUND - o.h, o.w, o.h)
    }
    ctx.fillStyle = dark() ? '#e2e8f0' : '#0f172a'
    ctx.font = 'bold 14px system-ui'
    ctx.fillText(String(score), 12, 22)
    if (gameOver) {
      ctx.fillStyle = 'rgba(0,0,0,0.5)'
      ctx.fillRect(0, 0, W, H)
    } else if (!playing) {
      ctx.fillStyle = dark() ? '#e2e8f0' : '#0f172a'
      ctx.font = '14px system-ui'
      ctx.textAlign = 'center'
      ctx.fillText('点跳跃开始', W / 2, H / 2)
      ctx.textAlign = 'left'
    }
  }

  canvas.addEventListener('click', jump)
  document.getElementById('jump').onclick = jump
  window.addEventListener('keydown', function (e) {
    if (e.code === 'Space') {
      jump()
      e.preventDefault()
    }
  })
  window.addEventListener('resize', function () {
    resize()
  })
  playing = false
  gameOver = false
  score = 0
  resize()
  last = performance.now()
  raf = requestAnimationFrame(loop)
  emit()
})()
