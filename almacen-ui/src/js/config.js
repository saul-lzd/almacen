// Config runtime del API base URL.
// En Docker, este archivo se regenera al arrancar el contenedor Nginx
// (ver docker-entrypoint-config.sh) a partir de la env var API_BASE_URL,
// sin necesidad de rebuildear la app. El valor de abajo es el default
// para desarrollo local sin Docker (ojet serve).
window.APP_CONFIG = {
  apiBaseUrl: "http://localhost:8080"
};
