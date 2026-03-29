const { getProjectAll } = require('../../api/project')
const { getProjectStatusMeta } = require('../../utils/presenter')
const { setActiveProject, getActiveProject } = require('../../utils/project-context')

const titleMap = {
  hub: '选择项目后进入工作台',
  create: '选择项目后录入 OPL',
  opl: '选择项目后查看问题库',
  stats: '选择项目后查看统计'
}

const buttonMap = {
  hub: '进入项目工作台',
  create: '进入录入页',
  opl: '进入项目问题库',
  stats: '进入项目统计'
}

function resolveTargetUrl(entry, project) {
  if (entry === 'create') {
    return `/pages/issue-create/index?projectId=${project.id}&projectName=${encodeURIComponent(project.projectName)}`
  }
  if (entry === 'opl') {
    return `/pages/issue-list/index?projectId=${project.id}&projectName=${encodeURIComponent(project.projectName)}`
  }
  if (entry === 'stats') {
    return `/pages/project-stats/index?projectId=${project.id}&projectName=${encodeURIComponent(project.projectName)}`
  }
  return `/pages/project-hub/index?projectId=${project.id}&projectName=${encodeURIComponent(project.projectName)}`
}

Page({
  data: {
    entry: 'hub',
    projects: [],
    keyword: '',
    emptyText: '当前没有可用项目',
    entryButtonText: buttonMap.hub
  },
  async onLoad(options) {
    const entry = options.entry || 'hub'
    this.setData({
      entry,
      entryButtonText: buttonMap[entry] || buttonMap.hub
    })
    wx.setNavigationBarTitle({
      title: titleMap[entry] || titleMap.hub
    })
    await this.loadProjects()
  },
  async loadProjects() {
    const activeProject = getActiveProject()
    const projects = await getProjectAll()
    const keyword = this.data.keyword.trim()
    const filtered = projects
      .filter((item) => {
        if (!keyword) {
          return true
        }
        return [item.projectNo, item.projectName, item.customerName]
          .filter(Boolean)
          .some((value) => value.toLowerCase().includes(keyword.toLowerCase()))
      })
      .map((item) => {
        const statusMeta = getProjectStatusMeta(item.status)
        return {
          ...item,
          statusLabel: statusMeta.label,
          statusTone: statusMeta.tone,
          isActive: Boolean(activeProject && activeProject.id === item.id)
        }
      })
    this.setData({ projects: filtered })
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },
  handleSearch() {
    this.loadProjects()
  },
  chooseProject(e) {
    const project = this.data.projects[Number(e.currentTarget.dataset.index)]
    setActiveProject(project)
    wx.redirectTo({ url: resolveTargetUrl(this.data.entry, project) })
  }
})
