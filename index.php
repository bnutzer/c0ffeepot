<?php

$preloadFilename = sys_get_temp_dir() . DIRECTORY_SEPARATOR . "c0ffeepot.json";

$originalRequestPath = $_GET['originalRequestPath'] ?? '(unknown)';
$originalRequestPathSegments = explode("/", $originalRequestPath);

function resolveVariable($name, $default) {

    global $originalRequestPathSegments, $db;

    $varInRequestPath = array_search($name, $originalRequestPathSegments);
    if ($varInRequestPath !== false && $varInRequestPath + 1 <= array_key_last($originalRequestPathSegments)) {
        return $originalRequestPathSegments[$varInRequestPath + 1];
    }

    return $_GET[$name] ?? $default;
}

$defaultResponseBody = '{ "hello": "world" }' . "\n";
$requestBody = file_get_contents('php://input');
$responseBody = $requestBody !== '' ? $requestBody : resolveVariable('body', $defaultResponseBody);

$vars = array(
    'status' => resolveVariable('status', 200),
    'contenttype' => resolveVariable('contenttype', 'application/json'),
    'body' => $responseBody,
    'location' => resolveVariable('location', ''),
);

if ($originalRequestPathSegments[0] === 'preload') {

    $file = fopen($preloadFilename, "w");
    fwrite($file, json_encode($vars));
    fclose($file);

    http_response_code(202);

} else {

    if (file_exists($preloadFilename)) {
        $preloadData = file_get_contents($preloadFilename);
        $vars = json_decode($preloadData, true);
        unlink($preloadFilename);
    }

    if ($vars['location']) {
        header('Location: ' . $vars['location']);
    }

    if ($vars['contenttype'])  {
        header('Content-Type: ' . $vars['contenttype']);
    }

    header('X-Original-Request-Path: ' . $originalRequestPath);
    header('X-Request-Method: ' . $_SERVER['REQUEST_METHOD']);

    http_response_code($vars['status']);

    print($vars['body']);
}

?>
