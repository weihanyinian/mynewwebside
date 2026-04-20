<script setup lang="ts">
import { ref, computed } from 'vue'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

// ─── MBTI 题库（60题，每题 4个维度选项）─────────────────────────────────────
// 维度：E/I, S/N, T/F, J/P
type Dimension = 'E' | 'I' | 'S' | 'N' | 'T' | 'F' | 'J' | 'P'

interface Question {
  id: number
  text: string
  a: { text: string; dim: Dimension }
  b: { text: string; dim: Dimension }
}

const questions: Question[] = [
  // E/I
  { id: 1,  text: '在一个派对结束后，你通常感觉…', a: { text: '精力充沛，意犹未尽', dim: 'E' }, b: { text: '筋疲力尽，需要独处充电', dim: 'I' } },
  { id: 2,  text: '你更倾向于…', a: { text: '主动发起聊天，认识新朋友', dim: 'E' }, b: { text: '等待别人来找你，深交少数人', dim: 'I' } },
  { id: 3,  text: '做决定时，你更希望…', a: { text: '和他人讨论，听取多方意见', dim: 'E' }, b: { text: '独自思考，梳理内心想法', dim: 'I' } },
  { id: 4,  text: '你的理想周末是…', a: { text: '出去参加活动，和朋友聚会', dim: 'E' }, b: { text: '在家看书、看剧或独自散步', dim: 'I' } },
  { id: 5,  text: '当你遇到问题时，你倾向于…', a: { text: '立刻找人倾诉讨论', dim: 'E' }, b: { text: '先自己默默思考消化', dim: 'I' } },
  { id: 6,  text: '在团队中，你通常…', a: { text: '是那个活跃发言、推动讨论的人', dim: 'E' }, b: { text: '观察全局，在合适时机表达意见', dim: 'I' } },
  { id: 7,  text: '陌生场合里，你…', a: { text: '很容易融入，快速交到朋友', dim: 'E' }, b: { text: '需要一段时间才能放松下来', dim: 'I' } },
  { id: 8,  text: '你更享受…', a: { text: '热闹的集体活动', dim: 'E' }, b: { text: '安静的个人时光', dim: 'I' } },
  // S/N
  { id: 9,  text: '学习新技能时，你倾向于…', a: { text: '按步骤操作，掌握具体方法', dim: 'S' }, b: { text: '先理解整体框架和背后原理', dim: 'N' } },
  { id: 10, text: '你更信任…', a: { text: '实际经验和已证明的事实', dim: 'S' }, b: { text: '直觉感受和未来的可能性', dim: 'N' } },
  { id: 11, text: '你更享受…', a: { text: '做踏实、有明确成果的工作', dim: 'S' }, b: { text: '探索新想法和脑洞大开的创意', dim: 'N' } },
  { id: 12, text: '你描述事物时更倾向于…', a: { text: '具体、精准，注重细节', dim: 'S' }, b: { text: '抽象、比喻，侧重整体印象', dim: 'N' } },
  { id: 13, text: '你对未来的态度是…', a: { text: '关注当下，脚踏实地', dim: 'S' }, b: { text: '喜欢畅想和规划未来图景', dim: 'N' } },
  { id: 14, text: '解决问题时，你更依赖…', a: { text: '过去类似情况的经验', dim: 'S' }, b: { text: '直觉和创造性思维', dim: 'N' } },
  { id: 15, text: '你注意到的更多是…', a: { text: '眼前的细节和现实情况', dim: 'S' }, b: { text: '事物之间的联系和潜在含义', dim: 'N' } },
  { id: 16, text: '阅读时，你更喜欢…', a: { text: '实用类书籍，学到具体技能', dim: 'S' }, b: { text: '充满隐喻和思想深度的作品', dim: 'N' } },
  // T/F
  { id: 17, text: '做重要决定时，你更看重…', a: { text: '客观逻辑和长期合理性', dim: 'T' }, b: { text: '对相关人员情感的影响', dim: 'F' } },
  { id: 18, text: '朋友向你倾诉烦恼，你通常…', a: { text: '帮他分析原因，提出解决方案', dim: 'T' }, b: { text: '先共情安慰，让他感到被理解', dim: 'F' } },
  { id: 19, text: '当别人的行为让你不满时，你倾向于…', a: { text: '直接指出问题，就事论事', dim: 'T' }, b: { text: '顾及对方感受，委婉表达', dim: 'F' } },
  { id: 20, text: '你认为批评他人时…', a: { text: '只要是事实和建设性意见，就应该直说', dim: 'T' }, b: { text: '要考虑时机和方式，减少伤害', dim: 'F' } },
  { id: 21, text: '你更容易被打动的是…', a: { text: '严密的逻辑论证', dim: 'T' }, b: { text: '真实感人的故事', dim: 'F' } },
  { id: 22, text: '在公平与和谐之间，你更优先…', a: { text: '公平，即便会让人不舒服', dim: 'T' }, b: { text: '和谐，避免不必要的冲突', dim: 'F' } },
  { id: 23, text: '别人说你更像…', a: { text: '理性冷静、思维清晰的人', dim: 'T' }, b: { text: '有温度、善解人意的人', dim: 'F' } },
  { id: 24, text: '面对不公平时，你…', a: { text: '会据理力争，追究责任', dim: 'T' }, b: { text: '会有强烈的情绪反应，担心影响关系', dim: 'F' } },
  // J/P
  { id: 25, text: '出行前，你倾向于…', a: { text: '提前规划好行程和住宿', dim: 'J' }, b: { text: '随性出发，走到哪算哪', dim: 'P' } },
  { id: 26, text: '你对截止日期的态度是…', a: { text: '早早完成，避免最后赶工', dim: 'J' }, b: { text: '临近截止才会开始全力冲刺', dim: 'P' } },
  { id: 27, text: '工作时你更喜欢…', a: { text: '按计划逐步推进，保持掌控感', dim: 'J' }, b: { text: '灵活调整，保留随机应变的空间', dim: 'P' } },
  { id: 28, text: '杂乱的环境会让你…', a: { text: '焦虑，必须整理后才能安心', dim: 'J' }, b: { text: '无所谓，甚至觉得有些活力', dim: 'P' } },
  { id: 29, text: '面对选择时，你希望…', a: { text: '尽快做决定，排除不确定性', dim: 'J' }, b: { text: '保留选项，收集更多信息再定', dim: 'P' } },
  { id: 30, text: '你的日程通常…', a: { text: '提前排好，每天有清晰的规划', dim: 'J' }, b: { text: '根据当天心情灵活安排', dim: 'P' } },
  { id: 31, text: '任务未完成时，你…', a: { text: '会一直挂心，无法完全放松', dim: 'J' }, b: { text: '能暂时放下，等有状态再继续', dim: 'P' } },
  { id: 32, text: '你觉得自己是…', a: { text: '有条不紊、喜欢秩序的人', dim: 'J' }, b: { text: '随性自由、喜欢弹性的人', dim: 'P' } },
]

