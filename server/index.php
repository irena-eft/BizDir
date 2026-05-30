<?php
// Simple health/landing page so the root URL doesn't 404.
header('Content-Type: application/json; charset=UTF-8');
echo json_encode([
    'service' => 'BizDir web services',
    'status'  => 'ok',
    'endpoints' => [
        'GET    /get_companies.php?category=Fun',
        'POST   /add_company.php',
        'PATCH  /update_company.php?id=123',
        'DELETE /delete_company.php?id=123',
        'POST   /upload_logo.php',
    ],
]);
