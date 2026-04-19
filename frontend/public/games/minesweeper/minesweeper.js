;(function () {
  var R, C, M, grid, started, dead, win, flags, t0
  var gridEl = document.getElementById('grid')
  var line2 = document.getElementById('line2')

  var DIFF = {
    easy: { r: 9, c: 9, m: 10 },
    mid: { r: 12, c: 12, m: 22 },
    hard: { r: 16, c: 16, m: 40 },
  }
  var mode = 'mid'

  function runPts() {
    var s = sec()
    return Math.max(0, 5000 - s * 50 + M * 18)
  }

  function sec() {
    if (!started || !t0) return 0
    return ((Date.now() - t0) / 1000) | 0
  }

  function fmtStats() {
    var hi = MOYU.getHi()
    if (win) {
      var sc = runPts()
      MOYU.setHi(sc)
      hi = MOYU.getHi()
      MOYU.stats('最高分 ' + hi + ' · 本局 ' + sc + ' 分')
    } else MOYU.stats('最高分 ' + hi + (started && !dead ? ' · 用时 ' + sec() + 's' : ''))
  }

  function empty() {
    return Array.from({ length: R }, function () {
      return Array.from({ length: C }, function () {
        return { mine: false, open: false, flag: false, n: 0 }
      })
    })
  }

  function countNeighbors(gr, y, x) {
    var n = 0
    for (var dy = -1; dy <= 1; dy++) {
      for (var dx = -1; dx <= 1; dx++) {
        if (!dy && !dx) continue
        var ny = y + dy,
          nx = x + dx
        if (ny >= 0 && ny < R && nx >= 0 && nx < C && gr[ny][nx].mine) n++
      }
    }
    return n
  }

  function placeMines(sy, sx) {
    var gr = empty()
    var left = M
    while (left > 0) {
      var y = (Math.random() * R) | 0
      var x = (Math.random() * C) | 0
      if (gr[y][x].mine) continue
      if (Math.abs(y - sy) <= 1 && Math.abs(x - sx) <= 1) continue
      gr[y][x].mine = true
      left--
    }
    for (y = 0; y < R; y++) for (x = 0; x < C; x++) if (!gr[y][x].mine) gr[y][x].n = countNeighbors(gr, y, x)
    return gr
  }

  function flood(gr, y, x) {
    var st = [[y, x]]
    while (st.length) {
      var p = st.pop()
      var cy = p[0],
        cx = p[1]
      var cell = gr[cy][cx]
      if (cell.open || cell.flag) continue
      cell.open = true
      if (cell.n === 0) {
        for (var dy = -1; dy <= 1; dy++) {
          for (var dx = -1; dx <= 1; dx++) {
            if (!dy && !dx) continue
            var ny = cy + dy,
              nx = cx + dx
            if (ny >= 0 && ny < R && nx >= 0 && nx < C) st.push([ny, nx])
          }
        }
      }
    }
  }

  function checkWin(gr) {
    for (var y = 0; y < R; y++)
      for (var x = 0; x < C; x++) {
        var c = gr[y][x]
        if (!c.mine && !c.open) return false
      }
    return true
  }

  function setCellCss() {
    var w = Math.min(window.innerWidth - 36, 560)
    var cell = Math.max(22, Math.min(38, (w / C - 6) | 0))
    gridEl.style.setProperty('--cell', cell + 'px')
  }

  function openCell(yy, xx) {
    if (dead || win) return
    var cell = grid[yy][xx]
    if (cell.flag) return
    if (!started) {
      grid = placeMines(yy, xx)
      started = true
      t0 = Date.now()
      cell = grid[yy][xx]
    }
    if (cell.open) return
    if (cell.mine) {
      dead = true
      for (var iy = 0; iy < R; iy++)
        for (var ix = 0; ix < C; ix++) if (grid[iy][ix].mine) grid[iy][ix].open = true
      MOYU.gameover({})
      fmtStats()
      render()
      return
    }
    flood(grid, yy, xx)
    if (checkWin(grid)) {
      win = true
      MOYU.setHi(runPts())
      MOYU.gameover({ win: true })
    }
    fmtStats()
    render()
  }

  function flipFlag(yy, xx) {
    if (dead || win) return
    var cell = grid[yy][xx]
    if (cell.open) return
    cell.flag = !cell.flag
    flags += cell.flag ? 1 : -1
    fmtStats()
    render()
  }

  function render() {
    setCellCss()
    gridEl.innerHTML = ''
    for (var y = 0; y < R; y++) {
      var row = document.createElement('div')
      row.className = 'row'
      for (var x = 0; x < C; x++) {
        ;(function (yy, xx) {
          var cell = grid[yy][xx]
          var btn = document.createElement('button')
          btn.type = 'button'
          btn.className = 'cell'
          if (cell.flag) btn.classList.add('flag')
          if (cell.open) {
            btn.classList.add('open')
            if (cell.mine) {
              btn.classList.add('mine')
              btn.textContent = '●'
            } else if (cell.n) {
              btn.classList.add('n' + cell.n)
              btn.textContent = String(cell.n)
            }
          }
          var holdT
          var longFired = false
          btn.oncontextmenu = function (e) {
            e.preventDefault()
            flipFlag(yy, xx)
          }
          btn.addEventListener('touchstart', function () {
            longFired = false
            holdT = setTimeout(function () {
              holdT = null
              longFired = true
              flipFlag(yy, xx)
            }, 480)
          })
          btn.addEventListener('touchmove', function () {
            clearTimeout(holdT)
          })
          btn.addEventListener('touchend', function (e) {
            clearTimeout(holdT)
            holdT = null
            if (longFired) {
              longFired = false
              return
            }
            e.preventDefault()
            openCell(yy, xx)
          })
          btn.addEventListener(
            'click',
            function (e) {
              if ('ontouchstart' in window) return
              openCell(yy, xx)
            },
            false
          )
          row.appendChild(btn)
        })(y, x)
      }
      gridEl.appendChild(row)
    }
    line2.textContent =
      '雷 ' + M + ' · 标旗 ' + flags + ' · 待排 ' + (M - flags) + ' · 用时 ' + sec() + 's'
  }

  function init() {
    var d = DIFF[mode]
    R = d.r
    C = d.c
    M = d.m
    grid = empty()
    started = false
    dead = false
    win = false
    flags = 0
    t0 = 0
    MOYU.post({ type: 'playing' })
    fmtStats()
    render()
  }

  document.querySelectorAll('[data-diff]').forEach(function (btn) {
    btn.onclick = function () {
      mode = btn.getAttribute('data-diff')
      init()
    }
  })
  document.getElementById('restart').onclick = init

  window.addEventListener('resize', render)
  init()
})()
