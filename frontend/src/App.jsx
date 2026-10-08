import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import RoleSelector, { ROLES } from './components/RoleSelector';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import Accounts from './pages/Accounts';
import Transactions from './pages/Transactions';
import Beneficiaries from './pages/Beneficiaries';
import Consents from './pages/Consents';
import OpenBankingConsumer from './pages/OpenBankingConsumer';
import AuditTrail from './pages/AuditTrail';
import SystemInfo from './pages/SystemInfo';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import AuthService from './api/authService';

export default function App() {
  const [authUser, setAuthUser] = useState(null);
  const [authView, setAuthView] = useState('signin'); // 'signin' | 'signup'
  const [currentRole, setCurrentRole] = useState('CUSTOMER');
  const [activeTab, setActiveTab] = useState('dashboard');
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    // Check for existing Keycloak authenticated session
    const stored = AuthService.getStoredUser();
    if (stored && AuthService.isAuthenticated()) {
      setAuthUser(stored);
      const role = stored.primaryRole || localStorage.getItem('banfico_current_role') || 'CUSTOMER';
      setCurrentRole(role);
      redirectForRole(role);
    }
    setInitializing(false);
  }, []);

  const redirectForRole = (role) => {
    switch (role) {
      case 'CONSENT_REQUESTER':
      case 'TPP':
        setActiveTab('tpp-console');
        break;
      case 'MAKER':
      case 'CHECKER':
        setActiveTab('transactions');
        break;
      case 'CONSENT_MANAGER':
        setActiveTab('audit');
        break;
      case 'CUSTOMER':
      case 'ADMIN':
      default:
        setActiveTab('dashboard');
        break;
    }
  };

  const handleAuthSuccess = (data) => {
    setAuthUser(data);
    const role = data.primaryRole || 'CUSTOMER';
    setCurrentRole(role);
    redirectForRole(role);
  };

  const handleLogout = () => {
    AuthService.logout();
    setAuthUser(null);
    setAuthView('signin');
  };

  if (initializing) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#0b0f19',
        color: '#ffffff'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div className="spinner" style={{ margin: '0 auto 16px auto' }} />
          <div>Initializing Banfico Banking System...</div>
        </div>
      </div>
    );
  }

  // If not authenticated, render real Keycloak Sign In / Sign Up UI
  if (!authUser) {
    if (authView === 'signup') {
      return (
        <SignUp 
          onAuthSuccess={handleAuthSuccess}
          onNavigateToSignIn={() => setAuthView('signin')}
        />
      );
    }
    return (
      <SignIn 
        onAuthSuccess={handleAuthSuccess}
        onNavigateToSignUp={() => setAuthView('signup')}
      />
    );
  }

  // Authenticated Banking Platform
  return (
    <div className="app-layout">
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        currentRole={currentRole}
      />
      
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <RoleSelector 
          currentRole={currentRole} 
          setCurrentRole={setCurrentRole} 
          setActiveTab={setActiveTab}
          authUser={authUser}
          onLogout={handleLogout}
        />

        <main className="main-content">
          {activeTab === 'dashboard' && <Dashboard setActiveTab={setActiveTab} currentRole={currentRole} />}
          {activeTab === 'customers' && <Customers currentRole={currentRole} />}
          {activeTab === 'accounts' && <Accounts currentRole={currentRole} />}
          {activeTab === 'transactions' && <Transactions currentRole={currentRole} />}
          {activeTab === 'beneficiaries' && <Beneficiaries currentRole={currentRole} />}
          {activeTab === 'consents' && <Consents currentRole={currentRole} />}
          {activeTab === 'tpp-console' && <OpenBankingConsumer currentRole={currentRole} />}
          {activeTab === 'audit' && <AuditTrail currentRole={currentRole} />}
          {activeTab === 'system' && <SystemInfo currentRole={currentRole} />}
        </main>
      </div>
    </div>
  );
}
