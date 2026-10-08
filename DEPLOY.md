# Deploy the Farmer+ backend to Render.com (free)

This makes the APK work **from anywhere** — no same-Wi-Fi requirement, no
manual server URL entry. Anyone who installs the APK can register/login
immediately.

## Architecture

```
Phone (anywhere) ──https──▶ Render.com (Spring Boot) ──▶ Render PostgreSQL
```

## Files added

| File | Purpose |
|------|---------|
| `backend/Dockerfile` | Builds the Spring Boot fat jar inside Docker |
| `backend/entrypoint.sh` | Converts Render's `DATABASE_URL` to Spring's JDBC format |
| `render.yaml` | Render Blueprint: web service + free PostgreSQL |
| `backend/src/main/resources/application.properties` | Now reads env vars with local fallbacks |

## Step-by-step deployment

### 1. Push the repo to GitHub

```bash
git init
git add .
git commit -m "Prepare backend for Render deployment"
git remote add origin https://github.com/<your-username>/farmer.git
git push -u origin main
```

> If `backend/entrypoint.sh` loses its executable bit on push, the Docker
> build still works because the Dockerfile runs `chmod +x` on it.

### 2. Create the Render account & blueprint

1. Go to https://dashboard.render.com and sign up (GitHub login is easiest).
2. Click **New +** → **Blueprint**.
3. Select your `farmer` repository → Render detects `render.yaml`.
4. Review: it will create **farmer-db** (free PostgreSQL) and
   **farmer-backend** (free web service).
5. Click **Apply**. First build takes ~5–10 minutes.

> When prompted for the `AI_API_KEY` env var, paste your key
> (`sk-scdpe2vFyhWRwhpvowBdLVBZW2evFwMUD7WG48FN1ff5mTOX`) or leave blank to
> run the chatbot in offline knowledge-base mode.

### 3. Get your public URL

After deploy, Render shows:

```
https://farmer-backend-xxxx.onrender.com
```

Test it in a browser:

```
https://farmer-backend-xxxx.onrender.com/api/products
```

You should see JSON (or `[]`). If the service was asleep, the first load
takes ~50 seconds — that's normal on the free tier.

### 4. Point the APK at the deployed backend

Edit [`app/build.gradle.kts`](app/build.gradle.kts) — replace the fallback
URL in **both** build types with your Render URL:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://farmer-backend-xxxx.onrender.com/\"")
```

Then rebuild:

```bash
gradlew.bat assembleRelease
```

Install `app/build/outputs/apk/release/app-release.apk` on any phone —
registration and login work immediately, from any network, with **zero
configuration**.

> The in-app **Settings → Server URL** override still works if you ever need
> to point a device at a different backend (e.g. your PC during development).

## Notes & limits (free tier)

- **Cold starts**: after 15 min idle the service sleeps; the first request
  takes ~50s. The app's read timeout is already raised to 90s to survive this.
  (Paid plans keep the service always-on.)
- **Database**: Render's free PostgreSQL expires after 30 days — upgrade the
  DB plan or re-create it to keep data. `ddl-auto=update` recreates the schema
  automatically, but user data is not migrated.
- **HTTPS**: Render provides it automatically — no certificate work needed.
- **CORS**: already open (`allowedOriginPatterns("*")`), so the app works.

## Local development is unchanged

`gradlew bootRun` in `backend/` still uses your local PostgreSQL
(`localhost:5432/farmerdb`) because the env vars simply aren't set locally.
