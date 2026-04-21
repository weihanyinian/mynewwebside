import { defineStore } from 'pinia'
import type { HomeWorkItem } from '../types/home'

const seedWorks: HomeWorkItem[] = [
  {
    title: '大语言模型微调与部署',
    desc: '基于 GLM4 与 LoRA 技术的大模型微调实战项目，探索垂直领域大语言模型应用。',
    detail: '覆盖数据构建、训练与推理部署流程，可作为 LLM 工程化落地的实践参考。',
    link: '#works',
    tag: 'LLM / GLM4',
    cover: '/avatar.png',
  },
  {
    title: 'Transformer 机器翻译',
    desc: '基于底层 Transformer 架构从零构建的机器翻译模型，深入理解 Attention 机制。',
    detail: '从编码器—解码器到多头注意力，适合巩固序列建模与并行训练要点。',
    link: '#works',
    tag: 'Deep Learning',
    cover: '/avatar.png',
  },
  {
    title: 'MyWebSide Blog',
    desc: '个人专属数字花园，基于 Spring Boot 3 与 Vue 3 构建的全栈展示平台。',
    detail: '博客、留言墙、工具栏与 OJ 一体化；代码开源，欢迎交流与共建。',
    link: 'https://github.com/weihanyinian/mynewwebside',
    tag: 'Full Stack',
    cover: '/avatar.png',
  },
  {
    title: '在线判题 OJ',
    desc: '内置算法题库与 Judge0 沙箱，支持 C/C++/Java/Python，ACM 与力扣风格评测。',
    detail: '登录后可提交代码、查看评测状态与历史提交记录。',
    link: '/tools/oj',
    tag: 'OJ / Sandbox',
    cover: '/avatar.png',
  },
]

export const useWorksStore = defineStore('works', {
  state: () => ({
    selectedCategory: 'all',
    works: seedWorks as HomeWorkItem[],
  }),
  getters: {
    categories: (s) => ['all', ...Array.from(new Set(s.works.map((w) => w.tag.split('/')[0].trim())))],
    filteredWorks: (s) =>
      s.selectedCategory === 'all'
        ? s.works
        : s.works.filter((w) => w.tag.toLowerCase().includes(s.selectedCategory.toLowerCase())),
  },
  actions: {
    setCategory(category: string) {
      this.selectedCategory = category
    },
  },
})
