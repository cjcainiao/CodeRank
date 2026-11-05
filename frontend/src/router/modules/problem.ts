// 做题页面路由
import type { RouteRecordRaw } from 'vue-router'

const problemRoutes: RouteRecordRaw[] = [
  {
    // 做题界面独立成页，不套首页导航栏布局
    path: '/problem',
    component: () => import('@/views/problem/LayoutView.vue'),
    children: [
      {
        path: ':id',
        name: 'problem-solve',
        component: () => import('@/views/problem/pages/SolveView.vue'),
        meta: {
          title: '做题',
        },
      },
    ],
  },
]

export default problemRoutes
