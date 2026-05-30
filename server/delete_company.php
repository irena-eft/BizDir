<?php
require_once '_helpers.php';

$id = isset($_GET['id']) ? trim($_GET['id']) : '';
if ($id === '' || !ctype_digit($id)) {
    send_json(400, ['error' => 'Missing or invalid id']);
}

$query = 'id=eq.' . $id;
$res = supabase_request('DELETE', '/rest/v1/companies', $query);
send_json($res['code'], $res['body']);
