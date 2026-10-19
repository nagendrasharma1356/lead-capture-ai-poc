import React, { useState } from 'react';
import LandingPage from './pages/LandingPage.jsx';
import Dashboard from './pages/Dashboard.jsx';
import TestRunner from './pages/TestRunner.jsx';

const TABS = [
  { id: 'landing', label: 'Landing page' },
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'tests', label: 'QA test runner' },
];

export default function App() {
  const [tab, setTab] = useState('landing');

  return (
    <div className="app">
      <header className="topbar">
        <h1>Lead Capture AI <span>POC</span></h1>
        <nav>
          {TABS.map((t) => (
            <button key={t.id} className={tab === t.id ? 'active' : ''} onClick={() => setTab(t.id)}>
              {t.label}
            </button>
          ))}
        </nav>
      </header>
      <main>
        {tab === 'landing' && <LandingPage />}
        {tab === 'dashboard' && <Dashboard />}
        {tab === 'tests' && <TestRunner />}
      </main>
    </div>
  );
}
