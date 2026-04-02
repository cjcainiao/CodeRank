<!-- 主布局组件 -->
<template>
    <div class="MainLayout">
        <a-layout style="height: 400px;">
            <!-- 导航栏 -->
            <a-layout-header>
                <Header></Header>
            </a-layout-header>

            <!-- 内容显示 -->
            <a-layout-content>
                <router-view />
            </a-layout-content>

            <a-layout-footer>
                <Footer />
            </a-layout-footer>
        </a-layout>
    </div>
</template>

<script setup>
import Header from '@/components/global/Header.vue';
import Footer from '@/components/global/Footer.vue';

</script>

<style scoped></style>