import React, { useState } from 'react';
import { 
  Building2, 
  Lock, 
  User, 
  ShieldCheck, 
  ArrowRight, 
  LogIn, 
  ExternalLink,
  Eye,
  EyeOff,
  Sparkles
} from 'lucide-react';
import AuthService from '../api/authService';

export const DEMO_ACTORS = [
  { role: 'CUSTOMER', label: 'Customer / Data Owner', username: 'customer_user', pass: 'customer123', color: '#34d399' },
  { role: 'TPP', label: 'Consent Requester (TPP)', username: 'requester_tpp', pass: 'tpp123', color: '#06b6d4' },
  { role: 'MAKER', label: 'Banking Maker', username: 'maker_user', pass: 'maker123', color: '#38bdf8' },
  { role: 'CHECKER', label: 'Banking Checker', username: 'checker_user', pass: 'checker123', color: '#f43f5e' },
  { role: 'CONSENT_MANAGER', label: 'Consent Manager', username: 'consent_manager_user', pass: 'consent123', color: '#fbbf24' },
  { role: 'ADMIN', label: 'System Admin', username: 'admin', pass: 'admin123', color: '#a855f7' }
];

export default function SignIn({ onAuthSuccess, onNavigateToSignUp }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please enter both username and password.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const authData = await AuthService.login(username, password);
      if (onAuthSuccess) {
        onAuthSuccess(authData);
      }
    } catch (err) {
      setError(err.message || 'Keycloak authentication failed. Please verify your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (user, pass) => {
    setUsername(user);
    setPassword(pass);
    setError(null);
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
      <div style={{ width: '100%', maxWidth: '460px' }}>
        
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: '28px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '56px',
            height: '56px',
            borderRadius: '14px',
            background: 'linear-gradient(135deg, var(--primary-500), var(--accent-cyan))',
            color: '#ffffff',
            boxShadow: '0 8px 24px rgba(99, 102, 241, 0.35)',
            marginBottom: '16px'
          }}>
            <Building2 size={30} />
          </div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#ffffff', letterSpacing: '-0.02em', margin: 0 }}>
            Banfico Banking
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '6px' }}>
            Open Banking IAM & Consent Management Platform
          </p>
        </div>

        {/* Auth Glass Card */}
        <div className="glass-panel" style={{ padding: '32px', borderColor: 'rgba(255,255,255,0.12)', boxShadow: '0 20px 40px rgba(0,0,0,0.5)' }}>
          
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: '#ffffff' }}>Sign In</h2>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              background: 'rgba(16, 185, 129, 0.12)',
              border: '1px solid rgba(16, 185, 129, 0.3)',
              borderRadius: '999px',
              padding: '3px 10px',
              fontSize: '0.7rem',
              color: '#34d399',
              fontWeight: 700
            }}>
              <ShieldCheck size={13} /> KEYCLOAK REALM RBAC
            </div>
          </div>

          {error && (
            <div className="alert-banner alert-error" style={{ marginBottom: '20px', fontSize: '0.85rem' }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group" style={{ marginBottom: '16px' }}>
              <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <User size={14} style={{ color: 'var(--text-muted)' }} /> Username or Email
              </label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. customer_user or customer@banfico.com"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="form-group" style={{ marginBottom: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <label className="form-label" style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Lock size={14} style={{ color: 'var(--text-muted)' }} /> Password
                </label>
              </div>
              <div style={{ position: 'relative' }}>
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-control"
                  placeholder="Enter your Keycloak password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  style={{ paddingRight: '42px' }}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: 'absolute',
                    right: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    background: 'none',
                    border: 'none',
                    color: 'var(--text-muted)',
                    cursor: 'pointer',
                    padding: 0,
                    display: 'flex',
                    alignItems: 'center'
                  }}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

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
              <LogIn size={18} /> {loading ? 'Authenticating via Keycloak...' : 'Sign In'}
            </button>

            {/* Keycloak Hosted SSO Button */}
            <button
              type="button"
              onClick={AuthService.loginWithKeycloakSSO}
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
              <ExternalLink size={15} /> Sign In with Keycloak SSO
            </button>
          </form>

          {/* Registration Redirect Option */}
          <div style={{ textAlign: 'center', marginTop: '24px', paddingTop: '16px', borderTop: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
            <span style={{ color: 'var(--text-muted)' }}>Don't have an account? </span>
            <button
              onClick={onNavigateToSignUp}
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
              Create Account / Sign Up
            </button>
          </div>
        </div>

        {/* Quick Demo Credentials Helper for Presentations */}
        <div className="glass-panel" style={{ marginTop: '20px', padding: '16px', background: 'rgba(0,0,0,0.4)', borderColor: 'rgba(255,255,255,0.06)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700, marginBottom: '10px' }}>
            <Sparkles size={14} style={{ color: '#fbbf24' }} />
            1-CLICK DEMO AUTHENTICATION (ALL 6 ACTORS):
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            {DEMO_ACTORS.map((actor) => (
              <button
                key={actor.username}
                type="button"
                onClick={() => handleQuickFill(actor.username, actor.pass)}
                style={{
                  background: 'rgba(255,255,255,0.04)',
                  border: `1px solid ${actor.color}40`,
                  borderRadius: '6px',
                  padding: '6px 8px',
                  textAlign: 'left',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <div style={{ fontSize: '0.75rem', fontWeight: 700, color: actor.color }}>
                  {actor.label}
                </div>
                <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                  {actor.username}
                </div>
              </button>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}