// ─── MBTI 16 种人格描述 ──────────────────────────────────────────────────────
interface MbtiInfo {
  type: string
  title: string
  emoji: string
  desc: string
  strengths: string[]
  weaknesses: string[]
  careers: string[]
  famous: string[]
  color: string
}

const mbtiMap: Record<string, MbtiInfo> = {
  INTJ: { type:'INTJ', title:'战略家', emoji:'🏯', color:'#4f46e5',
    desc:'独立思考的战略家，拥有强烈的内驱力和对未来的清晰愿景。他们善于长期规划，执行力强，对低效和平庸零容忍。',
    strengths:['战略眼光','执行力强','思维独立','博学多才'], weaknesses:['过于完美主义','不擅情感表达','固执己见','社交耗能'],
    careers:['科学家','工程师','战略顾问','架构师'], famous:['伊隆·马斯克','尼采','史蒂芬·霍金'] },
  INTP: { type:'INTP', title:'逻辑学家', emoji:'🔭', color:'#7c3aed',
    desc:'痴迷于逻辑和理论的思想家，总是在寻找万物背后的运行规律。他们的脑子里随时运转着一台复杂的思维机器。',
    strengths:['逻辑严密','创意十足','求知欲强','客观理性'], weaknesses:['拖延行动','表达模糊','情绪钝感','对细节漫不经心'],
    careers:['程序员','数学家','哲学家','研究员'], famous:['爱因斯坦','比尔·盖茨','查尔斯·达尔文'] },
  ENTJ: { type:'ENTJ', title:'指挥官', emoji:'⚔️', color:'#dc2626',
    desc:'天生的领袖人物，充满魅力与自信，善于制定战略并带领团队实现宏大目标。他们对效率的追求近乎偏执。',
    strengths:['领导魅力','决策果断','目标导向','高效执行'], weaknesses:['过于强势','缺乏耐心','忽视情感','控制欲强'],
    careers:['CEO','律师','政治家','创业者'], famous:['史蒂夫·乔布斯','拿破仑','玛格丽特·撒切尔'] },
  ENTP: { type:'ENTP', title:'辩论家', emoji:'⚡', color:'#ea580c',
    desc:'思维敏锐、充满活力的辩论者，享受智识交锋与思想碰撞。他们的大脑永远在质疑、推翻和重建中高速运转。',
    strengths:['思维灵活','口才出众','创新能力','善于辩论'], weaknesses:['虎头蛇尾','争强好胜','不够体贴','讨厌规则'],
    careers:['创业者','律师','广告人','记者'], famous:['托马斯·爱迪生','马克·吐温','苏格拉底'] },
  INFJ: { type:'INFJ', title:'提倡者', emoji:'🌙', color:'#7c3aed',
    desc:'世界上最稀有的人格类型之一。他们有坚定的理想主义信念，对人性有深刻洞察，致力于用自己的方式改变世界。',
    strengths:['洞察人心','富有理想','共情能力强','有创造力'], weaknesses:['过于完美主义','容易过度付出','不擅拒绝','敏感易伤'],
    careers:['心理咨询师','作家','教师','社会活动家'], famous:['曼德拉','马丁·路德·金','马利亚·居里'] },
  INFP: { type:'INFP', title:'调停者', emoji:'🌸', color:'#db2777',
    desc:'深情浪漫的理想主义者，内心世界极为丰富。他们有强烈的个人价值观，渴望生活有意义，对美与真情尤为敏感。',
    strengths:['富有同情心','创意丰富','忠诚真诚','理想主义'], weaknesses:['过于敏感','容易逃避现实','不善处理批评','效率偏低'],
    careers:['作家','诗人','艺术家','心理咨询师'], famous:['莎士比亚','托尔金','约翰·列侬'] },
  ENFJ: { type:'ENFJ', title:'主人公', emoji:'🌟', color:'#16a34a',
    desc:'天生的领袖与教育者，对他人有强烈的感召力。他们善于激励他人发挥潜能，将周围人凝聚在共同目标周围。',
    strengths:['感召力强','善于倾听','组织协调','关爱他人'], weaknesses:['过于牺牲自我','敏感于批评','有时操控欲强','边界模糊'],
    careers:['教师','心理咨询师','公关经理','社区领袖'], famous:['奥普拉·温弗瑞','贝拉克·奥巴马','马丁·路德·金'] },
  ENFP: { type:'ENFP', title:'活动家', emoji:'🎆', color:'#f59e0b',
    desc:'充满热情与创意的自由灵魂，总能在日常中发现意义与可能。他们的能量具有感染力，喜欢和人建立真实的连接。',
    strengths:['热情洋溢','创意无限','共情力强','沟通能力出色'], weaknesses:['注意力分散','情绪化','过度承诺','计划执行差'],
    careers:['演员','记者','咨询师','营销策划'], famous:['罗宾·威廉姆斯','威利·旺卡','Mark Twain'] },
  ISTJ: { type:'ISTJ', title:'物流师', emoji:'📋', color:'#0369a1',
    desc:'可靠稳重的实干家，是组织和社会的基石。他们对责任、传统和规则有深深的尊重，一旦承诺必然履行。',
    strengths:['可靠负责','注重细节','组织能力强','专注持久'], weaknesses:['墨守成规','不善变通','情感表达困难','固执'],
    careers:['会计师','律师','军人','项目经理'], famous:['乔治·华盛顿','沃伦·巴菲特','安吉拉·默克尔'] },
  ISFJ: { type:'ISFJ', title:'守卫者', emoji:'🛡️', color:'#0891b2',
    desc:'温暖体贴的守护者，默默为身边的人付出，是朋友和家人最坚实的后盾。他们细心周到，记住所有人的细节。',
    strengths:['温暖体贴','可靠忠诚','记忆力强','实际能干'], weaknesses:['过度牺牲','难以拒绝','不善变化','压抑自我'],
    careers:['护士','教师','社工','行政管理'], famous:['碧昂斯','凯特·米德尔顿','罗莎·帕克斯'] },
  ESTJ: { type:'ESTJ', title:'执行官', emoji:'🏛️', color:'#b45309',
    desc:'注重实效的管理者，擅长制定规则、维护秩序。他们相信传统与纪律的力量，是可以依赖的组织核心。',
    strengths:['组织能力强','高效可靠','直接诚实','责任心强'], weaknesses:['固执顽固','不体谅情感','缺乏弹性','判断他人'],
    careers:['经理','法官','军官','政府官员'], famous:['拜登','弗兰克·辛纳特拉','米歇尔·奥巴马'] },
  ESFJ: { type:'ESFJ', title:'执政官', emoji:'🤝', color:'#15803d',
    desc:'人际关系的粘合剂，热情好客、乐于助人。他们在意每个人的感受，善于营造温暖包容的社群氛围。',
    strengths:['热情友善','顾全大局','善于组织活动','忠诚可靠'], weaknesses:['过于在意他人评价','难以适应变化','过度牺牲','回避冲突'],
    careers:['护士','教师','活动策划','销售'], famous:['泰勒·斯威夫特','雨果','比尔·克林顿'] },
  ISTP: { type:'ISTP', title:'鉴赏家', emoji:'🔧', color:'#374151',
    desc:'冷静务实的实践者，擅长用双手和大脑解决现实问题。他们喜欢探索事物运作原理，危机时刻尤为冷静出色。',
    strengths:['动手能力强','冷静理性','应变能力强','独立自主'], weaknesses:['冷漠疏离','不擅长规划','冒险主义','难以承诺'],
    careers:['机械工程师','程序员','飞行员','外科医生'], famous:['克林特·伊斯特伍德','斯嘉丽·约翰逊','布鲁斯·李'] },
  ISFP: { type:'ISFP', title:'探险家', emoji:'🎨', color:'#7e22ce',
    desc:'安静而富有美感的艺术家，敏感地感知周遭世界。他们活在当下，用行动而非言语表达对生活的热爱。',
    strengths:['艺术感强','真实自然','开放包容','乐于助人'], weaknesses:['过于敏感','自我怀疑','不善规划','逃避冲突'],
    careers:['艺术家','设计师','厨师','摄影师'], famous:['迈克尔·杰克逊','玛丽莲·梦露','大卫·鲍伊'] },
  ESTP: { type:'ESTP', title:'企业家', emoji:'🚀', color:'#dc2626',
    desc:'精力充沛、行动导向的冒险家，活在当下，享受刺激与挑战。他们反应敏捷，在混乱中依然游刃有余。',
    strengths:['行动力强','魅力十足','应变灵活','实用主义'], weaknesses:['冲动莽撞','缺乏耐心','忽视情感','不喜规则'],
    careers:['企业家','销售','演员','急救人员'], famous:['唐纳德·特朗普','麦当娜','欧内斯特·海明威'] },
  ESFP: { type:'ESFP', title:'表演者', emoji:'🎭', color:'#f97316',
    desc:'天生的表演者，走到哪里哪里就充满欢笑。他们热爱生活、享受当下，用无尽的热情点亮身边每一个人。',
    strengths:['乐观活泼','善于交际','共情力强','享受当下'], weaknesses:['计划性差','容易分心','情绪化','回避深刻话题'],
    careers:['演员','歌手','主持人','导游'], famous:['阿黛尔','玛丽莲·梦露','艾尔顿·约翰'] },
}

