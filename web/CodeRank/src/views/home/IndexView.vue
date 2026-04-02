<!-- 首页 -->
<template>
    <div class="index-view">
        <!-- Hero 区域 -->
        <section class="hero">
            <div class="hero-container">
                <h1 class="hero-title">CodeRank</h1>
                <p class="hero-subtitle">
                    <span class="typing-text">{{ typingText }}</span>
                    <span class="typing-cursor"></span>
                </p>
                <p class="hero-description">
                    提供高质量编程题目、实时代码评测、智能反馈系统
                </p>
                <div class="hero-buttons">
                    <button class="btn btn-primary">开始评测</button>
                    <button class="btn btn-secondary">浏览题目</button>
                </div>
            </div>
        </section>

        <!-- 功能介绍 -->
        <section class="features">
            <div class="container">
                <h2 class="section-title">核心功能</h2>
                <div class="features-grid">
                    <div class="feature-card">
                        <div class="icon">
                            <span>📝</span>
                        </div>
                        <h3>丰富题库</h3>
                        <p>涵盖数据结构、算法、Web开发等多个领域的精选题目</p>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <span>⚡</span>
                        </div>
                        <h3>实时评测</h3>
                        <p>支持多种编程语言，秒级反馈，给您最快的代码验证体验</p>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <span>🎯</span>
                        </div>
                        <h3>智能反馈</h3>
                        <p>详细的错误提示和优化建议，帮助您持续进步</p>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <span>🏆</span>
                        </div>
                        <h3>排行榜</h3>
                        <p>竞争性排行榜，见证您的成长，与编程高手比肩</p>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <span>👥</span>
                        </div>
                        <h3>社区交流</h3>
                        <p>与全球程序员互动，分享解题思路和编程经验</p>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <span>📊</span>
                        </div>
                        <h3>数据分析</h3>
                        <p>完整的学习统计，可视化展示您的进度和成就</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- 统计数据 -->
        <section class="stats">
            <div class="container">
                <div class="stats-grid">
                    <div class="stat-item">
                        <div class="stat-number">2500+</div>
                        <div class="stat-label">编程题目</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">50K+</div>
                        <div class="stat-label">活跃用户</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">100M+</div>
                        <div class="stat-label">月均代码评测</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">99.9%</div>
                        <div class="stat-label">系统可用性</div>
                    </div>
                </div>
            </div>
        </section>

        <!-- CTA 区域 -->
        <section class="cta">
            <div class="container">
                <h2>准备好开始了吗？</h2>
                <p>加入数万名程序员，在 CodeRank 上提升编程实力</p>
                <button class="btn btn-primary btn-large">免费开始</button>
            </div>
        </section>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const stats = ref([
    { number: '2500+', label: '编程题目' },
    { number: '50K+', label: '活跃用户' },
    { number: '100M+', label: '月均代码评测' },
    { number: '99.9%', label: '系统可用性' }
]);

const typingText = ref('');
const fullText = '专业的在线编程评测系统';

/**
 * 启动打字动画效果
 * 逐字符打出文本，然后逐字符删除，循环进行
 */
const startTypingAnimation = () => {
    let currentIndex = 0;
    let isDeleting = false;
    const typingSpeed = 100; // 打字速度（毫秒）
    const deletingSpeed = 100; // 删除速度（毫秒）
    const pauseTime = 1000; // 打字完成后暂停时间

    const animate = () => {
        if (!isDeleting) {
            // 打字阶段：逐字符添加
            if (currentIndex < fullText.length) {
                typingText.value += fullText[currentIndex];
                currentIndex++;
                setTimeout(animate, typingSpeed);
            } else {
                // 打字完成，暂停后开始删除
                isDeleting = true;
                setTimeout(animate, pauseTime);
            }
        } else {
            // 删除阶段：逐字符删除
            if (currentIndex > 0) {
                typingText.value = fullText.substring(0, currentIndex - 1);
                currentIndex--;
                setTimeout(animate, deletingSpeed);
            } else {
                // 删除完成，重新开始打字
                isDeleting = false;
                setTimeout(animate, 500);
            }
        }
    };

    animate();
};

// 钩子函数
onMounted(() => {
    startTypingAnimation();
});
</script>

