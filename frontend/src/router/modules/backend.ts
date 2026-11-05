// 后台页面路由
import type { RouteRecordRaw } from 'vue-router'

const backendRoutes: RouteRecordRaw[] = [
  {
    path: '/backend',
    component: () => import('@/views/backend/LayoutView.vue'),
    children: [
      {
        path: '',
        name: 'backend-home',
        component: () => import('@/views/backend/pages/IndexView.vue'),
        meta: {
          backendTitle: '工作台',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'questions',
        name: 'backend-questions',
        component: () => import('@/views/backend/pages/questions/QuestionListView.vue'),
        meta: {
          backendTitle: '题目列表',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'questions/new',
        name: 'backend-question-new',
        component: () => import('@/views/backend/pages/questions/WriteQuestionView.vue'),
        meta: {
          backendTitle: '新建题目',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'questions/:id/edit',
        name: 'backend-question-edit',
        component: () => import('@/views/backend/pages/questions/WriteQuestionView.vue'),
        meta: {
          backendTitle: '编辑题目',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'tags',
        name: 'backend-tags',
        component: () => import('@/views/backend/pages/questions/TagManageView.vue'),
        meta: {
          backendTitle: '标签管理',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'submissions',
        name: 'backend-submissions',
        component: () => import('@/views/backend/pages/submissions/SubmissionListView.vue'),
        meta: {
          backendTitle: '提交列表',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'judge/status',
        name: 'backend-judge-status',
        component: () => import('@/views/backend/pages/judge/JudgeStatusView.vue'),
        meta: {
          backendTitle: '评测机状态',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'users',
        name: 'backend-users',
        component: () => import('@/views/backend/pages/users/UserListView.vue'),
        meta: {
          backendTitle: '用户列表',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
      {
        path: 'settings',
        name: 'backend-settings',
        component: () => import('@/views/backend/pages/settings/SettingsView.vue'),
        meta: {
          backendTitle: '设置',
          requiresAuth: true,
          roles: ['admin'],
        },
      },
    ],
  },
]

export default backendRoutes
