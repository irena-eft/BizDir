<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
require_once 'config.php';

$category = isset($_GET['category']) ? $_GET['category'] : '';
$search = isset($_GET['search']) ? $_GET['search'] : '';

try {
    $sql = "SELECT * FROM companies WHERE 1=1";
    $params = [];
    
    if (!empty($category)) {
        $sql .= " AND category LIKE ?";
        $params[] = "%$category%";
    }
    
    if (!empty($search)) {
        $sql .= " AND name LIKE ?";
        $params[] = "%$search%";
    }
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $companies = $stmt->fetchAll();
    
    echo json_encode(["success" => true, "data" => $companies]);
} catch(PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>