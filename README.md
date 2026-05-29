# BizDir

A small Android app (Java) that shows a directory of companies in four
categories (Services, Fun, Industry, Education), lets you add new ones, and
shows a toast when you walk within 50 m of one.

The backend is **Supabase** (hosted PostgreSQL + auto-generated REST API), so
there is no local server to run.

## One-time Supabase setup

1. Sign up at <https://supabase.com> (the free tier is plenty).
2. Click **New Project**, pick a name, set a strong database password, and
   wait ~2 minutes for it to provision.
3. In the left sidebar open **SQL Editor**, paste the contents of
   [`supabase_setup.sql`](supabase_setup.sql), and click **Run**.
   That creates the `companies` table, three sample rows, and two
   "allow anyone" Row Level Security policies.
4. Open **Project Settings -> API** and copy two values:
   - **Project URL** (e.g. `https://abcdefg.supabase.co`)
   - **anon public** key (a long string starting with `eyJ...`)

## Point the app at your project

Open `app/src/main/java/com/example/bizdir/ApiClient.java` and replace the
two placeholders near the top of the file:

```java
private static final String SUPABASE_URL = "https://YOUR_PROJECT.supabase.co";
private static final String SUPABASE_ANON_KEY = "YOUR_ANON_KEY";
```

The anon key is safe to ship inside a mobile app — it can only do what your
RLS policies allow.

## Run

Open the project in Android Studio, wait for Gradle sync, then press
**Run** on an emulator or a USB-debugging phone.

You should see the three sample companies under their tabs. Tap the gear
icon in the toolbar to open the Add Company screen.

## Project layout

| Path | What it is |
| --- | --- |
| `app/` | The Android app (Java) |
| `app/src/main/java/com/example/bizdir/ApiClient.java` | Retrofit + OkHttp setup, injects the Supabase headers |
| `app/src/main/java/com/example/bizdir/CompanyService.java` | The REST endpoints we call |
| `app/src/main/java/com/example/bizdir/Company.java` | The data model (matches the `companies` table) |
| `app/src/main/java/com/example/bizdir/MainActivity.java` | Tabs + search + location-based toasts |
| `app/src/main/java/com/example/bizdir/CompanyListFragment.java` | One tab's list of companies |
| `app/src/main/java/com/example/bizdir/AddCompanyActivity.java` | The form for adding a new company |
| `supabase_setup.sql` | Run this once in the Supabase SQL Editor |

## Common gotchas

- **"Server returned 401" or 404**: usually the URL or anon key is wrong.
  Double-check `ApiClient.java`.
- **The list loads, but adding a company fails with 403**: your RLS
  policies got dropped or were never created. Re-run `supabase_setup.sql`.
- **No proximity toast appears**: your test coordinates need to be within
  50 m of where the emulator/phone "is". In the emulator's extended
  controls you can set a fake GPS location.
