<template>
  <div class="stats-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">{{ t('stats.title') }}</p>
        <h2 class="opl-page-title">{{ pageTitle }}</h2>
      </div>

      <div class="opl-page-head-actions">
        <el-select
          v-model="selectedProjectId"
          clearable
          filterable
          class="project-select"
          :placeholder="t('stats.allProjects')"
          @change="handleProjectChange"
          @clear="handleProjectClear"
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.projectName"
            :value="project.id"
          />
        </el-select>
      </div>
    </section>

    <section v-if="activeProject" class="opl-grid-2">
      <el-card class="project-hero">
        <div class="project-head">
          <div>
            <p class="opl-section-title">{{ t('stats.currentProject') }}</p>
            <h3 class="opl-card-title">{{ activeProject.projectName }}</h3>
            <div class="opl-inline-meta">
              <span>{{ activeProject.projectNo }}</span>
              <span v-if="activeProject.customerName">{{ t('common.label.customer') }} {{ activeProject.customerName }}</span>
              <span v-if="activeProject.projectManagerName">{{ t('common.label.projectManager') }} {{ activeProject.projectManagerName }}</span>
            </div>
          </div>
          <span class="opl-chip orange">{{ activeProject.status || '-' }}</span>
        </div>
      </el-card>

      <el-card>
        <div class="scope-panel">
          <div class="scope-metric">
            <span class="opl-kv-label">{{ t('stats.scope') }}</span>
            <strong class="scope-value">{{ t('stats.currentProject') }}</strong>
          </div>
          <div class="scope-metric">
            <span class="opl-kv-label">{{ t('stats.closeRate') }}</span>
            <strong class="scope-value">{{ closeRate }}%</strong>
          </div>
          <div class="scope-metric">
            <span class="opl-kv-label">{{ t('stats.highPriorityRate') }}</span>
            <strong class="scope-value">{{ highPriorityRate }}%</strong>
          </div>
          <div class="scope-metric">
            <span class="opl-kv-label">{{ t('stats.overdueRate') }}</span>
            <strong class="scope-value">{{ overdueRate }}%</strong>
          </div>
        </div>
      </el-card>
    </section>

    <section v-else class="overview-banner">
      <el-card>
        <div class="overview-head">
          <div>
            <p class="opl-section-title">{{ t('stats.scope') }}</p>
            <h3 class="opl-card-title">{{ t('stats.globalOverview') }}</h3>
          </div>
          <div class="overview-chips">
            <span class="opl-chip slate">{{ t('stats.allProjectChip') }}</span>
            <span class="opl-chip teal">{{ t('stats.closeRate') }} {{ closeRate }}%</span>
            <span class="opl-chip amber">{{ t('stats.highPriorityRate') }} {{ highPriorityRate }}%</span>
            <span class="opl-chip rose">{{ t('stats.overdueRate') }} {{ overdueRate }}%</span>
          </div>
        </div>
      </el-card>
    </section>

    <section class="opl-grid-4">
      <el-card v-for="card in statCards" :key="card.label" class="opl-stat-card">
        <div class="opl-stat-label">{{ card.label }}</div>
        <div class="opl-stat-value">{{ card.value }}</div>
        <div class="card-note">{{ card.note }}</div>
      </el-card>
    </section>

    <section class="opl-grid-2 analysis-grid">
      <el-card>
        <template #header>
          <div class="chart-head">
            <div>
              <h3 class="opl-card-title">{{ t('stats.trendTitle') }}</h3>
              <div class="chart-subtitle">{{ chartSubtitle }}</div>
            </div>
          </div>
        </template>
        <div ref="trendChartRef" class="chart"></div>
      </el-card>

      <el-card>
        <template #header>
          <div class="chart-head">
            <div>
              <h3 class="opl-card-title">{{ t('stats.structureTitle') }}</h3>
              <div class="chart-subtitle">{{ structureSubtitle }}</div>
            </div>
          </div>
        </template>
        <div ref="structureChartRef" class="chart"></div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { fetchDashboardStats, type DashboardStats } from '@/api/stats'
import { fetchProjectAll, type ProjectItem } from '@/api/project'

const { t } = useI18n()
const trendChartRef = ref<HTMLDivElement>()
const structureChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let structureChart: echarts.ECharts | null = null

const projects = ref<ProjectItem[]>([])
const selectedProjectId = ref<number | undefined>()
const stats = ref<DashboardStats>({
  totalIssues: 0,
  openIssues: 0,
  highPriorityIssues: 0,
  overdueIssues: 0,
  trendDates: [],
  createdTrend: [],
  closedTrend: []
})

const activeProject = computed(() =>
  projects.value.find((item) => item.id === selectedProjectId.value) || null
)

const pageTitle = computed(() =>
  activeProject.value ? `${activeProject.value.projectName} / ${t('stats.title')}` : t('stats.title')
)

const closedIssues = computed(() => Math.max(stats.value.totalIssues - stats.value.openIssues, 0))

const closeRate = computed(() => {
  if (!stats.value.totalIssues) {
    return 0
  }
  return Math.round((closedIssues.value / stats.value.totalIssues) * 100)
})

