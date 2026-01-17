import React from 'react';
import './StatusBadge.css';

const StatusBadge = ({ status, children }) => {
  return (
    <span className={`status-badge status-${status?.toLowerCase() || 'default'}`}>
      {children || status}
    </span>
  );
};

export default StatusBadge;
