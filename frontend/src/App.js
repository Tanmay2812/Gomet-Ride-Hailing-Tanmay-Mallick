import React, { useState, useEffect } from 'react';
import './App.css';
import Dashboard from './components/Dashboard';
import RideRequest from './components/RideRequest';
import DriverPanel from './components/DriverPanel';
import RiderDetails from './components/RiderDetails';
import DriverDetails from './components/DriverDetails';

// GLOBAL TEST - runs immediately after imports
console.log('[App] 🚨🚨🚨 App.js MODULE LOADED!');
console.log('[App] ✅ All imports loaded - Dashboard:', typeof Dashboard);

function App() {
  console.log('[App] 🎯 App function EXECUTING!');
  
  // Initialize activeTab from localStorage or default to 'dashboard'
  const [activeTab, setActiveTab] = useState(() => {
    const savedTab = localStorage.getItem('activeTab');
    return savedTab || 'dashboard';
  });
  
  // Persist activeTab to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem('activeTab', activeTab);
    console.log('[App] 💾 Saved activeTab to localStorage:', activeTab);
  }, [activeTab]);
  
  console.log('[App] Active tab:', activeTab);
  console.log('[App] Should render Dashboard?', activeTab === 'dashboard');
  console.log('[App] Dashboard component:', Dashboard);

  return (
    <div className="App">
      <header className="App-header">
        <h1>🚗 GoComet Ride Hailing</h1>
        <nav className="nav-tabs">
          <button 
            className={activeTab === 'dashboard' ? 'active' : ''} 
            onClick={() => {
              console.log('[App] Dashboard button clicked!');
              setActiveTab('dashboard');
            }}
          >
            Dashboard
          </button>
          <button 
            className={activeTab === 'rider' ? 'active' : ''} 
            onClick={() => {
              console.log('[App] Request Ride button clicked!');
              setActiveTab('rider');
            }}
          >
            Request Ride
          </button>
          <button 
            className={activeTab === 'driver' ? 'active' : ''} 
            onClick={() => {
              console.log('[App] Driver Panel button clicked!');
              setActiveTab('driver');
            }}
          >
            Driver Panel
          </button>
          <button 
            className={activeTab === 'rider-details' ? 'active' : ''} 
            onClick={() => {
              console.log('[App] Rider Details button clicked!');
              setActiveTab('rider-details');
            }}
          >
            Rider Details
          </button>
          <button 
            className={activeTab === 'driver-details' ? 'active' : ''} 
            onClick={() => {
              console.log('[App] Driver Details button clicked!');
              setActiveTab('driver-details');
            }}
          >
            Driver Details
          </button>
        </nav>
      </header>

      <main className="App-main">
        {(() => {
          console.log('[App] 🎨 Rendering main content, activeTab:', activeTab);
          if (activeTab === 'dashboard') {
            console.log('[App] 🎯🎯🎯 RENDERING DASHBOARD NOW!');
            console.log('[App] Dashboard component type:', typeof Dashboard);
            return <Dashboard key="dashboard" />;
          } else if (activeTab === 'rider') {
            return <RideRequest key="rider" />;
          } else if (activeTab === 'driver') {
            return <DriverPanel key="driver" />;
          } else if (activeTab === 'rider-details') {
            return <RiderDetails key="rider-details" />;
          } else if (activeTab === 'driver-details') {
            return <DriverDetails key="driver-details" />;
          }
          return null;
        })()}
      </main>

      <footer className="App-footer">
        <p>© 2026 GoComet - Multi-Region Ride Hailing Platform</p>
      </footer>
    </div>
  );
}

export default App;
