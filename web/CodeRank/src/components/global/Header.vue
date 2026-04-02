<!-- 导航栏组件 -->
<template>
    <header class="navbar">
        <div class="nav-container">
            <!-- logo显示区域 -->
            <div class="logo-section" @click="$router.push('/index')">
                <div class="nav-logo">
                    <span class="logo-icon">⚡</span>
                    CodeRank
                </div>
            </div>

            <!-- 导航菜单 -->
            <nav class="nav-menu">
                <RouterLink v-for="item in navItems" :key="item.path" :to="item.path" class="nav-link"
                    :class="{ active: isActive(item.path) }">
                    {{ item.label }}
                </RouterLink>
            </nav>

            <!-- 操作按钮 -->
            <div class="nav-actions">
                <button class="search-btn" title="搜索">🔍</button>
                <button class="user-btn" title="用户">👤</button>
            </div>

        </div>
    </header>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { RouterLink } from 'vue-router'

const route = useRoute()

const navItems = [
    { path: '/index', label: '首页' },
    { path: '/question', label: '题库' },
    { path: '/contact', label: '联系' }
]

const isActive = (path) => {
    return route.path === path
}
</script>

<style scoped>
.navbar {
    background: #ffffff;
    border-bottom: 1px solid #e5e7eb;
    position: sticky;
    top: 0;
    z-index: 1000;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.nav-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 64px;
    gap: 48px;
    width: 100%;
}

@media (max-width: 1024px) {
    .nav-container {
        max-width: 100%;
        padding: 0 20px;
        gap: 32px;
    }
}

@media (max-width: 768px) {
    .nav-container {
        padding: 0 16px;
        gap: 24px;
    }

    .nav-menu {
        gap: 24px;
    }
}

.logo-section {
    display: flex;
    align-items: center;
    flex-shrink: 0;
}

.nav-logo {
    font-size: 18px;
    font-weight: 700;
    color: #1f2937;
    letter-spacing: 0.5px;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.3s ease;
    cursor: pointer;
}

.logo-icon {
    font-size: 22px;
    animation: iconBounce 2s ease-in-out infinite;
}

@keyframes iconBounce {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-2px);
    }
}

.nav-logo:hover {
    color: #059669;
    transform: translateX(2px);
}

.nav-menu {
    display: flex;
    list-style: none;
    gap: 40px;
    margin: 0;
    padding: 0;
    flex: 1;
}

.nav-link {
    position: relative;
    color: #6b7280;
    text-decoration: none;
    font-size: 14px;
    font-weight: 500;
    transition: color 0.3s ease;
    padding: 8px 0;
    white-space: nowrap;
}

.nav-link::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: #059669;
    transition: width 0.3s ease;
}

.nav-link:hover {
    color: #1f2937;
}

.nav-link:hover::after {
    width: 100%;
}

.nav-link.active {
    color: #059669;
}

.nav-link.active::after {
    width: 100%;
    background: #059669;
}

.nav-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
}

.search-btn,
.user-btn {
    background: #f3f4f6;
    border: 1px solid #e5e7eb;
    color: #6b7280;
    width: 40px;
    height: 40px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 18px;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}

.search-btn:hover,
.user-btn:hover {
    background: #ecfdf5;
    border-color: #059669;
    color: #059669;
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(5, 150, 105, 0.1);
}

@media (max-width: 768px) {
    .nav-container {
        padding: 0 16px;
        gap: 24px;
        height: 56px;
    }

    .nav-logo {
        font-size: 18px;
    }

    .logo-icon {
        font-size: 20px;
    }

    .nav-menu {
        gap: 20px;
        flex: 1;
        overflow-x: auto;
    }

    .nav-link {
        font-size: 13px;
        padding: 6px 0;
    }

    .search-btn,
    .user-btn {
        width: 36px;
        height: 36px;
        font-size: 16px;
    }
}
</style>