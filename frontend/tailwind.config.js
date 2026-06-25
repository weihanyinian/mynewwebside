/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f4ff',
          100: '#dbe4ff',
          200: '#bac8ff',
          300: '#91a7ff',
          400: '#748ffc',
          500: '#5c7cfa',
          600: '#4c6ef5',
          700: '#4263eb',
          800: '#3b5bdb',
          900: '#364fc7',
        },
        accent: {
          50: '#f8f0ff',
          100: '#f3d9ff',
          200: '#e599f7',
          300: '#cc5de8',
          400: '#b197fc',
          500: '#a58eea',
          600: '#8c6be8',
          700: '#7950f2',
          800: '#6741d9',
          900: '#5f3dc4',
        }
      },
      backdropBlur: {
        xs: '2px',
      },
      fontFamily: {
        sans: ['"Noto Sans SC"', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
        mono: ['"JetBrains Mono"', '"Fira Code"', 'monospace'],
      },
    },
  },
  plugins: [],
}