// ─── 答题逻辑 ─────────────────────────────────────────────────────────────────
const answers = ref<Record<number, 'a' | 'b'>>({})
const currentPage = ref(0)
const pageSize = 8
const showResult = ref(false)
const resultType = ref('')

const totalPages = Math.ceil(questions.length / pageSize)
const pageQuestions = computed(() => questions.slice(currentPage.value * pageSize, (currentPage.value + 1) * pageSize))
const answeredOnPage = computed(() => pageQuestions.value.every(q => answers.value[q.id] !== undefined))
const progress = computed(() => (Object.keys(answers.value).length / questions.length) * 100)

function choose(qid: number, opt: 'a' | 'b') {
  answers.value[qid] = opt
}

function nextPage() {
  if (currentPage.value < totalPages - 1) currentPage.value++
  else calcResult()
}

function prevPage() {
  if (currentPage.value > 0) currentPage.value--
}

function calcResult() {
  const score: Record<Dimension, number> = { E:0, I:0, S:0, N:0, T:0, F:0, J:0, P:0 }
  questions.forEach(q => {
    const ans = answers.value[q.id]
    if (ans === 'a') score[q.a.dim]++
    else if (ans === 'b') score[q.b.dim]++
  })
  const type = [
    score.E >= score.I ? 'E' : 'I',
    score.S >= score.N ? 'S' : 'N',
    score.T >= score.F ? 'T' : 'F',
    score.J >= score.P ? 'J' : 'P',
  ].join('')
  resultType.value = type
  showResult.value = true
}

