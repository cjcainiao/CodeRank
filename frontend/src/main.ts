// 应用初始化，注册路由、状态与组件库
import { createApp, watch } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import { useThemeMode } from './utils/themeModeUtil'
import 'element-plus/dist/index.css'
import './assets/css/init.css'
import './assets/css/code-font.css'
import './assets/css/element-theme.css'

// 所有页面共用根节点主题类，传送门组件也能读取当前配色
const { darkMode } = useThemeMode()
watch(
  darkMode,
  (dark) => {
    document.documentElement.classList.toggle('app-dark', dark)
  },
  { immediate: true },
)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
