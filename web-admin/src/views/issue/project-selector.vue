<template>
  <div class="selector-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">{{ t('layout.projectFirst') }}</p>
        <h2 class="opl-page-title">{{ t('projectSelector.title') }}</h2>
        <p class="opl-page-lead">{{ t('projectSelector.lead') }}</p>
      </div>
    </section>

    <el-card>
      <template #header>
        <div>
          <h3 class="opl-card-title">{{ t('projectSelector.filterTitle') }}</h3>
          <p class="opl-card-subtitle">{{ t('projectSelector.filterSubtitle') }}</p>
        </div>
      </template>

      <el-form :model="query" class="filter-grid">
        <el-form-item :label="t('common.label.keyword')">
          <el-input v-model="query.keyword" :placeholder="`${t('common.label.project')} / ${t('common.label.customer')}`" clearable />
        </el-form-item>
        <el-form-item :label="t('common.label.status')">
          <el-select v-model="query.status" :placeholder="t('issueList.allStatus')" clearable>
            <el-option v-for="item in projectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">{{ t('common.action.applyFilter') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.action.reset') }}</el-button>
      </div>
    </el-card>

    <div v-if="list.length" class="project-grid">
      <el-card v-for="item in list" :key="item.id" class="project-card">
        <div class="project-card-head">
          <div>
            <div class="project-no">{{ item.projectNo }}</div>
            <h3 class="project-name">{{ item.projectName }}</h3>
          </div>
          <el-tag :type="getProjectStatusMeta(item.status).tagType">
            {{ getProjectStatusMeta(item.status).label }}
          </el-tag>
        </div>

        <p class="project-desc">{{ item.customerName }} / {{ t('common.label.projectManager') }} {{ item.projectManagerName || '-' }}</p>

        <div class="opl-inline-meta">
          <span><strong>{{ t('common.label.startDate') }}:</strong>{{ item.startDate || '-' }}</span>
          <span><strong>{{ t('common.label.plannedEndDate') }}:</strong>{{ item.plannedEndDate || '-' }}</span>
        </div>

        <div class="project-actions">
          <el-button type="primary" @click="enterIssueLibrary(item)">{{ t('projectSelector.enterIssueLibrary') }}</el-button>
          <el-button plain @click="enterProjectManage">{{ t('projectSelector.openProject') }}</el-button>
        </div>
      </el-card>
    </div>
    <el-card v-else>
      <div class="opl-empty">{{ t('projectSelector.empty') }}</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { fetchProjectPage, type ProjectItem } from '@/api/project'
import { getProjectStatusMeta, getProjectStatusOptions } from '@/utils/view-mappers'

const router = useRouter()
const { t } = useI18n()
const list = ref<ProjectItem[]>([])

const query = reactive({
  current: 1,
  size: 100,
  keyword: '',
  status: ''
})

const projectStatusOptions = computed(() => getProjectStatusOptions())

async function loadData() {
  const { data } = await fetchProjectPage(query)
  list.value = data.records
}

function handleSearch() {
  query.current = 1
  loadData()
}

function resetQuery() {
  Object.assign(query, {
    current: 1,
    size: 100,
    keyword: '',
    status: ''
  })
  loadData()
}

function enterIssueLibrary(project: ProjectItem) {
  router.push({
    path: '/issues',
    query: {
      projectId: String(project.id)
    }
  })
}

function enterProjectManage() {
  router.push('/projects')
}

onMounted(loadData)
</script>

<style scoped>
.selector-page {
  display: grid;
  gap: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.project-card {
  min-height: 240px;
}

.project-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.project-no {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.project-name {
  margin: 10px 0 0;
  font-size: 24px;
  line-height: 1.2;
  color: var(--opl-text);
}

.project-desc {
  margin: 14px 0;
  color: var(--opl-muted);
  line-height: 1.8;
}

.project-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 18px;
}

@media (max-width: 960px) {
  .filter-grid,
  .project-grid {
    grid-template-columns: 1fr;
  }
}
</style>
