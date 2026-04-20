import { onMounted, onUnmounted, ref } from 'vue'

export function useHeroMotion() {
  const heroParallaxY = ref(0)
  const isNavScrolled = ref(false)
  const pointerX = ref(50)
  const pointerY = ref(50)
  let heroTicking = false
  let pointerTicking = false

  function onHeroParallax() {
    if (heroTicking) return
    heroTicking = true
    requestAnimationFrame(() => {
      heroParallaxY.value = Math.min(window.scrollY * 0.14, 88)
      isNavScrolled.value = window.scrollY > 24
      heroTicking = false
    })
  }

  function onPointerMove(e: PointerEvent) {
    if (pointerTicking) return
    pointerTicking = true
    requestAnimationFrame(() => {
      pointerX.value = (e.clientX / window.innerWidth) * 100
      pointerY.value = (e.clientY / window.innerHeight) * 100
      pointerTicking = false
    })
  }

  onMounted(() => {
    window.addEventListener('scroll', onHeroParallax, { passive: true })
    window.addEventListener('pointermove', onPointerMove, { passive: true })
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', onHeroParallax)
    window.removeEventListener('pointermove', onPointerMove)
  })

  return { heroParallaxY, isNavScrolled, pointerX, pointerY }
}
