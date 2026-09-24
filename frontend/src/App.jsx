import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import Accounts from './pages/Accounts';
import Transactions from './pages/Transactions';
import Beneficiaries from './pages/Beneficiaries';
import Consents from './pages/Consents';
import SystemInfo from './pages/SystemInfo';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');

  return (
    <div className="app-layout">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />
      
      <main className="main-content">
        {activeTab === 'dashboard' && <Dashboard setActiveTab={setActiveTab} />}
        {activeTab === 'customers' && <Customers />}
        {activeTab === 'accounts' && <Accounts />}
        {activeTab === 'transactions' && <Transactions />}
        {activeTab === 'beneficiaries' && <Beneficiaries />}
        {activeTab === 'consents' && <Consents />}
        {activeTab === 'system' && <SystemInfo />}
      </main>
    </div>
  );
}
