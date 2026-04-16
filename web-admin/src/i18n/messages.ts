import type { AppLocale } from './locales'

function merge(base: any, override: any): any {
  const output = { ...base }
  Object.keys(override).forEach((key) => {
    const baseValue = output[key]
    const overrideValue = override[key]
    if (
      baseValue &&
      overrideValue &&
      typeof baseValue === 'object' &&
      !Array.isArray(baseValue) &&
      typeof overrideValue === 'object' &&
      !Array.isArray(overrideValue)
    ) {
      output[key] = merge(baseValue, overrideValue)
    } else {
      output[key] = overrideValue
    }
  })
  return output
}

const zhCN = {
  common: {
    appName: 'Project Issue Hub',
    language: '语言',
    locale: {
      'zh-CN': '中文',
      'en-US': '英文',
      'th-TH': '泰语',
      'ja-JP': '日语',
      'ko-KR': '韩语',
      'hi-IN': '印度语'
    },
    action: {
      refresh: '刷新',
      search: '查询',
      reset: '重置',
      create: '新建',
      save: '保存',
      cancel: '取消',
      delete: '删除',
      edit: '编辑',
      remove: '移除',
      import: '批量导入',
      exportReport: '导出 Report',
      switchProject: '切换项目',
      submit: '提交',
      update: '更新',
      logout: '退出登录',
      viewDetail: '查看详情',
      back: '返回',
      applyFilter: '应用筛选',
      saveAttachments: '保存补充附件',
      uploadMedia: '上传图片 / 视频',
      startProcessing: '开始处理',
      submitProgress: '提交进度',
      closeIssue: '关闭问题',
      goProjectTeam: '项目团队'
    },
    label: {
      keyword: '关键字',
      status: '状态',
      priority: '优先级',
      impact: '影响等级',
      owner: '责任人',
      reporter: '提交人',
      project: '项目',
      projectManager: '项目经理',
      customer: '客户',
      startDate: '开始',
      plannedEndDate: '计划结束',
      occurredAt: '发生时间',
      dueAt: '截止时间',
      closedAt: '关闭时间',
      closedBy: '关闭人',
      closeReason: '关闭说明',
      closeEvidence: '关闭证据',
      department: '部门',
      departmentCode: '部门编码',
      username: '账号',
      realName: '姓名',
      mobile: '手机号',
      email: '邮箱',
      role: '角色',
      projectRole: '项目岗位',
      permissions: '权限',
      attachments: '附件',
      remark: '备注',
      description: '描述',
      title: '标题',
      teamSize: '团队人数',
      wechatBinding: '微信绑定',
      passwordStatus: '密码状态',
      recentLogin: '最近登录',
      projectCount: '参与项目',
      currentProject: '当前项目',
      currentStage: '当前阶段',
      nextStep: '下一步',
      latestAction: '最新动作',
      sortNo: '排序',
      summary: '概览'
    },
    empty: {
      none: '暂无',
      noData: '当前没有数据',
      unassigned: '待分派',
      noRole: '未分配角色',
      notLoggedIn: '未登录用户'
    },
    status: {
      enabled: '启用',
      disabled: '禁用',
      locked: '锁定',
      bound: '已绑定',
      unbound: '未绑定',
      passwordPending: '待改密',
      passwordUpdated: '已更新'
    },
    message: {
      networkError: '网络异常',
      unauthorized: '未认证或认证已过期',
      chooseProjectFirst: '请先选择项目',
      uploadImageVideoOnly: '只支持图片和视频',
      uploadSizeLimit: '单文件不能超过 5MB',
      downloaded: 'Report 已开始下载'
    },
    uploadTip: '支持图片和视频，单文件不超过 5MB。'
  },
  layout: {
    brandTitle: '项目问题协同平台',
    navigation: '导航',
    projectFirst: '项目优先',
    timezone: 'Asia/Shanghai',
    menus: {
      dashboard: '驾驶舱',
      issueProjects: '项目问题库',
      projects: '项目协同',
      stats: '统计分析',
      users: '用户管理',
      dicts: '字典配置'
    },
    titles: {
      dashboard: '管理驾驶舱',
      issueProjects: '项目问题库入口',
      issues: '项目问题清单',
      issueDetail: '问题详情',
      projects: '项目协同',
      stats: '统计分析',
      users: '用户与角色',
      dicts: '字典配置'
    }
  },
  login: {
    passwordChangeTitle: 'Change password required',
    passwordChangeDesc: 'For first login, you must set a strong password before continuing.',
    passwordPolicyHint: 'At least 10 characters with upper/lower case, number, and symbol.',
    passwordChangeAction: 'Update password',
    newPasswordPlaceholder: 'New password',
    confirmPasswordPlaceholder: 'Confirm new password',
    passwordMissing: 'Please enter and confirm your new password',
    mfaSetupTitle: 'Set up MFA',
    mfaSetupDesc: 'Scan the QR code with Authenticator, then enter the 6-digit code to verify.',
    mfaVerifyTitle: 'MFA verification',
    mfaVerifyDesc: 'Enter the 6-digit code from your Authenticator app.',
    mfaSecretLabel: 'Secret key',
    mfaCodePlaceholder: '6-digit code',
    mfaVerifyAction: 'Verify and continue',
    mfaCodeMissing: 'Please enter the MFA code',

    kicker: 'Project Issue Hub / Delivery Control',
    heroTitle: '让问题信息像现场一样清楚，而不是像 Excel 一样模糊。',
    heroDesc: '用图片、视频、责任人、截止时间和状态闭环，让问题推进更直接。',
    signIn: '登录',
    title: '登录 Project Issue Hub',
    desc: '使用统一账号进入项目问题库、协同管理和统计分析。',
    usernamePlaceholder: '请输入账号',
    passwordPlaceholder: '请输入密码',
    enter: '进入系统',
    metric1: '10 秒理解问题本质',
    metric2: '统一责任与状态闭环',
    metric3: '现场优先，图像驱动',
    hint1: 'Web 负责管理和分析，小程序负责现场录入和跟进。',
    hint2: '状态流转、权限和编号统一由后端控制。',
    hint3: '上传图片和视频后，管理层打开即可理解现场问题。',
    footnote: '推荐先用 admin / 123456 登录，再继续创建项目和问题。',
    missingCredentials: '请输入账号和密码'
  },
  page: {
    dashboard: '管理驾驶舱',
    projectSelector: '项目问题库入口',
    issueList: '项目问题清单',
    issueDetail: '问题详情',
    projects: '项目管理',
    stats: '统计分析',
    users: '用户管理',
    dict: '字典配置'
  },
  dashboard: {
    section: '控制塔',
    globalTitle: '全部项目驾驶舱',
    autoRotatePlaceholder: '未选择则自动轮巡',
    every8Seconds: '每 8 秒轮巡',
    totalIssues: '问题总数',
    openIssues: '未关闭问题',
    highPriorityIssues: '高优先级问题',
    overdueIssues: '超期问题',
    trendTitle: '新增 / 关闭趋势',
    focusTitle: '当前关注清单',
    created: '新增',
    closed: '关闭'
  },
  projectSelector: {
    title: '先选项目，再进入对应的问题库。',
    lead: '每个项目都应有独立的问题池，避免不同项目问题混在一起。',
    filterTitle: '项目筛选',
    filterSubtitle: '先锁定项目，再进入对应的问题清单。',
    enterIssueLibrary: '进入问题库',
    openProject: '查看项目',
    empty: '当前没有符合条件的项目。'
  },
  issueList: {
    title: '项目问题清单',
    lead: '先锁定项目，再看该项目自己的 OPL 和推进情况。',
    filterTitle: '筛选条件',
    filterSubtitle: '列表可按条件缩小范围，但顶部统计始终按整个项目口径统计。',
    tableTitle: '问题清单',
    tableSubtitle: '分页只影响列表，不影响项目统计。',
    infoColumn: '问题信息',
    issueNoPlaceholder: '问题编号 / 标题 / 描述',
    allOwners: '全部责任人',
    allStatus: '全部状态',
    allPriority: '全部优先级',
    overdueOnly: '仅看超期问题',
    overdueFlag: '已超期',
    tableCount: '共 {count} 条',
    issueCreate: '新建问题',
    createHere: '录入新问题',
    currentProjectBar: '当前项目',
    createDialogTitle: '新建问题'
  },
  issueDetail: {
    backToProject: '返回项目问题库',
    deleteMistake: '误录删除',
    noDescription: '暂无补充说明',
    appendAttachments: '补充附件',
    appendTip: '问题创建后可继续补充图片和视频，保存后会出现在当前附件区。',
    currentProgress: '当前推进',
    closeInfo: '关闭信息',
    managementActions: '管理操作',
    assignOwner: '责任分派',
    priorityPanel: '优先级',
    nextAction: '下一步操作',
    noFurtherAction: '当前状态无需继续推进',
    solutionDesc: '处理说明',
    holdReason: '挂起原因',
    progressRemark: '备注',
    submitFollowup: '提交跟进',
    followupPlaceholder: '补充现场进展、处理结果或风险',
    timeline: '时间线',
    timelineEmpty: '还没有时间线记录',
    systemRecord: '系统记录',
    manualRecord: '人工跟进',
    previewTitle: '附件预览',
    openAttachment: '打开附件'
  },
  project: {
    title: '项目管理',
    projectList: '项目列表',
    issueLibrary: '问题库',
    projectTeam: '项目团队',
    newProject: '新建项目',
    createProject: '创建项目',
    teamTip: '问题分派只允许从当前项目团队中选择。',
    syncFromOpl: '从 OPL 同步成员',
    addMember: '新增成员',
    importResult: '导入结果',
    syncResult: '同步结果',
    formatError: '导入格式错误'
  },
  stats: {
    title: '统计分析',
    allProjects: '全部项目',
    currentProject: '当前项目',
    trendTitle: '新增 / 关闭趋势',
    structureTitle: '问题结构',
    created: '新增',
    closed: '关闭',
    closedIssues: '已关闭',
    openIssues: '未关闭',
    highPriority: '高优先级',
    overdue: '已超期'
  },
  user: {
    title: '用户管理',
    lead: '统一维护账号、系统角色、微信绑定和参与项目。',
    createUser: '新建用户',
    importUsers: '批量导入',
    createUserTitle: '新建用户',
    editUserTitle: '编辑用户',
    importTitle: '批量导入用户',
    initialPassword: '初始密码',
    projectParticipation: '参与项目',
    importResult: '导入结果',
    formatError: '导入格式错误'
  },
  dict: {
    title: '分类、来源、部门和模块会在这里标准化。',
    lead: '统一编码后，统计、筛选和权限联动才有基础。',
    issueCategory: '问题分类',
    issueSource: '问题来源',
    moduleDept: '模块与部门'
  },
  enums: {
    issueStatus: {
      NEW: '新建',
      IN_PROGRESS: '处理中',
      PENDING_VERIFY: '待验证',
      CLOSED: '已关闭',
      ON_HOLD: '已挂起',
      CANCELED: '已取消'
    },
    priority: {
      LOW: '低',
      MEDIUM: '中',
      HIGH: '高',
      CRITICAL: '紧急'
    },
    impact: {
      LOW: '低影响',
      MEDIUM: '中影响',
      HIGH: '高影响',
      CRITICAL: '致命影响'
    },
    projectStatus: {
      PLANNING: '规划中',
      IN_PROGRESS: '进行中',
      DELIVERING: '交付中',
      CLOSED: '已关闭',
      CANCELED: '已取消'
    }
  },
  roles: {
    SITE_USER: '现场人员',
    RESP_ENGINEER: '责任工程师',
    PROJECT_MANAGER: '项目经理',
    MANAGEMENT: '管理层',
    ADMIN: '管理员',
    MFG_MANAGER: '制造经理',
    PLANT_MANAGER: '厂长',
    MECH_DESIGN_SUPERVISOR: '机械设计主管',
    DESIGN_MANAGER: '设计经理',
    AUTOMATION_DESIGN_SUPERVISOR: '自动化设计主管',
    AUTOMATION_ENGINEER: '自动化工程师',
    MECHANICAL_ENGINEER: '机械工程师',
    ELECTRICAL_TECHNICIAN: '电气技术员',
    MECHANICAL_TECHNICIAN: '机械技术员',
    ME: 'ME',
    MDA: 'MDA',
    AE: 'AE',
    MD_MANAGER: 'MD经理',
    IE: 'IE',
    LOGISTICS_SUPERVISOR: '物流主管',
    PROCUREMENT_MANAGER: '采购经理',
    FINANCE_MANAGER: '财务经理',
    HR_MANAGER: '人事经理',
    QUALITY_MANAGER: '质量经理',
    QUALITY_ENGINEER: '质量工程师',
    PLANT_REPRESENTATIVE: '工厂代表'
  }
}

