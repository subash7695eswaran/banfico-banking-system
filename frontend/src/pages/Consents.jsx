import React, { useState, useEffect } from 'react';
import { ShieldCheck, Plus, CheckCircle, XCircle, ShieldAlert, Key, Building } from 'lucide-react';
import { ConsentApi, CustomerApi, AccountApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';

export default function Consents() {
  const [consents, setConsents] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [formData, setFormData] = useState({
    customerId: '',
    bankAccountId: '',
    purpose: 'Account Information Service (AIS) - Balance & Transactions',
    tppClientId: 'fintech-aggregator-tpp'
  });
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
      setConsents(consentRes.data || []);
      setCustomers(custRes.data || []);
      setAccounts(accRes.data || []);
      if (custRes.data?.length > 0 && !formData.customerId) {
        setFormData(prev => ({
          ...prev,
          customerId: custRes.data[0].id,
          bankAccountId: accRes.data[0]?.id || ''
        }));
      }
    } catch (err) {
      setError(err.message || 'Failed to load consent data');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateConsent = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await ConsentApi.create({
        customerId: Number(formData.customerId),
        bankAccountId: formData.bankAccountId ? Number(formData.bankAccountId) : null,
        purpose: formData.purpose,
        tppClientId: formData.tppClientId
      });
      setSuccessMsg('Open Banking Consent request generated!');
      setIsModalOpen(false);
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to create consent request');
    }
  };

  const handleApprove = async (id) => {
    try {
      setError(null);
      await ConsentApi.approve(id);
      setSuccessMsg('Consent AUTHORISED! Third Party Provider (TPP) access granted.');
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to approve consent');
    }
  };

  const handleReject = async (id) => {
    try {
      setError(null);
      await ConsentApi.reject(id);
      setSuccessMsg('Consent REJECTED.');
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to reject consent');
    }
  };

  const filteredConsents = filterStatus === 'ALL' 
    ? consents 
    : consents.filter(c => c.status === filterStatus);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Open Banking Consent Management</h1>
          <p className="page-description">
            Regulated UK/Berlin Group Open Banking standard lifecycle (AIS & PIS authorization requests).
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> New Consent Request
        </button>
      </div>

      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}
      {error && <div className="alert-banner alert-error">{error}</div>}

      {/* Info Card on Open Banking Flow */}
      <div className="glass-panel" style={{ padding: '16px 20px', marginBottom: '24px', background: 'rgba(6, 182, 212, 0.05)', borderColor: 'rgba(6, 182, 212, 0.2)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <ShieldCheck size={24} style={{ color: 'var(--accent-cyan)' }} />
          <div style={{ fontSize: '0.85rem' }}>
            <span style={{ fontWeight: 700, color: '#ffffff' }}>Open Banking Lifecycle: </span>
            A regulated Third Party Provider (TPP) requests access to customer financial data. The customer or bank checker must explicitly <strong>AUTHORISE</strong> or <strong>REJECT</strong> the consent.
          </div>
        </div>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '16px' }}>
        {['ALL', 'PENDING', 'AUTHORISED', 'REJECTED'].map((st) => (
          <button
            key={st}
            onClick={() => setFilterStatus(st)}
            className={`btn btn-sm ${filterStatus === st ? 'btn-primary' : 'btn-secondary'}`}
          >
            {st}
          </button>
        ))}
      </div>

      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Consent Reference</th>
                <th>Customer</th>
                <th>Linked IBAN</th>
                <th>TPP Client (FinTech)</th>
                <th>Purpose</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredConsents.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading consents...' : 'No consent records found for this filter.'}
                  </td>
                </tr>
              ) : (
                filteredConsents.map((c) => (
                  <tr key={c.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8rem', color: 'var(--accent-cyan)' }}>
                      {c.consentReference}
                    </td>
                    <td style={{ fontWeight: 600 }}>{c.customerName || `Customer #${c.customerId}`}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {c.accountNumber || 'All Accounts'}
                    </td>
                    <td>
                      <span style={{ 
                        padding: '2px 8px', 
                        borderRadius: '4px', 
                        background: 'rgba(99, 102, 241, 0.15)', 
                        color: 'var(--primary-100)', 
                        fontSize: '0.75rem',
                        fontWeight: 600 
                      }}>
                        {c.tppClientId}
                      </span>
                    </td>
                    <td style={{ fontSize: '0.85rem' }}>{c.purpose}</td>
                    <td><StatusBadge status={c.status} /></td>
                    <td>
                      {c.status === 'PENDING' ? (
                        <div style={{ display: 'flex', gap: '6px' }}>
                          <button 
                            onClick={() => handleApprove(c.id)}
                            className="btn btn-success btn-sm"
                            title="Approve TPP Access"
                          >
                            <CheckCircle size={14} /> Authorise
                          </button>
                          <button 
                            onClick={() => handleReject(c.id)}
                            className="btn btn-danger btn-sm"
                            title="Reject TPP Request"
                          >
                            <XCircle size={14} /> Reject
                          </button>
                        </div>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Settled</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Initiate Open Banking Consent Request">
        <form onSubmit={handleCreateConsent}>
          <div className="form-group">
            <label className="form-label">Customer (Grantor)</label>
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
            <label className="form-label">Third Party Provider (TPP) Client ID</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="e.g. plum-fintech-tpp, quickbooks-accounting" 
              required
              value={formData.tppClientId}
              onChange={(e) => setFormData({ ...formData, tppClientId: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Consent Purpose / Scope</label>
            <select 
              className="form-control"
              value={formData.purpose}
              onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
            >
              <option value="Account Information Service (AIS) - Balance & Transactions">
                Account Information Service (AIS) - Read Balances & History
              </option>
              <option value="Payment Initiation Service (PIS) - Single Immediate Payment">
                Payment Initiation Service (PIS) - Single Immediate Payment
              </option>
              <option value="Confirmation of Funds (CoF) - Balance Availability Check">
                Confirmation of Funds (CoF) - Balance Availability
              </option>
            </select>
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Generate Consent Request
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
