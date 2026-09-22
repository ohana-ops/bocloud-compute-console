import request from '@/utils/request'

// 查询我的申请列表
export function listMyApply(query) {
  return request({
    url: '/bocompute/apply/myList',
    method: 'get',
    params: query
  })
}

// 查询全部申请列表（审批管理员用）
export function listApply(query) {
  return request({
    url: '/bocompute/apply/list',
    method: 'get',
    params: query
  })
}

// 查询申请单详细
export function getApply(applyId) {
  return request({
    url: '/bocompute/apply/' + applyId,
    method: 'get'
  })
}

// 提交资源申请
export function addApply(data) {
  return request({
    url: '/bocompute/apply',
    method: 'post',
    data: data
  })
}

// 修改资源申请
export function updateApply(data) {
  return request({
    url: '/bocompute/apply',
    method: 'put',
    data: data
  })
}

// 取消申请
export function cancelApply(applyId) {
  return request({
    url: '/bocompute/apply/cancel/' + applyId,
    method: 'put'
  })
}

// 审批通过
export function passApply(data) {
  return request({
    url: '/bocompute/apply/pass',
    method: 'put',
    data: data
  })
}

// 审批驳回
export function rejectApply(data) {
  return request({
    url: '/bocompute/apply/reject',
    method: 'put',
    data: data
  })
}

// 删除申请单
export function delApply(applyId) {
  return request({
    url: '/bocompute/apply/' + applyId,
    method: 'delete'
  })
}
