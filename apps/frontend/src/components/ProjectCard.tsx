import type { ProjectInfo } from '@/types/index'
import { Link } from 'react-router-dom'

export default function ProjectCard({ props }: { props: ProjectInfo }) {
  const { title, url, imageUrl, summary } = props

  return (
    <Link to={url} className="group block">
      <div className="w-full max-w-[340px] h-[420px] border border-gray-200 rounded-xl overflow-hidden bg-white shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
        {/* 이미지 영역 */}
        <div className="h-[240px] w-full bg-gradient-to-br from-gray-50 to-gray-100 overflow-hidden flex justify-center items-center">
          <img
            src={imageUrl || 'https://placehold.co/600x400?text=No+Image'}
            alt={title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        </div>

        {/* 텍스트 영역 */}
        <div className="h-[180px] p-5 flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2  transition-colors">
              {title}
            </h2>
            <p className="text-sm text-gray-600 line-clamp-3 leading-relaxed">
              {summary || 'No description available'}
            </p>
          </div>
        </div>
      </div>
    </Link>
  )
}
