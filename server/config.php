<?php
// =============================================================
// Configuration for the BizDir web services.
//
// In production (Render), secrets come from environment variables
// configured in the Render dashboard. Locally, you can either set
// the same env vars OR temporarily edit the fallback values below.
//
// IMPORTANT: the service_role key is the "admin" key. It bypasses
// Row Level Security and can do ANYTHING in your project. Keep it
// out of Git and never put it in the Android app.
// =============================================================

$SUPABASE_URL         = getenv('SUPABASE_URL');
$SUPABASE_SERVICE_KEY = getenv('SUPABASE_SERVICE_KEY');
$LOGO_BUCKET          = getenv('LOGO_BUCKET') ?: 'logos';

// Local-dev fallbacks. Safe to commit because they are placeholders.
if (empty($SUPABASE_URL)) {
    $SUPABASE_URL = "https://YOUR_PROJECT.supabase.co";
}
if (empty($SUPABASE_SERVICE_KEY)) {
    $SUPABASE_SERVICE_KEY = "YOUR_SERVICE_ROLE_KEY";
}
