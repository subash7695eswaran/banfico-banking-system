import React from 'react';
import { 
  User, 
  Building, 
  Layers, 
  ShieldAlert, 
  PenTool, 
  CheckCheck, 
  Settings,
  LogOut,
  ShieldCheck
} from 'lucide-react';

export const ROLES = {
  CUSTOMER: {
    id: 'CUSTOMER',
    name: 'Customer Profile',
    roleName: 'Customer / Data Owner',
    email: 'customer@banfico.com',
    customerId: 1,
    color: '#34d399',
    icon: User,
    description: 'Owns banking data; approves, rejects, and revokes TPP consent requests.',
    allowedTabs: ['dashboard', 'accounts', 'transactions', 'beneficiaries', 'consents']
  },
  CONSENT_REQUESTER: {
    id: 'CONSENT_REQUESTER',
    name: 'FinTech App (TPP)',
    roleName: 'Consent Requester / Data Consumer',
    email: 'tpp@fintech-app.com',
    tppClientId: 'fintech-aggregator-tpp',
    color: '#06b6d4',
    icon: Layers,
    description: 'Requests customer consent with defined purpose/scope; fetches authorized data.',
    allowedTabs: ['tpp-console', 'consents', 'system']
  },
  BANK_PROVIDER: {
    id: 'BANK_PROVIDER',
    name: 'Banfico Data Provider',
    roleName: 'Bank / Data Provider',
    email: 'dataprovider@banfico.com',
    color: '#818cf8',
    icon: Building,
    description: 'Maintains core banking accounts & validates consent before releasing data.',
    allowedTabs: ['dashboard', 'customers', 'accounts', 'transactions', 'consents']
  },
  CONSENT_MANAGER: {
    id: 'CONSENT_MANAGER',
    name: 'Sarah (Compliance)',
    roleName: 'Consent Manager',
    email: 'consentmanager@banfico.com',
    color: '#fbbf24',
    icon: ShieldAlert,
    description: 'Oversees complete consent lifecycle, state transitions, validity, and audit trail.',
    allowedTabs: ['consents', 'audit', 'system']
  },
  MAKER: {
    id: 'MAKER',
    name: 'Alex (Maker)',
    roleName: 'Banking Maker',
    email: 'maker@banfico.com',
    color: '#38bdf8',
    icon: PenTool,
    description: 'Initiates banking operations and high-value transfers requiring checker approval.',
    allowedTabs: ['dashboard', 'accounts', 'transactions', 'beneficiaries']
  },
  CHECKER: {
    id: 'CHECKER',
    name: 'Chris (Checker)',
    roleName: 'Banking Checker',
    email: 'checker@banfico.com',
    color: '#f43f5e',
    icon: CheckCheck,
    description: 'Reviews and provides dual authorization for maker operations & high-value transfers.',
    allowedTabs: ['dashboard', 'transactions', 'accounts', 'consents']
  },
  ADMIN: {
    id: 'ADMIN',
    name: 'System Administrator',
    roleName: 'Admin',
    email: 'admin@banfico.com',
    color: '#a855f7',
    icon: Settings,
    description: 'Manages users, roles, system health & audit overview without bypassing consent rules.',
    allowedTabs: ['dashboard', 'customers', 'accounts', 'transactions', 'beneficiaries', 'consents', 'tpp-console', 'audit', 'system']
  }
};

export default function RoleSelector({ currentRole, setCurrentRole, setActiveTab, authUser, onLogout }) {
  const activeRoleObj = ROLES[currentRole] || ROLES.CUSTOMER;
  const Icon = activeRoleObj.icon;

  const displayName = authUser?.fullName || authUser?.username || activeRoleObj.name;
  const displayEmail = authUser?.email || activeRoleObj.email;

  React.useEffect(() => {
    const active = ROLES[currentRole] || ROLES.CUSTOMER;
    localStorage.setItem('banfico_current_role', active.id);
    localStorage.setItem('banfico_current_user', displayEmail);
  }, [currentRole, displayEmail]);

  const handleRoleChange = (roleKey) => {
    setCurrentRole(roleKey);
    const targetRole = ROLES[roleKey];
    if (targetRole && !targetRole.allowedTabs.includes('dashboard')) {
      setActiveTab(targetRole.allowedTabs[0]);
    }
  };

  return (
    <div style={{
      background: 'rgba(17, 24, 39, 0.95)',
      borderBottom: '1px solid var(--border-color)',
      padding: '10px 24px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      position: 'sticky',
      top: 0,
      zIndex: 20,
      backdropFilter: 'blur(8px)'
    }}>
      {/* Active Persona Banner */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{
          width: '36px',
          height: '36px',
          borderRadius: '8px',
          background: `${activeRoleObj.color}22`,
          border: `1px solid ${activeRoleObj.color}55`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: activeRoleObj.color
        }}>
          <Icon size={18} />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#ffffff' }}>
              {displayName}
            </span>
            <span style={{
              fontSize: '0.68rem',
              fontWeight: 700,
              padding: '2px 8px',
              borderRadius: '999px',
              background: `${activeRoleObj.color}20`,
              color: activeRoleObj.color,
              border: `1px solid ${activeRoleObj.color}40`,
              letterSpacing: '0.03em',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px'
            }}>
              <ShieldCheck size={11} /> {activeRoleObj.roleName.toUpperCase()}
            </span>
            <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
              ({displayEmail})
            </span>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            {activeRoleObj.description}
          </div>
        </div>
      </div>

      {/* Right controls: Persona Switcher & Logout */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.72rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            ACTOR SWITCHER:
          </span>
          <select
            value={currentRole}
            onChange={(e) => handleRoleChange(e.target.value)}
            className="form-control"
            style={{
              width: 'auto',
              padding: '5px 10px',
              fontSize: '0.78rem',
              background: 'rgba(255,255,255,0.06)',
              borderColor: activeRoleObj.color,
              color: '#ffffff',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            {Object.keys(ROLES).map((key) => (
              <option key={key} value={key} style={{ background: '#111827', color: '#ffffff' }}>
                {ROLES[key].roleName}
              </option>
            ))}
          </select>
        </div>

        {/* Logout Button */}
        <button
          onClick={onLogout}
          className="btn btn-secondary btn-sm"
          title="Sign out from Keycloak session"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '6px 12px',
            fontSize: '0.78rem',
            color: '#fb7185',
            borderColor: 'rgba(244, 63, 94, 0.3)'
          }}
        >
          <LogOut size={14} /> Sign Out
        </button>
      </div>
    </div>
  );
}
