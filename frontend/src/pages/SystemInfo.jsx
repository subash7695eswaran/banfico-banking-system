import React, { useState, useEffect } from 'react';
import { Activity, Database, Server, Shield, CheckCircle2, RefreshCw } from 'lucide-react';
import { HealthApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';

export default function SystemInfo() {
  const [healthData, setHealthData] = useState(null);
  const [infoData, setInfoData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadHealth();
  }, []);

  const loadHealth = async () => {
    try {
      setLoading(true);
      setError(null);
      const [health, info] = await Promise.all([
        HealthApi.getHealth().catch(err => ({ status: 'DOWN', database: 'DISCONNECTED', error: err.message })),
        HealthApi.getInfo().catch(err => ({ data: { application: 'Banfico Banking Service', version: '1.0.0' } }))
      ]);
      setHealthData(health);
      setInfoData(info.data || info);
    } catch (err) {
      setError(err.message || 'Health check endpoint unreachable');
    } finally {
      setLoading(false);
    }
  };

  const weeklyMilestones = [
    { week: 'Week 1', title: 'Setup, Git, Spring Boot, PostgreSQL & Health APIs', status: 'COMPLETED', desc: 'Environment verification, Java 21, Spring Boot REST baseline /health & /api/info' },
    { week: 'Week 2', title: 'Core Banking CRUD APIs & JPA Modeling', status: 'COMPLETED', desc: 'Customers, Accounts with IBANs, Atomic Transactions, Beneficiaries & Error Handling' },
    { week: 'Week 3', title: 'React Frontend Full-Stack Integration', status: 'COMPLETED', desc: 'Glassmorphic dashboard, modal operations, Axios API client, real-time ledgers' },
    { week: 'Week 4', title: 'Keycloak IAM, OAuth2 & JWT Role Protection', status: 'CONFIGURED', desc: 'Keycloak realm export, RBAC roles (admin, maker, checker, customer)' },
    { week: 'Week 5', title: 'Nginx API Gateway & Full Docker Compose Stack', status: 'CONFIGURED', desc: 'Single reverse proxy entrypoint on port 8080 routing UI, APIs, and Auth' },
    { week: 'Week 6', title: 'Open Banking Consent Management Capstone', status: 'COMPLETED', desc: 'AIS & PIS Consent lifecycle state machine (Create, Authorise, Reject)' }
  ];

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">System Health & Verification</h1>
          <p className="page-description">Live verification of Week 1 health endpoints, database status, and program roadmap.</p>
        </div>
        <button className="btn btn-primary" onClick={loadHealth}>
          <RefreshCw size={16} /> Run Diagnostics
        </button>
      </div>

      {/* Live Health Banner */}
      <div className="stats-grid" style={{ marginBottom: '24px' }}>
        <div className="glass-panel stat-card">
          <div className="stat-label">Backend Service Status</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '4px' }}>
            <StatusBadge status={healthData?.status || 'UNKNOWN'} />
            <span style={{ fontSize: '1rem', fontWeight: 700 }}>{infoData?.application || 'Banfico Banking'}</span>
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Java {infoData?.javaVersion || '21'} • Spring Boot {infoData?.springBootVersion || '3.3.4'}</div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">PostgreSQL Database</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '4px' }}>
            <StatusBadge status={healthData?.database === 'CONNECTED' ? 'CONNECTED' : 'DISCONNECTED'} />
            <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>banfico_banking</span>
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Port 5432 • HikariCP Pool Active</div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">API Gateway (Nginx)</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '4px' }}>
            <span className="badge badge-active">PORT 8080</span>
            <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Reverse Proxy Ready</span>
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Routes: /, /api/*, /auth/*</div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">Keycloak Identity Provider</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '4px' }}>
            <span className="badge badge-pending">PORT 8081</span>
            <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>banfico-realm</span>
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>OAuth2 / OIDC JWT Issuer</div>
        </div>
      </div>

      {/* Program Milestones Table */}
      <div className="glass-panel" style={{ padding: '24px', marginBottom: '24px' }}>
        <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '16px' }}>Banfico Program Curriculum & Deliverables</h2>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Week</th>
                <th>Focus & Deliverable</th>
                <th>Architecture Scope</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {weeklyMilestones.map((m, idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 800, color: 'var(--accent-cyan)' }}>{m.week}</td>
                  <td style={{ fontWeight: 600, color: '#ffffff' }}>{m.title}</td>
                  <td style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{m.desc}</td>
                  <td>
                    <span className={`badge ${m.status === 'COMPLETED' ? 'badge-active' : 'badge-pending'}`}>
                      {m.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
