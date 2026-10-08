import React, { useEffect, useState } from 'react';
import { 
  Users, 
  CreditCard, 
  ArrowLeftRight, 
  ShieldCheck, 
  Plus, 
  ArrowUpRight, 
  CheckCircle, 
  XCircle, 
  Ban, 
  Database,
  Layers,
  Clock
} from 'lucide-react';
import { CustomerApi, AccountApi, TransactionApi, ConsentApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';
import { ROLES } from '../components/RoleSelector';

export default function Dashboard({ setActiveTab, currentRole }) {
  const [stats, setStats] = useState({
    customerCount: 0,
    accountCount: 0,
    totalBalance: 0,
    consentCount: 0,
    pendingConsentCount: 0,
    recentTransactions: [],
    recentConsents: []
  });
  const [loading, setLoading] = useState(true);
  const [successMsg, setSuccessMsg] = useState(null);

  const roleObj = ROLES[currentRole] || ROLES.CUSTOMER;

  useEffect(() => {
    loadDashboardData();
  }, [currentRole]);

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

      // If customer persona, filter to customer's own data
      let displayedAccounts = accounts;
      let displayedConsents = consents;
      let displayedTransactions = transactions;

      if (currentRole === 'CUSTOMER' && roleObj.customerId) {
        displayedAccounts = accounts.filter(a => a.customerId === roleObj.customerId);
        displayedConsents = consents.filter(c => c.customerId === roleObj.customerId);
        const accIds = displayedAccounts.map(a => a.id);
        displayedTransactions = transactions.filter(t => 
          (t.sourceAccountId && accIds.includes(t.sourceAccountId)) || 
          (t.destinationAccountId && accIds.includes(t.destinationAccountId))
        );
      }

      const totalBalance = displayedAccounts.reduce((acc, curr) => acc + Number(curr.balance || 0), 0);
      const pendingCount = displayedConsents.filter(c => c.status === 'PENDING').length;

      setStats({
        customerCount: customers.length,
        accountCount: displayedAccounts.length,
        totalBalance: totalBalance,
        consentCount: displayedConsents.length,
        pendingConsentCount: pendingCount,
        recentTransactions: displayedTransactions.slice(0, 5),
        recentConsents: displayedConsents.slice(0, 5)
      });
    } catch (err) {
      console.error('Error loading dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id) => {
    try {
      await ConsentApi.approve(id);
      setSuccessMsg('Consent AUTHORISED! Third Party Provider granted access.');
      loadDashboardData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      console.error(err);
    }
  };

  const handleReject = async (id) => {
    try {
      await ConsentApi.reject(id);
      setSuccessMsg('Consent REJECTED.');
      loadDashboardData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            {currentRole === 'CUSTOMER' ? `Welcome, ${roleObj.name}` : `${roleObj.roleName} Dashboard`}
          </h1>
          <p className="page-description">
            {currentRole === 'CUSTOMER' 
              ? 'Manage your personal bank accounts, review financial statements, and authorize or revoke third-party data sharing consents.'
              : currentRole === 'CONSENT_REQUESTER'
              ? 'Request open banking consent scopes, query customer accounts, and verify regulatory access rules.'
              : 'Real-time overview of core banking operations, maker-checker authorizations, and consent state machine.'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={loadDashboardData}>
            Refresh
          </button>
          {currentRole === 'CONSENT_REQUESTER' ? (
            <button className="btn btn-primary" onClick={() => setActiveTab('tpp-console')}>
              <Database size={16} /> Open Banking Data Console
            </button>
          ) : (
            <button className="btn btn-primary" onClick={() => setActiveTab('consents')}>
              <ShieldCheck size={16} /> Manage Consents
            </button>
          )}
        </div>
      </div>

      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      {/* Customer Action Notice if pending consent requests exist */}
      {currentRole === 'CUSTOMER' && stats.pendingConsentCount > 0 && (
        <div className="glass-panel" style={{ padding: '16px 20px', marginBottom: '24px', background: 'rgba(245, 158, 11, 0.08)', borderColor: 'rgba(245, 158, 11, 0.3)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Clock size={22} style={{ color: '#fbbf24' }} />
              <div>
                <strong style={{ color: '#ffffff' }}>Action Required: </strong>
                You have <strong>{stats.pendingConsentCount}</strong> Open Banking consent request(s) awaiting your decision.
              </div>
            </div>
            <button className="btn btn-primary btn-sm" onClick={() => setActiveTab('consents')}>
              Review Requests
            </button>
          </div>
        </div>
      )}

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="glass-panel stat-card">
          <div className="stat-label">
            {currentRole === 'CUSTOMER' ? 'My Total Balances' : 'Total Deposits (Core Bank)'}
          </div>
          <div className="stat-value" style={{ color: '#34d399' }}>
            ₹{stats.totalBalance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Across {stats.accountCount} active account{stats.accountCount === 1 ? '' : 's'}
          </div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">
            {currentRole === 'CUSTOMER' ? 'My Bank Accounts' : 'Active Customers'}
          </div>
          <div className="stat-value">
            {currentRole === 'CUSTOMER' ? stats.accountCount : stats.customerCount}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {currentRole === 'CUSTOMER' ? 'Savings & Current Accounts' : 'Verified customer profiles'}
          </div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">
            {currentRole === 'CUSTOMER' ? 'Active Consents' : 'Open Banking Consents'}
          </div>
          <div className="stat-value" style={{ color: 'var(--accent-cyan)' }}>
            {stats.consentCount}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {currentRole === 'CUSTOMER' ? 'Third-party app connections' : 'TPP AIS & PIS authorizations'}
          </div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-label">Pending Decisions</div>
          <div className="stat-value" style={{ color: stats.pendingConsentCount > 0 ? '#fbbf24' : '#34d399' }}>
            {stats.pendingConsentCount}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Awaiting Customer authorization
          </div>
        </div>
      </div>

      {/* Dual Tables Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(480px, 1fr))', gap: '24px' }}>
        
        {/* Recent Transactions */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>
              {currentRole === 'CUSTOMER' ? 'My Recent Transactions' : 'Recent Operations & Transfers'}
            </h2>
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
                      <td style={{ fontWeight: 700 }}>₹{Number(tx.amount).toFixed(2)}</td>
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
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>
              {currentRole === 'CUSTOMER' ? 'My Open Banking Consents' : 'Open Banking Consents (TPP)'}
            </h2>
            <button 
              onClick={() => setActiveTab('consents')}
              style={{ background: 'none', border: 'none', color: 'var(--accent-cyan)', cursor: 'pointer', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              Manage <ArrowUpRight size={14} />
            </button>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>TPP Client</th>
                  <th>Status</th>
                  {currentRole === 'CUSTOMER' && <th>Quick Action</th>}
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
                      <td style={{ color: 'var(--accent-cyan)', fontWeight: 600 }}>{c.tppClientId}</td>
                      <td><StatusBadge status={c.status} /></td>
                      {currentRole === 'CUSTOMER' && (
                        <td>
                          {c.status === 'PENDING' ? (
                            <div style={{ display: 'flex', gap: '4px' }}>
                              <button onClick={() => handleApprove(c.id)} className="btn btn-success btn-sm" style={{ padding: '3px 8px', fontSize: '0.7rem' }}>
                                Authorise
                              </button>
                              <button onClick={() => handleReject(c.id)} className="btn btn-danger btn-sm" style={{ padding: '3px 8px', fontSize: '0.7rem' }}>
                                Reject
                              </button>
                            </div>
                          ) : (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Settled</span>
                          )}
                        </td>
                      )}
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
