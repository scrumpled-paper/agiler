import { createBrowserRouter } from 'react-router-dom'
import MainLayout from './components/layout/MainLayout'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import NotFound from './pages/NotFound'
import Home from './pages/Home'
import Login from './pages/Login'
import DashBoard from './pages/DashBoard'
import Project from './pages/Project'
import ProjectSetting from './pages/ProjectSetting'
import ProjectUserSetting from './pages/ProjectUserSetting'
import DailyScrumList from './pages/DailyScrumList'
import DailyScrum from './pages/DailyScrum'
import ProjectManagement from './pages/ProjectManagement'

export const routers = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: <Home />,
  },
  {
    path: '/',
    element: <MainLayout />,
    errorElement: <NotFound />,
    children: [
      {
        path: 'dashboard',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute>
                <DashBoard />
              </ProtectedRoute>
            ),
          },
          {
            path: 'settings',
            element: (
              <ProtectedRoute>
                <ProjectSetting />
              </ProtectedRoute>
            ),
          },
        ],
      },
      {
        path: 'projects/:projectUrl',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute>
                <Project />
              </ProtectedRoute>
            ),
          },
          {
            path: 'settings',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute>
                    <ProjectSetting />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'users',
                element: (
                  <ProtectedRoute>
                    <ProjectUserSetting />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'project-management',
                element: (
                  <ProtectedRoute>
                    <ProjectManagement />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          {
            path: 'daily-scrum',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute>
                    <DailyScrumList />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':scrumId',
                element: (
                  <ProtectedRoute>
                    <DailyScrum />
                  </ProtectedRoute>
                ),
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
