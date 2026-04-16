import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import th from 'element-plus/es/locale/lang/th'
import ja from 'element-plus/es/locale/lang/ja'
import ko from 'element-plus/es/locale/lang/ko'
import hi from 'element-plus/es/locale/lang/hi'

export const LOCALE_STORAGE_KEY = 'opl_locale'

export const supportedLocales = ['zh-CN', 'en-US', 'th-TH', 'ja-JP', 'ko-KR', 'hi-IN'] as const

export type AppLocale = (typeof supportedLocales)[number]

export const defaultLocale: AppLocale = 'zh-CN'

export const localeLabelMap: Record<AppLocale, string> = {
  'zh-CN': '中文',
  'en-US': 'English',
  'th-TH': 'ไทย',
  'ja-JP': '日本語',
  'ko-KR': '한국어',
  'hi-IN': 'हिंदी'
}

export const elementLocaleMap = {
  'zh-CN': zhCn,
  'en-US': en,
  'th-TH': th,
  'ja-JP': ja,
  'ko-KR': ko,
  'hi-IN': hi
} as const

export function isSupportedLocale(value?: string): value is AppLocale {
  return Boolean(value && supportedLocales.includes(value as AppLocale))
}

export function getSavedLocale(): AppLocale {
  if (typeof window === 'undefined') {
    return defaultLocale
  }

  const saved = window.localStorage.getItem(LOCALE_STORAGE_KEY)
  if (saved && isSupportedLocale(saved)) {
    return saved
  }

  return defaultLocale
}

export function persistLocale(locale: AppLocale) {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(LOCALE_STORAGE_KEY, locale)
}
