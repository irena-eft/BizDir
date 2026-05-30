<?php
require_once '_helpers.php';

$category = isset($_GET['category']) ? trim($_GET['category']) : '';

$query = 'select=*&order=name.asc';
if ($category !== '') {
    // PostgREST case-insensitive "contains" filter
    $query .= '&category=' . urlencode('ilike.*' . $category . '*');
}

$res = supabase_request('GET', '/rest/v1/companies', $query);
send_json($res['code'], $res['body']);
