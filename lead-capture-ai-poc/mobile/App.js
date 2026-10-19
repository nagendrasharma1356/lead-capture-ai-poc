import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { API_URL } from './config';

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

const TYPES = ['SINGLE_FAMILY', 'MULTI_FAMILY', 'CONDO', 'LAND'];
const CONDITIONS = ['EXCELLENT', 'FAIR', 'NEEDS_REPAIR'];
const TIER_COLORS = { HOT: '#c0392b', WARM: '#d68910', COLD: '#5d6d7e' };

async function call(path, options) {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const first = data.errors ? Object.values(data.errors)[0] : null;
    throw new Error(first || data.message || `Request failed (${res.status})`);
  }
  return data;
}

export default function App() {
  const [tab, setTab] = useState('form');

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="light" />
      <View style={styles.header}>
        <Text style={styles.title}>Lead Capture AI</Text>
        <View style={styles.tabs}>
          <Tab label="Form" active={tab === 'form'} onPress={() => setTab('form')} />
          <Tab label="Leads" active={tab === 'leads'} onPress={() => setTab('leads')} />
        </View>
      </View>
      {tab === 'form' ? <LeadForm onDone={() => setTab('leads')} /> : <LeadList />}
    </SafeAreaView>
  );
}

function Tab({ label, active, onPress }) {
  return (
    <Pressable onPress={onPress} style={[styles.tab, active && styles.tabActive]}>
      <Text style={[styles.tabText, active && styles.tabTextActive]}>{label}</Text>
    </Pressable>
  );
}

function LeadForm({ onDone }) {
  const [form, setForm] = useState(EMPTY);
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);

  const set = (key) => (value) => setForm((f) => ({ ...f, [key]: value }));

  async function submit() {
    setBusy(true);
    try {
      const lead = await call('/leads?source=MOBILE', { method: 'POST', body: JSON.stringify(form) });
      setResult(lead);
      setForm(EMPTY);
    } catch (e) {
      Alert.alert('Could not submit', e.message);
    } finally {
      setBusy(false);
    }
  }

  if (result) {
    return (
      <ScrollView contentContainerStyle={styles.body}>
        <Text style={styles.h2}>Thanks, {result.name}!</Text>
        <View style={styles.aiBox}>
          <Text style={styles.bold}>AI agent result</Text>
          <Text style={styles.text}>{result.aiSummary}</Text>
          <Text style={styles.text}>
            Score {result.score} - Tier {result.tier} - {result.status}
          </Text>
        </View>
        <Pressable style={styles.button} onPress={() => setResult(null)}>
          <Text style={styles.buttonText}>Submit another</Text>
        </Pressable>
        <Pressable style={styles.buttonGhost} onPress={onDone}>
          <Text style={styles.buttonGhostText}>View leads</Text>
        </Pressable>
      </ScrollView>
    );
  }

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={styles.body} keyboardShouldPersistTaps="handled">
        <Text style={styles.h2}>Get Your Cash Offer</Text>
        <Text style={styles.muted}>Answer a few questions about your property.</Text>

        <Input label="Full name" value={form.name} onChangeText={set('name')} />
        <Input label="Phone" value={form.phone} onChangeText={set('phone')} keyboardType="phone-pad" />
        <Input label="Email (optional)" value={form.email} onChangeText={set('email')} keyboardType="email-address" autoCapitalize="none" />
        <Input label="Property address" value={form.propertyAddress} onChangeText={set('propertyAddress')} />
        <Input label="Zip code" value={form.zip} onChangeText={set('zip')} keyboardType="number-pad" />

        <Chips label="Property type" options={TYPES} value={form.propertyType} onChange={set('propertyType')} />
        <Chips label="Condition" options={CONDITIONS} value={form.propertyCondition} onChange={set('propertyCondition')} />

        <View style={styles.consentRow}>
          <Switch value={form.consent} onValueChange={set('consent')} />
          <Text style={styles.consentText}>
            I agree to be contacted by phone, SMS or email about my request. Message/data rates may apply.
          </Text>
        </View>

        <Pressable style={[styles.button, busy && { opacity: 0.6 }]} onPress={submit} disabled={busy}>
          {busy ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Get My Offer</Text>}
        </Pressable>
        <Text style={styles.tiny}>100% Free - No Obligation</Text>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

