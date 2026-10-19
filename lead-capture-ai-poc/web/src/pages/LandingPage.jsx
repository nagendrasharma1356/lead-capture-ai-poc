import React, { useState } from 'react';
import { api } from '../api.js';

const EMPTY = {
  name: '',
  phone: '',
  email: '',
  zip: '',
  propertyAddress: '',
  propertyType: 'SINGLE_FAMILY',
  propertyCondition: 'FAIR',
  consent: false,
};

export default function LandingPage() {
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const set = (key) => (e) =>
    setForm({ ...form, [key]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    setErrors({});
    try {
      const lead = await api.submitLead(form);
      setResult(lead);
      setForm(EMPTY);
    } catch (err) {
      setErrors(err.fieldErrors || {});
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (result) {
    return (
      <section className="card narrow">
        <h2>Thanks, {result.name}!</h2>
        <p>We received your request and will contact you shortly.</p>
        <div className="ai-box">
          <strong>AI agent result</strong>
          <p>{result.aiSummary}</p>
          <p>
            Score <b>{result.score}</b> &middot; Tier <span className={`pill ${result.tier}`}>{result.tier}</span>{' '}
            &middot; Status {result.status}
          </p>
        </div>
        <button onClick={() => setResult(null)}>Submit another</button>
      </section>
    );
  }

  return (
    <section className="card narrow">
      <h2>Get Your Cash Offer</h2>
      <p className="muted">Answer a few questions about your property.</p>
      <form onSubmit={submit} noValidate>
        <Field label="Full name" error={errors.name}>
          <input value={form.name} onChange={set('name')} placeholder="Your name" />
        </Field>
        <Field label="Phone" error={errors.phone}>
          <input value={form.phone} onChange={set('phone')} placeholder="312-555-0100" />
        </Field>
        <Field label="Email (optional)" error={errors.email}>
          <input value={form.email} onChange={set('email')} placeholder="you@example.com" />
        </Field>
        <Field label="Property address" error={errors.propertyAddress}>
          <input value={form.propertyAddress} onChange={set('propertyAddress')} placeholder="221 Lake Street, Chicago" />
        </Field>
        <Field label="Zip code" error={errors.zip}>
          <input value={form.zip} onChange={set('zip')} placeholder="60601" />
        </Field>
        <div className="row">
          <Field label="Property type">
            <select value={form.propertyType} onChange={set('propertyType')}>
              <option value="SINGLE_FAMILY">Single family</option>
              <option value="MULTI_FAMILY">Multi family</option>
              <option value="CONDO">Condo</option>
              <option value="LAND">Land</option>
            </select>
          </Field>
          <Field label="Condition">
            <select value={form.propertyCondition} onChange={set('propertyCondition')}>
              <option value="EXCELLENT">Excellent</option>
              <option value="FAIR">Fair</option>
              <option value="NEEDS_REPAIR">Needs repair</option>
            </select>
          </Field>
        </div>

        <label className="consent">
          <input type="checkbox" checked={form.consent} onChange={set('consent')} />
          <span>
            I agree to be contacted by phone, SMS or email about my request. Message/data rates may apply.
          </span>
        </label>
        {errors.consent && <div className="field-error">{errors.consent}</div>}

        {error && !Object.keys(errors).length && <div className="field-error">{error}</div>}
        <button className="primary" disabled={busy}>
          {busy ? 'Submitting...' : 'Get My Offer'}
        </button>
        <p className="tiny">100% Free &middot; No Obligation</p>
      </form>
    </section>
  );
}

function Field({ label, error, children }) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
      {error && <div className="field-error">{error}</div>}
    </div>
  );
}
