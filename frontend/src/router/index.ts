// 创建前端路由
import { createRouter, createWebHistory } from 'vue-router'

import { setPageTitle } from '@/utils/titleUtil'

import routes from './routes'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 每次切换页面后更新浏览器标题
router.afterEach((to) => {
  setPageTitle(to)
})

export default router
