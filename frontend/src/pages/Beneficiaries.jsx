import React, { useState, useEffect } from 'react';
import { Plus, Trash2, UserCheck } from 'lucide-react';
import { BeneficiaryApi, CustomerApi } from '../api/axiosClient';
import Modal from '../components/Modal';

export default function Beneficiaries() {
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    customerId: '',
    beneficiaryName: '',
    accountNumber: '',
    bankName: 'Banfico Bank'
  });
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [bRes, cRes] = await Promise.all([
        BeneficiaryApi.getAll(),
        CustomerApi.getAll()
      ]);
      setBeneficiaries(bRes.data || []);
      setCustomers(cRes.data || []);
      if (cRes.data?.length > 0 && !formData.customerId) {
        setFormData(prev => ({ ...prev, customerId: cRes.data[0].id }));
      }
    } catch (err) {
      setError(err.message || 'Failed to load beneficiaries');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await BeneficiaryApi.create({
        customerId: Number(formData.customerId),
        beneficiaryName: formData.beneficiaryName,
        accountNumber: formData.accountNumber,
        bankName: formData.bankName
      });
      setSuccessMsg('Beneficiary added successfully!');
      setIsModalOpen(false);
      setFormData(prev => ({ ...prev, beneficiaryName: '', accountNumber: '' }));
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to add beneficiary');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to remove this beneficiary?')) return;
    try {
      setError(null);
      await BeneficiaryApi.delete(id);
      setSuccessMsg('Beneficiary removed!');
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to delete beneficiary');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Beneficiary & Payee Directory</h1>
          <p className="page-description">Manage approved payees for fast electronic funds transfer.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> Add Beneficiary
        </button>
      </div>

      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}
      {error && <div className="alert-banner alert-error">{error}</div>}

      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Beneficiary Name</th>
                <th>IBAN / Account Number</th>
                <th>Bank Name</th>
                <th>Owner (Customer)</th>
                <th>Created Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {beneficiaries.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading payees...' : 'No beneficiaries added yet.'}
                  </td>
                </tr>
              ) : (
                beneficiaries.map((b) => (
                  <tr key={b.id}>
                    <td style={{ fontWeight: 700 }}>#{b.id}</td>
                    <td style={{ fontWeight: 600, color: '#ffffff' }}>{b.beneficiaryName}</td>
                    <td style={{ fontFamily: 'monospace', color: 'var(--accent-cyan)' }}>{b.accountNumber}</td>
                    <td>{b.bankName}</td>
                    <td>{b.customerName || `Customer #${b.customerId}`}</td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(b.createdAt).toLocaleDateString('en-GB')}
                    </td>
                    <td>
                      <button 
                        onClick={() => handleDelete(b.id)}
                        className="btn btn-danger btn-sm"
                        title="Delete beneficiary"
                      >
                        <Trash2 size={14} /> Remove
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add Trusted Beneficiary">
        <form onSubmit={handleCreate}>
          <div className="form-group">
            <label className="form-label">Customer (Account Owner)</label>
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
            <label className="form-label">Beneficiary Full Name / Business Name</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="e.g. Alice Johnson or Retail Corp" 
              required
              value={formData.beneficiaryName}
              onChange={(e) => setFormData({ ...formData, beneficiaryName: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Beneficiary IBAN / Account Number</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="GB29BNFC10000000000003" 
              required
              value={formData.accountNumber}
              onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Bank Name</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="e.g. Banfico Bank, Barclays, HSBC" 
              required
              value={formData.bankName}
              onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Save Beneficiary
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
