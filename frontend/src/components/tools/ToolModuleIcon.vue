<script setup lang="ts">
defineProps<{ icon: string }>()
</script>

<template>
  <span class="tool-module-icon" aria-hidden="true">
    <svg v-if="icon === 'moyu'" viewBox="0 0 24 24" class="tool-module-icon__svg tool-module-icon__svg--moyu">
      <path
        fill="currentColor"
        d="M6.7 5h10.6c1.4 0 2.6 1 2.9 2.3l1.1 5.2c.2.9-.4 1.8-1.3 2l-.4.1h-.2c-.6 0-1.1-.4-1.3-.9l-.2-.6c-.2-.6-.8-1-1.4-1H9.8c-.6 0-1.2.4-1.4 1l-.2.6c-.2.5-.7.9-1.3.9H4.2c-1 0-1.8-.8-1.8-1.8 0-.2 0-.3.1-.4l1.1-5.2C4.1 6 5.3 5 6.7 5zm2.7 3.5c-.8 0-1.5.7-1.5 1.5S8.6 11 9.4 11s1.5-.7 1.5-1.5-.7-1.5-1.5-1.5zm5 0c-.8 0-1.5.7-1.5 1.5s.7 1.5 1.5 1.5 1.5-.7 1.5-1.5-.7-1.5-1.5-1.5z"
      />
    </svg>
    <svg v-else-if="icon === 'reaction'" viewBox="0 0 24 24" class="tool-module-icon__svg">
      <path
        fill="currentColor"
        d="M11 21h-1l1-7H6.5c-.58 0-.57-.32-.38-.66.19-.34.05-.08.07-.12C8.48 10.94 10.42 7.54 13 3h1l-1 7h3.5c.49 0 .56.33.47.51l-.07.15C12.96 17.55 11 21 11 21z"
      />
    </svg>
    <svg v-else-if="icon === 'cps'" viewBox="0 0 24 24" class="tool-module-icon__svg tool-module-icon__svg--cps">
      <path
        fill="currentColor"
        d="M12 3.5c-3.05 0-5.5 2.45-5.5 5.5v5.4c0 3.35 2.72 6.1 6.1 6.1h.8c3.38 0 6.1-2.75 6.1-6.1V9c0-3.05-2.45-5.5-5.5-5.5zm0 2c1.93 0 3.5 1.57 3.5 3.5v.35h-7V9c0-1.93 1.57-3.5 3.5-3.5zm-1.25 5.25h2.5c.14 0 .25.11.25.25v2.25c0 1.1-.9 2-2 2s-2-.9-2-2v-2.25c0-.14.11-.25.25-.25z"
      />
    </svg>
    <svg v-else-if="icon === 'pomodoro'" viewBox="0 0 24 24" class="tool-module-icon__svg tool-module-icon__svg--tomato">
      <path
        fill="#15803d"
        d="M12 2.2c-.85 0-1.55.55-1.85 1.3-.12.28-.18.58-.18.9 0 .35.08.68.22.98.35-.15.73-.23 1.13-.23h1.36c.4 0 .78.08 1.13.23.14-.3.22-.63.22-.98 0-.32-.06-.62-.18-.9-.3-.75-1-1.3-1.85-1.3z"
      />
      <ellipse cx="12" cy="14.2" rx="7.2" ry="6.9" fill="#dc2626" />
      <path fill="rgba(255,255,255,0.38)" d="M9.2 11.5a4.2 4.8 0 015.6 6.35 6.9 6.9 0 01-6.95-5.9c.35-.55.78-1 1.35-1.45z" />
    </svg>
    <svg v-else-if="icon === 'schulte'" viewBox="0 0 24 24" class="tool-module-icon__svg tool-module-icon__svg--schulte">
      <rect x="3" y="3" width="18" height="18" rx="4.5" fill="none" stroke="currentColor" stroke-width="1.35" />
      <line x1="12" y1="3.65" x2="12" y2="20.35" stroke="currentColor" stroke-width="1.1" />
      <line x1="3.65" y1="12" x2="20.35" y2="12" stroke="currentColor" stroke-width="1.1" />
      <text x="7.35" y="10.4" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">1</text>
      <text x="16.65" y="10.4" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">2</text>
      <text x="7.35" y="18.6" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">3</text>
      <text x="16.65" y="18.6" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">4</text>
    </svg>
    <svg v-else-if="icon === 'mbti'" viewBox="0 0 24 24" class="tool-module-icon__svg tool-module-icon__svg--brain">
      <path
        fill="currentColor"
        d="M12 4.2c-.9 0-1.7.35-2.35.9-.55-.35-1.2-.55-1.9-.55-1.45 0-2.65 1-2.95 2.35-.85.2-1.55.85-1.75 1.7-.25 1.15.35 2.25 1.35 2.8-.05.25-.05.5 0 .75.15.85.85 1.5 1.7 1.65.1.9.65 1.65 1.45 2 .35 1.65 1.75 2.9 3.45 2.9s3.1-1.25 3.45-2.9c.8-.35 1.35-1.1 1.45-2 .85-.15 1.55-.8 1.7-1.65.05-.25.05-.5 0-.75 1-.55 1.6-1.65 1.35-2.8-.2-.85-.9-1.5-1.75-1.7-.3-1.35-1.5-2.35-2.95-2.35-.7 0-1.35.2-1.9.55-.65-.55-1.45-.9-2.35-.9zm-3.5 3.35c.35 0 .65.08.95.22-.32.45-.52.98-.52 1.58 0 .55.18 1.05.48 1.48-.28.12-.58.18-.9.18-1 0-1.8-.8-1.8-1.8s.8-1.8 1.8-1.8zm7 0c1 0 1.8.8 1.8 1.8s-.8 1.8-1.8 1.8c-.32 0-.62-.06-.9-.18.3-.43.48-.93.48-1.48 0-.6-.2-1.13-.52-1.58.3-.14.6-.22.95-.22zM12 9.6c.85 0 1.55.7 1.55 1.55S12.85 12.7 12 12.7s-1.55-.7-1.55-1.55S11.15 9.6 12 9.6z"
      />
    </svg>
    <svg v-else-if="icon === 'oj'" viewBox="0 0 24 24" class="tool-module-icon__svg tool-module-icon__svg--keyboard">
      <path
        fill="currentColor"
        d="M4 7c-.55 0-1 .45-1 1v8c0 .55.45 1 1 1h16c.55 0 1-.45 1-1V8c0-.55-.45-1-1-1H4zm1.2 2h1.6v1.4H5.2V9zm2.6 0H10v1.4H7.8V9zm2.8 0h2.4v1.4h-2.4V9zm2.8 0H16v1.4h-2.4V9zm2.8 0h1.6v1.4H16V9zM5.2 12.2h2.4v1.4H5.2v-1.4zm3.4 0H13v1.4H8.6v-1.4zm3.6 0h2.4v1.4h-2.4v-1.4zm3.4 0h3.4v1.4H15.6v-1.4zM5.2 15.4h4.6v1.4H5.2v-1.4zm5.6 0H19v1.4h-8.2v-1.4z"
      />
    </svg>
    <svg v-else-if="icon === 'stock'" viewBox="0 0 24 24" class="tool-module-icon__svg">
      <path
        fill="currentColor"
        d="M3.5 18.5h17v1.5h-17v-1.5zm1.5-3.2l3.5-3.5 2.8 2.8 5.2-6.2 3.5 4.2-1.15.96-2.55-3.05-5.35 6.35-2.65-2.65-2.5 2.5 4.2 4.2H5v-2.1zm8-8.3V5h2v2.5h-2zm3.5 1l1.75-1.75L18 8l-1.75 1.75L15 10.5z"
      />
    </svg>
    <svg v-else-if="icon === 'lock'" viewBox="0 0 24 24" class="tool-module-icon__svg">
      <path
        fill="currentColor"
        d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
      />
    </svg>
    <svg v-else-if="icon === 'b64'" viewBox="0 0 24 24" class="tool-module-icon__svg">
      <path
        fill="currentColor"
        d="M4 4h7v2H6v12h5v2H4V4zm9 4h7v2h-5v8h5v2h-7V8zm-1 4h2v4h-2v-4z"
      />
    </svg>
    <svg v-else viewBox="0 0 24 24" class="tool-module-icon__svg">
      <path
        fill="currentColor"
        d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"
      />
    </svg>
  </span>
</template>

<style scoped>
.tool-module-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.tool-module-icon__svg {
  width: 28px;
  height: 28px;
}
</style>
