import React, { useState, useEffect } from 'react';
import { ArrowLeftRight, Plus, ArrowUp, ArrowDown, Repeat } from 'lucide-react';
import { TransactionApi, AccountApi } from '../api/axiosClient';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    accountId: '',
    transactionType: 'DEPOSIT',
    amount: '250',
    destinationAccountId: '',
    description: ''
  });
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [txRes, accRes] = await Promise.all([
        TransactionApi.getAll(),
        AccountApi.getAll()
      ]);
      setTransactions(txRes.data || []);
      setAccounts(accRes.data || []);
      if (accRes.data?.length > 0 && !formData.accountId) {
        setFormData(prev => ({
          ...prev,
          accountId: accRes.data[0].id,
          destinationAccountId: accRes.data[1]?.id || ''
        }));
      }
    } catch (err) {
      setError(err.message || 'Failed to load transaction data');
    } finally {
      setLoading(false);
    }
  };

  const handleExecuteTransaction = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await TransactionApi.execute(Number(formData.accountId), {
        transactionType: formData.transactionType,
        amount: Number(formData.amount),
        destinationAccountId: formData.transactionType === 'TRANSFER' ? Number(formData.destinationAccountId) : null,
        description: formData.description
      });
      setSuccessMsg('Transaction executed successfully!');
      setIsModalOpen(false);
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Transaction failed');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Transaction History & Operations</h1>
          <p className="page-description">Execute deposits, withdrawals, transfers and review full ledger audit trail.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <ArrowLeftRight size={16} /> Execute Transaction
        </button>
      </div>

      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}
      {error && <div className="alert-banner alert-error">{error}</div>}

      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Type</th>
                <th>Amount (£)</th>
                <th>Source Account</th>
                <th>Destination Account</th>
                <th>Description</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {transactions.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading ledger...' : 'No transactions recorded yet.'}
                  </td>
                </tr>
              ) : (
                transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8rem' }}>
                      {tx.transactionReference}
                    </td>
                    <td>
                      <span style={{ 
                        fontWeight: 700, 
                        display: 'inline-flex', 
                        alignItems: 'center', 
                        gap: '4px',
                        color: tx.transactionType === 'DEPOSIT' ? '#34d399' : tx.transactionType === 'WITHDRAWAL' ? '#fb7185' : '#818cf8' 
                      }}>
                        {tx.transactionType === 'DEPOSIT' && <ArrowDown size={14} />}
                        {tx.transactionType === 'WITHDRAWAL' && <ArrowUp size={14} />}
                        {tx.transactionType === 'TRANSFER' && <Repeat size={14} />}
                        {tx.transactionType}
                      </span>
                    </td>
                    <td style={{ fontWeight: 800, fontSize: '0.95rem' }}>
                      £{Number(tx.amount).toFixed(2)}
                    </td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {tx.sourceAccountNumber || '—'}
                    </td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {tx.destinationAccountNumber || '—'}
                    </td>
                    <td style={{ fontSize: '0.85rem' }}>{tx.description}</td>
                    <td><StatusBadge status={tx.status} /></td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(tx.createdAt).toLocaleString('en-GB')}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Execute Banking Transaction">
        <form onSubmit={handleExecuteTransaction}>
          <div className="form-group">
            <label className="form-label">Transaction Type</label>
            <select 
              className="form-control"
              value={formData.transactionType}
              onChange={(e) => setFormData({ ...formData, transactionType: e.target.value })}
            >
              <option value="DEPOSIT">DEPOSIT (Credit funds)</option>
              <option value="WITHDRAWAL">WITHDRAWAL (Debit funds)</option>
              <option value="TRANSFER">TRANSFER (Account to Account)</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">
              {formData.transactionType === 'DEPOSIT' ? 'Destination Account (To Deposit)' : 'Source Account (From Account)'}
            </label>
            <select 
              className="form-control"
              value={formData.accountId}
              onChange={(e) => setFormData({ ...formData, accountId: e.target.value })}
              required
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountNumber} - {a.customerName} (Bal: £{Number(a.balance).toFixed(2)})
                </option>
              ))}
            </select>
          </div>

          {formData.transactionType === 'TRANSFER' && (
            <div className="form-group">
              <label className="form-label">Beneficiary Destination Account (Transfer To)</label>
              <select 
                className="form-control"
                value={formData.destinationAccountId}
                onChange={(e) => setFormData({ ...formData, destinationAccountId: e.target.value })}
                required
              >
                {accounts.filter(a => String(a.id) !== String(formData.accountId)).map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountNumber} - {a.customerName}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Amount (£)</label>
            <input 
              type="number" 
              step="0.01" 
              min="0.01" 
              className="form-control" 
              placeholder="100.00" 
              required
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Payment Reference / Note</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="e.g. Invoice settlement or Gift" 
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Execute Transaction
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
