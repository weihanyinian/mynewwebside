const SUITS = ["spade", "heart", "club", "diamond"]
const RANK_LABEL = {
  3: "3",
  4: "4",
  5: "5",
  6: "6",
  7: "7",
  8: "8",
  9: "9",
  10: "10",
  11: "J",
  12: "Q",
  13: "K",
  14: "A",
  15: "2",
  16: "SJ",
  17: "BJ",
}

export const PLAY_TYPES = {
  SINGLE: "single",
  PAIR: "pair",
  TRIPLE: "triple",
  TRIPLE_ONE: "triple_one",
  TRIPLE_PAIR: "triple_pair",
  STRAIGHT: "straight",
  PAIR_STRAIGHT: "pair_straight",
  AIRPLANE: "airplane",
  AIRPLANE_SINGLE: "airplane_single",
  AIRPLANE_PAIR: "airplane_pair",
  BOMB: "bomb",
  ROCKET: "rocket",
}

export function createDeck() {
  const deck = []
  let id = 1
  for (let rank = 3; rank <= 15; rank++) {
    for (const suit of SUITS) {
      deck.push({
        id: `c-${id++}`,
        rank,
        suit,
        label: `${RANK_LABEL[rank]}${suitSymbol(suit)}`,
      })
    }
  }
  deck.push({ id: `j-${id++}`, rank: 16, suit: "joker", joker: "small", label: "小王" })
  deck.push({ id: `j-${id++}`, rank: 17, suit: "joker", joker: "big", label: "大王" })
  return deck
}