const highPriorityRate = computed(() => {
  if (!stats.value.totalIssues) {
    return 0
  }
  return Math.round((stats.value.highPriorityIssues / stats.value.totalIssues) * 100)
})

const overdueRate = computed(() => {
  if (!stats.value.totalIssues) {
    return 0
  }
  return Math.round((stats.value.overdueIssues / stats.value.totalIssues) * 100)
})

const scopeText = computed(() => (activeProject.value ? t('stats.currentProject') : t('stats.allProjects')))

const statCards = computed(() => [
  {
    label: t('stats.totalIssues'),
    value: stats.value.totalIssues,
    note: t('stats.notes.totalIssues', { scope: scopeText.value })
  },
  {
    label: t('stats.openIssues'),
    value: stats.value.openIssues,
    note: t('stats.notes.openIssues')
  },
  {
    label: t('stats.highPriorityIssues'),
    value: stats.value.highPriorityIssues,
    note: t('stats.notes.highPriorityIssues')
  },
  {
    label: t('stats.overdueIssues'),
    value: stats.value.overdueIssues,
    note: t('stats.notes.overdueIssues')
  }
])

const chartSubtitle = computed(() =>
  activeProject.value ? `${activeProject.value.projectName} / ${t('stats.recent7Days')}` : `${t('stats.allProjects')} / ${t('stats.recent7Days')}`
)

const structureSubtitle = computed(() =>
  activeProject.value ? `${activeProject.value.projectName} / ${t('stats.currentStructure')}` : `${t('stats.allProjects')} / ${t('stats.currentStructure')}`
)

function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }

  trendChart = echarts.getInstanceByDom(trendChartRef.value) || echarts.init(trendChartRef.value)
  trendChart.setOption({
    color: ['#cb5d1f', '#0f766e'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(23, 33, 43, 0.88)',
      borderWidth: 0,
      textStyle: { color: '#fff' }
    },
    legend: {
      top: 0,
      icon: 'roundRect',
      itemWidth: 12,
      textStyle: { color: '#647382' }
    },
    grid: {
      left: 12,
      right: 12,
      top: 42,
      bottom: 10,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stats.value.trendDates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(100, 115, 130, 0.18)' } },
      axisLabel: { color: '#647382' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(100, 115, 130, 0.12)' } },
      axisLabel: { color: '#647382' }
    },
    series: [
      {
        name: t('stats.created'),
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(203, 93, 31, 0.22)' },
            { offset: 1, color: 'rgba(203, 93, 31, 0)' }
          ])
        },
        data: stats.value.createdTrend
      },
      {
        name: t('stats.closed'),
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(15, 118, 110, 0.22)' },
            { offset: 1, color: 'rgba(15, 118, 110, 0)' }
          ])
        },
        data: stats.value.closedTrend
      }
    ]
  })
}

function renderStructureChart() {
  if (!structureChartRef.value) {
    return
  }

  structureChart = echarts.getInstanceByDom(structureChartRef.value) || echarts.init(structureChartRef.value)
  structureChart.setOption({
    color: ['#0f766e', '#cb5d1f', '#c65c4e', '#235fa7'],
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(23, 33, 43, 0.88)',
      borderWidth: 0,
      textStyle: { color: '#fff' }
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      textStyle: { color: '#647382' }
    },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '46%'],
        itemStyle: {
          borderColor: 'rgba(255, 252, 247, 1)',
          borderWidth: 6
        },
        label: {
          color: '#17212b',
          formatter: '{b}\n{c}'
        },
        data: [
          { value: closedIssues.value, name: t('stats.closedIssues') },
          { value: stats.value.openIssues, name: t('stats.openIssues') },
          { value: stats.value.highPriorityIssues, name: t('stats.highPriority') },
          { value: stats.value.overdueIssues, name: t('stats.overdue') }
        ]
      }
    ]
  })
}

function renderCharts() {
  renderTrendChart()
  renderStructureChart()
}

function handleResize() {
  trendChart?.resize()
  structureChart?.resize()
}

async function loadProjects() {
  const { data } = await fetchProjectAll()
  projects.value = data || []
}

async function loadData(projectId?: number) {
  const { data } = await fetchDashboardStats(projectId)
  stats.value = data
  await nextTick()
  renderCharts()
}

async function handleProjectChange(value?: number) {
  selectedProjectId.value = value || undefined
  await loadData(selectedProjectId.value)
}

async function handleProjectClear() {
  selectedProjectId.value = undefined
  await loadData()
}

onMounted(async () => {
  await loadProjects()
  await loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  structureChart?.dispose()
  trendChart = null
  structureChart = null
})
</script>

<style scoped>
.stats-page {
  display: grid;
  gap: 16px;
}

.project-select {
  width: 260px;
}

.project-head,
.overview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.scope-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.scope-value {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  color: var(--opl-text);
}

.overview-chips {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.analysis-grid {
  align-items: start;
}

.chart {
  height: 360px;
}

.chart-subtitle,
.card-note {
  color: var(--opl-muted);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .scope-panel {
    grid-template-columns: 1fr;
  }

  .project-select {
    width: 100%;
  }
}
</style>
