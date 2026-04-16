<template>
  <div class="dict-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Dictionary Console</p>
        <h2 class="opl-page-title">字典配置</h2>
        <p class="opl-page-lead">管理员直接在页面里维护分类、来源、责任部门等字典内容，业务页会自动联动。</p>
      </div>
      <div class="opl-page-head-actions">
        <el-button @click="loadTypes">刷新</el-button>
        <el-button type="primary" @click="openTypeDialog()">新增字典类型</el-button>
      </div>
    </section>

    <section class="dict-layout">
      <el-card class="type-card">
        <template #header>
          <div class="card-head">
            <div>
              <h3 class="opl-card-title">字典类型</h3>
              <p class="opl-card-subtitle">按字典编码分组维护，类型删除会连同字典项一起删除。</p>
            </div>
          </div>
        </template>

        <div class="type-list">
          <div
            v-for="item in typeList"
            :key="item.id"
            class="type-item"
            :class="{ active: item.id === activeTypeId }"
            @click="selectType(item.id)"
          >
            <div class="type-item-top">
              <div>
                <div class="type-code">{{ item.dictCode }}</div>
                <div class="type-name">{{ item.dictName }}</div>
              </div>
              <el-tag :type="item.status === 'ENABLED' ? 'success' : 'info'" effect="plain">
                {{ item.status === 'ENABLED' ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="type-item-bottom">
              <span>{{ item.itemCount }} 个字典项</span>
              <span>{{ item.remark || '未填写说明' }}</span>
            </div>
            <div class="type-item-actions">
              <el-button link type="primary" @click.stop="openTypeDialog(item)">编辑</el-button>
              <el-button link type="danger" @click.stop="handleDeleteType(item)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="!typeList.length" description="当前还没有字典类型" />
        </div>
      </el-card>

      <el-card class="item-card">
        <template #header>
          <div class="card-head">
            <div>
              <h3 class="opl-card-title">{{ activeTypeDetail ? `${activeTypeDetail.dictName} 字典项` : '字典项' }}</h3>
              <p class="opl-card-subtitle">
                <template v-if="activeTypeDetail">
                  当前编码 {{ activeTypeDetail.dictCode }}，仅启用状态的字典项会出现在业务页面。
                </template>
                <template v-else>请先在左侧选择一个字典类型。</template>
              </p>
            </div>
            <el-button type="primary" :disabled="!activeTypeDetail" @click="openItemDialog()">新增字典项</el-button>
          </div>
        </template>

        <el-table v-if="activeTypeDetail" :data="activeTypeDetail.items" empty-text="当前字典下还没有条目">
          <el-table-column label="显示名称" min-width="160">
            <template #default="{ row }">
              <div class="item-label-cell">
                <span class="item-color-dot" :style="{ backgroundColor: row.itemColor || '#94a3b8' }"></span>
                <span>{{ row.itemLabel }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="字典值" min-width="150" prop="itemValue" />
          <el-table-column label="排序" width="80" prop="sortNo" />
          <el-table-column label="默认" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isDefault" effect="plain">默认</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" effect="plain">
                {{ row.status === 'ENABLED' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="{ row }">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openItemDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="请选择左侧字典类型" />
      </el-card>
    </section>

    <el-dialog v-model="typeDialogVisible" :title="typeDialogMode === 'create' ? '新增字典类型' : '编辑字典类型'" width="560px">
      <el-form :model="typeForm" label-position="top">
        <el-form-item label="字典编码">
          <el-input v-model="typeForm.dictCode" placeholder="例如 ISSUE_CATEGORY" />
        </el-form-item>
        <el-form-item label="字典名称">
          <el-input v-model="typeForm.dictName" placeholder="例如 问题分类" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="typeForm.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="可选说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingType" @click="submitType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="itemDialogMode === 'create' ? '新增字典项' : '编辑字典项'" width="620px">
      <el-form :model="itemForm" label-position="top" class="dialog-grid">
        <el-form-item label="显示名称">
          <el-input v-model="itemForm.itemLabel" placeholder="例如 机械" />
        </el-form-item>
        <el-form-item label="字典值">
          <el-input v-model="itemForm.itemValue" placeholder="例如 MECHANICAL" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-input v-model="itemForm.itemColor" placeholder="例如 #2563eb" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemForm.sortNo" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="默认值">
          <el-switch v-model="itemForm.isDefault" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="itemForm.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" class="full-span">
          <el-input v-model="itemForm.remark" type="textarea" :rows="3" placeholder="可选说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingItem" @click="submitItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  fetchDictTypeDetail,
  fetchDictTypes,
  updateDictItem,
  updateDictType,
  type DictItem,
  type DictItemPayload,
  type DictTypeDetail,
  type DictTypeItem,
  type DictTypePayload
} from '@/api/dict'

const typeList = ref<DictTypeItem[]>([])
const activeTypeId = ref<number>()
const activeTypeDetail = ref<DictTypeDetail>()
const typeDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const typeDialogMode = ref<'create' | 'edit'>('create')
const itemDialogMode = ref<'create' | 'edit'>('create')
const editingTypeId = ref<number>()
const editingItemId = ref<number>()
const savingType = ref(false)
const savingItem = ref(false)

const typeForm = reactive<DictTypePayload>({
  dictCode: '',
  dictName: '',
  status: 'ENABLED',
  remark: ''
})

const itemForm = reactive<DictItemPayload>({
  itemLabel: '',
  itemValue: '',
  itemColor: '',
  sortNo: 10,
  isDefault: false,
  status: 'ENABLED',
  remark: ''
})

async function loadTypes(selectId?: number) {
  const { data } = await fetchDictTypes()
  typeList.value = data
  const nextId = selectId ?? activeTypeId.value ?? data[0]?.id
  if (nextId) {
    await selectType(nextId)
  } else {
    activeTypeId.value = undefined
    activeTypeDetail.value = undefined
  }
}

async function selectType(typeId: number) {
  activeTypeId.value = typeId
  const { data } = await fetchDictTypeDetail(typeId)
  activeTypeDetail.value = data
}

function resetTypeForm() {
  Object.assign(typeForm, {
    dictCode: '',
    dictName: '',
    status: 'ENABLED',
    remark: ''
  })
}

function resetItemForm() {
  Object.assign(itemForm, {
    itemLabel: '',
    itemValue: '',
    itemColor: '',
    sortNo: activeTypeDetail.value ? activeTypeDetail.value.items.length * 10 + 10 : 10,
    isDefault: false,
    status: 'ENABLED',
    remark: ''
  })
}

function openTypeDialog(item?: DictTypeItem) {
  if (item) {
    typeDialogMode.value = 'edit'
    editingTypeId.value = item.id
    Object.assign(typeForm, {
      dictCode: item.dictCode,
      dictName: item.dictName,
      status: item.status,
      remark: item.remark || ''
    })
  } else {
    typeDialogMode.value = 'create'
    editingTypeId.value = undefined
    resetTypeForm()
  }
  typeDialogVisible.value = true
}

function openItemDialog(item?: DictItem) {
  if (!activeTypeId.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  if (item) {
    itemDialogMode.value = 'edit'
    editingItemId.value = item.id
    Object.assign(itemForm, {
      itemLabel: item.itemLabel,
      itemValue: item.itemValue,
      itemColor: item.itemColor || '',
      sortNo: item.sortNo,
      isDefault: item.isDefault,
      status: item.status,
      remark: item.remark || ''
    })
  } else {
    itemDialogMode.value = 'create'
    editingItemId.value = undefined
    resetItemForm()
  }
  itemDialogVisible.value = true
}

async function submitType() {
  if (!typeForm.dictCode.trim() || !typeForm.dictName.trim()) {
    ElMessage.warning('请填写字典编码和字典名称')
    return
  }
  savingType.value = true
  try {
    if (typeDialogMode.value === 'create') {
      const { data } = await createDictType({
        dictCode: typeForm.dictCode.trim(),
        dictName: typeForm.dictName.trim(),
        status: typeForm.status,
        remark: typeForm.remark?.trim() || undefined
      })
      ElMessage.success('字典类型已创建')
      typeDialogVisible.value = false
      await loadTypes(data)
      return
    }
    if (!editingTypeId.value) {
      return
    }
    await updateDictType(editingTypeId.value, {
      dictCode: typeForm.dictCode.trim(),
      dictName: typeForm.dictName.trim(),
      status: typeForm.status,
      remark: typeForm.remark?.trim() || undefined
    })
    ElMessage.success('字典类型已更新')
    typeDialogVisible.value = false
    await loadTypes(editingTypeId.value)
  } finally {
    savingType.value = false
  }
}

async function submitItem() {
  if (!activeTypeId.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  if (!itemForm.itemLabel.trim() || !itemForm.itemValue.trim()) {
    ElMessage.warning('请填写显示名称和字典值')
    return
  }
  savingItem.value = true
  try {
    const payload: DictItemPayload = {
      itemLabel: itemForm.itemLabel.trim(),
      itemValue: itemForm.itemValue.trim(),
      itemColor: itemForm.itemColor?.trim() || undefined,
      sortNo: itemForm.sortNo,
      isDefault: itemForm.isDefault,
      status: itemForm.status,
      remark: itemForm.remark?.trim() || undefined
    }
    if (itemDialogMode.value === 'create') {
      await createDictItem(activeTypeId.value, payload)
      ElMessage.success('字典项已创建')
    } else if (editingItemId.value) {
      await updateDictItem(editingItemId.value, payload)
      ElMessage.success('字典项已更新')
    }
    itemDialogVisible.value = false
    await selectType(activeTypeId.value)
    await loadTypes(activeTypeId.value)
  } finally {
    savingItem.value = false
  }
}

async function handleDeleteType(item: DictTypeItem) {
  await ElMessageBox.confirm(
    `确认删除字典类型 ${item.dictName} 吗？该类型下的字典项会一起删除。`,
    '删除字典类型',
    { type: 'warning' }
  )
  await deleteDictType(item.id)
  ElMessage.success('字典类型已删除')
  const fallback = typeList.value.find((candidate) => candidate.id !== item.id)?.id
  await loadTypes(fallback)
}

async function handleDeleteItem(item: DictItem) {
  await ElMessageBox.confirm(
    `确认删除字典项 ${item.itemLabel} 吗？`,
    '删除字典项',
    { type: 'warning' }
  )
  await deleteDictItem(item.id)
  ElMessage.success('字典项已删除')
  if (activeTypeId.value) {
    await selectType(activeTypeId.value)
    await loadTypes(activeTypeId.value)
  }
}

onMounted(loadTypes)
</script>

<style scoped>
.dict-page {
  display: grid;
  gap: 16px;
}

.dict-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.type-list {
  display: grid;
  gap: 12px;
}

.type-item {
  display: grid;
  gap: 10px;
  width: 100%;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(24, 33, 43, 0.08);
  background: rgba(255, 255, 255, 0.84);
  text-align: left;
  cursor: pointer;
  transition: 0.2s ease;
}

.type-item:hover,
.type-item.active {
  border-color: rgba(203, 93, 31, 0.28);
  box-shadow: 0 14px 30px rgba(24, 33, 43, 0.08);
  transform: translateY(-1px);
}

.type-item-top,
.type-item-bottom,
.type-item-actions {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.type-item-bottom {
  font-size: 12px;
  color: var(--opl-muted);
  flex-wrap: wrap;
}

.type-item-actions {
  justify-content: flex-end;
}

.type-code {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.type-name {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--opl-text);
}

.item-label-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-color-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex-shrink: 0;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-span {
  grid-column: 1 / -1;
}

@media (max-width: 1180px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