const enUS = {
  common: {
    appName: 'Project Issue Hub',
    language: 'Language',
    locale: {
      'zh-CN': 'Chinese',
      'en-US': 'English',
      'th-TH': 'Thai',
      'ja-JP': 'Japanese',
      'ko-KR': 'Korean',
      'hi-IN': 'Hindi'
    },
    action: {
      refresh: 'Refresh',
      search: 'Search',
      reset: 'Reset',
      create: 'Create',
      save: 'Save',
      cancel: 'Cancel',
      delete: 'Delete',
      edit: 'Edit',
      remove: 'Remove',
      import: 'Bulk Import',
      exportReport: 'Export Report',
      switchProject: 'Switch Project',
      submit: 'Submit',
      update: 'Update',
      logout: 'Sign Out',
      viewDetail: 'View Detail',
      back: 'Back',
      applyFilter: 'Apply Filters',
      saveAttachments: 'Save Added Attachments',
      uploadMedia: 'Upload Image / Video',
      startProcessing: 'Start Processing',
      submitProgress: 'Submit Progress',
      closeIssue: 'Close Issue',
      goProjectTeam: 'Project Team'
    },
    label: {
      keyword: 'Keyword',
      status: 'Status',
      priority: 'Priority',
      impact: 'Impact',
      owner: 'Owner',
      reporter: 'Reporter',
      project: 'Project',
      projectManager: 'Project Manager',
      customer: 'Customer',
      startDate: 'Start',
      plannedEndDate: 'Planned End',
      occurredAt: 'Occurred At',
      dueAt: 'Due At',
      closedAt: 'Closed At',
      closedBy: 'Closed By',
      closeReason: 'Close Note',
      closeEvidence: 'Close Evidence',
      department: 'Department',
      departmentCode: 'Department Code',
      username: 'Username',
      realName: 'Name',
      mobile: 'Mobile',
      email: 'Email',
      role: 'Role',
      projectRole: 'Project Role',
      permissions: 'Permissions',
      attachments: 'Attachments',
      remark: 'Remark',
      description: 'Description',
      title: 'Title',
      teamSize: 'Team Size',
      wechatBinding: 'WeChat Binding',
      passwordStatus: 'Password Status',
      recentLogin: 'Last Login',
      projectCount: 'Projects',
      currentProject: 'Current Project',
      currentStage: 'Current Stage',
      nextStep: 'Next Step',
      latestAction: 'Latest Action',
      sortNo: 'Sort No.',
      summary: 'Summary'
    },
    empty: {
      none: 'None',
      noData: 'No data',
      unassigned: 'Unassigned',
      noRole: 'No role assigned',
      notLoggedIn: 'Not signed in'
    },
    status: {
      enabled: 'Enabled',
      disabled: 'Disabled',
      locked: 'Locked',
      bound: 'Bound',
      unbound: 'Unbound',
      passwordPending: 'Password reset required',
      passwordUpdated: 'Updated'
    },
    message: {
      networkError: 'Network error',
      unauthorized: 'Authentication required or session expired',
      chooseProjectFirst: 'Please select a project first',
      uploadImageVideoOnly: 'Only images and videos are supported',
      uploadSizeLimit: 'Single file must be smaller than 5 MB',
      downloaded: 'Report download started'
    },
    uploadTip: 'Images and videos are supported. Single file must be under 5 MB.'
  },
  layout: {
    brandTitle: 'Project Issue Collaboration',
    navigation: 'Navigation',
    projectFirst: 'Project First',
    timezone: 'Asia/Shanghai',
    menus: {
      dashboard: 'Dashboard',
      issueProjects: 'Project Issue Library',
      projects: 'Projects',
      stats: 'Analytics',
      users: 'Users',
      dicts: 'Dictionary'
    },
    titles: {
      dashboard: 'Control Tower',
      issueProjects: 'Project Library Entry',
      issues: 'Project Issues',
      issueDetail: 'Issue Detail',
      projects: 'Project Management',
      stats: 'Statistics',
      users: 'User Management',
      dicts: 'Dictionary'
    }
  },
  login: {
    passwordChangeTitle: 'Change password required',
    passwordChangeDesc: 'For first login, you must set a strong password before continuing.',
    passwordPolicyHint: 'At least 10 characters with upper/lower case, number, and symbol.',
    passwordChangeAction: 'Update password',
    newPasswordPlaceholder: 'New password',
    confirmPasswordPlaceholder: 'Confirm new password',
    passwordMissing: 'Please enter and confirm your new password',
    mfaSetupTitle: 'Set up MFA',
    mfaSetupDesc: 'Scan the QR code with Authenticator, then enter the 6-digit code to verify.',
    mfaVerifyTitle: 'MFA verification',
    mfaVerifyDesc: 'Enter the 6-digit code from your Authenticator app.',
    mfaSecretLabel: 'Secret key',
    mfaCodePlaceholder: '6-digit code',
    mfaVerifyAction: 'Verify and continue',
    mfaCodeMissing: 'Please enter the MFA code',

    kicker: 'Project Issue Hub / Delivery Control',
    heroTitle: 'Make issue context as clear as the field, not as vague as an Excel row.',
    heroDesc: 'Use images, videos, owners, deadlines, and status flow to move issues directly.',
    signIn: 'Sign In',
    title: 'Sign in to Project Issue Hub',
    desc: 'Use your account to access project issues, collaboration, and analytics.',
    usernamePlaceholder: 'Enter username',
    passwordPlaceholder: 'Enter password',
    enter: 'Enter System',
    metric1: 'Understand the issue in 10 seconds',
    metric2: 'Single ownership and closed-loop status',
    metric3: 'Field first, visual driven',
    hint1: 'Web is for management and analytics. Mini app is for field capture and follow-up.',
    hint2: 'Status flow, permissions, and numbering are controlled by the backend.',
    hint3: 'Once media is uploaded, management can understand the issue immediately.',
    footnote: 'Start with admin / 123456, then create projects and issues.',
    missingCredentials: 'Please enter username and password'
  },
  page: {
    dashboard: 'Dashboard',
    projectSelector: 'Project Library Entry',
    issueList: 'Project Issues',
    issueDetail: 'Issue Detail',
    projects: 'Project Management',
    stats: 'Statistics',
    users: 'User Management',
    dict: 'Dictionary'
  },
  dashboard: {
    section: 'Control Tower',
    globalTitle: 'All Projects Cockpit',
    autoRotatePlaceholder: 'Auto-rotate when not selected',
    every8Seconds: 'Rotate every 8s',
    totalIssues: 'Total Issues',
    openIssues: 'Open Issues',
    highPriorityIssues: 'High Priority',
    overdueIssues: 'Overdue',
    trendTitle: 'Created / Closed Trend',
    focusTitle: 'Focus List',
    created: 'Created',
    closed: 'Closed'
  },
  projectSelector: {
    title: 'Choose a project before entering its issue library.',
    lead: 'Each project should have an independent issue pool.',
    filterTitle: 'Project Filter',
    filterSubtitle: 'Lock the project first, then open its issue list.',
    enterIssueLibrary: 'Open Issue Library',
    openProject: 'Open Project',
    empty: 'No projects match the current filters.'
  },
  issueList: {
    title: 'Project Issues',
    lead: 'Lock the project first, then review its OPL and current progress.',
    filterTitle: 'Filters',
    filterSubtitle: 'Filters only affect the list. Summary cards always use full-project scope.',
    tableTitle: 'Issue List',
    tableSubtitle: 'Pagination affects only the list, not project statistics.',
    infoColumn: 'Issue Info',
    issueNoPlaceholder: 'Issue No. / Title / Description',
    allOwners: 'All owners',
    allStatus: 'All status',
    allPriority: 'All priority',
    overdueOnly: 'Overdue only',
    overdueFlag: 'Overdue',
    tableCount: '{count} items',
    issueCreate: 'Create Issue',
    createHere: 'Log New Issue',
    currentProjectBar: 'Current Project',
    createDialogTitle: 'Create Issue'
  },
  issueDetail: {
    backToProject: 'Back to Project Library',
    deleteMistake: 'Delete Mistake Entry',
    noDescription: 'No additional description',
    appendAttachments: 'Add Attachments',
    appendTip: 'You can add images and videos after issue creation. Saved files appear in the main attachment gallery.',
    currentProgress: 'Current Progress',
    closeInfo: 'Closure Info',
    managementActions: 'Management Actions',
    assignOwner: 'Owner Assignment',
    priorityPanel: 'Priority',
    nextAction: 'Next Action',
    noFurtherAction: 'No further action is available for the current status',
    solutionDesc: 'Resolution Note',
    holdReason: 'On-hold Reason',
    progressRemark: 'Remark',
    submitFollowup: 'Submit Follow-up',
    followupPlaceholder: 'Add progress, result, or risk notes',
    timeline: 'Timeline',
    timelineEmpty: 'No timeline entries yet',
    systemRecord: 'System record',
    manualRecord: 'Manual follow-up',
    previewTitle: 'Attachment Preview',
    openAttachment: 'Open attachment'
  },
  project: {
    title: 'Project Management',
    projectList: 'Project List',
    issueLibrary: 'Issue Library',
    projectTeam: 'Project Team',
    newProject: 'Create Project',
    createProject: 'Create Project',
    teamTip: 'Issue assignment is limited to the current project team.',
    syncFromOpl: 'Sync Members from OPL',
    addMember: 'Add Member',
    importResult: 'Import Result',
    syncResult: 'Sync Result',
    formatError: 'Import Format Error'
  },
  stats: {
    title: 'Statistics',
    allProjects: 'All Projects',
    currentProject: 'Current Project',
    trendTitle: 'Created / Closed Trend',
    structureTitle: 'Issue Structure',
    created: 'Created',
    closed: 'Closed',
    closedIssues: 'Closed',
    openIssues: 'Open',
    highPriority: 'High Priority',
    overdue: 'Overdue'
  },
  user: {
    title: 'User Management',
    lead: 'Maintain accounts, system roles, WeChat binding, and project participation.',
    createUser: 'Create User',
    importUsers: 'Bulk Import',
    createUserTitle: 'Create User',
    editUserTitle: 'Edit User',
    importTitle: 'Bulk Import Users',
    initialPassword: 'Initial Password',
    projectParticipation: 'Project Participation',
    importResult: 'Import Result',
    formatError: 'Import Format Error'
  },
  dict: {
    title: 'Categories, sources, departments, and modules will be standardized here.',
    lead: 'Unified codes are the foundation for reporting, filtering, and permission linkage.',
    issueCategory: 'Issue Categories',
    issueSource: 'Issue Sources',
    moduleDept: 'Modules & Departments'
  },
  enums: {
    issueStatus: {
      NEW: 'New',
      IN_PROGRESS: 'In Progress',
      PENDING_VERIFY: 'Pending Verify',
      CLOSED: 'Closed',
      ON_HOLD: 'On Hold',
      CANCELED: 'Canceled'
    },
    priority: {
      LOW: 'Low',
      MEDIUM: 'Medium',
      HIGH: 'High',
      CRITICAL: 'Critical'
    },
    impact: {
      LOW: 'Low Impact',
      MEDIUM: 'Medium Impact',
      HIGH: 'High Impact',
      CRITICAL: 'Critical Impact'
    },
    projectStatus: {
      PLANNING: 'Planning',
      IN_PROGRESS: 'In Progress',
      DELIVERING: 'Delivering',
      CLOSED: 'Closed',
      CANCELED: 'Canceled'
    }
  },
  roles: {
    SITE_USER: 'Site User',
    RESP_ENGINEER: 'Responsible Engineer',
    PROJECT_MANAGER: 'Project Manager',
    MANAGEMENT: 'Management',
    ADMIN: 'Administrator',
    MFG_MANAGER: 'Manufacturing Manager',
    PLANT_MANAGER: 'Plant Manager',
    MECH_DESIGN_SUPERVISOR: 'Mechanical Design Supervisor',
    DESIGN_MANAGER: 'Design Manager',
    AUTOMATION_DESIGN_SUPERVISOR: 'Automation Design Supervisor',
    AUTOMATION_ENGINEER: 'Automation Engineer',
    MECHANICAL_ENGINEER: 'Mechanical Engineer',
    ELECTRICAL_TECHNICIAN: 'Electrical Technician',
    MECHANICAL_TECHNICIAN: 'Mechanical Technician',
    ME: 'ME',
    MDA: 'MDA',
    AE: 'AE',
    MD_MANAGER: 'MD Manager',
    IE: 'IE',
    LOGISTICS_SUPERVISOR: 'Logistics Supervisor',
    PROCUREMENT_MANAGER: 'Procurement Manager',
    FINANCE_MANAGER: 'Finance Manager',
    HR_MANAGER: 'HR Manager',
    QUALITY_MANAGER: 'Quality Manager',
    QUALITY_ENGINEER: 'Quality Engineer',
    PLANT_REPRESENTATIVE: 'Plant Representative'
  }
}

