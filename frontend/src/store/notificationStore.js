import { create } from 'zustand';
import { notificationAPI } from '../services/api';

const useNotificationStore = create((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,
  error: null,

  // Actions
  fetchNotifications: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const response = await notificationAPI.getNotifications(params);
      set({
        notifications: response.data.content || response.data,
        isLoading: false,
      });
      return response.data;
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to fetch notifications';
      set({ isLoading: false, error: errorMessage });
      throw error;
    }
  },

  fetchUnreadNotifications: async () => {
    try {
      const response = await notificationAPI.getUnreadNotifications();
      set({ notifications: response.data });
      return response.data;
    } catch (error) {
      console.error('Failed to fetch unread notifications:', error);
      throw error;
    }
  },

  fetchUnreadCount: async () => {
    try {
      const response = await notificationAPI.getUnreadNotificationCount();
      set({ unreadCount: response.data.count });
      return response.data.count;
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
      throw error;
    }
  },

  markAsRead: async (notificationId) => {
    try {
      await notificationAPI.markAsRead(notificationId);
      
      // Update local state
      set((state) => ({
        notifications: state.notifications.map((notification) =>
          notification.id === notificationId
            ? { ...notification, isRead: true }
            : notification
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      }));
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
      throw error;
    }
  },

  markAllAsRead: async () => {
    try {
      await notificationAPI.markAllAsRead();
      
      // Update local state
      set((state) => ({
        notifications: state.notifications.map((notification) => ({
          ...notification,
          isRead: true,
        })),
        unreadCount: 0,
      }));
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error);
      throw error;
    }
  },

  addNotification: (notification) => {
    set((state) => ({
      notifications: [notification, ...state.notifications],
      unreadCount: state.unreadCount + 1,
    }));
  },

  removeNotification: (notificationId) => {
    set((state) => ({
      notifications: state.notifications.filter(
        (notification) => notification.id !== notificationId
      ),
    }));
  },

  clearError: () => set({ error: null }),

  // Real-time notification handler
  handleRealTimeNotification: (notification) => {
    const { addNotification } = get();
    addNotification(notification);
    
    // Show browser notification if permission granted
    if (Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/favicon.ico',
        tag: notification.id,
      });
    }
  },

  // Request notification permission
  requestNotificationPermission: async () => {
    if ('Notification' in window) {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    }
    return false;
  },
}));

export default useNotificationStore;