const result = computed(() => mbtiMap[resultType.value] || null)

function restart() {
  answers.value = {}
  currentPage.value = 0
  showResult.value = false
  resultType.value = ''
}

// 维度百分比
const dimPercents = computed(() => {
  if (!showResult.value) return null
  const score: Record<Dimension, number> = { E:0, I:0, S:0, N:0, T:0, F:0, J:0, P:0 }
  questions.forEach(q => {
    const ans = answers.value[q.id]
    if (ans === 'a') score[q.a.dim]++
    else if (ans === 'b') score[q.b.dim]++
  })
  // 每个维度轴各 8 题
  const pairs: [Dimension, Dimension][] = [['E','I'],['S','N'],['T','F'],['J','P']]
  return pairs.map(([a, b]) => {
    const total = score[a] + score[b]
    return { a, b, pctA: total ? Math.round(score[a]/total*100) : 50, pctB: total ? Math.round(score[b]/total*100) : 50 }
  })
})
</script>

<template>
  <div class="mbti-page">
    <!-- 动态背景光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="mbti-back"><BackToHomeButton /></div>

    <!-- 标题区 -->
    <div class="mbti-header">
      <div class="mbti-icon">🧠</div>
      <h1 class="mbti-title">MBTI 人格测试</h1>
      <p class="mbti-subtitle">32 道题 · 解读你的人格密码 · 发现真实的自己</p>
    </div>

    <!-- ─── 结果页 ─── -->
    <template v-if="showResult && result">
      <transition name="result-pop" appear>
        <div class="result-card glass-surface">
        <!-- 发光边框 -->
        <div class="result-glow-border" :style="{ '--glow-color': result.color }"></div>

        <div class="result-badge" :style="{ background: result.color }">
          <span class="result-emoji">{{ result.emoji }}</span>
          <span class="result-type">{{ result.type }}</span>
        </div>
        <h2 class="result-title" :style="{ color: result.color }">{{ result.title }}</h2>
        <p class="result-desc">{{ result.desc }}</p>

        <!-- 维度条 -->
        <div class="dim-bars" v-if="dimPercents">
          <div v-for="(d, i) in dimPercents" :key="d.a" class="dim-row" :style="{ '--delay': i * 0.12 + 's' }">
            <span class="dim-label-l">{{ d.a }}</span>
            <div class="dim-bar-track">
              <div class="dim-bar-fill" :style="{ width: d.pctA + '%', '--bar-color': result.color }"></div>
              <div class="dim-bar-fill dim-bar-fill--off" :style="{ width: d.pctB + '%' }"></div>
            </div>
            <span class="dim-label-r">{{ d.b }}</span>
          </div>
        </div>

        <!-- 优劣 -->
        <div class="result-grid">
          <div class="result-col">
            <h4>✨ 优势</h4>
            <ul><li v-for="(s, i) in result.strengths" :key="s" :style="{ '--i': i }">{{ s }}</li></ul>
          </div>
          <div class="result-col">
            <h4>⚠️ 盲区</h4>
            <ul><li v-for="(w, i) in result.weaknesses" :key="w" :style="{ '--i': i }">{{ w }}</li></ul>
          </div>
        </div>
        <div class="result-section">
          <h4>💼 适合方向</h4>
          <div class="tag-row"><span v-for="(c, i) in result.careers" :key="c" class="tag" :style="{ '--i': i }">{{ c }}</span></div>
        </div>
        <div class="result-section">
          <h4>🌍 同类名人</h4>
          <div class="tag-row"><span v-for="(f, i) in result.famous" :key="f" class="tag tag--gold" :style="{ '--i': i }">{{ f }}</span></div>
        </div>

        <button class="btn-restart" @click="restart">
          <span>🔄</span> 重新测试
        </button>
      </div>
    </transition>
    </template>

    <!-- ─── 答题页 ─── -->
    <template v-else>
      <transition name="slide" mode="out-in">
        <div class="quiz-wrap">
      <!-- 进度条 -->
      <div class="progress-wrap">
        <div class="progress-bar" :style="{ width: progress + '%' }">
          <div class="progress-glow"></div>
        </div>
        <div class="progress-dots">
          <span
            v-for="i in totalPages"
            :key="i"
            class="dot"
            :class="{ done: (i - 1) * 8 <= Object.keys(answers).length, active: currentPage === i - 1 }"
          ></span>
        </div>
      </div>
      <div class="progress-text-wrap">
        <span class="progress-text">已完成 {{ Object.keys(answers).length }} / {{ questions.length }} 题</span>
        <span class="page-badge">{{ currentPage + 1 }} / {{ totalPages }}</span>
      </div>

      <!-- 题目列表 -->
      <transition name="slide" mode="out-in">
        <div :key="currentPage" class="questions-list">
          <div
            v-for="(q, idx) in pageQuestions"
            :key="q.id"
            class="question-card glass-surface"
            :class="{ answered: answers[q.id] !== undefined }"
            :style="{ '--idx': idx }"
          >
            <div class="q-header">
              <span class="q-num">Q{{ q.id }}</span>
              <span v-if="answers[q.id] !== undefined" class="q-check">✓</span>
            </div>
            <p class="q-text">{{ q.text }}</p>
            <div class="options">
              <button
                class="option-btn"
                :class="{ selected: answers[q.id] === 'a' }"
                @click="choose(q.id, 'a')"
              >
                <span class="opt-letter">A</span>
                <span class="opt-text">{{ q.a.text }}</span>
              </button>
              <button
                class="option-btn"
                :class="{ selected: answers[q.id] === 'b' }"
                @click="choose(q.id, 'b')"
              >
                <span class="opt-letter">B</span>
                <span class="opt-text">{{ q.b.text }}</span>
              </button>
            </div>
          </div>
        </div>
      </transition>

      <!-- 翻页按钮 -->
      <div class="page-nav">
        <button class="nav-btn nav-btn--prev" :disabled="currentPage === 0" @click="prevPage">
          <span>←</span> 上一页
        </button>
        <div class="nav-center">
          <span class="page-indicator">{{ currentPage + 1 }} / {{ totalPages }}</span>
        </div>
        <button
          class="nav-btn nav-btn--next"
          :disabled="!answeredOnPage"
          @click="nextPage"
        >
          {{ currentPage === totalPages - 1 ? '查看结果 🎯' : '下一页 →' }}
        </button>
      </div>
        </div>
      </transition>
    </template>
  </div>
