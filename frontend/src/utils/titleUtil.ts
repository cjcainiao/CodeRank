// 页面标题设置
import type { RouteLocationNormalized } from 'vue-router'

// 站点名称
const SITE_NAME = 'CodeRank'

// 从路由信息中取出页面标题，未声明时返回空
function routeTitle(route: RouteLocationNormalized): string {
  const title = route.meta.title ?? route.meta.backendTitle
  return typeof title === 'string' ? title : ''
}

// 按路由设置页面标题，未声明标题时只显示站点名
export function setPageTitle(route: RouteLocationNormalized): void {
  const title = routeTitle(route)
  document.title = title ? `${title} - ${SITE_NAME}` : SITE_NAME
}
