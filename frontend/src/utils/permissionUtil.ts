// 登录用户的权限判断
import type { RouteLocationNormalized } from 'vue-router'

// 当前登录用户，字段与后端返回保持一致
export interface UserInfo {
  id: number
  username: string
  role: string
}

// 读取路由声明的角色列表，配置异常时按未声明处理
function declaredRoles(route: RouteLocationNormalized): string[] {
  const declared = route.meta?.roles
  if (!Array.isArray(declared)) return []
  return declared.filter((role): role is string => typeof role === 'string')
}

// 判断当前用户能否访问目标路由
export function canAccessRoute(route: RouteLocationNormalized, userInfo: UserInfo | null): boolean {
  // 未要求登录的页面直接放行
  if (route.meta?.requiresAuth !== true) return true
  // 要求登录但当前没有用户信息时拒绝
  if (!userInfo) return false

  const roles = declaredRoles(route)
  // 未声明角色或声明通配符时，登录用户均可访问
  if (roles.length === 0 || roles.includes('*')) return true
  return roles.includes(userInfo.role)
}
