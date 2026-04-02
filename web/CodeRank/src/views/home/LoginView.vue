<template>
    <div class="login-view">
        <div class="login-container">
            <h2 class="login-title">登录 CodeRank</h2>
            <form class="login-form" @submit.prevent="handleLogin">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input id="username" v-model="username" type="text" placeholder="请输入用户名" required
                        class="form-input" />
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input id="password" v-model="password" type="password" placeholder="请输入密码" required
                        class="form-input" />
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary login-btn">登录</button>
                </div>
                <div class="form-links">
                    <a href="#" class="form-link">忘记密码？</a>
                    <span class="divider">|</span>
                    <a href="#" class="form-link">注册账号</a>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')

function handleLogin() {
    // 这里可以添加登录逻辑
    if (!username.value || !password.value) {
        alert('请输入用户名和密码')
        return
    }
    alert(`登录：${username.value}`)
}
</script>

<style scoped>
.login-view {
    min-height: 70vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f7f8fa;
}

.login-container {
    background: #fff;
    padding: 40px 32px 32px 32px;
    border-radius: 16px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
    width: 100%;
    max-width: 380px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.login-title {
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 32px;
    color: #222;
    letter-spacing: 1px;
}

.login-form {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.form-group label {
    font-size: 1rem;
    color: #333;
    font-weight: 500;
}

.form-input {
    padding: 10px 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 1rem;
    background: #fafbfc;
    transition: border 0.2s;
}

.form-input:focus {
    outline: none;
    border-color: #6366f1;
    background: #fff;
}

.form-actions {
    margin-top: 8px;
}

.login-btn {
    width: 100%;
    padding: 12px 0;
    font-size: 1.1rem;
    font-weight: 600;
    border-radius: 8px;
    background: linear-gradient(90deg, #6366f1 0%, #60a5fa 100%);
    color: #fff;
    border: none;
    cursor: pointer;
    transition: background 0.2s;
}

.login-btn:hover {
    background: linear-gradient(90deg, #4f46e5 0%, #2563eb 100%);
}

.form-links {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    margin-top: 10px;
    font-size: 0.97rem;
}

.form-link {
    color: #6366f1;
    text-decoration: none;
    transition: color 0.2s;
}

.form-link:hover {
    color: #2563eb;
}

.divider {
    color: #bbb;
}

@media (max-width: 500px) {
    .login-container {
        padding: 24px 8px 16px 8px;
        max-width: 98vw;
    }

    .login-title {
        font-size: 1.4rem;
    }
}
</style>