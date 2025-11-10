import { createBrowserRouter } from 'react-router-dom'
import MainLayout from './components/layout/MainLayout'
import NotFound from './pages/NotFound'
import Home from './pages/Home'
import DashBoard from './pages/DashBoard'
import Project from './pages/Project'
import ProjectSetting from './pages/ProjectSetting'
import DailyScrumList from './pages/DailyScrumList'
import DailyScrum from './pages/DailyScrum'

export const routers = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    errorElement: <NotFound />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'login',
        element: <div>login</div>,
      },
      {
        path: 'dashboard',
        children: [
          {
            index: true,
            element: <DashBoard />,
          },
          {
            path: 'settings',
            element: <ProjectSetting />,
          },
        ],
      },
      {
        path: 'projects/:projectUrl',
        children: [
          {
            index: true,
            element: <Project />,
          },
          {
            path: 'settings',
            element: <ProjectSetting />,
          },
          {
            path: 'daily-scrum',
            children: [
              {
                index: true,
                element: <DailyScrumList />,
              },
              {
                path: ':scrumId',
                element: <DailyScrum />,
              },
            ],
          },
        ],
      },
    ],
  },
])

// const history = createBrowserHistory()
// export const router = createRouter({
//   history,
//   routes,
// }).initialize()
