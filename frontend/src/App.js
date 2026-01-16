import React, { useState } from 'react';
import './App.css';
import Dashboard from './components/Dashboard';
import RideRequest from './components/RideRequest';
import DriverPanel from './components/DriverPanel';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');

  return (
    <div className="App">
      <header className="App-header">
        <h1>🚗 GoComet Ride Hailing</h1>
        <nav className="nav-tabs">
          <button 
            className={activeTab === 'dashboard' ? 'active' : ''} 
            onClick={() => setActiveTab('dashboard')}
          >
            Dashboard
          </button>
          <button 
            className={activeTab === 'rider' ? 'active' : ''} 
            onClick={() => setActiveTab('rider')}
          >
            Request Ride
          </button>
          <button 
            className={activeTab === 'driver' ? 'active' : ''} 
            onClick={() => setActiveTab('driver')}
          >
            Driver Panel
          </button>
        </nav>
      </header>

      <main className="App-main">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'rider' && <RideRequest />}
        {activeTab === 'driver' && <DriverPanel />}
      </main>

      <footer className="App-footer">
        <p>© 2026 GoComet - Multi-Region Ride Hailing Platform</p>
      </footer>
    </div>
  );
}

export default App;
