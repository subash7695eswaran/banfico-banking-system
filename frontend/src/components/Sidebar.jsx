import React from 'react';
import { 
  LayoutDashboard, 
  Users, 
  CreditCard, 
  ArrowLeftRight, 
  UserCheck, 
  ShieldCheck, 
  Layers,
  FileText,
  Activity 
} from 'lucide-react';
import { ROLES } from './RoleSelector';

export default function Sidebar({ activeTab, setActiveTab, currentRole }) {
  const roleObj = ROLES[currentRole] || ROLES.ADMIN;
  const allowedTabs = roleObj.allowedTabs || [];

  const allMenuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'customers', label: 'Customers', icon: Users },
    { id: 'accounts', label: 'Bank Accounts', icon: CreditCard },
    { id: 'transactions', label: 'Transactions', icon: ArrowLeftRight },
    { id: 'beneficiaries', label: 'Beneficiaries', icon: UserCheck },
    { id: 'consents', label: 'Open Banking Consents', icon: ShieldCheck, badge: 'Core' },
    { id: 'tpp-console', label: 'Data Consumer (TPP)', icon: Layers, badge: 'TPP' },
    { id: 'audit', label: 'Audit & Governance', icon: FileText, badge: 'RBAC' },
    { id: 'system', label: 'System Health & Info', icon: Activity },
  ];

  const visibleMenuItems = currentRole === 'ADMIN' 
    ? allMenuItems 
    : allMenuItems.filter(item => allowedTabs.includes(item.id));

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-icon">B</div>
        <div>
          <div className="brand-title">BANFICO</div>
          <div className="brand-subtitle">Open Banking Core</div>
        </div>
      </div>

      <nav className="nav-list">
        {visibleMenuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`nav-item ${isActive ? 'active' : ''}`}
            >
              <Icon size={18} />
              <span style={{ flex: 1 }}>{item.label}</span>
              {item.badge && (
                <span style={{ 
                  fontSize: '0.65rem', 
                  padding: '2px 6px', 
                  background: 'rgba(6, 182, 212, 0.2)', 
                  color: 'var(--accent-cyan)', 
                  borderRadius: '4px',
                  fontWeight: 700 
                }}>
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      <div style={{ 
        marginTop: 'auto', 
        padding: '12px', 
        background: 'rgba(255,255,255,0.02)', 
        borderRadius: 'var(--radius-sm)', 
        border: '1px solid var(--border-color)',
        fontSize: '0.75rem',
        color: 'var(--text-muted)'
      }}>
        <div style={{ color: 'var(--text-main)', fontWeight: 600, marginBottom: '2px' }}>Java 21 • Spring Boot 3</div>
        <div>PostgreSQL • Keycloak • Nginx</div>
      </div>
    </aside>
  );
}
