import { createBrowserRouter } from 'react-router-dom'
import MainLayout from './components/layout/MainLayout'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import NotFound from './pages/NotFound'
import Home from './pages/Home'
import Login from './pages/Login'
import DashBoard from './pages/DashBoard'
import Project from './pages/Project'
import ProjectSetting from './pages/projectSettings/ProjectSetting'
import ProjectUserSetting from './pages/projectSettings/ProjectUserSetting'
import DailyScrum from './pages/DailyScrum'
import ProjectManagement from './pages/projectSettings/ProjectManagement'
import ProjectLabelSetting from './pages/projectSettings/ProjectLabelSetting'
import ProjectTemplateSetting from './pages/projectSettings/ProjectTemplateSetting'
import ProjectNotificationsSetting from './pages/projectSettings/ProjectNotificationsSetting'
import DailyScrumsList from './pages/DailyScrumsList'
import MeetingsList from './pages/MeetingsList'
import RetrospectivesList from './pages/RetrospectList'

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
                    <ProjectUserSetting />
                    {/*[ ]  셋팅페이지 완성 후 수정 예정입니다 */}
                    {/* <ProjectSetting /> */}
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
              {
                path: 'project-label',
                element: (
                  <ProtectedRoute>
                    <ProjectLabelSetting />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'project-template',
                element: (
                  <ProtectedRoute>
                    <ProjectTemplateSetting />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'notifications',
                element: (
                  <ProtectedRoute>
                    <ProjectNotificationsSetting />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          {
            path: 'dailyscrums',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute>
                    <DailyScrumsList />
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
          {
            path: 'retrospectives',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute>
                    <RetrospectivesList />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':retrospectiveId',
                element: (
                  <ProtectedRoute>
                    <DailyScrum />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          {
            path: 'meetings',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute>
                    <MeetingsList />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':meetingId',
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
