import request from '@/utils/request'

// 查询看板统计数据
export function getStatistics() {
  return request({
    url: '/bocompute/dashboard/statistics',
    method: 'get'
  })
}