</template>

<style scoped>
.mbti-page {
  box-sizing: border-box;
  width: 100%;
  max-width: 760px;
  margin: 0 auto;
  padding: max(16px, env(safe-area-inset-top)) 16px max(40px, calc(env(safe-area-inset-bottom) + 20px));
  animation: fadeIn 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

/* 动态光球背景 */
.orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.18;
  pointer-events: none;
  z-index: 0;
  animation: orbFloat 20s ease-in-out infinite;
}
.orb-1 {
  width: 400px; height: 400px;
  background: #66d9ff;
  top: -100px; left: -100px;
  animation-duration: 22s;
}
.orb-2 {
  width: 300px; height: 300px;
  background: #a78bfa;
  top: 40%; right: -80px;
  animation-duration: 18s;
  animation-delay: -7s;
}
.orb-3 {
  width: 250px; height: 250px;
  background: #f472b6;
  bottom: 0; left: 30%;
  animation-duration: 25s;
  animation-delay: -12s;
}

.mbti-back { margin-bottom: 12px; position: relative; z-index: 1; }

.mbti-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}
.mbti-icon {
  font-size: 3.5rem;
  margin-bottom: 8px;
  animation: iconBounce 3s ease-in-out infinite;
  display: inline-block;
}
.mbti-title {
  font-size: clamp(1.8rem, 5vw, 2.6rem);
  background: linear-gradient(135deg, #66d9ff 0%, #a78bfa 50%, #f472b6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}
.mbti-subtitle {
  color: var(--text-color);
  opacity: 0.7;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
}

/* 进度条 */
.progress-wrap {
  position: relative;
  margin-bottom: 4px;
  z-index: 1;
}
.progress-bar {
  height: 8px;
  background: rgba(255,255,255,0.12);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}
.progress-bar {
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #1a1a2e, rgba(255,255,255,0.12));
  overflow: hidden;
}
.progress-bar::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, #66d9ff, #a78bfa, #f472b6);
  border-radius: 4px;
  width: var(--progress, 0%);
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.progress-glow {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, rgba(167,139,250,0.6) 0%, transparent 70%);
  animation: glowPulse 2s ease-in-out infinite;
}

.progress-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 8px;
}
.dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  transition: all 0.3s;
}
.dot.done { background: #66d9ff; transform: scale(1.2); }
.dot.active { background: #a78bfa; box-shadow: 0 0 8px #a78bfa; transform: scale(1.4); }

.progress-text-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}
.progress-text { font-size: 0.78rem; color: var(--text-color); opacity: 0.6; }
.page-badge {
  font-size: 0.72rem;
  padding: 3px 10px;
  border-radius: 20px;
  background: rgba(102,217,255,0.15);
  border: 1px solid rgba(102,217,255,0.3);
  color: #66d9ff;
  font-weight: 700;
}

/* 题目卡片 */
.quiz-wrap { position: relative; z-index: 1; }
.questions-list { display: flex; flex-direction: column; gap: 14px; }
.question-card {
  padding: 20px;
  border-radius: 16px;
  animation: cardSlideIn 0.4s ease-out both;
  animation-delay: calc(var(--idx, 0) * 0.08s);
}
.question-card.answered {
  border-color: rgba(167,139,250,0.5);
  box-shadow: 0 0 20px rgba(167,139,250,0.08);
}

.q-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.q-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #66d9ff, #a78bfa);
  color: #000;
  font-weight: 800;
  font-size: 0.75rem;
  padding: 3px 10px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}
