import React from 'react';

export default function StatusBadge({ status }) {
  if (!status) return null;
  const s = String(status).toUpperCase();

  let className = 'badge-pending';
  if (['ACTIVE', 'AUTHORISED', 'COMPLETED', 'UP', 'CONNECTED'].includes(s)) {
    className = 'badge-active';
  } else if (['REJECTED', 'FAILED', 'SUSPENDED', 'CLOSED', 'DOWN', 'DISCONNECTED'].includes(s)) {
    className = 'badge-rejected';
  }

  return (
    <span className={`badge ${className}`}>
      <span style={{ 
        width: '6px', 
        height: '6px', 
        borderRadius: '50%', 
        backgroundColor: 'currentColor' 
      }} />
      {s}
    </span>
  );
}
