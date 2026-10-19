import React, { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';

export default function TestRunner() {
  const [count, setCount] = useState(10);
  const [minDelay, setMinDelay] = useState(1000);
  const [maxDelay, setMaxDelay] = useState(3000);
  const [sendWebhook, setSendWebhook] = useState(false);
  const [status, setStatus] = useState(null);
  const [error, setError] = useState('');
  const timer = useRef(null);

  async function refresh() {
    try {
      setStatus(await api.testRunStatus());
    } catch (e) {
      setError(`Cannot reach the backend (${e.message}).`);
    }
  }

  useEffect(() => {
    refresh();
    timer.current = setInterval(refresh, 1500);
    return () => clearInterval(timer.current);
  }, []);

  async function start() {
    setError('');
    try {
      setStatus(await api.startTestRun({
        count: Number(count),
        minDelayMs: Number(minDelay),
        maxDelayMs: Number(maxDelay),
        sendWebhook,
      }));
    } catch (e) {
      setError(e.message);
    }
  }

  async function stop() {
    setStatus(await api.stopTestRun());
  }

  async function clearData() {
    await api.clearTestData();
    refresh();
  }

  const running = status?.running;
  const pct = status && status.total ? Math.round((status.sent + status.failed) * 100 / status.total) : 0;

  return (
    <section>
      <h2>QA test runner</h2>
      <p className="muted">
        Sends clearly fake records (<code>Test User N</code>, <code>555-01xx</code> phones, <code>@example.com</code> emails,
        source = TEST) through the same pipeline to check your form, AI step and webhook. Real people&apos;s data is never used.
      </p>

      <div className="card controls">
        <label>Records (max 100)
          <input type="number" min="1" max="100" value={count} onChange={(e) => setCount(e.target.value)} disabled={running} />
        </label>
        <label>Min delay (ms)
          <input type="number" min="200" value={minDelay} onChange={(e) => setMinDelay(e.target.value)} disabled={running} />
        </label>
        <label>Max delay (ms)
          <input type="number" min="200" value={maxDelay} onChange={(e) => setMaxDelay(e.target.value)} disabled={running} />
        </label>
        <label className="inline">
          <input type="checkbox" checked={sendWebhook} onChange={(e) => setSendWebhook(e.target.checked)} disabled={running} />
          Also send to webhook (Zapier / Make)
        </label>
        <div className="buttons">
          <button className="primary" onClick={start} disabled={running}>Start</button>
          <button onClick={stop} disabled={!running}>Stop</button>
          <button onClick={clearData} disabled={running}>Delete test leads</button>
        </div>
        {error && <div className="field-error">{error}</div>}
      </div>

      {status && (
        <div className="card">
          <div className="progress"><div style={{ width: `${pct}%` }} /></div>
          <p>
            {running ? 'Running' : 'Idle'} &middot; sent {status.sent} / {status.total} &middot; failed {status.failed}
          </p>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>#</th><th>Time</th><th>Lead</th><th>Zip</th><th>Status</th><th>Tier</th><th>ms</th><th>Webhook</th><th>Error</th></tr>
              </thead>
              <tbody>
                {[...status.log].reverse().map((l) => (
                  <tr key={l.n}>
                    <td>{l.n}</td>
                    <td>{new Date(l.time).toLocaleTimeString()}</td>
                    <td>{l.name} {l.leadId ? `(#${l.leadId})` : ''}</td>
                    <td>{l.zip}</td>
                    <td>{l.status}</td>
                    <td>{l.tier && <span className={`pill ${l.tier}`}>{l.tier}</span>}</td>
                    <td>{l.ms}</td>
                    <td>{l.webhookSent ? 'sent' : '-'}</td>
                    <td className="field-error">{l.error}</td>
                  </tr>
                ))}
                {!status.log.length && <tr><td colSpan="9" className="muted">No runs yet.</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </section>
  );
}
