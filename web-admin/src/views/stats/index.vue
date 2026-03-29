<template>
  <div class="stats-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Statistics</p>
        <h2 class="opl-page-title">{{ pageTitle }}</h2>
      </div>

      <div class="opl-page-head-actions">
        <el-select
          v-model="selectedProjectId"
          clearable
          filterable
          class="project-select"
          placeholder="全部项目"
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
            <p class="opl-section-title">Current Project</p>
            <h3 class="opl-card-title">{{ activeProject.projectName }}</h3>
            <div class="opl-inline-meta">
              <span>{{ activeProject.projectNo }}</span>
              <span v-if="activeProject.customerName">客户 {{ activeProject.customerName }}</span>
              <span v-if="activeProject.projectManagerName">项目经理 {{ activeProject.projectManagerName }}</span>
            </div>
          </div>
          <span class="opl-chip orange">{{ activeProject.status || '进行中' }}</span>
        </div>
      </el-card>

      <el-card>
        <div class="scope-panel">
          <div class="scope-metric">
            <span class="opl-kv-label">统计口径</span>
            <strong class="scope-value">当前项目</strong>
          </div>
          <div class="scope-metric">
            <span class="opl-kv-label">闭环率</span>
            <strong class="scope-value">{{ closeRate }}%</strong>
          </div>
          <div class="scope-metric">
            <span class="opl-kv-label">高优先级占比</span>
            <strong class="scope-value">{{ highPriorityRate }}%</strong>
          </div>
          <div class="scope-metric">
            <span class="opl-kv-label">超期占比</span>
            <strong class="scope-value">{{ overdueRate }}%</strong>
          </div>
        </div>
      </el-card>
    </section>

    <section v-else class="overview-banner">
      <el-card>
        <div class="overview-head">
          <div>
            <p class="opl-section-title">Scope</p>
            <h3 class="opl-card-title">全部项目总览</h3>
          </div>
          <div class="overview-chips">
            <span class="opl-chip slate">全项目</span>
            <span class="opl-chip teal">闭环率 {{ closeRate }}%</span>
            <span class="opl-chip amber">高优先级 {{ highPriorityRate }}%</span>
            <span class="opl-chip rose">超期 {{ overdueRate }}%</span>
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
              <h3 class="opl-card-title">新增 / 关闭趋势</h3>
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
              <h3 class="opl-card-title">问题结构</h3>
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
import * as echarts from 'echarts'
import { fetchDashboardStats, type DashboardStats } from '@/api/stats'
import { fetchProjectAll, type ProjectItem } from '@/api/project'

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
  activeProject.value ? `${activeProject.value.projectName} 统计分析` : '统计分析'
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

const scopeText = computed(() => (activeProject.value ? '当前项目口径' : '全部项目口径'))

const statCards = computed(() => [
  {
    label: '问题总数',
    value: stats.value.totalIssues,
    note: `${scopeText.value}下全部问题`
  },
  {
    label: '未关闭',
    value: stats.value.openIssues,
    note: '新建、处理中、待验证、挂起'
  },
  {
    label: '高优先级',
    value: stats.value.highPriorityIssues,
    note: 'HIGH / CRITICAL 且未关闭'
  },
  {
    label: '已超期',
    value: stats.value.overdueIssues,
    note: '超过截止时间且未关闭'
  }
])

const chartSubtitle = computed(() =>
  activeProject.value ? `${activeProject.value.projectName} 最近 7 天` : '全部项目最近 7 天'
)

const structureSubtitle = computed(() =>
  activeProject.value ? `${activeProject.value.projectName} 当前结构` : '全部项目当前结构'
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
        name: '新增',
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
        name: '关闭',
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
          { value: closedIssues.value, name: '已关闭' },
          { value: stats.value.openIssues, name: '未关闭' },
          { value: stats.value.highPriorityIssues, name: '高优先级' },
          { value: stats.value.overdueIssues, name: '已超期' }
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

.project-hero {
  background:
    radial-gradient(circle at left top, rgba(203, 93, 31, 0.12), transparent 28%),
    radial-gradient(circle at right top, rgba(15, 118, 110, 0.14), transparent 32%),
    linear-gradient(145deg, rgba(255, 251, 246, 0.94), rgba(255, 255, 255, 0.82));
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
  gap: 16px;
}

.scope-metric {
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.06);
}

.scope-value {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  color: var(--opl-text);
}

.overview-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.card-note,
.chart-subtitle {
  margin-top: 8px;
  color: var(--opl-muted);
  font-size: 13px;
  line-height: 1.7;
}

.analysis-grid {
  align-items: start;
}

.chart {
  height: 360px;
}

@media (max-width: 960px) {
  .project-select {
    width: 100%;
  }

  .project-head,
  .overview-head {
    flex-direction: column;
    align-items: stretch;
  }

  .overview-chips {
    justify-content: flex-start;
  }

  .scope-panel {
    grid-template-columns: 1fr;
  }
}
</style>
