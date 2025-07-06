import React from 'react';
import { Box, Typography, Paper } from '@mui/material';

const Messages = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Messages
      </Typography>
      <Paper sx={{ p: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Messaging interface coming soon...
        </Typography>
      </Paper>
    </Box>
  );
};

export default Messages;
