<?php
require_once '_helpers.php';

$id = isset($_GET['id']) ? trim($_GET['id']) : '';
if ($id === '' || !ctype_digit($id)) {
    send_json(400, ['error' => 'Missing or invalid id']);
}

$body = file_get_contents('php://input');
if (empty($body)) {
    send_json(400, ['error' => 'Empty request body']);
}

$query = 'id=eq.' . $id;
$res = supabase_request('PATCH', '/rest/v1/companies', $query, $body);
send_json($res['code'], $res['body']);
