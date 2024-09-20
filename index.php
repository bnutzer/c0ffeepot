<?php

$status = $_GET['status'] ?? 200;
$contentType = $_GET['contenttype'] ?? 'application/json';
$body = $_GET['body'] ?? '{ "hello": "world" }';
$location = $_GET['location'] ?? '';

if ($location)  {
	Header('Location: ' . $location);
}

if ($contentType)  {
	Header('Content-Type: ' . $contentType);
}

http_response_code($status);

if ($body) {
	print($body . "\n");
}

?>
