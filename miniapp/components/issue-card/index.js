const {
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
    createdText: '-',
    nextActionText: '-'
  },
  observers: {
    issue(value) {
      const priority = getPriorityMeta(value.priority)
      const progress = getProgressMeta(value.status, Boolean(value.overdue))
      this.setData({
        progressLabel: progress.label,
        progressTone: progress.tone,
        priorityLabel: priority.label,
        priorityTone: priority.tone,
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
