import React from 'react';
import { Box, Typography, Paper } from '@mui/material';

const Users = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        User Management
      </Typography>
      <Paper sx={{ p: 3 }}>
        <Typography variant="body1" color="text.secondary">
          User management interface coming soon...
        </Typography>
      </Paper>
    </Box>
  );
};

export default Users;
