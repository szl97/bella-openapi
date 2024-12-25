'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

export function LogsSidebar() {
  const pathname = usePathname()

  return (
    <div className="w-64 bg-white border-r border-gray-200 min-h-screen p-4">
      <div className="space-y-4">
        <div className="text-sm font-medium text-gray-500 uppercase">日志查询</div>
        <nav className="space-y-2">
          <Link
            href="/logs"
            className={`flex items-center px-3 py-2 text-sm rounded-md ${
              pathname === '/logs'
                ? 'text-blue-600 bg-blue-50'
                : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'
            }`}
          >
            OpenAPI 服务查询
          </Link>
          <Link
            href="/logs/trace"
            className={`flex items-center px-3 py-2 text-sm rounded-md ${
              pathname === '/logs/trace'
                ? 'text-blue-600 bg-blue-50'
                : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'
            }`}
          >
            Bella 链路查询
          </Link>
        </nav>
      </div>
    </div>
  )
}
