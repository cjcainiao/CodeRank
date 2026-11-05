// 首页页面路由
import type { RouteRecordRaw } from 'vue-router'

const homeRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/views/home/LayoutView.vue'),
    children: [
      {
        path: '',
        name: 'home-index',
        component: () => import('@/views/home/pages/IndexView.vue'),
        meta: {
          title: '首页',
        },
      },
      {
        path: 'questions',
        name: 'home-questions',
        component: () => import('@/views/home/pages/questions/QuestionBankView.vue'),
        meta: {
          title: '题库',
        },
      },
      {
        path: 'contests',
        name: 'home-contests',
        component: () => import('@/views/home/pages/contests/ContestListView.vue'),
        meta: {
          title: '比赛',
        },
      },
    ],
  },
]

export default homeRoutes
