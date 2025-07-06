import React from 'react';
import { Box, Typography, Paper } from '@mui/material';

const Departments = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Departments
      </Typography>
      <Paper sx={{ p: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Department management interface coming soon...
        </Typography>
      </Paper>
    </Box>
  );
};

export default Departments;
