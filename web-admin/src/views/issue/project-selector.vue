<template>
  <div class="selector-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Project First</p>
        <h2 class="opl-page-title">先选项目，再进入该项目的问题库。</h2>
        <p class="opl-page-lead">
          每个项目都应该有自己的问题池。这样管理时不会把不同客户、不同交付阶段的问题混在一起，责任、优先级和节奏都更清楚。
        </p>
      </div>
    </section>

    <el-card>
      <template #header>
        <div>
          <h3 class="opl-card-title">项目筛选</h3>
          <p class="opl-card-subtitle">先锁定项目，再进入该项目的问题清单库。</p>
        </div>
      </template>

      <el-form :model="query" class="filter-grid">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="项目编号 / 名称 / 客户" clearable />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option v-for="item in projectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">应用筛选</el-button>
        <el-button @click="resetQuery">重置</el-button>
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

        <p class="project-desc">{{ item.customerName }} · 项目经理 {{ item.projectManagerName }}</p>

        <div class="opl-inline-meta">
          <span><strong>开始：</strong>{{ item.startDate || '-' }}</span>
          <span><strong>计划结束：</strong>{{ item.plannedEndDate || '-' }}</span>
        </div>

        <div class="project-actions">
          <el-button type="primary" @click="enterIssueLibrary(item)">进入问题库</el-button>
          <el-button plain @click="enterProjectManage">查看项目</el-button>
        </div>
      </el-card>
    </div>
    <el-card v-else>
      <div class="opl-empty">当前没有符合条件的项目。</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchProjectPage, type ProjectItem } from '@/api/project'
import { getProjectStatusMeta, projectStatusOptions } from '@/utils/view-mappers'

const router = useRouter()
const list = ref<ProjectItem[]>([])

const query = reactive({
  current: 1,
  size: 100,
  keyword: '',
  status: ''
})

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