const thTH = { ...enUS, common: { ...enUS.common, language: 'ภาษา', locale: { ...enUS.common.locale, 'th-TH': 'ไทย' } } }
const jaJP = { ...enUS, common: { ...enUS.common, language: '言語', locale: { ...enUS.common.locale, 'ja-JP': '日本語' } } }
const koKR = { ...enUS, common: { ...enUS.common, language: '언어', locale: { ...enUS.common.locale, 'ko-KR': '한국어' } } }
const hiIN = { ...enUS, common: { ...enUS.common, language: 'भाषा', locale: { ...enUS.common.locale, 'hi-IN': 'हिन्दी' } } }

Object.assign(zhCN.issueList, {
  overdueOnlyLabel: '超期筛选',
  overdueFlag: '已超期',
  createTitlePlaceholder: '一句话说清问题本质',
  createDescriptionPlaceholder: '补充现场现象、影响和当前判断',
  category: '问题分类',
  source: '问题来源',
  selectCategory: '选择分类',
  selectSource: '选择来源',
  selectPriority: '选择优先级',
  selectImpact: '选择影响等级',
  device: '设备',
  module: '模块',
  optional: '可选',
  siteFlags: '现场标记',
  affectShipment: '影响出货',
  affectCommissioning: '影响联调',
  needShutdown: '需要停机',
  categoryOptions: {
    MECHANICAL: '机械',
    ELECTRICAL: '电气',
    AUTOMATION: '自动化',
    PROCESS: '工艺',
    QUALITY: '质量',
    DELIVERY: '交付',
    OTHER: '其他'
  },
  sourceOptions: {
    SITE_CHECK: '现场巡检',
    CUSTOMER_FEEDBACK: '客户反馈',
    COMMISSIONING: '联调发现',
    INTERNAL_REVIEW: '内部评审'
  },
  summary: {
    highPriority: '整个项目范围内需要优先处理的问题。',
    unassignedLabel: '待分派',
    unassignedNote: '整个项目范围内尚未明确责任人的问题。',
    processingLabel: '处理中 / 待验证',
    processingNote: '整个项目范围内正在推进闭环的问题。',
    overdueNote: '整个项目范围内已经超期的问题。'
  },
  validation: {
    titleRequired: '请填写问题标题'
  },
  messages: {
    created: '问题已创建'
  }
})

Object.assign(zhCN.common, {
  actionLabel: '操作'
})

Object.assign(enUS.issueList, {
  overdueOnlyLabel: 'Overdue Filter',
  overdueFlag: 'Overdue',
  createTitlePlaceholder: 'Describe the issue in one sentence',
  createDescriptionPlaceholder: 'Add on-site symptoms, impact, and current judgment',
  category: 'Category',
  source: 'Source',
  selectCategory: 'Select category',
  selectSource: 'Select source',
  selectPriority: 'Select priority',
  selectImpact: 'Select impact',
  device: 'Device',
  module: 'Module',
  optional: 'Optional',
  siteFlags: 'On-site Flags',
  affectShipment: 'Affects shipment',
  affectCommissioning: 'Affects commissioning',
  needShutdown: 'Requires shutdown',
  categoryOptions: {
    MECHANICAL: 'Mechanical',
    ELECTRICAL: 'Electrical',
    AUTOMATION: 'Automation',
    PROCESS: 'Process',
    QUALITY: 'Quality',
    DELIVERY: 'Delivery',
    OTHER: 'Other'
  },
  sourceOptions: {
    SITE_CHECK: 'Site Check',
    CUSTOMER_FEEDBACK: 'Customer Feedback',
    COMMISSIONING: 'Commissioning',
    INTERNAL_REVIEW: 'Internal Review'
  },
  summary: {
    highPriority: 'Issues in the current project that need immediate attention.',
    unassignedLabel: 'Unassigned',
    unassignedNote: 'Issues in this project that still do not have an owner.',
    processingLabel: 'In Progress / Pending Verify',
    processingNote: 'Issues in this project that are moving through the closure flow.',
    overdueNote: 'Issues in this project that have already passed the due time.'
  },
  validation: {
    titleRequired: 'Please enter the issue title'
  },
  messages: {
    created: 'Issue created'
  }
})

Object.assign(enUS.common, {
  actionLabel: 'Actions'
})

Object.assign(zhCN.project, {
  projectNo: '项目编号',
  projectName: '项目名称',
  customerName: '客户名称',
  projectCountLabel: '共 {count} 个项目',
  searchKeywordPlaceholder: '项目编号 / 项目名称 / 客户',
  managerRequired: '请选择项目经理',
  created: '项目已创建',
  deleted: '项目已删除'
})

Object.assign(enUS.project, {
  projectNo: 'Project No.',
  projectName: 'Project Name',
  customerName: 'Customer Name',
  projectCountLabel: '{count} projects',
  searchKeywordPlaceholder: 'Project No / Project Name / Customer',
  managerRequired: 'Please select the project manager',
  created: 'Project created',
  deleted: 'Project deleted'
})

Object.assign(zhCN.user, {
  searchKeywordPlaceholder: '账号 / 姓名 / 部门',
  usernameRequired: '账号和姓名不能为空',
  roleRequired: '至少选择一个角色',
  created: '用户已创建',
  updated: '用户已更新',
  passwordReset: '密码已重置',
  wechatUnbound: '微信已解绑'
})

Object.assign(enUS.user, {
  searchKeywordPlaceholder: 'Username / Name / Department',
  usernameRequired: 'Username and name are required',
  roleRequired: 'Please select at least one role',
  created: 'User created',
  updated: 'User updated',
  passwordReset: 'Password reset',
  wechatUnbound: 'WeChat unbound'
})

Object.assign(thTH, {
  issueList: { ...enUS.issueList },
  project: { ...enUS.project },
  user: { ...enUS.user }
})

Object.assign(jaJP, {
  issueList: { ...enUS.issueList },
  project: { ...enUS.project },
  user: { ...enUS.user }
})

Object.assign(koKR, {
  issueList: { ...enUS.issueList },
  project: { ...enUS.project },
  user: { ...enUS.user }
})

