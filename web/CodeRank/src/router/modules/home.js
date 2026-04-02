// 首页路由

const homeRoutes = [
    {
        path: '/',
        redirect: '/index',
        component: () => import('@/layout/MainLayout.vue'),
        children: [
            {
                path: '/index',
                name: '首页',
                component: () => import('@/views/home/IndexView.vue')
            },
            {
                path: '/login',
                name: '登录',
                component: () => import('@/views/home/LoginView.vue')
            }
        ]
    }
]

export default homeRoutes