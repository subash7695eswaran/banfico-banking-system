import React, { useState, useEffect } from 'react';
import { 
  Layers, 
  Plus, 
  Search, 
  ShieldCheck, 
  ShieldAlert, 
  Database, 
  CheckCircle, 
  XCircle, 
  AlertCircle,
  Clock,
  ArrowRight,
  Code
} from 'lucide-react';
import { ConsentApi, CustomerApi, AccountApi, OpenBankingApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';

export default function OpenBankingConsumer({ currentRole }) {
  const [consents, setConsents] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Form Data for TPP Consent Request
  const [formData, setFormData] = useState({
    customerId: '',
    bankAccountId: '',
    purpose: 'Account Information Service (AIS) - Balance & Transactions for Personal Finance App',
    tppClientId: 'fintech-aggregator-tpp',
    scopes: ['READ_ACCOUNTS', 'READ_BALANCES', 'READ_TRANSACTIONS'],
    validDays: 90
  });

  // Data Access Query Console state
  const [selectedRef, setSelectedRef] = useState('');
  const [queryResource, setQueryResource] = useState('ALL');
  const [queryResult, setQueryResult] = useState(null);
  const [queryError, setQueryError] = useState(null);
  const [querying, setQuerying] = useState(false);

  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [consentRes, custRes, accRes] = await Promise.all([
        ConsentApi.getAll(),
        CustomerApi.getAll(),
        AccountApi.getAll()
      ]);
      const allConsents = consentRes.data || [];
      setConsents(allConsents);
      setCustomers(custRes.data || []);
      setAccounts(accRes.data || []);

      if (custRes.data?.length > 0 && !formData.customerId) {
        setFormData(prev => ({
          ...prev,
          customerId: custRes.data[0].id,
          bankAccountId: ''
        }));
      }

      if (allConsents.length > 0 && !selectedRef) {
        setSelectedRef(allConsents[0].consentReference);
      }
    } catch (err) {
      setError(err.message || 'Failed to load consent data');
    } finally {
      setLoading(false);
    }
  };

  const handleScopeToggle = (scope) => {
    setFormData(prev => {
      const exists = prev.scopes.includes(scope);
      const newScopes = exists ? prev.scopes.filter(s => s !== scope) : [...prev.scopes, scope];
      return { ...prev, scopes: newScopes };
    });
  };

  const handleCreateConsentRequest = async (e) => {
    e.preventDefault();
    if (formData.scopes.length === 0) {
      setError('Please select at least one permission scope.');
      return;
    }

    try {
      setError(null);
      const validUntil = new Date();
      validUntil.setDate(validUntil.getDate() + Number(formData.validDays));

      await ConsentApi.create({
        customerId: Number(formData.customerId),
        bankAccountId: formData.bankAccountId ? Number(formData.bankAccountId) : null,
        purpose: formData.purpose,
        tppClientId: formData.tppClientId,
        scopes: formData.scopes.join(','),
        validUntil: validUntil.toISOString()
      });

      setSuccessMsg('Open Banking Consent request submitted successfully to Customer for approval!');
      setIsModalOpen(false);
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to initiate consent request');
    }
  };

  const handleTestDataAccess = async () => {
    if (!selectedRef) return;
    try {
      setQuerying(true);
      setQueryResult(null);
      setQueryError(null);
      const res = await OpenBankingApi.getData(selectedRef, queryResource);
      setQueryResult(res.data || res);
    } catch (err) {
      setQueryError(err.response?.data?.message || err.message || 'Open Banking data access request was rejected');
    } finally {
      setQuerying(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Consent Requester & Data Consumer (TPP)</h1>
          <p className="page-description">
            Regulated Third Party Provider (FinTech) console for requesting consent and consuming authorized Open Banking data.
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> Request Customer Consent
        </button>
      </div>

      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}
      {error && <div className="alert-banner alert-error">{error}</div>}

      {/* Regulated Open Banking Compliance Rule Card */}
      <div className="glass-panel" style={{ padding: '16px 20px', marginBottom: '24px', background: 'rgba(6, 182, 212, 0.05)', borderColor: 'rgba(6, 182, 212, 0.2)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <ShieldAlert size={26} style={{ color: 'var(--accent-cyan)' }} />
          <div style={{ fontSize: '0.85rem' }}>
            <span style={{ fontWeight: 700, color: '#ffffff' }}>Open Banking Security Rules: </span>
            A Data Consumer can <strong>never</strong> access customer data without an active <strong>AUTHORISED</strong> consent. If consent is <strong>PENDING</strong>, <strong>REJECTED</strong>, <strong>REVOKED</strong>, or <strong>EXPIRED</strong>, data access will be strictly blocked.
          </div>
        </div>
      </div>

      {/* Section 1: Live Open Banking Data Access Testing Console */}
      <div className="glass-panel" style={{ padding: '24px', marginBottom: '32px', borderColor: 'rgba(99, 102, 241, 0.3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '18px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Database size={20} style={{ color: 'var(--primary-500)' }} />
            <h2 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Live Data Access & Validation Console</h2>
          </div>
          <span style={{ fontSize: '0.75rem', background: 'rgba(99, 102, 241, 0.2)', color: 'var(--primary-100)', padding: '4px 10px', borderRadius: '4px', fontWeight: 700 }}>
            GET /api/open-banking/consents/{'{ref}'}/data
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 200px auto', gap: '12px', alignItems: 'flex-end', marginBottom: '20px' }}>
          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Select Consent Reference to Test</label>
            <select 
              className="form-control"
              value={selectedRef}
              onChange={(e) => setSelectedRef(e.target.value)}
            >
              {consents.map((c) => (
                <option key={c.id} value={c.consentReference}>
                  {c.consentReference} — {c.customerName || `Customer #${c.customerId}`} [{c.status}]
                </option>
              ))}
            </select>
          </div>

          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Target Resource Scope</label>
            <select 
              className="form-control"
              value={queryResource}
              onChange={(e) => setQueryResource(e.target.value)}
            >
              <option value="ALL">ALL Scopes</option>
              <option value="ACCOUNTS">ACCOUNTS Only</option>
              <option value="BALANCES">BALANCES Only</option>
              <option value="TRANSACTIONS">TRANSACTIONS Only</option>
            </select>
          </div>

          <button 
            className="btn btn-primary"
            onClick={handleTestDataAccess}
            disabled={querying || !selectedRef}
            style={{ height: '42px' }}
          >
            <Search size={16} /> {querying ? 'Validating...' : 'Execute Data Query'}
          </button>
        </div>

        {/* Live Query Results Output */}
        {queryError && (
          <div className="glass-panel" style={{ padding: '20px', background: 'rgba(244, 63, 94, 0.1)', borderColor: 'rgba(244, 63, 94, 0.4)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
              <XCircle size={22} style={{ color: '#fb7185', marginTop: '2px' }} />
              <div>
                <div style={{ fontWeight: 700, color: '#fb7185', fontSize: '1rem' }}>
                  403 Forbidden: Open Banking Data Access Denied
                </div>
                <div style={{ color: '#ffffff', marginTop: '4px', fontSize: '0.9rem' }}>
                  {queryError}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '8px' }}>
                  Regulatory Compliance: The bank refused data sharing because the requested consent is not in an active AUTHORISED state.
                </div>
              </div>
            </div>
          </div>
        )}

        {queryResult && (
          <div className="glass-panel" style={{ padding: '20px', background: 'rgba(16, 185, 129, 0.08)', borderColor: 'rgba(16, 185, 129, 0.3)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <CheckCircle size={22} style={{ color: '#34d399' }} />
                <div>
                  <div style={{ fontWeight: 700, color: '#34d399' }}>200 OK: Data Access Authorized</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Customer: {queryResult.customerName} | Scopes: {queryResult.authorizedScopes} | Purpose: {queryResult.purpose}
                  </div>
                </div>
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Access Timestamp: {new Date(queryResult.accessTimestamp).toLocaleTimeString('en-IN')}
              </span>
            </div>

            {/* Display Released Accounts */}
            {queryResult.accounts?.length > 0 && (
              <div style={{ marginBottom: '16px' }}>
                <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '8px' }}>
                  Authorized Bank Accounts ({queryResult.accounts.length}):
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px' }}>
                  {queryResult.accounts.map((a) => (
                    <div key={a.id} style={{ background: 'rgba(0,0,0,0.3)', padding: '12px', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
                      <div style={{ fontFamily: 'monospace', fontWeight: 600, color: 'var(--accent-cyan)', fontSize: '0.85rem' }}>{a.accountNumber}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '2px' }}>{a.accountType} Account</div>
                      <div style={{ fontSize: '1rem', fontWeight: 700, color: '#34d399', marginTop: '6px' }}>
                        {a.balance !== null ? `₹${Number(a.balance).toFixed(2)}` : '<Balance Masked by Scope>'}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Display Released Transactions */}
            {queryResult.transactions?.length > 0 && (
              <div>
                <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '8px' }}>
                  Authorized Transaction Ledger ({queryResult.transactions.length}):
                </div>
                <table className="data-table" style={{ background: 'rgba(0,0,0,0.2)' }}>
                  <thead>
                    <tr>
                      <th>Tx Reference</th>
                      <th>Type</th>
                      <th>Amount (₹)</th>
                      <th>Description</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {queryResult.transactions.map((tx) => (
                      <tr key={tx.id}>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>{tx.transactionReference}</td>
                        <td style={{ fontWeight: 600 }}>{tx.transactionType}</td>
                        <td style={{ fontWeight: 700 }}>₹{Number(tx.amount).toFixed(2)}</td>
                        <td style={{ fontSize: '0.8rem' }}>{tx.description}</td>
                        <td><StatusBadge status={tx.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Section 2: Submitted Requests Registry */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '16px' }}>My Submitted Consent Requests</h2>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Consent Reference</th>
                <th>Target Customer</th>
                <th>Requested Scopes</th>
                <th>Purpose</th>
                <th>Valid Until</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {consents.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading...' : 'No consent requests found.'}
                  </td>
                </tr>
              ) : (
                consents.map((c) => (
                  <tr key={c.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, color: 'var(--accent-cyan)', fontSize: '0.8rem' }}>
                      {c.consentReference}
                    </td>
                    <td style={{ fontWeight: 600 }}>{c.customerName || `Customer #${c.customerId}`}</td>
                    <td>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                        {(c.scopes || 'READ_ACCOUNTS').split(',').map((sc) => (
                          <span key={sc} style={{ fontSize: '0.65rem', padding: '2px 6px', background: 'rgba(99, 102, 241, 0.15)', color: 'var(--primary-100)', borderRadius: '3px', fontWeight: 600 }}>
                            {sc}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td style={{ fontSize: '0.8rem' }}>{c.purpose}</td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {c.validUntil ? new Date(c.validUntil).toLocaleDateString('en-IN') : '90 Days'}
                    </td>
                    <td><StatusBadge status={c.status} /></td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal for Creating Consent Request */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Create Open Banking Consent Request">
        <form onSubmit={handleCreateConsentRequest}>
          <div className="form-group">
            <label className="form-label">Customer (Data Owner)</label>
            <select 
              className="form-control"
              value={formData.customerId}
              onChange={(e) => setFormData({ ...formData, customerId: e.target.value })}
              required
            >
              {customers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.email})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Select Resource Permissions (Scopes)</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '10px', background: 'rgba(0,0,0,0.2)', borderRadius: '6px' }}>
              {[
                { id: 'READ_ACCOUNTS', label: 'READ_ACCOUNTS (Read Account Numbers, Types, & IBANs)' },
                { id: 'READ_BALANCES', label: 'READ_BALANCES (Read Real-time Account Balances)' },
                { id: 'READ_TRANSACTIONS', label: 'READ_TRANSACTIONS (Read Historical Transaction Statements)' }
              ].map((scopeItem) => (
                <label key={scopeItem.id} style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '0.85rem', cursor: 'pointer' }}>
                  <input 
                    type="checkbox"
                    checked={formData.scopes.includes(scopeItem.id)}
                    onChange={() => handleScopeToggle(scopeItem.id)}
                  />
                  <span>{scopeItem.label}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Specific Bank Account (Optional)</label>
            <select 
              className="form-control"
              value={formData.bankAccountId}
              onChange={(e) => setFormData({ ...formData, bankAccountId: e.target.value })}
            >
              <option value="">All Accounts of Customer</option>
              {accounts.filter(a => String(a.customerId) === String(formData.customerId)).map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountNumber} ({a.accountType})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Consent Purpose</label>
            <input 
              type="text" 
              className="form-control" 
              required
              value={formData.purpose}
              onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Validity Period (Days)</label>
            <select 
              className="form-control"
              value={formData.validDays}
              onChange={(e) => setFormData({ ...formData, validDays: e.target.value })}
            >
              <option value="30">30 Days</option>
              <option value="60">60 Days</option>
              <option value="90">90 Days (Standard Open Banking AIS)</option>
              <option value="180">180 Days</option>
            </select>
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Submit Consent Request
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