Object.assign(hiIN, {
  issueList: { ...enUS.issueList },
  project: { ...enUS.project },
  user: { ...enUS.user }
})

export const messages: Record<AppLocale, typeof zhCN> = {
  'zh-CN': zhCN,
  'en-US': enUS as typeof zhCN,
  'th-TH': thTH as typeof zhCN,
  'ja-JP': jaJP as typeof zhCN,
  'ko-KR': koKR as typeof zhCN,
  'hi-IN': hiIN as typeof zhCN
}

Object.assign(thTH, merge(thTH, {
  common: {
    language: 'ภาษา',
    actionLabel: 'การดำเนินการ',
    locale: { 'zh-CN': 'จีน', 'en-US': 'อังกฤษ', 'th-TH': 'ไทย', 'ja-JP': 'ญี่ปุ่น', 'ko-KR': 'เกาหลี', 'hi-IN': 'ฮินดี' },
    action: { refresh: 'รีเฟรช', search: 'ค้นหา', reset: 'รีเซ็ต', create: 'สร้าง', save: 'บันทึก', cancel: 'ยกเลิก', delete: 'ลบ', edit: 'แก้ไข', import: 'นำเข้า', exportReport: 'ส่งออกรายงาน', switchProject: 'สลับโครงการ', submit: 'ส่ง', logout: 'ออกจากระบบ', viewDetail: 'ดูรายละเอียด' },
    label: { keyword: 'คำค้น', status: 'สถานะ', priority: 'ความสำคัญ', owner: 'ผู้รับผิดชอบ', project: 'โครงการ', projectManager: 'ผู้จัดการโครงการ', startDate: 'วันเริ่มต้น', plannedEndDate: 'วันสิ้นสุดแผน', dueAt: 'กำหนดเสร็จ', attachments: 'ไฟล์แนบ', username: 'บัญชีผู้ใช้', realName: 'ชื่อ', department: 'แผนก' },
    empty: { unassigned: 'ยังไม่มอบหมาย', noRole: 'ไม่มีบทบาท' },
    status: { enabled: 'ใช้งาน', disabled: 'ปิดใช้งาน', locked: 'ล็อก', bound: 'ผูกแล้ว', unbound: 'ยังไม่ผูก' },
    message: { networkError: 'เครือข่ายผิดพลาด', unauthorized: 'ยังไม่ได้รับการยืนยันตัวตนหรือหมดอายุแล้ว', chooseProjectFirst: 'กรุณาเลือกโครงการก่อน', uploadImageVideoOnly: 'รองรับเฉพาะรูปภาพและวิดีโอ', uploadSizeLimit: 'ไฟล์ต้องไม่เกิน 5MB', downloaded: 'เริ่มดาวน์โหลดรายงานแล้ว' },
    uploadTip: 'รองรับรูปภาพและวิดีโอ ขนาดไฟล์เดี่ยวไม่เกิน 5MB'
  },
  layout: { brandTitle: 'ศูนย์ประสานงานปัญหาโครงการ', navigation: 'เมนู', projectFirst: 'เลือกโครงการก่อน', menus: { dashboard: 'แดชบอร์ด', issueProjects: 'คลังปัญหาโครงการ', projects: 'โครงการ', stats: 'สถิติ', users: 'ผู้ใช้', dicts: 'พจนานุกรม' }, titles: { dashboard: 'แดชบอร์ด', issueProjects: 'ทางเข้าคลังปัญหา', issues: 'รายการปัญหา', issueDetail: 'รายละเอียดปัญหา', projects: 'การจัดการโครงการ', stats: 'สถิติ', users: 'การจัดการผู้ใช้', dicts: 'พจนานุกรม' } },
  login: {
    passwordChangeTitle: 'Change password required',
    passwordChangeDesc: 'For first login, you must set a strong password before continuing.',
    passwordPolicyHint: 'At least 10 characters with upper/lower case, number, and symbol.',
    passwordChangeAction: 'Update password',
    newPasswordPlaceholder: 'New password',
    confirmPasswordPlaceholder: 'Confirm new password',
    passwordMissing: 'Please enter and confirm your new password',
    mfaSetupTitle: 'Set up MFA',
    mfaSetupDesc: 'Scan the QR code with Authenticator, then enter the 6-digit code to verify.',
    mfaVerifyTitle: 'MFA verification',
    mfaVerifyDesc: 'Enter the 6-digit code from your Authenticator app.',
    mfaSecretLabel: 'Secret key',
    mfaCodePlaceholder: '6-digit code',
    mfaVerifyAction: 'Verify and continue',
    mfaCodeMissing: 'Please enter the MFA code',
 signIn: 'เข้าสู่ระบบ', title: 'เข้าสู่ Project Issue Hub', desc: 'ใช้บัญชีของคุณเพื่อเข้าถึงปัญหาโครงการ การทำงานร่วมกัน และสถิติ', usernamePlaceholder: 'กรอกชื่อผู้ใช้', passwordPlaceholder: 'กรอกรหัสผ่าน', enter: 'เข้าสู่ระบบ', missingCredentials: 'กรุณากรอกชื่อผู้ใช้และรหัสผ่าน' },
  page: { dashboard: 'แดชบอร์ด', projectSelector: 'ทางเข้าโครงการ', issueList: 'รายการปัญหาโครงการ', issueDetail: 'รายละเอียดปัญหา', projects: 'การจัดการโครงการ', stats: 'สถิติ', users: 'การจัดการผู้ใช้', dict: 'พจนานุกรม' },
  dashboard: { globalTitle: 'ภาพรวมทุกโครงการ', totalIssues: 'ปัญหาทั้งหมด', openIssues: 'ปัญหาที่ยังไม่ปิด', highPriorityIssues: 'ปัญหาสำคัญสูง', overdueIssues: 'ปัญหาเกินกำหนด', trendTitle: 'แนวโน้มเปิด / ปิด', focusTitle: 'รายการที่ต้องติดตาม', created: 'เปิดใหม่', closed: 'ปิดแล้ว' },
  projectSelector: { title: 'เลือกโครงการก่อนเข้าสู่คลังปัญหา', lead: 'แต่ละโครงการควรมีคลังปัญหาแยกกัน', filterTitle: 'ตัวกรองโครงการ', filterSubtitle: 'ล็อกโครงการก่อน แล้วเปิดรายการปัญหา', enterIssueLibrary: 'เปิดคลังปัญหา', openProject: 'เปิดโครงการ', empty: 'ไม่มีโครงการที่ตรงกับเงื่อนไข' },
  issueList: { title: 'รายการปัญหาโครงการ', lead: 'เลือกโครงการก่อน แล้วตรวจสอบ OPL และความคืบหน้าของโครงการนั้น', filterTitle: 'ตัวกรอง', filterSubtitle: 'ตัวกรองมีผลเฉพาะรายการ การ์ดสรุปยังอิงทั้งโครงการ', infoColumn: 'ข้อมูลปัญหา', allOwners: 'ผู้รับผิดชอบทั้งหมด', allStatus: 'ทุกสถานะ', allPriority: 'ทุกระดับความสำคัญ', overdueOnlyLabel: 'ตัวกรองเกินกำหนด', overdueOnly: 'เฉพาะเกินกำหนด', overdueFlag: 'เกินกำหนด', tableTitle: 'รายการปัญหา', tableSubtitle: 'การแบ่งหน้ามีผลเฉพาะรายการ', issueCreate: 'สร้างปัญหา', createHere: 'บันทึกปัญหาใหม่', currentProjectBar: 'โครงการปัจจุบัน', createDialogTitle: 'สร้างปัญหา' },
  stats: { title: 'สถิติ', allProjects: 'ทุกโครงการ', currentProject: 'โครงการปัจจุบัน', trendTitle: 'แนวโน้มเปิด / ปิด', structureTitle: 'โครงสร้างปัญหา', created: 'เปิดใหม่', closed: 'ปิดแล้ว', closedIssues: 'ปิดแล้ว', openIssues: 'ยังไม่ปิด', highPriority: 'สำคัญสูง', overdue: 'เกินกำหนด' },
  user: { title: 'การจัดการผู้ใช้', lead: 'จัดการบัญชี บทบาท การผูก WeChat และการเข้าร่วมโครงการ', createUser: 'สร้างผู้ใช้', importUsers: 'นำเข้าจำนวนมาก', createUserTitle: 'สร้างผู้ใช้', editUserTitle: 'แก้ไขผู้ใช้', importTitle: 'นำเข้าผู้ใช้จำนวนมาก' }
}))

Object.assign(zhCN, merge(zhCN, {
  layout: {
    menus: { myIssues: '个人问题清单' },
    titles: { myIssues: '个人问题清单' }
  },
  page: { myIssues: '个人问题清单' },
  myIssues: {
    title: '我的问题总览',
    lead: '汇总当前登录人作为责任人的跨项目问题，并按计划完成日期优先排序。',
    filterTitle: '个人筛选',
    filterSubtitle: '支持按项目、问题属性、状态、优先级和超期筛选。',
    allProjects: '全部项目',
    tableTitle: '个人问题列表',
    tableSubtitle: '列表仅展示当前登录人负责的问题。',
    projectColumn: '所属项目',
    deadlineOrderHint: '按计划完成日期升序排列，无截止时间置后。'
  }
}))

Object.assign(enUS, merge(enUS, {
  layout: {
    menus: { myIssues: 'My Issues' },
    titles: { myIssues: 'My Issues' }
  },
  page: { myIssues: 'My Issues' },
  myIssues: {
    title: 'My Issue Queue',
    lead: 'See all cross-project issues currently owned by the signed-in user, ordered by planned due date.',
    filterTitle: 'My Filters',
    filterSubtitle: 'Filter by project, function, status, priority, and overdue condition.',
    allProjects: 'All Projects',
    tableTitle: 'My Issue List',
    tableSubtitle: 'This view only includes issues currently assigned to me.',
    projectColumn: 'Project',
    deadlineOrderHint: 'Sorted by planned due date ascending. Items without due dates are placed last.'
  }
}))

Object.assign(thTH, merge(thTH, {
  layout: {
    menus: { myIssues: 'My Issues' },
    titles: { myIssues: 'My Issues' }
  },
  page: { myIssues: 'My Issues' },
  myIssues: {
    title: 'My Issue Queue',
    lead: 'See all cross-project issues currently owned by the signed-in user, ordered by planned due date.',
    filterTitle: 'My Filters',
    filterSubtitle: 'Filter by project, function, status, priority, and overdue condition.',
    allProjects: 'All Projects',
    tableTitle: 'My Issue List',
    tableSubtitle: 'This view only includes issues currently assigned to me.',
    projectColumn: 'Project',
    deadlineOrderHint: 'Sorted by planned due date ascending. Items without due dates are placed last.'
  }
}))

