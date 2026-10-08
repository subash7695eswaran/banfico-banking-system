import React, { useState } from 'react';
import { 
  Building2, 
  Lock, 
  User, 
  Mail, 
  Phone, 
  ShieldCheck, 
  UserPlus, 
  ExternalLink,
  Eye,
  EyeOff,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import AuthService from '../api/authService';

export default function SignUp({ onAuthSuccess, onNavigateToSignIn }) {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    password: '',
    confirmPassword: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    try {
      setLoading(true);
      const authData = await AuthService.register({
        username: formData.username.trim(),
        email: formData.email.trim(),
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        phone: formData.phone.trim(),
        password: formData.password
      });

      if (onAuthSuccess) {
        onAuthSuccess(authData);
      }
    } catch (err) {
      setError(err.message || 'Keycloak registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'radial-gradient(circle at top right, rgba(99, 102, 241, 0.15), transparent 40%), radial-gradient(circle at bottom left, rgba(6, 182, 212, 0.1), #0b0f19 70%)',
      padding: '24px'
    }}>
      <div style={{ width: '100%', maxWidth: '520px' }}>
        
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '52px',
            height: '52px',
            borderRadius: '14px',
            background: 'linear-gradient(135deg, var(--primary-500), var(--accent-cyan))',
            color: '#ffffff',
            boxShadow: '0 8px 24px rgba(99, 102, 241, 0.35)',
            marginBottom: '12px'
          }}>
            <Building2 size={28} />
          </div>
          <h1 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#ffffff', letterSpacing: '-0.02em', margin: 0 }}>
            Create Customer Account
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '4px' }}>
            Open Banking Registration via Keycloak IAM
          </p>
        </div>

        {/* Form Card */}
        <div className="glass-panel" style={{ padding: '30px', borderColor: 'rgba(255,255,255,0.12)', boxShadow: '0 20px 40px rgba(0,0,0,0.5)' }}>
          
          {/* Security & Role Badge */}
          <div style={{
            background: 'rgba(52, 211, 153, 0.08)',
            border: '1px solid rgba(52, 211, 153, 0.25)',
            borderRadius: '8px',
            padding: '10px 14px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: '20px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={18} style={{ color: '#34d399' }} />
              <div>
                <div style={{ fontSize: '0.78rem', fontWeight: 700, color: '#34d399' }}>
                  ASSIGNED ROLE: CUSTOMER / DATA OWNER
                </div>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                  Self-registration automatically grants customer banking privileges.
                </div>
              </div>
            </div>
            <span style={{ fontSize: '0.65rem', background: 'rgba(52,211,153,0.2)', color: '#34d399', padding: '2px 6px', borderRadius: '4px', fontWeight: 700 }}>
              RBAC PROTECTED
            </span>
          </div>

          {error && (
            <div className="alert-banner alert-error" style={{ marginBottom: '18px', fontSize: '0.85rem' }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            {/* First & Last Name */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '14px' }}>
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">First Name</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Rahul"
                  value={formData.firstName}
                  onChange={(e) => handleChange('firstName', e.target.value)}
                  required
                />
              </div>
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Last Name</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Sharma"
                  value={formData.lastName}
                  onChange={(e) => handleChange('lastName', e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Email & Phone */}
            <div className="form-group" style={{ marginBottom: '14px' }}>
              <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Mail size={14} style={{ color: 'var(--text-muted)' }} /> Email Address
              </label>
              <input
                type="email"
                className="form-control"
                placeholder="e.g. rahul.sharma@example.com"
                value={formData.email}
                onChange={(e) => handleChange('email', e.target.value)}
                required
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '14px' }}>
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <User size={14} style={{ color: 'var(--text-muted)' }} /> Username
                </label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. rahul_s"
                  value={formData.username}
                  onChange={(e) => handleChange('username', e.target.value)}
                  required
                />
              </div>
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Phone size={14} style={{ color: 'var(--text-muted)' }} /> Phone Number
                </label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="+91 98765 43210"
                  value={formData.phone}
                  onChange={(e) => handleChange('phone', e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Password & Confirm */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '22px' }}>
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Lock size={14} style={{ color: 'var(--text-muted)' }} /> Password
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    className="form-control"
                    placeholder="Min 6 characters"
                    value={formData.password}
                    onChange={(e) => handleChange('password', e.target.value)}
                    required
                    style={{ paddingRight: '36px' }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                      position: 'absolute',
                      right: '10px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                      padding: 0
                    }}
                  >
                    {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                </div>
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Confirm Password</label>
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-control"
                  placeholder="Re-enter password"
                  value={formData.confirmPassword}
                  onChange={(e) => handleChange('confirmPassword', e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Submit Registration */}
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '0.95rem',
                fontWeight: 700,
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <UserPlus size={18} /> {loading ? 'Registering with Keycloak...' : 'Create Account & Sign In'}
            </button>

            {/* Keycloak Hosted SSO Registration */}
            <button
              type="button"
              onClick={AuthService.registerWithKeycloakSSO}
              className="btn btn-secondary"
              style={{
                width: '100%',
                marginTop: '10px',
                padding: '10px',
                fontSize: '0.85rem',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: '8px',
                borderColor: 'rgba(255,255,255,0.1)'
              }}
            >
              <ExternalLink size={15} /> Register on Keycloak SSO Page
            </button>
          </form>

          {/* Navigation back to Sign In */}
          <div style={{ textAlign: 'center', marginTop: '20px', paddingTop: '14px', borderTop: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
            <span style={{ color: 'var(--text-muted)' }}>Already have an account? </span>
            <button
              onClick={onNavigateToSignIn}
              style={{
                background: 'none',
                border: 'none',
                color: 'var(--primary-500)',
                fontWeight: 700,
                cursor: 'pointer',
                padding: 0,
                textDecoration: 'underline'
              }}
            >
              Sign In
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