.q-check {
  font-size: 1.1rem;
  color: #10b981;
  animation: checkPop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.q-text {
  font-size: 1rem;
  line-height: 1.65;
  color: var(--text-color);
  margin-bottom: 14px;
  font-weight: 500;
}

.options { display: flex; flex-direction: column; gap: 10px; }
@media (min-width: 540px) { .options { flex-direction: row; } }

.option-btn {
  flex: 1;
  padding: 14px 16px;
  border-radius: 14px;
  border: 2px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.05);
  color: var(--text-color);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 1.4;
}
.opt-letter {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: rgba(255,255,255,0.1);
  font-size: 0.75rem;
  font-weight: 800;
  flex-shrink: 0;
  transition: all 0.2s;
}
.opt-text { flex: 1; }
.option-btn:hover {
  border-color: rgba(167,139,250,0.6);
  background: rgba(167,139,250,0.08);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(167,139,250,0.15);
}
.option-btn:hover .opt-letter { background: rgba(167,139,250,0.2); }
.option-btn.selected {
  border-color: #a78bfa;
  background: linear-gradient(135deg, rgba(167,139,250,0.2), rgba(102,217,255,0.12));
  color: #e0d4ff;
  font-weight: 600;
  box-shadow: 0 4px 20px rgba(167,139,250,0.2);
}
.option-btn.selected .opt-letter {
  background: linear-gradient(135deg, #a78bfa, #66d9ff);
  color: #000;
}

/* 翻页 */
.page-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 28px;
  gap: 12px;
}
.nav-btn {
  padding: 11px 22px;
  border-radius: 14px;
  border: 1.5px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.06);
  color: var(--text-color);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 6px;
}
.nav-btn:hover:not(:disabled) { background: rgba(255,255,255,0.12); transform: translateY(-1px); }
.nav-btn:active:not(:disabled) { transform: translateY(0) scale(0.97); }
.nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.nav-btn--next:not(:disabled) {
  background: linear-gradient(135deg, #a78bfa, #66d9ff);
  color: #000;
  border-color: transparent;
  font-weight: 800;
  box-shadow: 0 4px 16px rgba(167,139,250,0.3);
}
.nav-btn--next:not(:disabled):hover {
  box-shadow: 0 6px 24px rgba(167,139,250,0.45);
  transform: translateY(-2px);
}
.nav-center { flex: 1; text-align: center; }
.page-indicator { font-size: 0.85rem; color: var(--text-color); opacity: 0.5; }

/* 结果卡片 */
.result-card {
  padding: 32px 28px;
  border-radius: 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
  animation: resultReveal 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.result-glow-border {
  position: absolute;
  inset: -2px;
  border-radius: 26px;
  background: conic-gradient(from var(--angle, 0deg), var(--glow-color), transparent 30%, var(--glow-color));
  opacity: 0.6;
  z-index: -1;
  animation: borderRotate 4s linear infinite;
}
.result-badge {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 12px 28px;
  border-radius: 60px;
  color: #fff;
  font-weight: 800;
  font-size: 1.5rem;
  margin-bottom: 18px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.3);
  animation: badgePop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s both;
}
.result-emoji { font-size: 2rem; }
.result-type { font-size: 2rem; letter-spacing: 6px; }
.result-title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  margin-bottom: 14px;
  font-weight: 800;
  animation: fadeUp 0.5s ease 0.3s both;
}
.result-desc {
  color: var(--text-color);
  opacity: 0.88;
  line-height: 1.75;
  margin-bottom: 24px;
  font-size: 0.96rem;
  animation: fadeUp 0.5s ease 0.4s both;
}

