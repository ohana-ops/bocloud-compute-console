import request from '@/utils/request'

// 查询算力设备列表
export function listDevice(query) {
  return request({
    url: '/bocompute/device/list',
    method: 'get',
    params: query
  })
}

// 查询算力设备详细
export function getDevice(deviceId) {
  return request({
    url: '/bocompute/device/' + deviceId,
    method: 'get'
  })
}

// 查询某次分配绑定的设备清单
export function listDeviceByAlloc(allocId) {
  return request({
    url: '/bocompute/device/byAlloc/' + allocId,
    method: 'get'
  })
}

// 新增算力设备
export function addDevice(data) {
  return request({
    url: '/bocompute/device',
    method: 'post',
    data: data
  })
}

// 按资源池批量初始化设备
export function batchInitDevice(data) {
  return request({
    url: '/bocompute/device/batchInit',
    method: 'post',
    data: data
  })
}

// 修改算力设备
export function updateDevice(data) {
  return request({
    url: '/bocompute/device',
    method: 'put',
    data: data
  })
}

// 设备设为故障
export function faultDevice(deviceId) {
  return request({
    url: '/bocompute/device/fault/' + deviceId,
    method: 'put'
  })
}

// 设备设为维护
export function maintainDevice(deviceId) {
  return request({
    url: '/bocompute/device/maintain/' + deviceId,
    method: 'put'
  })
}

// 设备上线（故障/维护 -> 空闲）
export function onlineDevice(deviceId) {
  return request({
    url: '/bocompute/device/online/' + deviceId,
    method: 'put'
  })
}

// 删除算力设备
export function delDevice(deviceId) {
  return request({
    url: '/bocompute/device/' + deviceId,
    method: 'delete'
  })
}