Object.assign(jaJP, merge(jaJP, {
  layout: {
    menus: { myIssues: 'My Issues' },
    titles: { myIssues: 'My Issues' }
  },
  page: { myIssues: 'My Issues' },
  myIssues: {
    title: 'My Issue Queue',
    lead: 'See all cross-project issues currently owned by the signed-in user, ordered by planned due date.',
    filterTitle: 'My Filters',
    filterSubtitle: 'Filter by project, function, status, priority, and overdue condition.',
    allProjects: 'All Projects',
    tableTitle: 'My Issue List',
    tableSubtitle: 'This view only includes issues currently assigned to me.',
    projectColumn: 'Project',
    deadlineOrderHint: 'Sorted by planned due date ascending. Items without due dates are placed last.'
  }
}))

Object.assign(koKR, merge(koKR, {
  layout: {
    menus: { myIssues: 'My Issues' },
    titles: { myIssues: 'My Issues' }
  },
  page: { myIssues: 'My Issues' },
  myIssues: {
    title: 'My Issue Queue',
    lead: 'See all cross-project issues currently owned by the signed-in user, ordered by planned due date.',
    filterTitle: 'My Filters',
    filterSubtitle: 'Filter by project, function, status, priority, and overdue condition.',
    allProjects: 'All Projects',
    tableTitle: 'My Issue List',
    tableSubtitle: 'This view only includes issues currently assigned to me.',
    projectColumn: 'Project',
    deadlineOrderHint: 'Sorted by planned due date ascending. Items without due dates are placed last.'
  }
}))

Object.assign(hiIN, merge(hiIN, {
  layout: {
    menus: { myIssues: 'My Issues' },
    titles: { myIssues: 'My Issues' }
  },
  page: { myIssues: 'My Issues' },
  myIssues: {
    title: 'My Issue Queue',
    lead: 'See all cross-project issues currently owned by the signed-in user, ordered by planned due date.',
    filterTitle: 'My Filters',
    filterSubtitle: 'Filter by project, function, status, priority, and overdue condition.',
    allProjects: 'All Projects',
    tableTitle: 'My Issue List',
    tableSubtitle: 'This view only includes issues currently assigned to me.',
    projectColumn: 'Project',
    deadlineOrderHint: 'Sorted by planned due date ascending. Items without due dates are placed last.'
  }
}))

Object.assign(zhCN, merge(zhCN, {
  dashboard: {
    customer: '客户',
    projectScope: '{name} 驾驶舱',
    globalScope: '全部项目',
    cockpitSuffix: '驾驶舱',
    focus: {
      overdue: '{name} 当前有 {count} 项超期问题',
      overdueDesc: '请优先协调资源，避免交付节点继续受影响。',
      priority: '{name} 当前有 {count} 项高优先级问题',
      priorityDesc: '建议优先确认责任人、处理动作和现场支持需求。',
      open: '{name} 当前仍有 {count} 项未闭环问题',
      openDesc: '需要持续推进处理、验证与关闭证据留存。',
      clear: '{name} 当前没有重点告警',
      clearDesc: '项目整体处于可控状态，可继续按计划推进。'
    }
  },
  stats: {
    scope: '统计范围',
    globalOverview: '全部项目概览',
    allProjectChip: '全部项目',
    closeRate: '关闭率',
    highPriorityRate: '高优占比',
    overdueRate: '超期占比',
    totalIssues: '问题总数',
    highPriorityIssues: '高优问题',
    overdueIssues: '超期问题',
    recent7Days: '最近 7 天',
    currentStructure: '当前结构',
    notes: {
      totalIssues: '{scope}范围内累计问题总量。',
      openIssues: '当前仍在处理或待验证的问题。',
      highPriorityIssues: '当前需要优先协调推进的问题。',
      overdueIssues: '当前已超过目标时限的问题。'
    }
  },
  project: {
    totalProjects: '项目总数',
    inProgress: '进行中',
    planning: '规划中',
    teamMembers: '团队人数',
    notes: {
      totalProjects: '当前列表内项目总量。',
      inProgress: '当前状态为进行中的项目。',
      planning: '当前还处于规划阶段的项目。',
      teamMembers: '当前列表项目团队成员总数。'
    }
  },
  common: {
    actionLabel: '操作',
    label: {
      customer: '客户',
      description: '描述',
      teamSize: '团队人数'
    }
  }
}))

Object.assign(enUS, merge(enUS, {
  dashboard: {
    customer: 'Customer',
    projectScope: '{name} Cockpit',
    globalScope: 'All Projects',
    cockpitSuffix: 'Cockpit',
    focus: {
      overdue: '{name} has {count} overdue issues',
      overdueDesc: 'Coordinate resources first to avoid further delivery impact.',
      priority: '{name} has {count} high-priority issues',
      priorityDesc: 'Confirm owner, action, and on-site support as the first move.',
      open: '{name} still has {count} open issues',
      openDesc: 'Continue pushing handling, verification, and closure evidence.',
      clear: '{name} has no major alerts right now',
      clearDesc: 'The project is stable and can continue on the planned track.'
    }
  },
  stats: {
    scope: 'Scope',
    globalOverview: 'Global Overview',
    allProjectChip: 'All Projects',
    closeRate: 'Close Rate',
    highPriorityRate: 'High Priority Rate',
    overdueRate: 'Overdue Rate',
    totalIssues: 'Total Issues',
    highPriorityIssues: 'High Priority Issues',
    overdueIssues: 'Overdue Issues',
    recent7Days: 'Last 7 Days',
    currentStructure: 'Current Structure',
    notes: {
      totalIssues: 'Total issues within the {scope} scope.',
      openIssues: 'Issues that are still in handling or pending verification.',
      highPriorityIssues: 'Issues that need immediate coordination and attention.',
      overdueIssues: 'Issues that have already passed the target due time.'
    }
  },
  project: {
    totalProjects: 'Total Projects',
    inProgress: 'In Progress',
    planning: 'Planning',
    teamMembers: 'Team Members',
    notes: {
      totalProjects: 'Total projects in the current list.',
      inProgress: 'Projects currently in progress.',
      planning: 'Projects still in the planning stage.',
      teamMembers: 'Total members across the current project list.'
    }
  },
  common: {
    actionLabel: 'Actions',
    label: {
      customer: 'Customer',
      description: 'Description',
      teamSize: 'Team Size'
    }
  }
}))

Object.assign(thTH, merge(thTH, {
  dashboard: {
    customer: 'ลูกค้า',
    projectScope: '{name} แดชบอร์ด',
    globalScope: 'ทุกโครงการ',
    cockpitSuffix: 'แดชบอร์ด',
    focus: {
      overdue: '{name} มีปัญหาเกินกำหนด {count} รายการ',
      overdueDesc: 'ควรเร่งประสานทรัพยากรเพื่อหลีกเลี่ยงผลกระทบต่อการส่งมอบ',
      priority: '{name} มีปัญหาความสำคัญสูง {count} รายการ',
      priorityDesc: 'ควรยืนยันผู้รับผิดชอบ แผนงาน และทรัพยากรหน้างานก่อน',
      open: '{name} ยังมีปัญหาที่ไม่ปิด {count} รายการ',
      openDesc: 'ต้องติดตามการแก้ไข การยืนยัน และหลักฐานการปิดอย่างต่อเนื่อง',
      clear: '{name} ยังไม่มีสัญญาณเตือนสำคัญ',
      clearDesc: 'โครงการอยู่ในสถานะควบคุมได้และสามารถเดินหน้าตามแผน'
    }
  },
  stats: {
    scope: 'ขอบเขตสถิติ',
    globalOverview: 'ภาพรวมทุกโครงการ',
    allProjectChip: 'ทุกโครงการ',
    closeRate: 'อัตราการปิด',
    highPriorityRate: 'สัดส่วนความสำคัญสูง',
    overdueRate: 'สัดส่วนเกินกำหนด',
    totalIssues: 'จำนวนปัญหาทั้งหมด',
    highPriorityIssues: 'ปัญหาความสำคัญสูง',
    overdueIssues: 'ปัญหาเกินกำหนด',
    recent7Days: '7 วันที่ผ่านมา',
    currentStructure: 'โครงสร้างปัจจุบัน',
    notes: {
      totalIssues: 'จำนวนปัญหาทั้งหมดภายใต้ขอบเขต {scope}',
      openIssues: 'ปัญหาที่ยังอยู่ระหว่างดำเนินการหรือรอตรวจสอบ',
      highPriorityIssues: 'ปัญหาที่ต้องเร่งประสานและให้ความสำคัญก่อน',
      overdueIssues: 'ปัญหาที่เกินเวลาที่กำหนดแล้ว'
    }
  },
  project: {
    totalProjects: 'จำนวนโครงการ',
    inProgress: 'กำลังดำเนินการ',
    planning: 'อยู่ระหว่างวางแผน',
    teamMembers: 'จำนวนสมาชิกทีม',
    notes: {
      totalProjects: 'จำนวนโครงการทั้งหมดในรายการปัจจุบัน',
      inProgress: 'โครงการที่อยู่ระหว่างดำเนินการในขณะนี้',
      planning: 'โครงการที่ยังอยู่ในช่วงวางแผน',
      teamMembers: 'จำนวนสมาชิกทีมรวมของโครงการในรายการปัจจุบัน'
    }
  },
  common: {
    actionLabel: 'การดำเนินการ',
    label: {
      customer: 'ลูกค้า',
      description: 'รายละเอียด',
      teamSize: 'จำนวนทีม'
    }
  }
}))

