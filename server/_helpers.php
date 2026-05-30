<?php
require_once 'config.php';

/**
 * Send a JSON response to the Android app and stop the script.
 * If $payload is already a JSON string we forward it as-is, which
 * lets us pass Supabase responses through untouched.
 */
function send_json($status_code, $payload) {
    http_response_code($status_code);
    header('Content-Type: application/json; charset=UTF-8');
    if (is_string($payload)) {
        echo $payload;
    } else {
        echo json_encode($payload);
    }
    exit;
}

/**
 * Talk to the Supabase REST API using cURL.
 * Returns ['code' => int, 'body' => string].
 */
function supabase_request($method, $path, $query = '', $body = null, $content_type = 'application/json') {
    global $SUPABASE_URL, $SUPABASE_SERVICE_KEY;

    $url = $SUPABASE_URL . $path;
    if (!empty($query)) {
        $url .= '?' . $query;
    }

    $headers = [
        'apikey: '         . $SUPABASE_SERVICE_KEY,
        'Authorization: '  . 'Bearer ' . $SUPABASE_SERVICE_KEY,
        'Content-Type: '   . $content_type,
        'Prefer: return=representation',
    ];

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    if ($body !== null) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
    }

    $response = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    if ($response === false) {
        $err = curl_error($ch);
        curl_close($ch);
        return ['code' => 500, 'body' => json_encode(['error' => $err])];
    }
    curl_close($ch);

    return ['code' => $code, 'body' => $response];
}
