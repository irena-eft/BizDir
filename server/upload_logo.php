<?php
require_once '_helpers.php';

// Android sends the raw image bytes as the request body. The mime type
// comes in as the request's Content-Type header (e.g. "image/jpeg").
$bytes = file_get_contents('php://input');
if (empty($bytes)) {
    send_json(400, ['error' => 'No image data received']);
}

$mime = isset($_SERVER['CONTENT_TYPE']) ? $_SERVER['CONTENT_TYPE'] : 'image/jpeg';

$ext = '.jpg';
if (strpos($mime, 'png')  !== false) $ext = '.png';
else if (strpos($mime, 'webp') !== false) $ext = '.webp';
else if (strpos($mime, 'gif')  !== false) $ext = '.gif';

$filename   = 'logo_' . round(microtime(true) * 1000) . $ext;
$path       = '/storage/v1/object/' . $LOGO_BUCKET . '/' . $filename;
$public_url = $SUPABASE_URL . '/storage/v1/object/public/' . $LOGO_BUCKET . '/' . $filename;

// Custom Storage upload: cannot use supabase_request() because the body
// is not JSON and we don't want the "Prefer: return=representation" header.
$ch = curl_init($SUPABASE_URL . $path);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'POST');
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'apikey: '        . $SUPABASE_SERVICE_KEY,
    'Authorization: ' . 'Bearer ' . $SUPABASE_SERVICE_KEY,
    'Content-Type: '  . $mime,
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $bytes);

$response = curl_exec($ch);
$code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($code >= 200 && $code < 300) {
    send_json(200, ['public_url' => $public_url]);
} else {
    send_json($code, ['error' => 'Upload failed', 'detail' => $response]);
}
