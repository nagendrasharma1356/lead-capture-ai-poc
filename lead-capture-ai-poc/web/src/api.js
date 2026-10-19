const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const err = new Error(data.message || `Request failed (${res.status})`);
    err.fieldErrors = data.errors || {};
    throw err;
  }
  return data;
}

export const api = {
  submitLead: (lead) => request('/leads?source=WEB', { method: 'POST', body: JSON.stringify(lead) }),
  leads: () => request('/leads'),
  stats: () => request('/stats'),
  startTestRun: (cfg) => request('/test-runs/start', { method: 'POST', body: JSON.stringify(cfg) }),
  stopTestRun: () => request('/test-runs/stop', { method: 'POST' }),
  testRunStatus: () => request('/test-runs/status'),
  clearTestData: () => request('/test-runs/data', { method: 'DELETE' }),
};
