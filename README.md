# BizDir

A small Android app (Java) that shows a directory of companies in four
categories (Services, Fun, Industry, Education), lets you add, edit and
delete them, attach a logo image, and shows a toast when you walk within
50 m of one.

## Architecture

```
Android app  ──HTTPS──▶  PHP web services  ──HTTPS──▶  Supabase
 (Retrofit)            (hosted on Render)         (PostgreSQL + Storage)
```

The Android app never talks to Supabase directly. It only talks to the
PHP scripts in `server/`, which are the **web services developed for
this project**. PHP holds the Supabase service-role key (a secret that
must never leave the server) and forwards the work to Supabase.

The PHP web services are:

| File | Purpose | HTTP |
| --- | --- | --- |
| `server/get_companies.php`   | List companies, optional `?category=` filter | `GET` |
| `server/add_company.php`     | Insert a new company (JSON body)             | `POST` |
| `server/update_company.php`  | Update a company by `?id=`                   | `PATCH` |
| `server/delete_company.php`  | Delete a company by `?id=`                   | `DELETE` |
| `server/upload_logo.php`     | Upload a logo image, returns its public URL | `POST` |

## One-time setup

### 1. Create the Supabase project

1. Sign up at <https://supabase.com> (free tier).
2. **New Project** -> name it -> wait ~2 minutes.
3. Left sidebar **SQL Editor** -> paste the contents of
   [`supabase_setup.sql`](supabase_setup.sql) -> **Run**.
4. **Project Settings -> API** and copy two values you'll need next:
   - **Project URL** (e.g. `https://abcdefg.supabase.co`)
   - **service_role secret** (NOT the anon key - we want the admin one)

The service_role key bypasses Row Level Security so PHP can read and
write anything. It is safe to use on the server because clients never
see it.

### 2. Push this project to GitHub

If it isn't already on GitHub, create a public or private repository and
push:

```bash
git remote add origin https://github.com/YOUR_USERNAME/BizDir.git
git push -u origin main
```

### 3. Deploy the PHP web services to Render

1. Sign up at <https://render.com> (the free tier is plenty).
2. Click **New +** -> **Web Service** -> **Build and deploy from a Git
   repository** -> connect your GitHub account and pick the BizDir repo.
3. Render reads [`render.yaml`](render.yaml) and offers to use the
   blueprint - accept it. (If it does not, set these manually: Runtime
   `Docker`, Root Directory `server`, Dockerfile Path `./Dockerfile`,
   Plan `Free`.)
4. Under **Environment Variables**, set:
   - `SUPABASE_URL` = your Project URL from step 1
   - `SUPABASE_SERVICE_KEY` = your service_role secret from step 1
   - `LOGO_BUCKET` is already set to `logos` by the blueprint
5. Click **Create Web Service**. The first build takes ~3 minutes.

Render gives the service an HTTPS URL like
`https://bizdir-server.onrender.com`. Test it in your browser:

<https://bizdir-server.onrender.com/get_companies.php>

You should see a JSON array of the seed companies. If not, check the
**Logs** tab in the Render dashboard.

> **Note on free tier**: Render's free plan spins the server down after
> ~15 minutes of inactivity. The first request after a sleep takes
> ~30 seconds to wake up, then it's fast again. For a demo / school
> project this is fine.

### 4. Point the Android app at your Render URL

Open `app/src/main/java/com/example/bizdir/ApiClient.java` and replace
`BASE_URL`:

```java
private static final String BASE_URL = "https://bizdir-server.onrender.com/";
```

The trailing slash is required.

### 5. Run the app

Open the project in Android Studio, sync Gradle, hit Run. Use the
emulator or a real phone with USB debugging.

## Local development without Render (optional)

If you ever want to run the PHP server on your own machine instead, you
have two options:

### Option A: Docker (uses the same image as Render)

```bash
cd server
docker build -t bizdir-server .
docker run --rm -p 8080:10000 \
  -e SUPABASE_URL=https://YOUR_PROJECT.supabase.co \
  -e SUPABASE_SERVICE_KEY=eyJ... \
  bizdir-server
```

Then set `BASE_URL = "http://10.0.2.2:8080/"` for the emulator.

### Option B: XAMPP (Mac/Windows)

1. Install XAMPP, start Apache.
2. Copy the `server/` folder into Apache's `htdocs/business_directory/`.
3. Edit `server/config.php` and replace the fallback values with your
   Supabase URL and service-role key (env vars also work).
4. Set `BASE_URL = "http://10.0.2.2/business_directory/"` for the
   emulator.

## Project layout

| Path | What it is |
| --- | --- |
| `app/` | The Android app (Java) |
| `app/src/main/java/com/example/bizdir/ApiClient.java` | Retrofit + image upload helper |
| `app/src/main/java/com/example/bizdir/CompanyService.java` | The REST endpoints we call (PHP) |
| `app/src/main/java/com/example/bizdir/Company.java` | The data model |
| `app/src/main/java/com/example/bizdir/MainActivity.java` | Tabs + search + location toasts |
| `app/src/main/java/com/example/bizdir/CompanyListFragment.java` | One tab's list of companies |
| `app/src/main/java/com/example/bizdir/CompanyDetailActivity.java` | Detail screen with Edit/Delete |
| `app/src/main/java/com/example/bizdir/AddCompanyActivity.java` | Add/Edit form |
| `server/` | The PHP web services |
| `server/Dockerfile` | The image Render builds and runs |
| `render.yaml` | Render Blueprint - the deploy recipe |
| `supabase_setup.sql` | Run once in the Supabase SQL Editor |

## Common gotchas

- **App hangs for ~30 seconds on first request, then works**: Render's
  free dyno is waking up from sleep. Subsequent requests will be fast.
- **App shows "Connection refused" or HTTP error**: the Render service
  failed to build. Open the Render dashboard -> **Logs** tab.
- **PHP returns `{"error": "..."}`**: usually a typo in `SUPABASE_URL`
  or `SUPABASE_SERVICE_KEY` env vars. Fix in Render, then click
  **Manual Deploy** to apply.
- **PHP works in browser but app shows JSON parse error**: that means
  the service didn't return JSON. Check `Content-Type` and the raw
  response in the Render logs.
- **Image upload fails**: the `logos` Storage bucket is missing in
  Supabase. Re-run `supabase_setup.sql`.
- **"Use my current location" returns nothing in the emulator**: open
  the emulator's three-dots menu -> Location -> set a position and
  click Set Location, then tap the button again.
