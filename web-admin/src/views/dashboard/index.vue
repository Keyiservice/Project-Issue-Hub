<template>
  <div class="dashboard-page">
    <section class="dashboard-hero opl-grid-2">
      <el-card class="hero-card">
        <div class="hero-toolbar">
          <div>
            <p class="opl-section-title">Control Tower</p>
            <h2 class="hero-title">{{ heroTitle }}</h2>
            <div class="hero-meta opl-inline-meta">
              <span v-if="displayProject?.customerName">
                客户 <strong>{{ displayProject.customerName }}</strong>
              </span>
              <span v-if="displayProject?.projectManagerName">
                项目经理 <strong>{{ displayProject.projectManagerName }}</strong>
              </span>
              <span>{{ modeDescription }}</span>
            </div>
          </div>

          <div class="hero-controls">
            <el-select
              v-model="selectedProjectId"
              clearable
              filterable
              placeholder="未选择则自动轮巡"
              class="project-select"
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
            <span class="opl-chip" :class="modeTone">{{ modeLabel }}</span>
          </div>
        </div>

        <div class="hero-tags">
          <span v-if="displayProject?.projectNo" class="opl-chip slate">{{ displayProject.projectNo }}</span>
          <span class="opl-chip orange">{{ contextLabel }}</span>
          <span v-if="autoRotationActive" class="opl-chip blue">每 8 秒轮巡</span>
        </div>
      </el-card>

      <el-card class="hero-side">
        <div class="hero-side-header">
          <div>
            <h3 class="opl-card-title">{{ displayProject?.projectName || '全部项目' }}</h3>
            <p class="hero-side-subtitle">
              {{ displayProject?.status || 'GLOBAL' }} · {{ stats.totalIssues }} 个问题
            </p>
          </div>
          <span class="opl-chip" :class="alertTone">{{ alertLabel }}</span>
        </div>

        <div class="hero-kpis">
          <div class="hero-kpi">
            <span class="opl-kv-label">闭环率</span>
            <strong class="hero-kpi-value">{{ closeRate }}%</strong>
          </div>
          <div class="hero-kpi">
            <span class="opl-kv-label">高优先级占比</span>
            <strong class="hero-kpi-value">{{ highPriorityRate }}%</strong>
          </div>
          <div class="hero-kpi">
            <span class="opl-kv-label">超期占比</span>
            <strong class="hero-kpi-value">{{ overdueRate }}%</strong>
          </div>
        </div>

        <div class="health-list">
          <div v-for="item in healthList" :key="item.label" class="health-item">
            <div class="health-head">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}%</strong>
            </div>
            <div class="health-track">
              <div class="health-fill" :class="item.tone" :style="{ width: `${item.value}%` }"></div>
            </div>
          </div>
        </div>
      </el-card>
    </section>

    <section class="opl-grid-4">
      <el-card v-for="card in cards" :key="card.label" class="opl-stat-card">
        <div class="opl-stat-label">{{ card.label }}</div>
        <div class="opl-stat-value">{{ card.value }}</div>
        <div class="stat-context">{{ card.note }}</div>
      </el-card>
    </section>

    <section class="opl-grid-2 analysis-grid">
      <el-card>
        <template #header>
          <div>
            <h3 class="opl-card-title">新增 / 关闭趋势</h3>
            <p class="hero-side-subtitle">{{ trendSubtitle }}</p>
          </div>
        </template>
        <div ref="chartRef" class="chart"></div>
      </el-card>

      <el-card>
        <template #header>
          <div>
            <h3 class="opl-card-title">当前关注清单</h3>
            <p class="hero-side-subtitle">{{ focusSubtitle }}</p>
          </div>
        </template>

        <div class="focus-list">
          <div v-for="item in focusList" :key="item.title" class="focus-item">
            <div class="focus-badge" :class="item.tone">{{ item.level }}</div>
            <div class="focus-body">
              <div class="focus-title">{{ item.title }}</div>
              <div class="focus-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardStats } from '@/api/stats'
import { fetchProjectAll, type ProjectItem } from '@/api/project'

const chartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let rotationTimer: number | null = null
let requestSeq = 0

const projects = ref<ProjectItem[]>([])
const selectedProjectId = ref<number | undefined>()
const rotationIndex = ref(0)
const stats = ref({
  totalIssues: 0,
  openIssues: 0,
  highPriorityIssues: 0,
  overdueIssues: 0,
  trendDates: [] as string[],
  createdTrend: [] as number[],
  closedTrend: [] as number[]
})

