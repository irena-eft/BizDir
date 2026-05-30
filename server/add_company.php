<?php
require_once '_helpers.php';

$body = file_get_contents('php://input');
if (empty($body)) {
    send_json(400, ['error' => 'Empty request body']);
}

// Basic sanity check: must be JSON with at least a "name".
$decoded = json_decode($body, true);
if (!is_array($decoded) || empty($decoded['name'])) {
    send_json(400, ['error' => 'name is required']);
}

$res = supabase_request('POST', '/rest/v1/companies', '', $body);
send_json($res['code'], $res['body']);
