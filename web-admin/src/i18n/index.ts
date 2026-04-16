import { createI18n } from 'vue-i18n'
import { defaultLocale, getSavedLocale } from './locales'
import { messages } from './messages'

const i18n = createI18n({
  legacy: false,
  locale: getSavedLocale(),
  fallbackLocale: defaultLocale,
  messages
})

export default i18n
