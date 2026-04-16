<template>
  <div class="my-issues-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Personal Queue</p>
        <h2 class="opl-page-title">{{ t('page.myIssues') }}</h2>
        <p class="opl-page-lead">{{ t('myIssues.lead') }}</p>
      </div>
      <div class="opl-page-head-actions">
        <el-button @click="loadData">{{ t('common.action.refresh') }}</el-button>
      </div>
    </section>

    <el-card class="my-issues-note-card">
      <div class="my-issues-note-head">
        <div>
          <div class="opl-card-title">{{ t('myIssues.title') }}</div>
          <div class="opl-card-subtitle">{{ t('myIssues.deadlineOrderHint') }}</div>
        </div>
        <div class="my-issues-total">{{ t('issueList.tableCount', { count: total }) }}</div>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div>
          <h3 class="opl-card-title">{{ t('myIssues.filterTitle') }}</h3>
          <p class="opl-card-subtitle">{{ t('myIssues.filterSubtitle') }}</p>
        </div>
      </template>

      <el-form :model="query" class="filter-grid">
        <el-form-item :label="t('common.label.keyword')">
          <el-input v-model="query.keyword" :placeholder="t('issueList.issueNoPlaceholder')" clearable />
        </el-form-item>
        <el-form-item :label="t('common.label.project')">
          <el-select v-model="query.projectId" :placeholder="t('myIssues.allProjects')" clearable filterable>
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.projectName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.issueFunction')">
          <el-select v-model="query.issueFunctionCode" :placeholder="t('issueList.allFunction')" clearable>
            <el-option v-for="item in issueFunctionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.status')">
          <el-select v-model="query.status" :placeholder="t('issueList.allStatus')" clearable>
            <el-option v-for="item in issueStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.priority')">
          <el-select v-model="query.priority" :placeholder="t('issueList.allPriority')" clearable>
            <el-option v-for="item in issuePriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('issueList.overdueOnlyLabel')">
          <el-switch v-model="query.overdueOnly" />
        </el-form-item>
      </el-form>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">{{ t('common.action.applyFilter') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.action.reset') }}</el-button>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="my-issues-table-head">
          <div>
            <h3 class="opl-card-title">{{ t('myIssues.tableTitle') }}</h3>
            <p class="opl-card-subtitle">{{ t('myIssues.tableSubtitle') }}</p>
          </div>
          <div class="my-issues-order-chip">{{ t('myIssues.deadlineOrderHint') }}</div>
        </div>
      </template>

      <el-table :data="list" class="issue-table" :empty-text="t('common.empty.noData')">
        <el-table-column :label="t('issueList.infoColumn')" min-width="300">
          <template #default="{ row }">
            <div class="issue-main-cell">
              <div class="issue-main-top">
                <span class="issue-main-no">{{ row.issueNo }}</span>
                <span v-if="row.oplNo" class="issue-main-opl">OPL {{ row.oplNo }}</span>
              </div>
              <div class="issue-main-title">{{ row.title }}</div>
              <div class="issue-main-meta">
                <span>{{ t('common.label.owner') }} {{ row.ownerName || t('common.empty.unassigned') }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('myIssues.projectColumn')" min-width="160">
          <template #default="{ row }">
            <div class="issue-project-name">{{ row.projectName || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.issueFunction')" width="120">
          <template #default="{ row }">
            <el-tag effect="plain" :type="getIssueFunctionMeta(row.issueFunctionCode).tagType">
              {{ getIssueFunctionMeta(row.issueFunctionCode).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.priority')" width="120">
          <template #default="{ row }">
            <el-tag effect="plain" :type="getIssuePriorityMeta(row.priority).tagType">
              {{ getIssuePriorityMeta(row.priority).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.status')" width="140">
          <template #default="{ row }">
            <el-tag effect="plain" :type="getIssueStatusMeta(row.status).tagType">
              {{ getIssueStatusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.dueAt')" width="200">
          <template #default="{ row }">
            <div class="issue-due-cell">
              <span>{{ row.dueAt || '-' }}</span>
              <el-tag v-if="row.overdue" type="danger" effect="plain">{{ t('issueList.overdueFlag') }}</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.actionLabel')" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id, row.projectId)">{{ t('common.action.viewDetail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          background
          layout="total, prev, pager, next, sizes"
          :page-sizes="[20, 50, 100]"
          :total="total"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { fetchIssuePage, type IssueQuery } from '@/api/issue'
import { fetchProjectAll, type ProjectItem } from '@/api/project'
import type { IssueItem } from '@/types/api'
import {
  getIssueFunctionMeta,
  getIssueFunctionOptions,
  getIssuePriorityMeta,
  getIssuePriorityOptions,
  getIssueStatusMeta,
  getIssueStatusOptions
} from '@/utils/view-mappers'

const router = useRouter()
const { t } = useI18n()

const list = ref<IssueItem[]>([])
const total = ref(0)
const projectOptions = ref<ProjectItem[]>([])

const query = reactive<IssueQuery & { status?: string }>({
  current: 1,
  size: 50,
  keyword: '',
  projectId: undefined,
  issueFunctionCode: '',
  priority: '',
  status: '',
  overdueOnly: false,
  currentUserRelated: true,
  sortByDueAt: true
})

const issueStatusOptions = computed(() => getIssueStatusOptions())
const issuePriorityOptions = computed(() => getIssuePriorityOptions())
const issueFunctionOptions = computed(() => getIssueFunctionOptions())

async function loadOptions() {
  const { data } = await fetchProjectAll()
  projectOptions.value = data
}

async function loadData() {
  const { data } = await fetchIssuePage({
    current: query.current,
    size: query.size,
    keyword: query.keyword,
    projectId: query.projectId,
    issueFunctionCode: query.issueFunctionCode || undefined,
    priority: query.priority || undefined,
    statusList: query.status ? [query.status] : undefined,
    overdueOnly: query.overdueOnly,
    currentUserRelated: true,
    sortByDueAt: true
  })
  list.value = data.records
  total.value = data.total
}

function handleSearch() {
  query.current = 1
  loadData()
}

function resetQuery() {
  Object.assign(query, {
    current: 1,
    size: 50,
    keyword: '',
    projectId: undefined,
    issueFunctionCode: '',
    priority: '',
    status: '',
    overdueOnly: false,
    currentUserRelated: true,
    sortByDueAt: true
  })
  loadData()
}

function handleSizeChange() {
  query.current = 1
  loadData()
}

function goDetail(issueId: number, projectId?: number) {
  router.push({
    path: `/issues/${issueId}`,
    query: {
      source: 'my-issues',
      ...(projectId ? { projectId: String(projectId) } : {})
    }
  })
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadData()])
})
</script>

<style scoped>
.my-issues-page {
  display: grid;
  gap: 20px;
}

.my-issues-note-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.my-issues-note-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.my-issues-total {
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(244, 247, 249, 0.96);
  color: #53657d;
  font-weight: 700;
}

.my-issues-table-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.my-issues-order-chip {
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(207, 214, 223, 0.72);
  color: #60738d;
  font-size: 13px;
  font-weight: 700;
  background: rgba(248, 250, 252, 0.96);
}

.issue-main-cell {
  display: grid;
  gap: 8px;
}

.issue-main-top {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #8ea0b9;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.issue-main-no {
  font-weight: 700;
}

.issue-main-opl {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(233, 239, 245, 0.8);
}

.issue-main-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

.issue-main-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #66758a;
  font-size: 13px;
}

.issue-project-name {
  font-weight: 700;
  color: #243447;
}

.issue-due-cell {
  display: grid;
  gap: 8px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 1200px) {
  .my-issues-note-head,
  .my-issues-table-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-footer {
    justify-content: flex-start;
  }
}
</style>
