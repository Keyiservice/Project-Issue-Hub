const { getIssueList } = require('../api/issue')
const { getPriorityMeta, getStatusMeta, getProgressMeta, getNextActionHint, formatDateTime } = require('./presenter')

const ACTIVE_STATUS = ['NEW', 'ACCEPTED', 'IN_PROGRESS', 'PENDING_VERIFY', 'ON_HOLD']
const STATUS_ORDER = ['NEW', 'IN_PROGRESS', 'PENDING_VERIFY', 'ON_HOLD', 'CLOSED', 'CANCELED']
const PRIORITY_ORDER = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']

function normalizeIssueStatus(status) {
  return status === 'ACCEPTED' ? 'IN_PROGRESS' : status
}

async function loadProjectIssueDataset(projectId, pageSize = 100, maxPages = 20) {
  const records = []
  let current = 1
  let total = 0

  do {
    const data = await getIssueList({
      current,
      size: pageSize,
      projectId
    })
    total = data.total || 0
    records.push(...(data.records || []))
    current += 1
  } while (records.length < total && current <= maxPages)

  return {
    total: total || records.length,
    records
  }
}

function scoreIssue(issue) {
  let score = 0
  if (issue.overdue) {
    score += 100
  }
  if (issue.priority === 'CRITICAL') {
    score += 80
  } else if (issue.priority === 'HIGH') {
    score += 50
  }
  if (issue.status === 'PENDING_VERIFY') {
    score += 40
  }
  if (!issue.ownerName) {
    score += 20
  }
  return score
}

function buildBarItems(records, keys, getMeta, selector) {
  const total = records.length || 1
  return keys.map((key) => {
    const count = records.filter((item) => selector(item, key)).length
    const meta = getMeta(key)
    const percent = Math.round((count / total) * 100)
    return {
      key,
      label: meta.label,
      tone: meta.tone,
      count,
      percent,
      widthStyle: `width: ${Math.max(count ? percent : 6, 6)}%;`
    }
  })
}

function buildProjectOverview(records, total) {
  const openIssues = records.filter((item) => ACTIVE_STATUS.includes(item.status)).length
  const closedIssues = records.filter((item) => item.status === 'CLOSED').length
  const overdueIssues = records.filter((item) => item.overdue).length
  const highPriorityIssues = records.filter((item) => item.priority === 'HIGH' || item.priority === 'CRITICAL').length
  const pendingVerifyIssues = records.filter((item) => item.status === 'PENDING_VERIFY').length

  const statusBars = buildBarItems(records, STATUS_ORDER, getStatusMeta, (item, key) => normalizeIssueStatus(item.status) === key)
  const priorityBars = buildBarItems(records, PRIORITY_ORDER, getPriorityMeta, (item, key) => item.priority === key)

  const focusIssues = records
    .slice()
    .sort((a, b) => scoreIssue(b) - scoreIssue(a))
    .slice(0, 5)
    .map((item) => {
      const progressMeta = getProgressMeta(item.status, Boolean(item.overdue))
      return {
        id: item.id,
        issueNo: item.issueNo,
        title: item.title,
        ownerName: item.ownerName || '待分派',
        progressLabel: progressMeta.label,
        progressTone: progressMeta.tone,
        nextActionText: getNextActionHint(item.status, Boolean(item.ownerName)),
        dueText: formatDateTime(item.dueAt)
      }
    })

  return {
    summary: {
      totalIssues: total,
      openIssues,
      closedIssues,
      overdueIssues,
      highPriorityIssues,
      pendingVerifyIssues
    },
    statusBars,
    priorityBars,
    focusIssues
  }
}

module.exports = {
  loadProjectIssueDataset,
  buildProjectOverview
}
