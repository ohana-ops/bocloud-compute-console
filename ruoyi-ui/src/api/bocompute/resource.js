import request from '@/utils/request'

// 查询算力资源列表
export function listResource(query) {
  return request({
    url: '/bocompute/resource/list',
    method: 'get',
    params: query
  })
}

// 查询算力资源详细
export function getResource(resourceId) {
  return request({
    url: '/bocompute/resource/' + resourceId,
    method: 'get'
  })
}

// 查询所有可用算力资源（下拉选择用）
export function optionselectResource() {
  return request({
    url: '/bocompute/resource/optionselect',
    method: 'get'
  })
}

// 新增算力资源
export function addResource(data) {
  return request({
    url: '/bocompute/resource',
    method: 'post',
    data: data
  })
}

// 修改算力资源
export function updateResource(data) {
  return request({
    url: '/bocompute/resource',
    method: 'put',
    data: data
  })
}

// 删除算力资源
export function delResource(resourceId) {
  return request({
    url: '/bocompute/resource/' + resourceId,
    method: 'delete'
  })
}
