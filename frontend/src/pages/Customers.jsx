import React, { useState, useEffect } from 'react';
import { Plus, Users, Mail, Phone, Calendar } from 'lucide-react';
import { CustomerApi } from '../api/axiosClient';
import Modal from '../components/Modal';

export default function Customers() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({ name: '', email: '', phone: '' });
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const res = await CustomerApi.getAll();
      setCustomers(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCustomer = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await CustomerApi.create(formData);
      setSuccessMsg('Customer onboarded successfully!');
      setIsModalOpen(false);
      setFormData({ name: '', email: '', phone: '' });
      loadCustomers();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to create customer');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Customer Management</h1>
          <p className="page-description">Onboard new customers, manage identities, and view linked accounts.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> New Customer
        </button>
      </div>

      {successMsg && (
        <div className="alert-banner alert-success">
          {successMsg}
        </div>
      )}

      {error && (
        <div className="alert-banner alert-error">
          {error}
        </div>
      )}

      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Full Name</th>
                <th>Email Address</th>
                <th>Phone Number</th>
                <th>Accounts</th>
                <th>Created At</th>
              </tr>
            </thead>
            <tbody>
              {customers.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    {loading ? 'Loading customers...' : 'No customers found. Click "+ New Customer" to onboard one.'}
                  </td>
                </tr>
              ) : (
                customers.map((c) => (
                  <tr key={c.id}>
                    <td style={{ fontWeight: 700 }}>#{c.id}</td>
                    <td style={{ fontWeight: 600, color: '#ffffff' }}>{c.name}</td>
                    <td style={{ color: 'var(--primary-100)' }}>{c.email}</td>
                    <td>{c.phone}</td>
                    <td>
                      <span className="badge badge-active">{c.accountCount || 0} Accounts</span>
                    </td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(c.createdAt).toLocaleDateString('en-IN')}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Onboard New Customer">
        <form onSubmit={handleCreateCustomer}>
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="e.g. Subash Murugan" 
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input 
              type="email" 
              className="form-control" 
              placeholder="subash@banfico.com" 
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number</label>
            <input 
              type="tel" 
              className="form-control" 
              placeholder="+91 9876543210" 
              required
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Register Customer
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
