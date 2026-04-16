import { defineStore } from 'pinia'
import i18n from '@/i18n'
import { defaultLocale, isSupportedLocale, LOCALE_STORAGE_KEY, persistLocale, type AppLocale } from '@/i18n/locales'

function resolveInitialLocale(): AppLocale {
  const saved = localStorage.getItem(LOCALE_STORAGE_KEY)
  if (saved && isSupportedLocale(saved)) {
    return saved
  }
  return defaultLocale
}

export const useLocaleStore = defineStore('locale', {
  state: () => ({
    currentLocale: resolveInitialLocale() as AppLocale
  }),
  actions: {
    setLocale(locale: AppLocale) {
      this.currentLocale = locale
      i18n.global.locale.value = locale
      persistLocale(locale)
      document.documentElement.lang = locale
    }
  }
})