const displayProject = computed(() => {
  if (selectedProjectId.value) {
    return projects.value.find((item) => item.id === selectedProjectId.value) || null
  }
  if (!projects.value.length) {
    return null
  }
  return projects.value[rotationIndex.value] || null
})

const autoRotationActive = computed(() => !selectedProjectId.value && projects.value.length > 1)
const contextLabel = computed(() => (displayProject.value ? `${displayProject.value.projectName} 项目视角` : '全部项目视角'))
const modeLabel = computed(() => (selectedProjectId.value ? '已锁定项目' : autoRotationActive.value ? '自动轮巡' : '当前项目'))
const modeTone = computed(() => (selectedProjectId.value ? 'orange' : autoRotationActive.value ? 'blue' : 'teal'))
const modeDescription = computed(() => {
  if (selectedProjectId.value) {
    return '已锁定到单个项目，趋势和清单只看当前项目。'
  }
  if (autoRotationActive.value) {
    return '未锁定项目，驾驶舱会自动轮巡各项目。'
  }
  if (displayProject.value) {
    return '当前仅有一个项目，驾驶舱固定展示该项目。'
  }
  return '当前没有可轮巡项目，回退为全部项目统计。'
})

const heroTitle = computed(() => (displayProject.value ? `${displayProject.value.projectName} 驾驶舱` : '全部项目驾驶舱'))
const trendSubtitle = computed(() => `最近 7 天的新增与关闭节奏，当前视角：${contextLabel.value}`)
const focusSubtitle = computed(() => `当前只列出 ${displayProject.value ? displayProject.value.projectName : '全部项目'} 需要动作的信号。`)

const cards = computed(() => [
  {
    label: '问题总数',
    value: stats.value.totalIssues,
    note: `${contextLabel.value}下的全部问题规模`
  },
  {
    label: '未关闭问题',
    value: stats.value.openIssues,
    note: '新建、处理中、待验证和挂起总和'
  },
  {
    label: '高优先级问题',
    value: stats.value.highPriorityIssues,
    note: 'HIGH / CRITICAL 且未关闭'
  },
  {
    label: '超期问题',
    value: stats.value.overdueIssues,
    note: '超过截止时间且未关闭'
  }
])

