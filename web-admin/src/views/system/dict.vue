<template>
  <div class="placeholder-page">
    <el-card class="hero-card">
      <p class="opl-section-title">Dictionary</p>
      <h2 class="opl-page-title">分类、来源、部门和模块会在这里标准化。</h2>
      <p class="opl-page-lead">
        字典是整个系统的数据底座。没有统一编码，后续的统计、筛选和权限联动都会逐步失真。
      </p>
    </el-card>

    <section class="opl-grid-3">
      <el-card class="placeholder-card">
        <h3 class="opl-card-title">问题分类</h3>
        <p class="opl-card-subtitle">机械、电气、软件、工艺、质量等分类统一编码。</p>
      </el-card>
      <el-card class="placeholder-card">
        <h3 class="opl-card-title">问题来源</h3>
        <p class="opl-card-subtitle">现场巡检、客户反馈、联调验证、出货检查等来源统一口径。</p>
      </el-card>
      <el-card class="placeholder-card">
        <h3 class="opl-card-title">模块与部门</h3>
        <p class="opl-card-subtitle">模块、责任部门和业务标签统一配置，统计分析才有意义。</p>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.placeholder-page {
  display: grid;
  gap: 16px;
}

.hero-card {
  background:
    radial-gradient(circle at right top, rgba(15, 118, 110, 0.14), transparent 32%),
    radial-gradient(circle at left, rgba(203, 93, 31, 0.12), transparent 28%),
    linear-gradient(145deg, rgba(255, 251, 246, 0.94), rgba(255, 255, 255, 0.78));
}

.placeholder-card {
  min-height: 160px;
}
</style>
