// 汇总各页面模块的路由
import type { RouteRecordRaw } from 'vue-router'

import backendRoutes from './modules/backend'
import homeRoutes from './modules/home'
import problemRoutes from './modules/problem'

const routes: RouteRecordRaw[] = [...homeRoutes, ...problemRoutes, ...backendRoutes]

export default routes