const closeRate = computed(() => {
  if (!stats.value.totalIssues) {
    return 0
  }
  return Math.round(((stats.value.totalIssues - stats.value.openIssues) / stats.value.totalIssues) * 100)
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

const alertLabel = computed(() => {
  if (stats.value.overdueIssues > 0) {
    return '需升级处理'
  }
  if (stats.value.highPriorityIssues > 0) {
    return '重点盯防'
  }
  return '节奏稳定'
})

const alertTone = computed(() => {
  if (stats.value.overdueIssues > 0) {
    return 'rose'
  }
  if (stats.value.highPriorityIssues > 0) {
    return 'amber'
  }
  return 'teal'
})

const healthList = computed(() => [
  { label: '闭环效率', value: closeRate.value, tone: 'teal' },
  { label: '高优压力', value: highPriorityRate.value, tone: 'amber' },
  { label: '超期风险', value: overdueRate.value, tone: 'rose' }
])

const focusList = computed(() => {
  const label = displayProject.value ? displayProject.value.projectName : '当前视角'
  const items = []

  if (stats.value.overdueIssues > 0) {
    items.push({
      level: 'P1',
      tone: 'rose',
      title: `${label}存在 ${stats.value.overdueIssues} 个超期问题`,
      desc: '先盯超期和未分派项，避免项目节奏继续拖延。'
    })
  }

  if (stats.value.highPriorityIssues > 0) {
    items.push({
      level: 'P2',
      tone: 'amber',
      title: `${label}存在 ${stats.value.highPriorityIssues} 个高优先级问题`,
      desc: '建议优先核对责任人、下一步动作和关闭验证安排。'
    })
  }

  if (stats.value.openIssues > 0) {
    items.push({
      level: 'P3',
      tone: 'blue',
      title: `${label}仍有 ${stats.value.openIssues} 个未关闭问题`,
      desc: '新增和关闭趋势要同步看，避免问题池持续堆积。'
    })
  }

  if (!items.length) {
    items.push({
      level: 'OK',
      tone: 'teal',
      title: `${label}当前没有未关闭问题`,
      desc: '问题池清空，可转为关注新增质量和项目资源准备。'
    })
  }

  return items
})

function renderChart() {
  if (!chartRef.value) {
    return
  }

  trendChart = echarts.getInstanceByDom(chartRef.value) || echarts.init(chartRef.value)
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

function handleResize() {
  trendChart?.resize()
}

function stopRotation() {
  if (rotationTimer) {
    window.clearInterval(rotationTimer)
    rotationTimer = null
  }
}

function startRotation() {
  stopRotation()
  if (selectedProjectId.value || projects.value.length <= 1) {
    return
  }

  rotationTimer = window.setInterval(async () => {
    rotationIndex.value = (rotationIndex.value + 1) % projects.value.length
    await loadDashboard(displayProject.value?.id)
  }, 8000)
}

async function loadDashboard(projectId?: number) {
  const seq = ++requestSeq
  const { data } = await fetchDashboardStats(projectId)
  if (seq !== requestSeq) {
    return
  }
  stats.value = data
  await nextTick()
  renderChart()
}

async function loadProjects() {
  const { data } = await fetchProjectAll()
  projects.value = data || []
}

async function enterAutoRotation() {
  selectedProjectId.value = undefined
  rotationIndex.value = 0
  await loadDashboard(displayProject.value?.id)
  startRotation()
}

async function handleProjectChange(value?: number) {
  selectedProjectId.value = value || undefined
  if (!selectedProjectId.value) {
    await enterAutoRotation()
    return
  }

  stopRotation()
  const targetIndex = projects.value.findIndex((item) => item.id === selectedProjectId.value)
  if (targetIndex >= 0) {
    rotationIndex.value = targetIndex
  }
  await loadDashboard(selectedProjectId.value)
}

async function handleProjectClear() {
  await enterAutoRotation()
}

onMounted(async () => {
  await loadProjects()
  if (projects.value.length) {
    await enterAutoRotation()
  } else {
    await loadDashboard()
  }
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  stopRotation()
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  trendChart = null
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 16px;
}

.hero-card {
  min-height: 280px;
  background:
    radial-gradient(circle at right top, rgba(15, 118, 110, 0.16), transparent 32%),
    radial-gradient(circle at left, rgba(203, 93, 31, 0.14), transparent 28%),
    linear-gradient(145deg, rgba(255, 251, 246, 0.94), rgba(255, 255, 255, 0.78));
}

.hero-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.hero-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.project-select {
  width: 280px;
}

.hero-title {
  margin: 14px 0 10px;
  font-size: 40px;
  line-height: 1.06;
  color: var(--opl-text);
}

.hero-meta {
  margin-top: 8px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.hero-side-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.hero-side-subtitle,
.stat-context {
  margin-top: 8px;
  color: var(--opl-muted);
  line-height: 1.7;
  font-size: 13px;
}

.hero-kpis {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 24px;
}

.hero-kpi {
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.08);
}

.hero-kpi-value {
  display: block;
  margin-top: 10px;
  font-size: 28px;
  line-height: 1;
  color: var(--opl-text);
}

.health-list {
  margin-top: 24px;
  display: grid;
  gap: 16px;
}

.health-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: var(--opl-muted);
  font-size: 13px;
}

.health-head strong {
  color: var(--opl-text);
}

.health-track {
  overflow: hidden;
  height: 10px;
  border-radius: 999px;
  background: rgba(24, 33, 43, 0.08);
}

.health-fill {
  height: 100%;
  border-radius: 999px;
}

.health-fill.teal {
  background: linear-gradient(90deg, #0f766e, #21a598);
}

.health-fill.amber {
  background: linear-gradient(90deg, #cb7d1f, #e6a447);
}

.health-fill.rose {
  background: linear-gradient(90deg, #c65c4e, #e08b7d);
}

.analysis-grid {
  align-items: start;
}

.chart {
  height: 360px;
}

.focus-list {
  display: grid;
  gap: 14px;
}

.focus-item {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 14px;
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.06);
}

.focus-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.focus-badge.rose {
  color: #9f4135;
  background: var(--opl-rose-soft);
}

.focus-badge.amber {
  color: #825113;
  background: var(--opl-amber-soft);
}

.focus-badge.blue {
  color: #164a84;
  background: var(--opl-blue-soft);
}

.focus-badge.teal {
  color: #0b5b55;
  background: var(--opl-teal-soft);
}

.focus-title {
  font-weight: 700;
  color: var(--opl-text);
}

.focus-desc {
  margin-top: 6px;
  color: var(--opl-muted);
  line-height: 1.8;
}

@media (max-width: 960px) {
  .hero-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-controls {
    justify-content: flex-start;
  }

  .project-select {
    width: 100%;
  }

  .hero-kpis {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 34px;
  }
}
</style>
