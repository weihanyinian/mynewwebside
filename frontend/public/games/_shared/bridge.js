/**
 * 摸鱼中心 iframe 与父页通信、与站点统一的 localStorage 键（moyu-hi-<id>）
 */
;(function () {
  var q = new URLSearchParams(location.search || '')
  var gid = q.get('id') || 'game'
  window.MOYU_GAME_ID = gid
  var theme = q.get('theme') || 'light'
  window.MOYU_THEME = theme

  document.documentElement.setAttribute('data-moyu-theme', theme)
  if (theme === 'dark') document.documentElement.classList.add('moyu-dark')
  else document.documentElement.classList.remove('moyu-dark')

  function hiKey() {
    return 'moyu-hi-' + gid
  }
  function getHi() {
    var v = localStorage.getItem(hiKey())
    return v ? parseInt(v, 10) || 0 : 0
  }
  function setHi(score) {
    var o = getHi()
    if (score > o) localStorage.setItem(hiKey(), String(score))
    return Math.max(o, score)
  }
  function post(msg) {
    try {
      parent.postMessage(
        Object.assign({ __moyu: true, gameId: MOYU_GAME_ID }, msg),
        '*'
      )
    } catch (e) {}
  }

  window.MOYU = {
    id: gid,
    getHi: getHi,
    setHi: setHi,
    post: post,
    /** @param {string} text 顶栏展示的统计文案 */
    stats: function (text) {
      post({ type: 'stats', text: text })
    },
    gameover: function (extra) {
      post(Object.assign({ type: 'gameover' }, extra || {}))
    },
    /** 父页切换主题时 postMessage 调用 */
    setTheme: function (t) {
      window.MOYU_THEME = t
      document.documentElement.setAttribute('data-moyu-theme', t)
      document.documentElement.classList.toggle('moyu-dark', t === 'dark')
    }
  }

  window.addEventListener('message', function (ev) {
    var d = ev.data
    if (!d || typeof d !== 'object') return
    if (d.action === 'theme' && d.theme) window.MOYU.setTheme(d.theme)
  })
})()
