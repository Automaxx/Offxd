import React from 'react';
import { Box, Typography, Paper } from '@mui/material';

const Notifications = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Notifications
      </Typography>
      <Paper sx={{ p: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Notifications interface coming soon...
        </Typography>
      </Paper>
    </Box>
  );
};

export default Notifications;
