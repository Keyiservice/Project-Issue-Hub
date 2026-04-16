import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface DictTypeItem {
  id: number
  dictCode: string
  dictName: string
  status: string
  remark?: string
  itemCount: number
  createdAt?: string
  updatedAt?: string
}

export interface DictItem {
  id: number
  dictTypeId: number
  itemLabel: string
  itemValue: string
  itemColor?: string
  sortNo: number
  isDefault: boolean
  status: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface DictTypeDetail extends Omit<DictTypeItem, 'itemCount'> {
  items: DictItem[]
}

export interface DictTypePayload {
  dictCode: string
  dictName: string
  status: string
  remark?: string
}

export interface DictItemPayload {
  itemLabel: string
  itemValue: string
  itemColor?: string
  sortNo: number
  isDefault: boolean
  status: string
  remark?: string
}

export function fetchDictTypes() {
  return request.get<unknown, Result<DictTypeItem[]>>('/dicts/types')
}

export function fetchDictTypeDetail(typeId: number) {
  return request.get<unknown, Result<DictTypeDetail>>(`/dicts/types/${typeId}`)
}

export function createDictType(data: DictTypePayload) {
  return request.post<unknown, Result<number>>('/dicts/types', data)
}

export function updateDictType(typeId: number, data: DictTypePayload) {
  return request.put<unknown, Result<null>>(`/dicts/types/${typeId}`, data)
}

export function deleteDictType(typeId: number) {
  return request.delete<unknown, Result<null>>(`/dicts/types/${typeId}`)
}

export function createDictItem(typeId: number, data: DictItemPayload) {
  return request.post<unknown, Result<number>>(`/dicts/types/${typeId}/items`, data)
}

export function updateDictItem(itemId: number, data: DictItemPayload) {
  return request.put<unknown, Result<null>>(`/dicts/items/${itemId}`, data)
}

export function deleteDictItem(itemId: number) {
  return request.delete<unknown, Result<null>>(`/dicts/items/${itemId}`)
}

export function fetchDictOptions(dictCode: string) {
  return request.get<unknown, Result<DictItem[]>>(`/dicts/options/${dictCode}`)
}