<style scoped>
.index-view {
    width: 100%;
    overflow: hidden;
}

.container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 24px;
}

/* Hero 区域 */
.hero {
    background: linear-gradient(135deg, #059669 0%, #047857 100%);
    color: white;
    padding: 100px 24px;
    text-align: center;
    animation: slideDown 0.8s ease-out;
}

.hero-container {
    max-width: 800px;
    margin: 0 auto;
}

.hero-title {
    font-size: 72px;
    font-weight: 800;
    margin: 0 0 20px 0;
    letter-spacing: -2px;
}

.hero-subtitle {
    font-size: 32px;
    margin: 0 0 20px 0;
    font-weight: 300;
    opacity: 0.95;
    min-height: 45px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
}

.typing-text {
    display: inline-block;
}

.typing-cursor {
    display: inline-block;
    width: 2px;
    height: 32px;
    background-color: white;
    animation: blink 0.7s infinite;
    margin-left: 4px;
}

@keyframes blink {

    0%,
    50% {
        opacity: 1;
    }

    51%,
    100% {
        opacity: 0;
    }
}

.hero-description {
    font-size: 18px;
    margin: 0 0 40px 0;
    opacity: 0.9;
    line-height: 1.6;
}

.hero-buttons {
    display: flex;
    gap: 20px;
    justify-content: center;
    flex-wrap: wrap;
}

/* 按钮样式 */
.btn {
    padding: 14px 32px;
    font-size: 16px;
    font-weight: 600;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-primary {
    background-color: white;
    color: #059669;
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.btn-secondary {
    background-color: rgba(255, 255, 255, 0.2);
    color: white;
    border: 2px solid white;
}

.btn-secondary:hover {
    background-color: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
}

.btn-large {
    padding: 18px 48px;
    font-size: 18px;
}

/* 功能区域 */
.features {
    padding: 80px 24px;
    background-color: #f8f9fa;
}

.section-title {
    font-size: 40px;
    font-weight: 700;
    text-align: center;
    margin: 0 0 60px 0;
    color: #1f2937;
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
    max-width: 1400px;
    margin: 0 auto;
}

.feature-card {
    background: white;
    padding: 40px 30px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 32px rgba(5, 150, 105, 0.12);
    border-top: 4px solid #059669;
}

.feature-card .icon {
    font-size: 48px;
    margin: 0 0 20px 0;
    display: inline-block;
}

.feature-card h3 {
    font-size: 20px;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 12px 0;
}

.feature-card p {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
    margin: 0;
}

/* 统计区域 */
.stats {
    padding: 80px 24px;
    background: white;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 40px;
    max-width: 1400px;
    margin: 0 auto;
    padding: 60px 0;
    border-top: 2px solid #e5e7eb;
    border-bottom: 2px solid #e5e7eb;
}

.stat-item {
    text-align: center;
}

.stat-number {
    font-size: 52px;
    font-weight: 800;
    color: #059669;
    margin: 0 0 10px 0;
}

.stat-label {
    font-size: 16px;
    color: #6b7280;
    font-weight: 500;
}

/* CTA 区域 */
.cta {
    padding: 80px 24px;
    background: linear-gradient(135deg, #f8f9fa 0%, #f3f4f6 100%);
    text-align: center;
}

.cta h2 {
    font-size: 44px;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 20px 0;
}

.cta p {
    font-size: 18px;
    color: #6b7280;
    margin: 0 0 40px 0;
    line-height: 1.6;
}

/* 动画 */
@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .hero {
        padding: 60px 24px;
    }

    .hero-title {
        font-size: 48px;
    }

    .hero-subtitle {
        font-size: 24px;
    }

    .hero-description {
        font-size: 16px;
    }

    .hero-buttons {
        flex-direction: column;
    }

    .section-title {
        font-size: 32px;
        margin-bottom: 40px;
    }

    .features-grid {
        grid-template-columns: 1fr;
        gap: 20px;
    }

    .feature-card {
        padding: 30px 20px;
    }

    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
        padding: 40px 0;
    }

    .stat-number {
        font-size: 36px;
    }

    .cta h2 {
        font-size: 32px;
    }

    .cta p {
        font-size: 16px;
    }

    .btn-large {
        width: 100%;
    }
}
</style>