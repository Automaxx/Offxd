import React, { useEffect } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import useAuthStore from './store/authStore'
import ProtectedRoute from './components/ProtectedRoute'
import MainLayout from './components/Layout/MainLayout'

// Lazy load components
const Login = React.lazy(() => import('./pages/auth/Login'))
const Register = React.lazy(() => import('./pages/auth/Register'))
const ForgotPassword = React.lazy(() => import('./pages/auth/ForgotPassword'))
const ResetPassword = React.lazy(() => import('./pages/auth/ResetPassword'))
const Dashboard = React.lazy(() => import('./pages/Dashboard'))
const Files = React.lazy(() => import('./pages/Files'))
const Messages = React.lazy(() => import('./pages/Messages'))
const Departments = React.lazy(() => import('./pages/Departments'))
const Users = React.lazy(() => import('./pages/Users'))
const Analytics = React.lazy(() => import('./pages/Analytics'))
const Profile = React.lazy(() => import('./pages/Profile'))
const Notifications = React.lazy(() => import('./pages/Notifications'))
const Unauthorized = React.lazy(() => import('./pages/Unauthorized'))
const NotFound = React.lazy(() => import('./pages/NotFound'))

function App() {
  const { isAuthenticated, initializeAuth } = useAuthStore()

  useEffect(() => {
    initializeAuth()
  }, [initializeAuth])

  return (
    <React.Suspense
      fallback={
        <Box
          display="flex"
          justifyContent="center"
          alignItems="center"
          minHeight="100vh"
        >
          <CircularProgress />
        </Box>
      }
    >
      <Routes>
        {/* Public routes */}
        <Route
          path="/login"
          element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <Login />}
        />
        <Route
          path="/register"
          element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <Register />}
        />
        <Route
          path="/forgot-password"
          element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <ForgotPassword />}
        />
        <Route
          path="/reset-password"
          element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <ResetPassword />}
        />

        {/* Protected routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="files" element={<Files />} />
          <Route path="messages" element={<Messages />} />
          <Route path="departments" element={<Departments />} />
          <Route path="profile" element={<Profile />} />
          <Route path="notifications" element={<Notifications />} />

          {/* Admin/Manager only routes */}
          <Route
            path="users"
            element={
              <ProtectedRoute requiredRoles={['ADMIN', 'MANAGER']}>
                <Users />
              </ProtectedRoute>
            }
          />
          <Route
            path="analytics"
            element={
              <ProtectedRoute requiredRoles={['ADMIN', 'MANAGER']}>
                <Analytics />
              </ProtectedRoute>
            }
          />
        </Route>

        {/* Error routes */}
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </React.Suspense>
  )
}

export default App
