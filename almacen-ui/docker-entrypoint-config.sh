#!/bin/sh
# Regenera config.js en runtime a partir de la env var API_BASE_URL,
# sin necesidad de rebuildear la imagen. Corre automaticamente antes de
# que arranque Nginx (hook oficial de la imagen nginx: /docker-entrypoint.d/*.sh).
set -e

cat > /usr/share/nginx/html/js/config.js <<EOF
window.APP_CONFIG = { apiBaseUrl: "${API_BASE_URL:-}" };
EOF