Object.assign(jaJP, merge(jaJP, {
  dashboard: {
    customer: '顧客',
    projectScope: '{name} ダッシュボード',
    globalScope: '全プロジェクト',
    cockpitSuffix: 'ダッシュボード',
    focus: {
      overdue: '{name} に超期案件が {count} 件あります',
      overdueDesc: '納期影響を広げないため、優先的に支援と調整が必要です。',
      priority: '{name} に高優先度案件が {count} 件あります',
      priorityDesc: '担当者、処置方針、現場支援の要否を先に固めてください。',
      open: '{name} に未クローズ案件が {count} 件あります',
      openDesc: '処置、検証、クローズ証跡の追跡を継続する必要があります。',
      clear: '{name} に重要アラートはありません',
      clearDesc: 'プロジェクトは安定しており、計画どおり前進できます。'
    }
  },
  stats: {
    scope: '集計範囲',
    globalOverview: '全プロジェクト概況',
    allProjectChip: '全プロジェクト',
    closeRate: 'クローズ率',
    highPriorityRate: '高優先度比率',
    overdueRate: '超期比率',
    totalIssues: '問題総数',
    highPriorityIssues: '高優先度問題',
    overdueIssues: '超期問題',
    recent7Days: '直近7日',
    currentStructure: '現在構成',
    notes: {
      totalIssues: '{scope} 範囲内の問題総数です。',
      openIssues: '現在処理中または検証待ちの問題です。',
      highPriorityIssues: '優先的な調整と対応が必要な問題です。',
      overdueIssues: '目標期限を超過した問題です。'
    }
  },
  project: {
    totalProjects: 'プロジェクト総数',
    inProgress: '進行中',
    planning: '計画中',
    teamMembers: 'チーム人数',
    notes: {
      totalProjects: '現在の一覧に含まれるプロジェクト総数です。',
      inProgress: '現在進行中のプロジェクト数です。',
      planning: 'まだ計画段階にあるプロジェクト数です。',
      teamMembers: '現在の一覧に含まれるチーム人数の合計です。'
    }
  },
  common: {
    actionLabel: '操作',
    label: {
      customer: '顧客',
      description: '説明',
      teamSize: 'チーム人数'
    }
  }
}))

Object.assign(koKR, merge(koKR, {
  dashboard: {
    customer: '고객',
    projectScope: '{name} 대시보드',
    globalScope: '전체 프로젝트',
    cockpitSuffix: '대시보드',
    focus: {
      overdue: '{name}에 지연 이슈가 {count}건 있습니다',
      overdueDesc: '납기 영향 확대를 막기 위해 우선적으로 자원 조정이 필요합니다.',
      priority: '{name}에 고우선순위 이슈가 {count}건 있습니다',
      priorityDesc: '담당자, 조치 방향, 현장 지원 필요 여부를 먼저 확정해야 합니다.',
      open: '{name}에 미종결 이슈가 {count}건 남아 있습니다',
      openDesc: '처리, 검증, 종료 증빙까지 계속 추적해야 합니다.',
      clear: '{name}에는 현재 주요 경고가 없습니다',
      clearDesc: '프로젝트가 안정적이며 계획에 따라 계속 추진할 수 있습니다.'
    }
  },
  stats: {
    scope: '통계 범위',
    globalOverview: '전체 프로젝트 개요',
    allProjectChip: '전체 프로젝트',
    closeRate: '종결률',
    highPriorityRate: '고우선 비율',
    overdueRate: '지연 비율',
    totalIssues: '전체 이슈 수',
    highPriorityIssues: '고우선 이슈',
    overdueIssues: '지연 이슈',
    recent7Days: '최근 7일',
    currentStructure: '현재 구성',
    notes: {
      totalIssues: '{scope} 범위 내 전체 이슈 수입니다.',
      openIssues: '현재 처리 중이거나 검증 대기 중인 이슈입니다.',
      highPriorityIssues: '우선적으로 조정과 대응이 필요한 이슈입니다.',
      overdueIssues: '목표 기한을 초과한 이슈입니다.'
    }
  },
  project: {
    totalProjects: '프로젝트 수',
    inProgress: '진행 중',
    planning: '계획 중',
    teamMembers: '팀 인원',
    notes: {
      totalProjects: '현재 목록에 포함된 전체 프로젝트 수입니다.',
      inProgress: '현재 진행 중인 프로젝트 수입니다.',
      planning: '아직 계획 단계에 있는 프로젝트 수입니다.',
      teamMembers: '현재 목록의 전체 팀 인원 합계입니다.'
    }
  },
  common: {
    actionLabel: '작업',
    label: {
      customer: '고객',
      description: '설명',
      teamSize: '팀 인원'
    }
  }
}))

Object.assign(hiIN, merge(hiIN, {
  dashboard: {
    customer: 'ग्राहक',
    projectScope: '{name} डैशबोर्ड',
    globalScope: 'सभी प्रोजेक्ट',
    cockpitSuffix: 'डैशबोर्ड',
    focus: {
      overdue: '{name} में {count} ओवरड्यू मुद्दे हैं',
      overdueDesc: 'डिलीवरी पर असर बढ़ने से पहले संसाधन समन्वय को प्राथमिकता दें।',
      priority: '{name} में {count} उच्च प्राथमिकता वाले मुद्दे हैं',
      priorityDesc: 'पहले जिम्मेदार व्यक्ति, कार्रवाई और साइट सपोर्ट की पुष्टि करें।',
      open: '{name} में अभी भी {count} खुले मुद्दे हैं',
      openDesc: 'हैंडलिंग, वेरिफिकेशन और क्लोजर एविडेंस को लगातार ट्रैक करना होगा।',
      clear: '{name} में अभी कोई प्रमुख अलर्ट नहीं है',
      clearDesc: 'प्रोजेक्ट स्थिर है और योजना के अनुसार आगे बढ़ सकता है।'
    }
  },
  stats: {
    scope: 'सांख्यिकीय दायरा',
    globalOverview: 'सभी प्रोजेक्ट का अवलोकन',
    allProjectChip: 'सभी प्रोजेक्ट',
    closeRate: 'क्लोजर दर',
    highPriorityRate: 'उच्च प्राथमिकता अनुपात',
    overdueRate: 'ओवरड्यू अनुपात',
    totalIssues: 'कुल मुद्दे',
    highPriorityIssues: 'उच्च प्राथमिकता मुद्दे',
    overdueIssues: 'ओवरड्यू मुद्दे',
    recent7Days: 'पिछले 7 दिन',
    currentStructure: 'वर्तमान संरचना',
    notes: {
      totalIssues: '{scope} दायरे में कुल मुद्दों की संख्या।',
      openIssues: 'वे मुद्दे जो अभी भी प्रक्रिया में हैं या सत्यापन की प्रतीक्षा में हैं।',
      highPriorityIssues: 'वे मुद्दे जिन्हें तुरंत समन्वय और प्राथमिक कार्रवाई चाहिए।',
      overdueIssues: 'वे मुद्दे जो लक्ष्य समय सीमा से आगे निकल चुके हैं।'
    }
  },
  project: {
    totalProjects: 'कुल प्रोजेक्ट',
    inProgress: 'प्रगति में',
    planning: 'योजना में',
    teamMembers: 'टीम सदस्य',
    notes: {
      totalProjects: 'वर्तमान सूची में कुल प्रोजेक्ट की संख्या।',
      inProgress: 'वर्तमान में चल रहे प्रोजेक्ट की संख्या।',
      planning: 'वे प्रोजेक्ट जो अभी योजना चरण में हैं।',
      teamMembers: 'वर्तमान सूची के सभी प्रोजेक्टों के कुल टीम सदस्य।'
    }
  },
  common: {
    actionLabel: 'कार्य',
    label: {
      customer: 'ग्राहक',
      description: 'विवरण',
      teamSize: 'टीम आकार'
    }
  }
}))

Object.assign(zhCN, merge(zhCN, {
  common: {
    label: {
      issueFunction: '问题属性'
    }
  },
  issueList: {
    function: '问题属性',
    selectFunction: '选择问题属性',
    allFunction: '全部属性',
    validation: {
      titleRequired: '请填写问题标题',
      functionRequired: '请选择问题属性'
    }
  },
  issueDetail: {
    functionPanel: '问题属性',
    functionRemark: '备注',
    functionRemarkPlaceholder: '补充属性调整原因',
    updateFunction: '更新问题属性'
  },
  enums: {
    issueFunction: {
      PAT: 'PAT',
      FAT: 'FAT',
      DESIGN: '设计',
      SAFETY: '安全',
      LOGISTICS: '物流',
      PROCUREMENT: '采购',
      ASSEMBLY: '装配'
    }
  }
}))

Object.assign(enUS, merge(enUS, {
  common: {
    label: {
      issueFunction: 'Issue Function'
    }
  },
  issueList: {
    function: 'Issue Function',
    selectFunction: 'Select issue function',
    allFunction: 'All functions',
    validation: {
      titleRequired: 'Please enter the issue title',
      functionRequired: 'Please select the issue function'
    }
  },
  issueDetail: {
    functionPanel: 'Issue Function',
    functionRemark: 'Remark',
    functionRemarkPlaceholder: 'Add the reason for this function update',
    updateFunction: 'Update Issue Function'
  },
  enums: {
    issueFunction: {
      PAT: 'PAT',
      FAT: 'FAT',
      DESIGN: 'Design',
      SAFETY: 'Safety',
      LOGISTICS: 'Logistics',
      PROCUREMENT: 'Procurement',
      ASSEMBLY: 'Assembly'
    }
  }
}))

Object.assign(thTH, merge(thTH, {
  common: { label: { issueFunction: 'ประเภทปัญหา' } },
  issueList: { function: 'ประเภทปัญหา', selectFunction: 'เลือกประเภทปัญหา', allFunction: 'ทุกประเภท' },
  issueDetail: { functionPanel: 'ประเภทปัญหา', functionRemark: 'หมายเหตุ', functionRemarkPlaceholder: 'ระบุเหตุผลในการปรับประเภท', updateFunction: 'อัปเดตประเภทปัญหา' },
  enums: { issueFunction: { PAT: 'PAT', FAT: 'FAT', DESIGN: 'Design', SAFETY: 'Safety', LOGISTICS: 'Logistics', PROCUREMENT: 'Procurement', ASSEMBLY: 'Assembly' } }
}))

Object.assign(jaJP, merge(jaJP, {
  common: { label: { issueFunction: '問題属性' } },
  issueList: { function: '問題属性', selectFunction: '問題属性を選択', allFunction: '全属性' },
  issueDetail: { functionPanel: '問題属性', functionRemark: '備考', functionRemarkPlaceholder: '属性変更理由を補足', updateFunction: '問題属性を更新' },
  enums: { issueFunction: { PAT: 'PAT', FAT: 'FAT', DESIGN: '設計', SAFETY: '安全', LOGISTICS: '物流', PROCUREMENT: '調達', ASSEMBLY: '組立' } }
}))

Object.assign(koKR, merge(koKR, {
  common: { label: { issueFunction: '이슈 속성' } },
  issueList: { function: '이슈 속성', selectFunction: '이슈 속성 선택', allFunction: '전체 속성' },
  issueDetail: { functionPanel: '이슈 속성', functionRemark: '비고', functionRemarkPlaceholder: '속성 변경 사유를 보충하세요', updateFunction: '이슈 속성 업데이트' },
  enums: { issueFunction: { PAT: 'PAT', FAT: 'FAT', DESIGN: '설계', SAFETY: '안전', LOGISTICS: '물류', PROCUREMENT: '구매', ASSEMBLY: '조립' } }
}))

