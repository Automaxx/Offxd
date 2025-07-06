import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    // Don't add auth token to auth endpoints (login, register, forgot-password, reset-password)
    const isAuthEndpoint = config.url?.startsWith('/auth/');

    if (!isAuthEndpoint) {
      const token = localStorage.getItem('accessToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });

          const { accessToken } = response.data;
          localStorage.setItem('accessToken', accessToken);

          // Retry the original request
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken }),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) => api.post('/auth/reset-password', { token, newPassword }),
  validateToken: () => api.get('/auth/validate'),
};

// User API
export const userAPI = {
  getCurrentUser: () => api.get('/users/me'),
  updateProfile: (userData) => api.put('/users/me', userData),
  changePassword: (passwordData) => api.post('/users/me/change-password', passwordData),
  getUsers: (params) => api.get('/users', { params }),
  getUserById: (id) => api.get(`/users/${id}`),
  updateUserRole: (id, role) => api.put(`/users/${id}/role`, null, { params: { role } }),
  toggleUserStatus: (id) => api.put(`/users/${id}/toggle-status`),
  deleteUser: (id) => api.delete(`/users/${id}`),
  getUserStatistics: () => api.get('/users/statistics'),
};

// File API
export const fileAPI = {
  uploadFile: (formData) => api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  getFiles: (params) => api.get('/files', { params }),
  downloadFile: (id) => api.get(`/files/download/${id}`, { responseType: 'blob' }),
  shareFile: (shareData) => api.post('/files/share', shareData),
  getFileShares: (id) => api.get(`/files/${id}/shares`),
  toggleFileVisibility: (id) => api.put(`/files/${id}/visibility`),
  deleteFile: (id) => api.delete(`/files/${id}`),
  getUserFolders: () => api.get('/files/folders'),
  getFileStatistics: () => api.get('/files/statistics'),
};

// Department API
export const departmentAPI = {
  getDepartments: (params) => api.get('/departments', { params }),
  getDepartmentById: (id) => api.get(`/departments/${id}`),
  createDepartment: (departmentData) => api.post('/departments', departmentData),
  updateDepartment: (id, departmentData) => api.put(`/departments/${id}`, departmentData),
  deleteDepartment: (id) => api.delete(`/departments/${id}`),
  getUserDepartments: () => api.get('/departments/my'),
  addUserToDepartment: (departmentId, userId) => api.post(`/departments/${departmentId}/users/${userId}`),
  removeUserFromDepartment: (departmentId, userId) => api.delete(`/departments/${departmentId}/users/${userId}`),
  getDepartmentUsers: (id) => api.get(`/departments/${id}/users`),
};

// Message API
export const messageAPI = {
  sendMessage: (messageData) => api.post('/messages', messageData),
  getDirectMessages: (params) => api.get('/messages/direct', { params }),
  getDepartmentMessages: (params) => api.get('/messages/department', { params }),
  getAnnouncements: (params) => api.get('/messages/announcements', { params }),
  searchMessages: (params) => api.get('/messages/search', { params }),
  getUnreadMessages: () => api.get('/messages/unread'),
  getUnreadMessageCount: () => api.get('/messages/unread/count'),
  markMessageAsRead: (id) => api.put(`/messages/${id}/read`),
};

// Notification API
export const notificationAPI = {
  getNotifications: (params) => api.get('/notifications', { params }),
  getUnreadNotifications: () => api.get('/notifications/unread'),
  getUnreadNotificationCount: () => api.get('/notifications/unread/count'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
};

// Analytics API
export const analyticsAPI = {
  getDashboardStatistics: () => api.get('/analytics/dashboard'),
  getUserActivityStatistics: (days) => api.get('/analytics/user-activity', { params: { days } }),
  getFileStatistics: (days) => api.get('/analytics/files', { params: { days } }),
  getMessageStatistics: (days) => api.get('/analytics/messages', { params: { days } }),
  getSystemHealthStatistics: () => api.get('/analytics/system-health'),
  getActivityTrends: (days) => api.get('/analytics/activity-trends', { params: { days } }),
  getUserEngagementMetrics: () => api.get('/analytics/user-engagement'),
};

export default api;