export function shuffle(deck) {
  const arr = deck.slice()
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

export function sortCards(cards) {
  return cards.slice().sort((a, b) => b.rank - a.rank || suitWeight(b.suit) - suitWeight(a.suit))
}

function suitWeight(suit) {
  if (suit === "joker") return 9
  if (suit === "spade") return 4
  if (suit === "heart") return 3
  if (suit === "club") return 2
  return 1
}

function suitSymbol(suit) {
  if (suit === "spade") return "♠"
  if (suit === "heart") return "♥"
  if (suit === "club") return "♣"
  return "♦"
}

function countByRank(cards) {
  const map = new Map()
  for (const c of cards) {
    map.set(c.rank, (map.get(c.rank) || 0) + 1)
  }
  return map
}

function cardsByRank(cards) {
  const map = new Map()
  for (const c of sortCards(cards)) {
    if (!map.has(c.rank)) map.set(c.rank, [])
    map.get(c.rank).push(c)
  }
  return map
}

function isConsecutive(ranks) {
  for (let i = 1; i < ranks.length; i++) {
    if (ranks[i] !== ranks[i - 1] + 1) return false
  }
  return true
}

function getConsecutiveSegments(ranks, minLen) {
  const out = []
  let i = 0
  while (i < ranks.length) {
    let j = i
    while (j + 1 < ranks.length && ranks[j + 1] === ranks[j] + 1) j++
    const len = j - i + 1
    if (len >= minLen) {
      for (let start = i; start <= j; start++) {
        for (let end = start + minLen - 1; end <= j; end++) {
          out.push(ranks.slice(start, end + 1))
        }
      }
    }
    i = j + 1
  }
  return out
}

/**
 * 牌型判断（核心）
 * 参考思路：
 * - 先统计各点数出现次数
 * - 对固定长度牌型直接匹配
 * - 顺子/连对/飞机使用“连续核心 + 附件”模型验证
 */
export function analyzePlay(cards) {
  if (!cards || cards.length === 0) return null
  const sorted = sortCards(cards)
  const len = sorted.length
  const rankMap = countByRank(sorted)
  const uniqueRanks = Array.from(rankMap.keys()).sort((a, b) => a - b)
  const counts = Array.from(rankMap.values()).sort((a, b) => b - a)

  if (len === 2 && rankMap.has(16) && rankMap.has(17)) {
    return { type: PLAY_TYPES.ROCKET, mainRank: 17, length: 2, cards: sorted }
  }
  if (len === 4 && counts[0] === 4) {
    return { type: PLAY_TYPES.BOMB, mainRank: uniqueRanks[0], length: 4, cards: sorted }
  }
  if (len === 1) return { type: PLAY_TYPES.SINGLE, mainRank: sorted[0].rank, length: 1, cards: sorted }
  if (len === 2 && counts[0] === 2) return { type: PLAY_TYPES.PAIR, mainRank: uniqueRanks[0], length: 2, cards: sorted }
  if (len === 3 && counts[0] === 3) return { type: PLAY_TYPES.TRIPLE, mainRank: uniqueRanks[0], length: 3, cards: sorted }

  if (len === 4 && counts[0] === 3) {
    const tripleRank = uniqueRanks.find((r) => rankMap.get(r) === 3)
    return { type: PLAY_TYPES.TRIPLE_ONE, mainRank: tripleRank, length: 4, cards: sorted }
  }
  if (len === 5 && counts[0] === 3 && counts[1] === 2) {
    const tripleRank = uniqueRanks.find((r) => rankMap.get(r) === 3)
    return { type: PLAY_TYPES.TRIPLE_PAIR, mainRank: tripleRank, length: 5, cards: sorted }
  }

  if (len >= 5 && counts[0] === 1 && !uniqueRanks.some((r) => r >= 15) && isConsecutive(uniqueRanks)) {
    return { type: PLAY_TYPES.STRAIGHT, mainRank: uniqueRanks[uniqueRanks.length - 1], length: len, cards: sorted }
  }

  if (len >= 6 && len % 2 === 0 && counts[0] === 2 && !uniqueRanks.some((r) => r >= 15) && isConsecutive(uniqueRanks)) {
    return { type: PLAY_TYPES.PAIR_STRAIGHT, mainRank: uniqueRanks[uniqueRanks.length - 1], length: len, cards: sorted }
  }

  const airplane = analyzeAirplane(rankMap, len, sorted)
  if (airplane) return airplane
  return null
}

function analyzeAirplane(rankMap, totalLen, sorted) {
  const tripleRanks = Array.from(rankMap.entries())
    .filter(([rank, cnt]) => cnt >= 3 && rank < 15)
    .map(([rank]) => rank)
    .sort((a, b) => a - b)
  if (tripleRanks.length < 2) return null

  const segments = getConsecutiveSegments(tripleRanks, 2)
  for (const seg of segments) {
    const n = seg.length
    const copy = new Map(rankMap)
    for (const r of seg) copy.set(r, copy.get(r) - 3)

    const remain = []
    for (const [rank, cnt] of copy.entries()) {
      for (let i = 0; i < cnt; i++) remain.push(rank)
    }

    if (totalLen === n * 3 && remain.length === 0) {
      return { type: PLAY_TYPES.AIRPLANE, mainRank: seg[seg.length - 1], length: totalLen, chainLength: n, cards: sorted }
    }

    if (totalLen === n * 4 && remain.length === n) {
      return { type: PLAY_TYPES.AIRPLANE_SINGLE, mainRank: seg[seg.length - 1], length: totalLen, chainLength: n, cards: sorted }
    }

    if (totalLen === n * 5 && remain.length === n * 2) {
      const remainMap = new Map()
      for (const r of remain) remainMap.set(r, (remainMap.get(r) || 0) + 1)
      if (Array.from(remainMap.values()).every((v) => v === 2) && remainMap.size === n) {
        return { type: PLAY_TYPES.AIRPLANE_PAIR, mainRank: seg[seg.length - 1], length: totalLen, chainLength: n, cards: sorted }
      }
    }
  }
  return null
}

export function canBeatPlay(nextPlay, prevPlay) {
  if (!nextPlay) return false
  if (!prevPlay) return true
  if (nextPlay.type === PLAY_TYPES.ROCKET) return true
  if (prevPlay.type === PLAY_TYPES.ROCKET) return false
  if (nextPlay.type === PLAY_TYPES.BOMB && prevPlay.type !== PLAY_TYPES.BOMB) return true
  if (nextPlay.type === PLAY_TYPES.BOMB && prevPlay.type === PLAY_TYPES.BOMB) {
    return nextPlay.mainRank > prevPlay.mainRank
  }
  if (nextPlay.type !== prevPlay.type) return false
  if (nextPlay.length !== prevPlay.length) return false
  return nextPlay.mainRank > prevPlay.mainRank
}

function playKey(play) {
  const ids = sortCards(play.cards).map((c) => c.id).join(",")
  return `${play.type}:${play.mainRank}:${play.length}:${ids}`
}

function buildPlayIfValid(cards) {
  const p = analyzePlay(cards)
  return p ? p : null
}

function pickCards(rankMap, rank, count) {
  const list = rankMap.get(rank) || []
  return list.slice(0, count)
}

/**
 * 枚举可出牌（用于提示和 AI）
 * 为了效率，附件组合使用“最小附件优先”策略，避免组合爆炸。
 */
export function getAllPlayableOptions(handCards) {
  const sorted = sortCards(handCards)
  const byRank = cardsByRank(sorted)
  const rankCnt = countByRank(sorted)
  const ranksAsc = Array.from(byRank.keys()).sort((a, b) => a - b)
  const options = []
  const seen = new Set()

  const pushPlay = (cards) => {
    const p = buildPlayIfValid(cards)
    if (!p) return
    const key = playKey(p)
    if (seen.has(key)) return
    seen.add(key)
    options.push(p)
  }

  for (const c of sorted) pushPlay([c])
  for (const r of ranksAsc) {
    if ((rankCnt.get(r) || 0) >= 2) pushPlay(pickCards(byRank, r, 2))
    if ((rankCnt.get(r) || 0) >= 3) pushPlay(pickCards(byRank, r, 3))
    if ((rankCnt.get(r) || 0) >= 4) pushPlay(pickCards(byRank, r, 4))
  }
  if (byRank.has(16) && byRank.has(17)) pushPlay([byRank.get(16)[0], byRank.get(17)[0]])

  for (const r of ranksAsc) {
    if ((rankCnt.get(r) || 0) < 3) continue
    const triple = pickCards(byRank, r, 3)
    const remainSingles = sorted.filter((c) => c.rank !== r)
    if (remainSingles.length > 0) pushPlay([...triple, remainSingles[remainSingles.length - 1]])
    const remainPairs = ranksAsc.filter((x) => x !== r && (rankCnt.get(x) || 0) >= 2)
    if (remainPairs.length > 0) {
      const pr = remainPairs[0]
      pushPlay([...triple, ...pickCards(byRank, pr, 2)])
    }
  }

  const straightRanks = ranksAsc.filter((r) => r < 15 && (rankCnt.get(r) || 0) >= 1)
  const straightSegments = getConsecutiveSegments(straightRanks, 5)
  for (const seg of straightSegments) {
    pushPlay(seg.map((r) => pickCards(byRank, r, 1)[0]))
  }

  const pairStraightRanks = ranksAsc.filter((r) => r < 15 && (rankCnt.get(r) || 0) >= 2)
  const pairSegments = getConsecutiveSegments(pairStraightRanks, 3)
  for (const seg of pairSegments) {
    const cards = []
    for (const r of seg) cards.push(...pickCards(byRank, r, 2))
    pushPlay(cards)
  }

  const tripleChainRanks = ranksAsc.filter((r) => r < 15 && (rankCnt.get(r) || 0) >= 3)
  const tripleSegments = getConsecutiveSegments(tripleChainRanks, 2)
  for (const seg of tripleSegments) {
    const core = []
    for (const r of seg) core.push(...pickCards(byRank, r, 3))
    pushPlay(core)

    const coreSet = new Set(seg)
    const remainAll = sortCards(sorted.filter((c) => !coreSet.has(c.rank) || (rankCnt.get(c.rank) || 0) > 3))
    if (remainAll.length >= seg.length) {
      pushPlay([...core, ...remainAll.slice(remainAll.length - seg.length)])
    }

    const pairRanks = ranksAsc.filter((r) => !coreSet.has(r) && (rankCnt.get(r) || 0) >= 2)
    if (pairRanks.length >= seg.length) {
      const attach = []
      for (let i = 0; i < seg.length; i++) attach.push(...pickCards(byRank, pairRanks[i], 2))
      pushPlay([...core, ...attach])
    }
  }

  return options
}

function playSortWeight(play) {
  const order = {
    [PLAY_TYPES.SINGLE]: 1,
    [PLAY_TYPES.PAIR]: 2,
    [PLAY_TYPES.TRIPLE]: 3,
    [PLAY_TYPES.TRIPLE_ONE]: 4,
    [PLAY_TYPES.TRIPLE_PAIR]: 5,
    [PLAY_TYPES.STRAIGHT]: 6,
    [PLAY_TYPES.PAIR_STRAIGHT]: 7,
    [PLAY_TYPES.AIRPLANE]: 8,
    [PLAY_TYPES.AIRPLANE_SINGLE]: 9,
    [PLAY_TYPES.AIRPLANE_PAIR]: 10,
    [PLAY_TYPES.BOMB]: 11,
    [PLAY_TYPES.ROCKET]: 12,
  }
  return order[play.type] * 1000 + play.mainRank * 10 + play.length
}

export function findHint(handCards, prevPlay) {
  const all = getAllPlayableOptions(handCards)
    .filter((p) => canBeatPlay(p, prevPlay))
    .sort((a, b) => playSortWeight(a) - playSortWeight(b))
  return all.length ? all[0].cards : []
}

export function chooseOpeningPlay(handCards) {
  const all = getAllPlayableOptions(handCards).sort((a, b) => playSortWeight(a) - playSortWeight(b))
  const nonBomb = all.filter((p) => p.type !== PLAY_TYPES.BOMB && p.type !== PLAY_TYPES.ROCKET)
  return (nonBomb[0] || all[0] || null)?.cards || []
}

/**
 * AI 叫地主评分（ratel 风格简化版）
 * - 王和2权重最高
 * - 炸弹/三张给额外奖励
 */
export function evaluateLandlordScore(cards) {
  const cnt = countByRank(cards)
  let score = 0
  score += (cnt.get(17) || 0) * 8
  score += (cnt.get(16) || 0) * 6
  score += (cnt.get(15) || 0) * 4
  for (const [rank, n] of cnt.entries()) {
    if (n === 4) score += 10
    else if (n === 3) score += 3
    else if (n === 2 && rank >= 13) score += 1
  }
  return score
}

export function aiBidDecision(cards) {
  const s = evaluateLandlordScore(cards)
  return s >= 16
}

export function formatCardText(card) {
  return card.label || RANK_LABEL[card.rank] || String(card.rank)
}