Object.assign(hiIN, merge(hiIN, {
  common: { label: { issueFunction: 'इश्यू गुण' } },
  issueList: { function: 'इश्यू गुण', selectFunction: 'इश्यू गुण चुनें', allFunction: 'सभी गुण' },
  issueDetail: { functionPanel: 'इश्यू गुण', functionRemark: 'टिप्पणी', functionRemarkPlaceholder: 'इस गुण परिवर्तन का कारण लिखें', updateFunction: 'इश्यू गुण अपडेट करें' },
  enums: { issueFunction: { PAT: 'PAT', FAT: 'FAT', DESIGN: 'डिज़ाइन', SAFETY: 'सुरक्षा', LOGISTICS: 'लॉजिस्टिक्स', PROCUREMENT: 'प्रोक्योरमेंट', ASSEMBLY: 'असेंबली' } }
}))

Object.assign(jaJP, merge(jaJP, {
  common: {
    language: '言語',
    actionLabel: '操作',
    locale: { 'zh-CN': '中国語', 'en-US': '英語', 'th-TH': 'タイ語', 'ja-JP': '日本語', 'ko-KR': '韓国語', 'hi-IN': 'ヒンディー語' },
    action: { refresh: '更新', search: '検索', reset: 'リセット', create: '新規作成', save: '保存', cancel: 'キャンセル', delete: '削除', edit: '編集', import: '一括取込', exportReport: 'レポート出力', switchProject: 'プロジェクト切替', submit: '送信', logout: 'ログアウト', viewDetail: '詳細表示' },
    label: { keyword: 'キーワード', status: '状態', priority: '優先度', owner: '担当者', project: 'プロジェクト', projectManager: 'プロジェクトマネージャー', startDate: '開始日', plannedEndDate: '計画終了日', dueAt: '期限', attachments: '添付', username: 'アカウント', realName: '氏名', department: '部門' },
    empty: { unassigned: '未割当', noRole: 'ロール未設定' },
    status: { enabled: '有効', disabled: '無効', locked: 'ロック', bound: '連携済み', unbound: '未連携' },
    message: { networkError: 'ネットワークエラー', unauthorized: '未認証、または認証の有効期限切れです', chooseProjectFirst: '先にプロジェクトを選択してください', uploadImageVideoOnly: '画像と動画のみ対応しています', uploadSizeLimit: 'ファイルは 5MB 以下にしてください', downloaded: 'レポートのダウンロードを開始しました' },
    uploadTip: '画像と動画に対応、単一ファイルは 5MB 以下です'
  },
  layout: { brandTitle: 'プロジェクト課題連携プラットフォーム', navigation: 'ナビゲーション', projectFirst: 'まずプロジェクト選択', menus: { dashboard: 'ダッシュボード', issueProjects: 'プロジェクト課題庫', projects: 'プロジェクト', stats: '統計分析', users: 'ユーザー', dicts: '辞書' }, titles: { dashboard: 'ダッシュボード', issueProjects: '課題庫入口', issues: '課題一覧', issueDetail: '課題詳細', projects: 'プロジェクト管理', stats: '統計分析', users: 'ユーザー管理', dicts: '辞書' } },
  login: {
    passwordChangeTitle: 'Change password required',
    passwordChangeDesc: 'For first login, you must set a strong password before continuing.',
    passwordPolicyHint: 'At least 10 characters with upper/lower case, number, and symbol.',
    passwordChangeAction: 'Update password',
    newPasswordPlaceholder: 'New password',
    confirmPasswordPlaceholder: 'Confirm new password',
    passwordMissing: 'Please enter and confirm your new password',
    mfaSetupTitle: 'Set up MFA',
    mfaSetupDesc: 'Scan the QR code with Authenticator, then enter the 6-digit code to verify.',
    mfaVerifyTitle: 'MFA verification',
    mfaVerifyDesc: 'Enter the 6-digit code from your Authenticator app.',
    mfaSecretLabel: 'Secret key',
    mfaCodePlaceholder: '6-digit code',
    mfaVerifyAction: 'Verify and continue',
    mfaCodeMissing: 'Please enter the MFA code',
 signIn: 'ログイン', title: 'Project Issue Hub にログイン', desc: 'アカウントでプロジェクト課題、協働、分析にアクセスします', usernamePlaceholder: 'ユーザー名を入力', passwordPlaceholder: 'パスワードを入力', enter: 'システムへ入る', missingCredentials: 'ユーザー名とパスワードを入力してください' },
  page: { dashboard: 'ダッシュボード', projectSelector: 'プロジェクト入口', issueList: 'プロジェクト課題一覧', issueDetail: '課題詳細', projects: 'プロジェクト管理', stats: '統計分析', users: 'ユーザー管理', dict: '辞書' },
  dashboard: { globalTitle: '全プロジェクトのダッシュボード', totalIssues: '課題総数', openIssues: '未クローズ課題', highPriorityIssues: '高優先課題', overdueIssues: '期限超過課題', trendTitle: '新規 / クローズ推移', focusTitle: '注目リスト', created: '新規', closed: 'クローズ' },
  projectSelector: { title: '先にプロジェクトを選択してから課題ライブラリへ入ります', lead: '各プロジェクトは独立した課題プールを持つべきです', filterTitle: 'プロジェクト絞り込み', filterSubtitle: 'まずプロジェクトを固定し、その後課題一覧を開きます', enterIssueLibrary: '課題ライブラリを開く', openProject: 'プロジェクトを開く', empty: '条件に一致するプロジェクトはありません' },
  issueList: { title: 'プロジェクト課題一覧', lead: '先にプロジェクトを固定し、その OPL と進捗を確認します', filterTitle: 'フィルター', filterSubtitle: 'フィルターは一覧のみに影響し、サマリーはプロジェクト全体を対象とします', infoColumn: '課題情報', allOwners: '全担当者', allStatus: '全ステータス', allPriority: '全優先度', overdueOnlyLabel: '期限超過フィルター', overdueOnly: '期限超過のみ', overdueFlag: '期限超過', tableTitle: '課題一覧', tableSubtitle: 'ページングは一覧のみに影響します', issueCreate: '課題作成', createHere: '新規課題登録', currentProjectBar: '現在のプロジェクト', createDialogTitle: '課題作成' },
  stats: { title: '統計分析', allProjects: '全プロジェクト', currentProject: '現在のプロジェクト', trendTitle: '新規 / クローズ推移', structureTitle: '課題構成', created: '新規', closed: 'クローズ', closedIssues: 'クローズ済み', openIssues: '未クローズ', highPriority: '高優先', overdue: '期限超過' },
  user: { title: 'ユーザー管理', lead: 'アカウント、ロール、WeChat連携、プロジェクト参加を管理します', createUser: 'ユーザー作成', importUsers: '一括インポート', createUserTitle: 'ユーザー作成', editUserTitle: 'ユーザー編集', importTitle: 'ユーザー一括インポート' }
}))

Object.assign(koKR, merge(koKR, {
  common: {
    language: '언어',
    actionLabel: '작업',
    locale: { 'zh-CN': '중국어', 'en-US': '영어', 'th-TH': '태국어', 'ja-JP': '일본어', 'ko-KR': '한국어', 'hi-IN': '힌디어' },
    action: { refresh: '새로고침', search: '검색', reset: '초기화', create: '생성', save: '저장', cancel: '취소', delete: '삭제', edit: '편집', import: '일괄 가져오기', exportReport: '보고서 내보내기', switchProject: '프로젝트 전환', submit: '제출', logout: '로그아웃', viewDetail: '상세 보기' },
    label: { keyword: '키워드', status: '상태', priority: '우선순위', owner: '담당자', project: '프로젝트', projectManager: '프로젝트 관리자', startDate: '시작일', plannedEndDate: '계획 종료일', dueAt: '마감일', attachments: '첨부파일', username: '계정', realName: '이름', department: '부서' },
    empty: { unassigned: '미배정', noRole: '역할 없음' },
    status: { enabled: '사용', disabled: '비활성', locked: '잠김', bound: '연동됨', unbound: '미연동' },
    message: { networkError: '네트워크 오류', unauthorized: '인증이 필요하거나 세션이 만료되었습니다', chooseProjectFirst: '먼저 프로젝트를 선택하세요', uploadImageVideoOnly: '이미지와 비디오만 지원합니다', uploadSizeLimit: '파일은 5MB 이하여야 합니다', downloaded: '보고서 다운로드가 시작되었습니다' },
    uploadTip: '이미지와 비디오를 지원하며 단일 파일은 5MB 이하여야 합니다'
  },
  layout: { brandTitle: '프로젝트 이슈 협업 플랫폼', navigation: '탐색', projectFirst: '프로젝트 우선', menus: { dashboard: '대시보드', issueProjects: '프로젝트 이슈 라이브러리', projects: '프로젝트', stats: '통계', users: '사용자', dicts: '사전' }, titles: { dashboard: '대시보드', issueProjects: '이슈 라이브러리 입구', issues: '이슈 목록', issueDetail: '이슈 상세', projects: '프로젝트 관리', stats: '통계 분석', users: '사용자 관리', dicts: '사전' } },
  login: {
    passwordChangeTitle: 'Change password required',
    passwordChangeDesc: 'For first login, you must set a strong password before continuing.',
    passwordPolicyHint: 'At least 10 characters with upper/lower case, number, and symbol.',
    passwordChangeAction: 'Update password',
    newPasswordPlaceholder: 'New password',
    confirmPasswordPlaceholder: 'Confirm new password',
    passwordMissing: 'Please enter and confirm your new password',
    mfaSetupTitle: 'Set up MFA',
    mfaSetupDesc: 'Scan the QR code with Authenticator, then enter the 6-digit code to verify.',
    mfaVerifyTitle: 'MFA verification',
    mfaVerifyDesc: 'Enter the 6-digit code from your Authenticator app.',
    mfaSecretLabel: 'Secret key',
    mfaCodePlaceholder: '6-digit code',
    mfaVerifyAction: 'Verify and continue',
    mfaCodeMissing: 'Please enter the MFA code',
 signIn: '로그인', title: 'Project Issue Hub 로그인', desc: '계정으로 프로젝트 이슈, 협업, 분석에 접근합니다', usernamePlaceholder: '사용자명을 입력하세요', passwordPlaceholder: '비밀번호를 입력하세요', enter: '시스템 입장', missingCredentials: '사용자명과 비밀번호를 입력하세요' },
  page: { dashboard: '대시보드', projectSelector: '프로젝트 입구', issueList: '프로젝트 이슈 목록', issueDetail: '이슈 상세', projects: '프로젝트 관리', stats: '통계 분석', users: '사용자 관리', dict: '사전' },
  dashboard: { globalTitle: '전체 프로젝트 대시보드', totalIssues: '총 이슈 수', openIssues: '미종결 이슈', highPriorityIssues: '높은 우선순위', overdueIssues: '기한 초과', trendTitle: '신규 / 종료 추이', focusTitle: '집중 관리 목록', created: '신규', closed: '종료' },
  projectSelector: { title: '프로젝트를 먼저 선택한 뒤 이슈 라이브러리로 들어갑니다', lead: '각 프로젝트는 독립된 이슈 풀을 가져야 합니다', filterTitle: '프로젝트 필터', filterSubtitle: '프로젝트를 고정한 뒤 이슈 목록을 엽니다', enterIssueLibrary: '이슈 라이브러리 열기', openProject: '프로젝트 열기', empty: '조건에 맞는 프로젝트가 없습니다' },
  issueList: { title: '프로젝트 이슈 목록', lead: '먼저 프로젝트를 고정한 뒤 해당 프로젝트의 OPL과 진행 상태를 확인합니다', filterTitle: '필터', filterSubtitle: '필터는 목록에만 영향을 주며 상단 요약은 프로젝트 전체 기준입니다', infoColumn: '이슈 정보', allOwners: '전체 담당자', allStatus: '전체 상태', allPriority: '전체 우선순위', overdueOnlyLabel: '기한 초과 필터', overdueOnly: '기한 초과만', overdueFlag: '기한 초과', tableTitle: '이슈 목록', tableSubtitle: '페이지는 목록에만 영향을 줍니다', issueCreate: '이슈 생성', createHere: '새 이슈 등록', currentProjectBar: '현재 프로젝트', createDialogTitle: '이슈 생성' },
  stats: { title: '통계 분석', allProjects: '전체 프로젝트', currentProject: '현재 프로젝트', trendTitle: '신규 / 종료 추이', structureTitle: '이슈 구조', created: '신규', closed: '종료', closedIssues: '종료됨', openIssues: '열림', highPriority: '높은 우선순위', overdue: '기한 초과' },
  user: { title: '사용자 관리', lead: '계정, 역할, WeChat 바인딩, 프로젝트 참여를 관리합니다', createUser: '사용자 생성', importUsers: '일괄 가져오기', createUserTitle: '사용자 생성', editUserTitle: '사용자 수정', importTitle: '사용자 일괄 가져오기' }
}))