/* 维度条 */
.dim-bars { margin: 20px 0 24px; }
.dim-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  animation: fadeUp 0.5s ease calc(0.4s + var(--delay, 0s)) both;
}
.dim-label-l, .dim-label-r {
  width: 22px;
  font-weight: 800;
  font-size: 0.95rem;
  color: var(--bar-color, #a78bfa);
  flex-shrink: 0;
}
.dim-label-r { text-align: right; }
.dim-bar-track {
  flex: 1;
  height: 12px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  background: rgba(255,255,255,0.08);
}
.dim-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--bar-color, #a78bfa), color-mix(in srgb, var(--bar-color, #a78bfa) 60%, #fff));
  border-radius: 6px 0 0 6px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  animation: barFill 0.8s cubic-bezier(0.4, 0, 0.2, 1) calc(0.4s + var(--delay, 0s)) both;
}
.dim-bar-fill--off {
  background: rgba(255,255,255,0.15);
  border-radius: 0 6px 6px 0;
  animation: barFill 0.8s cubic-bezier(0.4, 0, 0.2, 1) calc(0.4s + var(--delay, 0s)) both;
}

/* 优劣势 */
.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 20px;
  text-align: left;
  animation: fadeUp 0.5s ease 0.8s both;
}
@media (max-width: 480px) { .result-grid { grid-template-columns: 1fr; } }
.result-col {
  background: rgba(255,255,255,0.05);
  border-radius: 14px;
  padding: 16px;
  border: 1px solid rgba(255,255,255,0.08);
}
.result-col h4 { margin: 0 0 10px; font-size: 0.88rem; color: #a78bfa; font-weight: 700; }
.result-col ul { list-style: none; padding: 0; margin: 0; }
.result-col li {
  font-size: 0.85rem;
  color: var(--text-color);
  opacity: 0.85;
  padding: 4px 0;
  animation: fadeUp 0.3s ease calc(0.9s + var(--i, 0) * 0.05s) both;
}
.result-col li::before { content: '✦ '; color: #a78bfa; font-size: 0.7rem; }

.result-section {
  text-align: left;
  margin-bottom: 16px;
  animation: fadeUp 0.5s ease 1s both;
}
.result-section h4 { font-size: 0.9rem; color: #a78bfa; margin-bottom: 10px; font-weight: 700; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; }
.tag {
  padding: 5px 14px;
  border-radius: 20px;
  background: rgba(167,139,250,0.12);
  border: 1px solid rgba(167,139,250,0.25);
  color: var(--text-color);
  font-size: 0.82rem;
  animation: fadeUp 0.3s ease calc(1s + var(--i, 0) * 0.06s) both;
}
.tag--gold {
  background: rgba(245,158,11,0.12);
  border-color: rgba(245,158,11,0.25);
}

.btn-restart {
  margin-top: 28px;
  padding: 14px 36px;
  border-radius: 16px;
  border: 2px solid rgba(167,139,250,0.5);
  background: linear-gradient(135deg, rgba(167,139,250,0.15), rgba(102,217,255,0.1));
  color: #e0d4ff;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  animation: fadeUp 0.5s ease 1.1s both;
}
.btn-restart:hover {
  background: linear-gradient(135deg, rgba(167,139,250,0.3), rgba(102,217,255,0.2));
  border-color: #a78bfa;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(167,139,250,0.3);
}
.btn-restart:active { transform: translateY(0) scale(0.97); }

/* 动画 */
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from { opacity: 0; transform: translateX(30px); }
.slide-leave-to { opacity: 0; transform: translateX(-30px); }

.result-pop-enter-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.result-pop-enter-from { opacity: 0; transform: scale(0.9) translateY(20px); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
@keyframes cardSlideIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes checkPop { from { transform: scale(0); } to { transform: scale(1.2); } }
@keyframes badgePop { from { transform: scale(0.5); opacity: 0; } to { transform: scale(1); opacity: 1; } }
@keyframes resultReveal { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
@keyframes barFill { from { width: 0 !important; } }
@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -40px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(40px, 10px) scale(1.05); }
}
@keyframes glowPulse { 0%, 100% { opacity: 0.6; } 50% { opacity: 1; } }
@keyframes iconBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
@keyframes borderRotate { to { --angle: 360deg; } }
</style>
