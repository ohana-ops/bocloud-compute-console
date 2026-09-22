import request from '@/utils/request'

// 查询资源分配记录列表
export function listAlloc(query) {
  return request({
    url: '/bocompute/alloc/list',
    method: 'get',
    params: query
  })
}

// 查询我的资源列表
export function listMyAlloc(query) {
  return request({
    url: '/bocompute/alloc/myList',
    method: 'get',
    params: query
  })
}

// 查询分配记录详细
export function getAlloc(allocId) {
  return request({
    url: '/bocompute/alloc/' + allocId,
    method: 'get'
  })
}

// 释放资源
export function releaseAlloc(allocId) {
  return request({
    url: '/bocompute/alloc/release/' + allocId,
    method: 'put'
  })
}

// 删除分配记录
export function delAlloc(allocId) {
  return request({
    url: '/bocompute/alloc/' + allocId,
    method: 'delete'
  })
}
