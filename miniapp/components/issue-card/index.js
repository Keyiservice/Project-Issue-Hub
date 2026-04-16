const {
  getIssueFunctionMeta,
  getPriorityMeta,
  formatDateTime,
  getNextActionHint,
  getProgressMeta
} = require('../../utils/presenter')

Component({
  properties: {
    issue: {
      type: Object,
      value: {}
    }
  },
  data: {
    progressLabel: '-',
    progressTone: 'gray',
    priorityLabel: '-',
    priorityTone: 'gray',
    issueFunctionLabel: '-',
    createdText: '-',
    nextActionText: '-'
  },
  observers: {
    issue(value) {
      const priority = getPriorityMeta(value.priority)
      const progress = getProgressMeta(value.status, Boolean(value.overdue))
      const issueFunction = getIssueFunctionMeta(value.issueFunctionCode)
      this.setData({
        progressLabel: progress.label,
        progressTone: progress.tone,
        priorityLabel: priority.label,
        priorityTone: priority.tone,
        issueFunctionLabel: issueFunction.label,
        createdText: formatDateTime(value.createdAt),
        nextActionText: value.nextActionText || getNextActionHint(value.status, Boolean(value.ownerName))
      })
    }
  },
  methods: {
    onTap() {
      this.triggerEvent('tap', this.properties.issue)
    }
  }
})
