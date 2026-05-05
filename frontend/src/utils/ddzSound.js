/**
 * 斗地主音效 — 基于 Web Audio API 的程序化合成音效
 * 参考 QQ 欢乐斗地主音效风格
 */

let ctx = null

function getCtx() {
  if (!ctx) {
    ctx = new (window.AudioContext || window.webkitAudioContext)()
  }
  return ctx
}

function playTone(freq, duration, type = 'sine', gain = 0.12) {
  const c = getCtx()
  const osc = c.createOscillator()
  const g = c.createGain()
  osc.type = type
  osc.frequency.value = freq
  g.gain.setValueAtTime(gain, c.currentTime)
  g.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration)
  osc.connect(g)
  g.connect(c.destination)
  osc.start(c.currentTime)
  osc.stop(c.currentTime + duration)
}

function noiseBurst(duration, gain = 0.06) {
  const c = getCtx()
  const bufferSize = c.sampleRate * duration
  const buffer = c.createBuffer(1, bufferSize, c.sampleRate)
  const data = buffer.getChannelData(0)
  for (let i = 0; i < bufferSize; i++) {
    data[i] = (Math.random() * 2 - 1) * Math.pow(1 - i / bufferSize, 2)
  }
  const src = c.createBufferSource()
  src.buffer = buffer
  const g = c.createGain()
  g.gain.setValueAtTime(gain, c.currentTime)
  g.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration)
  src.connect(g)
  g.connect(c.destination)
  src.start(c.currentTime)
}

export function playCardSound(cardType, cardCount) {
  try {
    getCtx().resume()
  } catch { return }

  switch (cardType) {
    case 'ROCKET':
      playTone(880, 0.08, 'sawtooth', 0.08)
      setTimeout(() => playTone(1200, 0.12, 'sawtooth', 0.1), 60)
      setTimeout(() => playTone(1760, 0.2, 'triangle', 0.07), 120)
      break
    case 'BOMB':
      playTone(220, 0.3, 'sine', 0.15)
      playTone(260, 0.28, 'sawtooth', 0.08)
      noiseBurst(0.15, 0.06)
      setTimeout(() => playTone(330, 0.2, 'square', 0.06), 80)
      break
    case 'AIRPLANE':
    case 'AIRPLANE_SINGLE':
    case 'AIRPLANE_PAIR':
      playTone(440, 0.12, 'triangle', 0.1)
      setTimeout(() => playTone(523, 0.1, 'triangle', 0.1), 80)
      setTimeout(() => playTone(659, 0.1, 'triangle', 0.1), 160)
      setTimeout(() => playTone(784, 0.14, 'triangle', 0.1), 240)
      break
    case 'STRAIGHT':
    case 'PAIR_STRAIGHT':
      playTone(392, 0.1, 'triangle', 0.09)
      setTimeout(() => playTone(440, 0.1, 'triangle', 0.09), 70)
      setTimeout(() => playTone(494, 0.1, 'triangle', 0.09), 140)
      break
    case 'TRIPLE':
    case 'TRIPLE_ONE':
    case 'TRIPLE_PAIR':
      playTone(523, 0.1, 'triangle', 0.1)
      setTimeout(() => playTone(659, 0.08, 'triangle', 0.08), 70)
      break
    case 'PAIR':
      playTone(660, 0.08, 'sine', 0.1)
      setTimeout(() => playTone(784, 0.06, 'sine', 0.08), 50)
      break
    case 'SINGLE':
    default:
      playTone(880, 0.06, 'sine', 0.1)
      break
  }
}

export function playBidSound(call) {
  try {
    getCtx().resume()
  } catch { return }

  if (call) {
    playTone(523, 0.12, 'square', 0.08)
    setTimeout(() => playTone(659, 0.12, 'square', 0.08), 100)
    setTimeout(() => playTone(784, 0.16, 'square', 0.08), 200)
  } else {
    playTone(200, 0.2, 'sine', 0.06)
  }
}

export function playPassSound() {
  try {
    getCtx().resume()
  } catch { return }
  playTone(180, 0.15, 'sine', 0.05)
}

export function playWinSound(landlordWin) {
  try {
    getCtx().resume()
  } catch { return }

  const notes = landlordWin
    ? [523, 587, 659, 698, 784, 880, 988, 1047]
    : [440, 494, 523, 587, 659, 698, 784, 880]

  notes.forEach((freq, i) => {
    setTimeout(() => playTone(freq, 0.2, 'triangle', 0.1), i * 120)
  })
}

export function playDealSound() {
  try {
    getCtx().resume()
  } catch { return }

  for (let i = 0; i < 8; i++) {
    const delay = i * 60 + Math.random() * 30
    setTimeout(() => {
      noiseBurst(0.03, 0.03)
      playTone(600 + Math.random() * 400, 0.02, 'sine', 0.03)
    }, delay)
  }
}

export function playHintSound() {
  try {
    getCtx().resume()
  } catch { return }

  playTone(988, 0.06, 'sine', 0.08)
  setTimeout(() => playTone(1175, 0.08, 'sine', 0.08), 60)
}
