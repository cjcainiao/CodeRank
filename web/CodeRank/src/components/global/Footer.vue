<!-- 页脚组件 -->
<template>
    <footer class="footer">
        <div class="footer-content">
            <!-- 左侧：Logo 和描述 -->
            <div class="footer-section">
                <h3 class="footer-logo">
                    <span class="logo-icon">⚡</span>CodeRank
                </h3>
                <p class="footer-description">
                    专业的在线编程评测系统，帮助编程爱好者提升技能
                </p>
                <div class="social-links">
                    <a href="#" class="social-icon" title="GitHub">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                            <path
                                d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.6.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v 3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
                        </svg>
                    </a>
                    <a href="#" class="social-icon" title="微博">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                            <path
                                d="M10.5 9c1.933 0 3.5-1.567 3.5-3.5S12.433 2 10.5 2 7 3.567 7 5.5 8.567 9 10.5 9zm7 0c1.933 0 3.5-1.567 3.5-3.5S19.433 2 17.5 2 14 3.567 14 5.5 15.567 9 17.5 9zM10.5 16c1.933 0 3.5-1.567 3.5-3.5S12.433 9 10.5 9 7 10.567 7 12.5 8.567 16 10.5 16zm7 0c1.933 0 3.5-1.567 3.5-3.5s-1.567-3.5-3.5-3.5-3.5 1.567-3.5 3.5 1.567 3.5 3.5 3.5z" />
                        </svg>
                    </a>
                    <a href="#" class="social-icon" title="邮箱">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <rect x="2" y="4" width="20" height="16" rx="2" />
                            <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                        </svg>
                    </a>
                </div>
            </div>

            <!-- 中间：快速链接 -->
            <div class="footer-section">
                <h4 class="footer-title">快速导航</h4>
                <ul class="footer-links">
                    <li><a href="#/">首页</a></li>
                    <li><a href="#/problems">题库</a></li>
                    <li><a href="#/rank">排行榜</a></li>
                    <li><a href="#/community">社区</a></li>
                </ul>
            </div>

            <!-- 中间：产品 -->
            <div class="footer-section">
                <h4 class="footer-title">产品</h4>
                <ul class="footer-links">
                    <li><a href="#/">关于我们</a></li>
                    <li><a href="#/">使用帮助</a></li>
                    <li><a href="#/">API 文档</a></li>
                    <li><a href="#/">问题反馈</a></li>
                </ul>
            </div>

            <!-- 右侧：法律和其他 -->
            <div class="footer-section">
                <h4 class="footer-title">法律</h4>
                <ul class="footer-links">
                    <li><a href="#/">服务条款</a></li>
                    <li><a href="#/">隐私政策</a></li>
                    <li><a href="#/">Cookie 设置</a></li>
                    <li><a href="#/">联系我们</a></li>
                </ul>
            </div>
        </div>

        <!-- 底部版权 -->
        <div class="footer-bottom">
            <p>&copy; {{ currentYear }} CodeRank. All rights reserved.</p>
            <p>Made with <span class="heart">♥</span> by CodeRank Team</p>
        </div>
    </footer>
</template>

<script setup>
import { ref } from 'vue'

const currentYear = ref(new Date().getFullYear())
</script>

<style scoped>
.footer {
    background: linear-gradient(135deg, #f5f5f5 0%, #efefef 100%);
    border-top: 1px solid #e0e0e0;
    margin-top: 80px;
}

.footer-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 60px 24px 40px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 40px;
}

.footer-section {
    animation: fadeInUp 0.6s ease-out;
}

.footer-logo {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 12px;
    color: #059669;
    display: flex;
    align-items: center;
    gap: 6px;
}

.logo-icon {
    font-size: 24px;
    display: inline-block;
    animation: float 3s ease-in-out infinite;
}

.footer-description {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin-bottom: 16px;
}

.social-links {
    display: flex;
    gap: 12px;
}

.social-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    color: #059669;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid #e0e0e0;
}

.social-icon:hover {
    background: #059669;
    color: #fff;
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(5, 150, 105, 0.2);
}

.footer-title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #1a1a1a;
}

.footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
}

.footer-links li {
    margin-bottom: 10px;
}

.footer-links a {
    color: #666;
    text-decoration: none;
    font-size: 14px;
    transition: all 0.3s ease;
    display: inline-block;
    position: relative;
}

.footer-links a::before {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: #059669;
    transition: width 0.3s ease;
}

.footer-links a:hover {
    color: #059669;
}

.footer-links a:hover::before {
    width: 100%;
}

.footer-bottom {
    border-top: 1px solid #e0e0e0;
    padding: 24px;
    text-align: center;
    color: #999;
    font-size: 13px;
}

.footer-bottom p {
    margin: 4px 0;
}

.heart {
    color: #ef4444;
    animation: heartbeat 1.5s ease-in-out infinite;
    display: inline-block;
}

@keyframes float {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-8px);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes heartbeat {

    0%,
    100% {
        transform: scale(1);
    }

    25% {
        transform: scale(1.1);
    }

    50% {
        transform: scale(1);
    }
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .footer-content {
        grid-template-columns: repeat(2, 1fr);
        gap: 30px;
    }
}

@media (max-width: 640px) {
    .footer-content {
        grid-template-columns: 1fr;
        gap: 30px;
        padding: 40px 16px 30px;
    }

    .footer-logo {
        font-size: 18px;
    }

    .footer-title {
        font-size: 14px;
    }

    .social-links {
        gap: 10px;
    }

    .social-icon {
        width: 36px;
        height: 36px;
    }
}
</style>