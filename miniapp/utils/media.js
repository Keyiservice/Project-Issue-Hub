const MAX_MEDIA_SIZE = 5 * 1024 * 1024
const MAX_VIDEO_DURATION = 15
const VIDEO_COMPRESS_QUALITIES = ['medium', 'low']

function chooseMedia(options) {
  return new Promise((resolve, reject) => {
    wx.chooseMedia({
      ...options,
      success: resolve,
      fail: reject
    })
  })
}

function chooseVideo(options) {
  return new Promise((resolve, reject) => {
    wx.chooseVideo({
      ...options,
      success: resolve,
      fail: reject
    })
  })
}

function getFileInfo(filePath) {
  return new Promise((resolve, reject) => {
    wx.getFileInfo({
      filePath,
      success: resolve,
      fail: reject
    })
  })
}

function getVideoInfo(src) {
  return new Promise((resolve, reject) => {
    wx.getVideoInfo({
      src,
      success: resolve,
      fail: reject
    })
  })
}

function hasNumber(value) {
  return typeof value === 'number' && !Number.isNaN(value)
}

function compressImage(src, quality) {
  return new Promise((resolve, reject) => {
    wx.compressImage({
      src,
      quality,
      success: resolve,
      fail: reject
    })
  })
}

function compressVideo(src, quality) {
  return new Promise((resolve, reject) => {
    if (typeof wx.compressVideo !== 'function') {
      reject(new Error('当前环境不支持视频压缩'))
      return
    }

    wx.compressVideo({
      src,
      quality,
      success: resolve,
      fail: reject
    })
  })
}

async function ensureFileSize(filePath) {
  const info = await getFileInfo(filePath)
  return info.size
}

function formatSizeInMb(size) {
  return `${(size / 1024 / 1024).toFixed(1)}MB`
}

async function normalizeImage(file) {
  let currentPath = file.tempFilePath
  let size = file.size || await ensureFileSize(currentPath)

  for (const quality of [80, 65, 50]) {
    if (size <= MAX_MEDIA_SIZE) {
      break
    }
    const compressed = await compressImage(currentPath, quality)
    currentPath = compressed.tempFilePath
    size = await ensureFileSize(currentPath)
  }

  if (size > MAX_MEDIA_SIZE) {
    throw new Error(`图片压缩后仍超过 5MB，当前约 ${formatSizeInMb(size)}`)
  }

  return {
    ...file,
    tempFilePath: currentPath,
    size
  }
}

async function normalizeVideo(file) {
  let currentPath = file.tempFilePath
  let duration = hasNumber(file.duration) ? file.duration : null
  let size = hasNumber(file.size) ? file.size : null

  if (size === null) {
    size = await ensureFileSize(currentPath)
  }

  if (duration === null) {
    try {
      const videoInfo = await getVideoInfo(currentPath)
      duration = videoInfo.duration
      size = videoInfo.size || size
    } catch (error) {
      const ffmpegError = error && error.errMsg && error.errMsg.includes('ffmpeg')
      if (!ffmpegError) {
        throw error
      }
      console.warn('[issue-create] skip getVideoInfo in devtools because ffmpeg is unavailable')
    }
  }

  console.info('[issue-create] raw video selected', {
    duration,
    size
  })

  if (hasNumber(duration) && duration > MAX_VIDEO_DURATION + 0.5) {
    throw new Error(`视频时长不能超过 ${MAX_VIDEO_DURATION} 秒，当前约 ${Math.ceil(duration)} 秒`)
  }

  if (size > MAX_MEDIA_SIZE && typeof wx.compressVideo === 'function') {
    for (const quality of VIDEO_COMPRESS_QUALITIES) {
      try {
        const compressed = await compressVideo(currentPath, quality)
        currentPath = compressed.tempFilePath
        size = await ensureFileSize(currentPath)
        if (!hasNumber(duration)) {
          try {
            const videoInfo = await getVideoInfo(currentPath)
            duration = videoInfo.duration
            size = videoInfo.size || size
          } catch (error) {
            console.warn('[issue-create] getVideoInfo after compress failed', error)
          }
        }
        console.info('[issue-create] video compressed', {
          quality,
          duration,
          size
        })
        if (size <= MAX_MEDIA_SIZE) {
          break
        }
      } catch (error) {
        console.warn('[issue-create] video compression failed', quality, error)
      }
    }
  }

  if (size > MAX_MEDIA_SIZE) {
    throw new Error(
      `视频压缩后仍超过 5MB，当前约 ${formatSizeInMb(size)}。开发者工具若未安装 ffmpeg，无法继续压缩，请改用更短视频、安装 ffmpeg，或用真机上传`
    )
  }

  return {
    ...file,
    tempFilePath: currentPath,
    size,
    duration: duration || 0
  }
}

async function prepareImages(maxCount) {
  const res = await chooseMedia({
    count: maxCount,
    mediaType: ['image'],
    sourceType: ['camera', 'album'],
    sizeType: ['compressed']
  })
  const files = []
  for (const item of res.tempFiles || []) {
    files.push(await normalizeImage(item))
  }
  return files
}

async function prepareVideo() {
  const res = await chooseVideo({
    sourceType: ['camera', 'album'],
    compressed: true,
    maxDuration: MAX_VIDEO_DURATION,
    camera: 'back'
  })

  if (!res || !res.tempFilePath) {
    return []
  }

  return [await normalizeVideo({
    tempFilePath: res.tempFilePath,
    size: res.size,
    duration: res.duration,
    thumbTempFilePath: res.thumbTempFilePath,
    fileType: 'video'
  })]
}

module.exports = {
  MAX_MEDIA_SIZE,
  MAX_VIDEO_DURATION,
  prepareImages,
  prepareVideo
}