function LeadList() {
  const [leads, setLeads] = useState([]);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setRefreshing(true);
    try {
      setLeads(await call('/leads'));
      setError('');
    } catch (e) {
      setError(`Cannot reach the backend: ${e.message}. Check API_URL in config.js.`);
    } finally {
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <FlatList
      data={leads}
      keyExtractor={(l) => String(l.id)}
      contentContainerStyle={styles.body}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={load} />}
      ListHeaderComponent={error ? <Text style={styles.error}>{error}</Text> : null}
      ListEmptyComponent={<Text style={styles.muted}>No leads yet. Pull to refresh.</Text>}
      renderItem={({ item }) => (
        <View style={styles.leadCard}>
          <View style={styles.leadTop}>
            <Text style={styles.bold}>{item.name}</Text>
            <Text style={[styles.pill, { backgroundColor: TIER_COLORS[item.tier] || '#999' }]}>{item.tier}</Text>
          </View>
          <Text style={styles.muted}>{item.phone} - {item.zip} - {item.source}</Text>
          <Text style={styles.text}>{item.aiSummary}</Text>
        </View>
      )}
    />
  );
}

function Input({ label, ...props }) {
  return (
    <View style={styles.field}>
      <Text style={styles.label}>{label}</Text>
      <TextInput style={styles.input} placeholderTextColor="#9aa5a0" {...props} />
    </View>
  );
}

function Chips({ label, options, value, onChange }) {
  return (
    <View style={styles.field}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.chips}>
        {options.map((o) => (
          <Pressable key={o} onPress={() => onChange(o)} style={[styles.chip, value === o && styles.chipActive]}>
            <Text style={[styles.chipText, value === o && styles.chipTextActive]}>{o.replace('_', ' ').toLowerCase()}</Text>
          </Pressable>
        ))}
      </View>
    </View>
  );
}

const GREEN = '#1f5d3a';
const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#f7f3e8' },
  header: { backgroundColor: '#15432a', paddingHorizontal: 16, paddingTop: 12, paddingBottom: 8 },
  title: { color: '#fff', fontSize: 18, fontWeight: '700', marginBottom: 8 },
  tabs: { flexDirection: 'row', gap: 8 },
  tab: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 8 },
  tabActive: { backgroundColor: GREEN },
  tabText: { color: '#d8eadf' },
  tabTextActive: { color: '#fff', fontWeight: '600' },
  body: { padding: 16, paddingBottom: 48 },
  h2: { fontSize: 22, fontWeight: '700', color: '#1d2a22', marginBottom: 4 },
  muted: { color: '#6b7a70', marginBottom: 8 },
  text: { color: '#1d2a22', marginTop: 4 },
  bold: { fontWeight: '700', color: '#1d2a22' },
  tiny: { textAlign: 'center', color: '#6b7a70', fontSize: 12, marginTop: 8 },
  error: { color: '#c0392b', marginBottom: 12 },
  field: { marginTop: 12 },
  label: { color: '#6b7a70', fontSize: 13, marginBottom: 4 },
  input: { backgroundColor: '#fff', borderColor: '#e1dccb', borderWidth: 1, borderRadius: 8, padding: 10, fontSize: 16 },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { paddingVertical: 6, paddingHorizontal: 12, borderRadius: 999, borderWidth: 1, borderColor: '#e1dccb', backgroundColor: '#fff' },
  chipActive: { backgroundColor: GREEN, borderColor: GREEN },
  chipText: { color: '#1d2a22', textTransform: 'capitalize' },
  chipTextActive: { color: '#fff' },
  consentRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 16 },
  consentText: { flex: 1, fontSize: 13, color: '#1d2a22' },
  button: { backgroundColor: '#e8590c', borderRadius: 8, padding: 14, alignItems: 'center', marginTop: 16 },
  buttonText: { color: '#fff', fontWeight: '700', fontSize: 16 },
  buttonGhost: { borderWidth: 1, borderColor: GREEN, borderRadius: 8, padding: 14, alignItems: 'center', marginTop: 10 },
  buttonGhostText: { color: GREEN, fontWeight: '600' },
  aiBox: { backgroundColor: '#eef7f1', borderColor: '#c8e2d1', borderWidth: 1, borderRadius: 10, padding: 12, marginTop: 12 },
  leadCard: { backgroundColor: '#fff', borderColor: '#e1dccb', borderWidth: 1, borderRadius: 12, padding: 12, marginBottom: 10 },
  leadTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  pill: { color: '#fff', fontSize: 11, fontWeight: '700', paddingHorizontal: 8, paddingVertical: 2, borderRadius: 999, overflow: 'hidden' },
});
