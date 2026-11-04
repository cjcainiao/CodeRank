// 汇总各页面模块的路由
import type { RouteRecordRaw } from 'vue-router'

import backendRoutes from './modules/backend'

const routes: RouteRecordRaw[] = [...backendRoutes]

export default routes
