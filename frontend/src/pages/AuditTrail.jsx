import React, { useState, useEffect } from 'react';
import { ShieldCheck, Activity, Search, RefreshCw, Lock, CheckCircle, XCircle } from 'lucide-react';
import { ConsentApi } from '../api/axiosClient';

export default function AuditTrail() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [actionFilter, setActionFilter] = useState('ALL');
  const [error, setError] = useState(null);

  useEffect(() => {
    loadAuditLogs();
  }, []);

  const loadAuditLogs = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await ConsentApi.getAuditLogs();
      setLogs(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load audit trail');
    } finally {
      setLoading(false);
    }
  };

  const filteredLogs = logs.filter(log => {
    const matchesAction = actionFilter === 'ALL' || log.action === actionFilter;
    const matchesSearch = !searchTerm || 
      log.consentReference?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.actorRole?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.actorIdentifier?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.details?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesAction && matchesSearch;
  });

  const getActionBadgeColor = (action) => {
    switch (action) {
      case 'CONSENT_AUTHORISED':
      case 'DATA_ACCESS_PERMITTED':
        return '#34d399';
      case 'CONSENT_CREATED':
      case 'STATUS_CHANGED':
        return '#38bdf8';
      case 'CONSENT_REVOKED':
      case 'CONSENT_REJECTED':
      case 'DATA_ACCESS_DENIED':
        return '#fb7185';
      case 'CONSENT_EXPIRED':
        return '#fbbf24';
      default:
        return '#9ca3af';
    }
  };

  const permissionMatrix = [
    { role: 'CUSTOMER (Data Owner)', viewOwnData: 'Yes', requestConsent: 'No', approveReject: 'Yes', revokeConsent: 'Yes', accessTPPData: 'No', manageSystem: 'No' },
    { role: 'CONSENT_REQUESTER (Data Consumer / TPP)', viewOwnData: 'No', requestConsent: 'Yes', approveReject: 'No', revokeConsent: 'No', accessTPPData: 'Yes (With Active Consent)', manageSystem: 'No' },
    { role: 'BANK_PROVIDER (Data Provider)', viewOwnData: 'Yes (Core)', requestConsent: 'No', approveReject: 'No', revokeConsent: 'No', accessTPPData: 'Enforces Consent', manageSystem: 'No' },
    { role: 'CONSENT_MANAGER', viewOwnData: 'Metadata', requestConsent: 'No', approveReject: 'Status Update', revokeConsent: 'Audit', accessTPPData: 'Validates Rules', manageSystem: 'Lifecycle' },
    { role: 'MAKER (Banking Operations)', viewOwnData: 'Initiate Ops', requestConsent: 'No', approveReject: 'No', revokeConsent: 'No', accessTPPData: 'No', manageSystem: 'No' },
    { role: 'CHECKER (Banking Approver)', viewOwnData: 'Review Ops', requestConsent: 'No', approveReject: 'Approve Maker', revokeConsent: 'No', accessTPPData: 'No', manageSystem: 'No' },
    { role: 'ADMIN (System Administrator)', viewOwnData: 'Admin View', requestConsent: 'No', approveReject: 'No (Cannot Bypass)', revokeConsent: 'No', accessTPPData: 'No (Follows Rules)', manageSystem: 'Full IAM' },
  ];

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Consent Governance & Audit Trail</h1>
          <p className="page-description">
            Complete immutable ledger of all Open Banking consent creations, approvals, revocations, and data accesses.
          </p>
        </div>
        <button className="btn btn-secondary" onClick={loadAuditLogs}>
          <RefreshCw size={16} /> Refresh Audit Trail
        </button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      {/* Role Permission Matrix Presentation Card */}
      <div className="glass-panel" style={{ padding: '24px', marginBottom: '28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
          <Lock size={20} style={{ color: 'var(--primary-500)' }} />
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Actor & Role Permission Matrix (RBAC)</h2>
        </div>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Actor / Role</th>
                <th>View Accounts</th>
                <th>Initiate Consent</th>
                <th>Approve / Reject</th>
                <th>Revoke Consent</th>
                <th>Fetch TPP Data</th>
                <th>System & Governance</th>
              </tr>
            </thead>
            <tbody>
              {permissionMatrix.map((row, idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 700, color: '#ffffff' }}>{row.role}</td>
                  <td>{row.viewOwnData}</td>
                  <td>{row.requestConsent}</td>
                  <td>{row.approveReject}</td>
                  <td>{row.revokeConsent}</td>
                  <td style={{ color: 'var(--accent-cyan)', fontWeight: 600 }}>{row.accessTPPData}</td>
                  <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{row.manageSystem}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Audit Search & Filter Bar */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '16px', flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: '260px', position: 'relative' }}>
          <input 
            type="text" 
            className="form-control"
            placeholder="Search by reference, actor, or event detail..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div style={{ display: 'flex', gap: '6px' }}>
          {['ALL', 'CONSENT_CREATED', 'CONSENT_AUTHORISED', 'CONSENT_REVOKED', 'CONSENT_REJECTED', 'DATA_ACCESS_PERMITTED', 'DATA_ACCESS_DENIED'].map(act => (
            <button
              key={act}
              onClick={() => setActionFilter(act)}
              className={`btn btn-sm ${actionFilter === act ? 'btn-primary' : 'btn-secondary'}`}
              style={{ fontSize: '0.75rem' }}
            >
              {act.replace('CONSENT_', '')}
            </button>
          ))}
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <h2 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '16px' }}>
          Real-time Audit Trail ({filteredLogs.length} Events)
        </h2>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Action</th>
                <th>Consent Reference</th>
                <th>Actor Role</th>
                <th>Actor Identifier</th>
                <th>Event Details</th>
              </tr>
            </thead>
            <tbody>
              {filteredLogs.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading audit trail...' : 'No audit records match the current filters.'}
                  </td>
                </tr>
              ) : (
                filteredLogs.map((log) => {
                  const color = getActionBadgeColor(log.action);
                  return (
                    <tr key={log.id}>
                      <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                        {new Date(log.createdAt).toLocaleString('en-IN')}
                      </td>
                      <td>
                        <span style={{
                          padding: '3px 8px',
                          borderRadius: '4px',
                          background: `${color}15`,
                          color: color,
                          border: `1px solid ${color}40`,
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          whiteSpace: 'nowrap'
                        }}>
                          {log.action}
                        </span>
                      </td>
                      <td style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: 'var(--accent-cyan)', fontWeight: 600 }}>
                        {log.consentReference}
                      </td>
                      <td style={{ fontWeight: 600, fontSize: '0.85rem' }}>{log.actorRole}</td>
                      <td style={{ fontSize: '0.85rem', color: 'var(--primary-100)' }}>{log.actorIdentifier}</td>
                      <td style={{ fontSize: '0.85rem', color: 'var(--text-main)' }}>{log.details}</td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
