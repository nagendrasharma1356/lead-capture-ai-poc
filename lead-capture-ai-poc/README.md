# Lead Capture AI POC (Java + React + React Native)

Consent-based lead capture ka POC: landing page / mobile form -> **AI agent** lead ko score karta hai -> webhook (Zapier / Make.com) -> dashboard.
Saath mein ek **QA test runner** hai jo sirf **fake test data** se aapka apna pipeline check karta hai.

```
React landing page ─┐
                    ├─> Spring Boot API ─> AI qualifier (rule / Claude) ─> H2 DB ─> Webhook (Zapier/Make)
React Native form ──┘                                  ▲
                                         QA test runner (fake data, source=TEST)
```

## Folder structure

| Folder | Kya hai |
|---|---|
| `backend/` | Java 17, Spring Boot 3.3, JPA, H2, Maven |
| `web/` | React (Vite): landing page, dashboard, QA test runner |
| `mobile/` | React Native (Expo): lead form + leads list |
| `postman_collection.json` | Saari APIs ka Postman collection |

## 1. Backend chalana (IntelliJ)

1. IntelliJ mein `backend/` folder ko **Open as Maven project** karein.
2. `LeadCaptureApplication` run karein (port **8080**).
3. H2 console: http://localhost:8080/h2-console  
   JDBC URL: `jdbc:h2:file:./data/leaddb`, user `sa`, password khali.
4. Tests: `mvn test` (consent rule, duplicate detection, test runner).

### Optional settings (environment variables)

| Variable | Matlab |
|---|---|
| `AI_PROVIDER=claude` + `ANTHROPIC_API_KEY=...` | Real Claude se lead qualify hoti hai. Default `rule` hai: free, kisi key ki zaroorat nahi. Claude fail ho to rule-based par fallback. |
| `ANTHROPIC_MODEL` | Default `claude-sonnet-5-5` |
| `WEBHOOK_URL` | Zapier / Make.com "catch hook" URL. Khali ho to sirf log hota hai. |

## 2. Web app chalana

```bash
cd web
npm install
npm run dev        # http://localhost:5173
```

Teen tabs: **Landing page** (consent checkbox ke saath form), **Dashboard** (stats + leads), **QA test runner**.
Backend alag URL par ho to `VITE_API_URL` set karein.

## 3. Mobile app chalana (Expo)

```bash
cd mobile
npm install
npx expo install --fix     # Expo SDK ke hisaab se versions align karta hai
```

`mobile/config.js` mein `API_URL` ko apne laptop ke LAN IP par set karein (phone aur laptop same Wi-Fi par), phir:

```bash
npx expo start             # Expo Go app se QR scan karein
```

## 4. Flow ko test karna

1. Landing page ya mobile form bharein (consent tick karna zaroori hai, warna API `400` deti hai).
2. Dashboard mein lead dikhegi: score, tier (HOT / WARM / COLD), AI summary, webhook status.
3. Same phone dobara bhejne par status `DUPLICATE` ho jata hai.
4. **QA test runner** tab: records (max 100), min/max delay chunein, **Start**; kabhi bhi **Stop**.
   - Sirf clearly fake data banta hai: `Test User N`, `555-01xx` phones, `test+N@example.com`, `source=TEST`.
   - "Also send to webhook" tick karenge to Zapier/Make zap ko payload milta hai (`"test": true` ke saath, zap mein filter laga sakte hain).
   - **Delete test leads** se test data saaf ho jata hai.

## API summary

| Method | Path | Kaam |
|---|---|---|
| POST | `/api/leads?source=WEB\|MOBILE` | Lead submit (consent=true zaroori) |
| GET | `/api/leads` | Saari leads |
| GET | `/api/stats` | Counts |
| POST | `/api/test-runs/start` | `{count, minDelayMs, maxDelayMs, sendWebhook}` |
| POST | `/api/test-runs/stop` | Run rokna |
| GET | `/api/test-runs/status` | Progress + log |
| DELETE | `/api/test-runs/data` | Test leads delete |

## Design notes

- Har real lead ke saath user ki consent save hoti hai (`consent`, `consentText`).
- AI step ek interface (`LeadQualifier`) ke peeche hai: rule-based aur Claude, dono swap ho sakte hain.
- Test runner kisi real person ka data, proxy ya IP rotation use nahi karta; sirf aapke apne system ko check karta hai.

## Next steps (production ke liye)

Spring Security + JWT (aapke customer-management-api jaisa), MySQL/PostgreSQL, rate limiting + CAPTCHA, Docker, CRM connector (GHL, etc.).
