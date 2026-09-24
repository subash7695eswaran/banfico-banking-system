import React, { useState, useEffect } from 'react';
import { Plus, CreditCard } from 'lucide-react';
import { AccountApi, CustomerApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';

export default function Accounts() {
  const [accounts, setAccounts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    customerId: '',
    accountType: 'SAVINGS',
    initialDeposit: '1000'
  });
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [accRes, custRes] = await Promise.all([
        AccountApi.getAll(),
        CustomerApi.getAll()
      ]);
      setAccounts(accRes.data || []);
      setCustomers(custRes.data || []);
      if (custRes.data?.length > 0 && !formData.customerId) {
        setFormData(prev => ({ ...prev, customerId: custRes.data[0].id }));
      }
    } catch (err) {
      setError(err.message || 'Failed to load bank accounts');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await AccountApi.create({
        customerId: Number(formData.customerId),
        accountType: formData.accountType,
        initialDeposit: Number(formData.initialDeposit)
      });
      setSuccessMsg('Bank account opened successfully!');
      setIsModalOpen(false);
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to create bank account');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Bank Account Management</h1>
          <p className="page-description">Open, monitor, and manage customer accounts with IBAN generation.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> Open New Account
        </button>
      </div>

      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}
      {error && <div className="alert-banner alert-error">{error}</div>}

      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Account ID</th>
                <th>IBAN / Account Number</th>
                <th>Customer Name</th>
                <th>Account Type</th>
                <th>Balance (£)</th>
                <th>Status</th>
                <th>Created Date</th>
              </tr>
            </thead>
            <tbody>
              {accounts.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading bank accounts...' : 'No bank accounts found.'}
                  </td>
                </tr>
              ) : (
                accounts.map((a) => (
                  <tr key={a.id}>
                    <td style={{ fontWeight: 700 }}>#{a.id}</td>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, color: 'var(--accent-cyan)' }}>
                      {a.accountNumber}
                    </td>
                    <td>{a.customerName || `Customer #${a.customerId}`}</td>
                    <td>
                      <span style={{ 
                        padding: '3px 8px', 
                        borderRadius: '4px', 
                        fontSize: '0.75rem', 
                        background: 'rgba(255,255,255,0.05)',
                        border: '1px solid var(--border-color)' 
                      }}>
                        {a.accountType}
                      </span>
                    </td>
                    <td style={{ fontWeight: 800, fontSize: '1rem', color: '#ffffff' }}>
                      £{Number(a.balance).toFixed(2)}
                    </td>
                    <td><StatusBadge status={a.status} /></td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(a.createdAt).toLocaleDateString('en-GB')}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Open New Bank Account">
        <form onSubmit={handleCreateAccount}>
          <div className="form-group">
            <label className="form-label">Select Customer</label>
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
            <label className="form-label">Account Type</label>
            <select 
              className="form-control"
              value={formData.accountType}
              onChange={(e) => setFormData({ ...formData, accountType: e.target.value })}
            >
              <option value="SAVINGS">SAVINGS (Interest Bearing)</option>
              <option value="CURRENT">CURRENT (Daily Operational)</option>
              <option value="CHECKING">CHECKING (Checking Account)</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Initial Deposit (£)</label>
            <input 
              type="number" 
              step="0.01" 
              min="0" 
              className="form-control" 
              placeholder="1000.00" 
              required
              value={formData.initialDeposit}
              onChange={(e) => setFormData({ ...formData, initialDeposit: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Generate IBAN & Open
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
