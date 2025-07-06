import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  People,
  Folder,
  Message,
  Notifications,
} from '@mui/icons-material';
import useAuthStore from '../store/authStore';
import { analyticsAPI } from '../services/api';

const StatCard = ({ title, value, icon, color = 'primary' }) => (
  <Card>
    <CardContent>
      <Box display="flex" alignItems="center" justifyContent="space-between">
        <Box>
          <Typography color="textSecondary" gutterBottom variant="h6">
            {title}
          </Typography>
          <Typography variant="h4">
            {value}
          </Typography>
        </Box>
        <Box color={`${color}.main`}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuthStore();

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        setLoading(true);
        const response = await analyticsAPI.getDashboardStatistics();
        setStats(response.data);
      } catch (err) {
        setError('Failed to load dashboard statistics');
        console.error('Dashboard stats error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Welcome back, {user?.firstName}!
      </Typography>
      
      <Typography variant="body1" color="text.secondary" gutterBottom sx={{ mb: 3 }}>
        Here's what's happening in your workspace today.
      </Typography>

      <Grid container spacing={3}>
        {/* Statistics Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={stats?.totalUsers || 0}
            icon={<People fontSize="large" />}
            color="primary"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Files"
            value={stats?.totalFiles || 0}
            icon={<Folder fontSize="large" />}
            color="secondary"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Messages"
            value={stats?.totalMessages || 0}
            icon={<Message fontSize="large" />}
            color="success"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Storage Used"
            value={stats?.totalFileSizeFormatted || '0 B'}
            icon={<Folder fontSize="large" />}
            color="warning"
          />
        </Grid>

        {/* Recent Activity */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activity (24h)
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                File Uploads: {stats?.recentFileUploads || 0}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Messages Sent: {stats?.recentMessages || 0}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Notifications: {stats?.recentNotifications || 0}
              </Typography>
            </Box>
          </Paper>
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              System Overview
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Active Users: {stats?.activeUsers || 0}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Departments: {stats?.totalDepartments || 0}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Administrators: {stats?.adminCount || 0}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Managers: {stats?.managerCount || 0}
              </Typography>
            </Box>
          </Paper>
        </Grid>

        {/* Welcome Message */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Getting Started
            </Typography>
            <Typography variant="body1" paragraph>
              Welcome to SecureOffice Communication Hub! Here are some things you can do:
            </Typography>
            <Box component="ul" sx={{ pl: 2 }}>
              <Typography component="li" variant="body2" gutterBottom>
                Upload and share files securely with your team
              </Typography>
              <Typography component="li" variant="body2" gutterBottom>
                Send direct messages or communicate with your department
              </Typography>
              <Typography component="li" variant="body2" gutterBottom>
                Stay updated with real-time notifications
              </Typography>
              <Typography component="li" variant="body2" gutterBottom>
                Manage your profile and account settings
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
