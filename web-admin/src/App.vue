<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { elementLocaleMap } from '@/i18n/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()

const elementLocale = computed(() => elementLocaleMap[localeStore.currentLocale])

onMounted(() => {
  document.documentElement.lang = localeStore.currentLocale
})
</script>
