<?php

$status = $_GET['status'] ?? 200;
$contentType = $_GET['contenttype'] ?? 'application/json';
$body = $_GET['body'] ?? '{ "hello": "world" }';
$location = $_GET['location'] ?? '';

$originalRequestPath = $_GET['originalRequestPath'] ?? '(unknown)';

if ($location)  {
	header('Location: ' . $location);
}

if ($contentType)  {
	header('Content-Type: ' . $contentType);
}

header('X-Original-Request-Path: ' . $originalRequestPath);
header('X-Request-Method: ' . $_SERVER['REQUEST_METHOD']);

http_response_code($status);

if ($body) {
	print($body . "\n");
}

?>
