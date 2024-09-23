<?php

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

$vars = array(
    'status' => resolveVariable('status', 200),
    'contenttype' => resolveVariable('contenttype', 'application/json'),
    'body' => resolveVariable('body', '{ "hello": "world" }'),
    'location' => resolveVariable('location', ''),
);

if ($vars['location']) {
    header('Location: ' . $vars['location']);
}

if ($vars['contenttype'])  {
    header('Content-Type: ' . $vars['contenttype']);
}

header('X-Original-Request-Path: ' . $originalRequestPath);
header('X-Request-Method: ' . $_SERVER['REQUEST_METHOD']);

http_response_code($vars['status']);

if ($vars['body']) {
    print($vars['body'] . "\n");
}

?>