Object.assign(hiIN, merge(hiIN, {
  common: {
    language: 'भाषा',
    actionLabel: 'क्रियाएँ',
    locale: { 'zh-CN': 'चीनी', 'en-US': 'अंग्रेज़ी', 'th-TH': 'थाई', 'ja-JP': 'जापानी', 'ko-KR': 'कोरियाई', 'hi-IN': 'हिंदी' },
    action: { refresh: 'रीफ़्रेश', search: 'खोज', reset: 'रीसेट', create: 'बनाएँ', save: 'सहेजें', cancel: 'रद्द करें', delete: 'हटाएँ', edit: 'संपादित करें', import: 'समूह आयात', exportReport: 'रिपोर्ट निर्यात करें', switchProject: 'प्रोजेक्ट बदलें', submit: 'जमा करें', logout: 'लॉग आउट', viewDetail: 'विवरण देखें' },
    label: { keyword: 'कीवर्ड', status: 'स्थिति', priority: 'प्राथमिकता', owner: 'जिम्मेदार व्यक्ति', project: 'प्रोजेक्ट', projectManager: 'प्रोजेक्ट मैनेजर', startDate: 'आरंभ तिथि', plannedEndDate: 'योजना समाप्ति', dueAt: 'समय सीमा', attachments: 'संलग्नक', username: 'खाता', realName: 'नाम', department: 'विभाग' },
    empty: { unassigned: 'असाइन नहीं', noRole: 'कोई भूमिका नहीं' },
    status: { enabled: 'सक्रिय', disabled: 'निष्क्रिय', locked: 'लॉक', bound: 'बाउंड', unbound: 'अनबाउंड' },
    message: { networkError: 'नेटवर्क त्रुटि', unauthorized: 'प्रमाणीकरण आवश्यक है या सत्र समाप्त हो गया है', chooseProjectFirst: 'कृपया पहले प्रोजेक्ट चुनें', uploadImageVideoOnly: 'केवल चित्र और वीडियो समर्थित हैं', uploadSizeLimit: 'फ़ाइल 5MB से कम होनी चाहिए', downloaded: 'रिपोर्ट डाउनलोड शुरू हो गया है' },
    uploadTip: 'चित्र और वीडियो समर्थित हैं, एकल फ़ाइल 5MB से कम होनी चाहिए'
  },
  layout: { brandTitle: 'प्रोजेक्ट इश्यू सहयोग मंच', navigation: 'नेविगेशन', projectFirst: 'प्रोजेक्ट पहले', menus: { dashboard: 'डैशबोर्ड', issueProjects: 'प्रोजेक्ट इश्यू लाइब्रेरी', projects: 'प्रोजेक्ट', stats: 'सांख्यिकी', users: 'उपयोगकर्ता', dicts: 'शब्दकोश' }, titles: { dashboard: 'डैशबोर्ड', issueProjects: 'इश्यू लाइब्रेरी प्रवेश', issues: 'इश्यू सूची', issueDetail: 'इश्यू विवरण', projects: 'प्रोजेक्ट प्रबंधन', stats: 'सांख्यिकी', users: 'उपयोगकर्ता प्रबंधन', dicts: 'शब्दकोश' } },
  login: {
    passwordChangeTitle: 'Change password required',
    passwordChangeDesc: 'For first login, you must set a strong password before continuing.',
    passwordPolicyHint: 'At least 10 characters with upper/lower case, number, and symbol.',
    passwordChangeAction: 'Update password',
    newPasswordPlaceholder: 'New password',
    confirmPasswordPlaceholder: 'Confirm new password',
    passwordMissing: 'Please enter and confirm your new password',
    mfaSetupTitle: 'Set up MFA',
    mfaSetupDesc: 'Scan the QR code with Authenticator, then enter the 6-digit code to verify.',
    mfaVerifyTitle: 'MFA verification',
    mfaVerifyDesc: 'Enter the 6-digit code from your Authenticator app.',
    mfaSecretLabel: 'Secret key',
    mfaCodePlaceholder: '6-digit code',
    mfaVerifyAction: 'Verify and continue',
    mfaCodeMissing: 'Please enter the MFA code',
 signIn: 'साइन इन', title: 'Project Issue Hub में लॉगिन करें', desc: 'अपने खाते से प्रोजेक्ट इश्यू, सहयोग और विश्लेषण तक पहुँचें', usernamePlaceholder: 'उपयोगकर्ता नाम दर्ज करें', passwordPlaceholder: 'पासवर्ड दर्ज करें', enter: 'सिस्टम में प्रवेश', missingCredentials: 'कृपया उपयोगकर्ता नाम और पासवर्ड दर्ज करें' },
  page: { dashboard: 'डैशबोर्ड', projectSelector: 'प्रोजेक्ट प्रवेश', issueList: 'प्रोजेक्ट इश्यू सूची', issueDetail: 'इश्यू विवरण', projects: 'प्रोजेक्ट प्रबंधन', stats: 'सांख्यिकी', users: 'उपयोगकर्ता प्रबंधन', dict: 'शब्दकोश' },
  dashboard: { globalTitle: 'सभी प्रोजेक्ट का डैशबोर्ड', totalIssues: 'कुल इश्यू', openIssues: 'खुले इश्यू', highPriorityIssues: 'उच्च प्राथमिकता', overdueIssues: 'समय सीमा पार', trendTitle: 'नया / बंद रुझान', focusTitle: 'मुख्य फोकस सूची', created: 'नया', closed: 'बंद' },
  projectSelector: { title: 'इश्यू लाइब्रेरी में जाने से पहले प्रोजेक्ट चुनें', lead: 'हर प्रोजेक्ट का अपना स्वतंत्र इश्यू पूल होना चाहिए', filterTitle: 'प्रोजेक्ट फ़िल्टर', filterSubtitle: 'पहले प्रोजेक्ट तय करें, फिर उसकी इश्यू सूची खोलें', enterIssueLibrary: 'इश्यू लाइब्रेरी खोलें', openProject: 'प्रोजेक्ट खोलें', empty: 'मौजूदा फ़िल्टर से कोई प्रोजेक्ट नहीं मिला' },
  issueList: { title: 'प्रोजेक्ट इश्यू सूची', lead: 'पहले प्रोजेक्ट चुनें, फिर उसी प्रोजेक्ट का OPL और प्रगति देखें', filterTitle: 'फ़िल्टर', filterSubtitle: 'फ़िल्टर केवल सूची को प्रभावित करते हैं, सारांश कार्ड पूरे प्रोजेक्ट पर आधारित रहते हैं', infoColumn: 'इश्यू जानकारी', allOwners: 'सभी जिम्मेदार', allStatus: 'सभी स्थिति', allPriority: 'सभी प्राथमिकता', overdueOnlyLabel: 'ओवरड्यू फ़िल्टर', overdueOnly: 'केवल ओवरड्यू', overdueFlag: 'ओवरड्यू', tableTitle: 'इश्यू सूची', tableSubtitle: 'पेजिंग केवल सूची को प्रभावित करती है', issueCreate: 'इश्यू बनाएँ', createHere: 'नया इश्यू दर्ज करें', currentProjectBar: 'वर्तमान प्रोजेक्ट', createDialogTitle: 'इश्यू बनाएँ' },
  stats: { title: 'सांख्यिकी', allProjects: 'सभी प्रोजेक्ट', currentProject: 'वर्तमान प्रोजेक्ट', trendTitle: 'नया / बंद रुझान', structureTitle: 'इश्यू संरचना', created: 'नया', closed: 'बंद', closedIssues: 'बंद', openIssues: 'खुले', highPriority: 'उच्च प्राथमिकता', overdue: 'समय सीमा पार' },
  user: { title: 'उपयोगकर्ता प्रबंधन', lead: 'खाते, भूमिकाएँ, WeChat बाइंडिंग और प्रोजेक्ट भागीदारी प्रबंधित करें', createUser: 'उपयोगकर्ता बनाएँ', importUsers: 'समूह आयात', createUserTitle: 'उपयोगकर्ता बनाएँ', editUserTitle: 'उपयोगकर्ता संपादित करें', importTitle: 'समूह उपयोगकर्ता आयात' }
}))
