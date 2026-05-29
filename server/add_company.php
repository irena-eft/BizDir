<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
require_once 'config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!$data) {
    echo json_encode(["success" => false, "message" => "Invalid JSON"]);
    exit;
}

try {
    $sql = "INSERT INTO companies (name, address, latitude, longitude, email, telephone, website, category) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        $data['name'],
        $data['address'],
        $data['latitude'],
        $data['longitude'],
        $data['email'],
        $data['telephone'],
        $data['website'],
        $data['category']
    ]);
    
    echo json_encode(["success" => true, "id" => $pdo->lastInsertId()]);
} catch(PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>