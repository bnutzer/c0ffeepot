FROM php:8-apache

RUN a2enmod rewrite

COPY index.php /var/www/html/index.php

COPY <<'EOF' /var/www/html/.htaccess
RewriteEngine on
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^(.*)$ /index.php?originalRequestPath=$1 [qsappend]
EOF
