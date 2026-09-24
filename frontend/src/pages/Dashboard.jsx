import React, { useEffect, useState } from 'react';
import { Users, CreditCard, ArrowLeftRight, ShieldCheck, Plus, ArrowUpRight } from 'lucide-react';
import { CustomerApi, AccountApi, TransactionApi, ConsentApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';

export default function Dashboard({ setActiveTab }) {
  const [stats, setStats] = useState({
    customerCount: 0,
    accountCount: 0,
    totalBalance: 0,
    consentCount: 0,
    recentTransactions: [],
    recentConsents: []
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [custRes, accRes, txRes, consentRes] = await Promise.all([
        CustomerApi.getAll().catch(() => ({ data: [] })),
        AccountApi.getAll().catch(() => ({ data: [] })),
        TransactionApi.getAll().catch(() => ({ data: [] })),
        ConsentApi.getAll().catch(() => ({ data: [] }))
      ]);

      const customers = custRes.data || [];
      const accounts = accRes.data || [];
      const transactions = txRes.data || [];
      const consents = consentRes.data || [];

      const totalBalance = accounts.reduce((acc, curr) => acc + Number(curr.balance || 0), 0);

      setStats({
        customerCount: customers.length,
        accountCount: accounts.length,
        totalBalance: totalBalance,
        consentCount: consents.length,
        recentTransactions: transactions.slice(0, 5),
        recentConsents: consents.slice(0, 4)
      });
    } catch (err) {
      console.error('Error loading dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Executive Dashboard</h1>
          <p className="page-description">Real-time overview of core banking metrics, transactions, and open banking consents.</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={loadDashboardData}>
            Refresh
          </button>
          <button className="btn btn-primary" onClick={() => setActiveTab('consents')}>
            <ShieldCheck size={16} /> Open Banking Consent
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="glass-panel stat-card">
          <div className="stat-label">Total Deposits (Core Bank)</div>
          <div className="stat-value" style={{ color: '#34d399' }}>
            £{stats.totalBalance.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Across {stats.accountCount} active accounts</div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">Active Customers</div>
          <div className="stat-value">{stats.customerCount}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Verified accounts on platform</div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">Bank Accounts</div>
          <div className="stat-value">{stats.accountCount}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Savings, Current & Checking</div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">Open Banking Consents</div>
          <div className="stat-value" style={{ color: 'var(--accent-cyan)' }}>{stats.consentCount}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TPP AIS & PIS authorizations</div>
        </div>
      </div>

      {/* Dual Tables Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(480px, 1fr))', gap: '24px' }}>
        
        {/* Recent Transactions */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Recent Transactions</h2>
            <button 
              onClick={() => setActiveTab('transactions')}
              style={{ background: 'none', border: 'none', color: 'var(--primary-500)', cursor: 'pointer', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              View All <ArrowUpRight size={14} />
            </button>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentTransactions.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No transactions yet</td>
                  </tr>
                ) : (
                  stats.recentTransactions.map((tx) => (
                    <tr key={tx.id}>
                      <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{tx.transactionReference}</td>
                      <td>
                        <span style={{ 
                          fontWeight: 600, 
                          color: tx.transactionType === 'DEPOSIT' ? '#34d399' : tx.transactionType === 'WITHDRAWAL' ? '#fb7185' : '#818cf8' 
                        }}>
                          {tx.transactionType}
                        </span>
                      </td>
                      <td style={{ fontWeight: 700 }}>£{Number(tx.amount).toFixed(2)}</td>
                      <td><StatusBadge status={tx.status} /></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Recent Open Banking Consents */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Open Banking Consents (TPP)</h2>
            <button 
              onClick={() => setActiveTab('consents')}
              style={{ background: 'none', border: 'none', color: 'var(--accent-cyan)', cursor: 'pointer', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              Manage Consents <ArrowUpRight size={14} />
            </button>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Customer</th>
                  <th>TPP Client</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentConsents.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No consents recorded</td>
                  </tr>
                ) : (
                  stats.recentConsents.map((c) => (
                    <tr key={c.id}>
                      <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{c.consentReference}</td>
                      <td>{c.customerName || `Customer #${c.customerId}`}</td>
                      <td style={{ color: 'var(--accent-cyan)' }}>{c.tppClientId}</td>
                      <td><StatusBadge status={c.status} /></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
}
