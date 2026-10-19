import React, { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [leads, setLeads] = useState([]);
  const [error, setError] = useState('');

  async function load() {
    try {
      const [s, l] = await Promise.all([api.stats(), api.leads()]);
      setStats(s);
      setLeads(l);
      setError('');
    } catch (e) {
      setError(`Cannot reach the backend (${e.message}). Is Spring Boot running on port 8080?`);
    }
  }

  useEffect(() => {
    load();
    const id = setInterval(load, 4000);
    return () => clearInterval(id);
  }, []);

  return (
    <section>
      <h2>Lead dashboard</h2>
      {error && <div className="field-error">{error}</div>}
      {stats && (
        <div className="stats">
          <Stat label="Total" value={stats.total} />
          <Stat label="Hot" value={stats.hot} cls="HOT" />
          <Stat label="Warm" value={stats.warm} cls="WARM" />
          <Stat label="Cold" value={stats.cold} cls="COLD" />
          <Stat label="Duplicates" value={stats.duplicates} />
          <Stat label="Web / Mobile / Test" value={`${stats.web} / ${stats.mobile} / ${stats.test}`} />
        </div>
      )}
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>#</th><th>Name</th><th>Phone</th><th>Zip</th><th>Source</th>
              <th>Score</th><th>Tier</th><th>Status</th><th>AI summary</th><th>Webhook</th>
            </tr>
          </thead>
          <tbody>
            {leads.map((l) => (
              <tr key={l.id}>
                <td>{l.id}</td>
                <td>{l.name}</td>
                <td>{l.phone}</td>
                <td>{l.zip}</td>
                <td><span className="pill src">{l.source}</span></td>
                <td>{l.score}</td>
                <td><span className={`pill ${l.tier}`}>{l.tier}</span></td>
                <td>{l.status}</td>
                <td className="summary">{l.aiSummary}</td>
                <td>{l.webhookSent ? 'sent' : '-'}</td>
              </tr>
            ))}
            {!leads.length && (
              <tr><td colSpan="10" className="muted">No leads yet. Submit the landing page or run the QA test runner.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Stat({ label, value, cls }) {
  return (
    <div className={`stat ${cls || ''}`}>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}
