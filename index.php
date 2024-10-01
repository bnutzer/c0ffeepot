<?php

$preloadFilename = sys_get_temp_dir() . DIRECTORY_SEPARATOR . "c0ffeepot.json";

$originalRequestPath = $_GET['originalRequestPath'] ?? '(unknown)';
$originalRequestPathSegments = explode("/", $originalRequestPath);

function resolveVariable($name, &$vars) {

    global $originalRequestPathSegments;

    $varInRequestPath = array_search($name, $originalRequestPathSegments);

    if ($varInRequestPath !== false
        && $varInRequestPath + 1 <= array_key_last($originalRequestPathSegments)
        && $originalRequestPathSegments[$varInRequestPath + 1]) {
        $vars[$name] = $originalRequestPathSegments[$varInRequestPath + 1];
    } else if (array_key_exists($name, $_GET)) {
        $vars[$name] = $_GET[$name];
    }
}

function resolveParameters($defaultVars) {
    $vars = $defaultVars;
    foreach (array_keys($vars) as $key) {
        resolveVariable($key, $vars);
    }
    return $vars;
}

$defaultResponseBody = '{ "hello": "world" }' . "\n";
$requestBody = file_get_contents('php://input');

$defaultVars = array(
    'status' => 200,
    'contenttype' => 'application/json',
    'body' => $requestBody ?? $defaultResponseBody,
    'location' => ''
);

$vars = resolveParameters($defaultVars);

function sendResponseCode($vars) {
    // The following response codes are not supported by PHP's http_response_code function
    // and need to be handled manually
    $unsupportedResponseCodes = array(
        418,
        419,
        420,
        425,
        509,
        511
    );

    if (in_array($vars['status'], $unsupportedResponseCodes)) {
        header('HTTP/1.1 ' . $vars['status'] . ' Unsupported response code');
    } else {
        http_response_code($vars['status']);
    }
}

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

    if (array_key_exists('location', $vars) && $vars['location']) {
        header('Location: ' . $vars['location']);
    }

    if (array_key_exists('contenttype', $vars) && $vars['contenttype'])  {
        header('Content-Type: ' . $vars['contenttype']);
    }

    header('X-Original-Request-Path: ' . $originalRequestPath);
    header('X-Request-Method: ' . $_SERVER['REQUEST_METHOD']);

    sendResponseCode($vars);

    print($vars['body']);
}

?>
