#!/bin/sh
# Elige la config de nginx segun si hay un certificado montado o no (ver
# docs/EP2_PLAN.md seccion 8.2 y nginx/http.conf / nginx/https.conf).
set -e

CERT=/etc/nginx/certs/fullchain.pem
KEY=/etc/nginx/certs/privkey.pem

if [ -f "$CERT" ] && [ -f "$KEY" ]; then
    echo "Certificado encontrado en /etc/nginx/certs: sirviendo HTTPS (443) con redireccion desde 80."
    cp /etc/nginx/templates/https.conf /etc/nginx/conf.d/default.conf
else
    echo "Sin certificado montado: sirviendo solo HTTP (80)."
    cp /etc/nginx/templates/http.conf /etc/nginx/conf.d/default.conf
fi

exec nginx -g 'daemon off;'
