const TOKEN_KEY = "almacen.token";
const API = window.APP_CONFIG?.apiBaseUrl ?? "http://localhost:8080";

(function checkSession() {
  const token = sessionStorage.getItem(TOKEN_KEY);
  if (!token) return;
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    if (payload.exp * 1000 > Date.now()) {
      window.location.href = "index.html";
    }
  } catch (_) {}
})();

document.getElementById("loginForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;
  const btn = document.getElementById("btnLogin");
  const errorDiv = document.getElementById("errorMsg");

  if (!username || !password) {
    showError("Ingresa usuario y contraseña.");
    return;
  }

  btn.disabled = true;
  btn.textContent = "Ingresando...";
  errorDiv.style.display = "none";

  try {
    const res = await fetch(`${API}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    if (!res.ok) {
      const body = await res.json().catch(() => null);
      throw new Error(body?.mensaje ?? "Credenciales incorrectas.");
    }

    const data = await res.json();
    sessionStorage.setItem(TOKEN_KEY, data.token);
    window.location.href = "index.html";

  } catch (err) {
    showError(err.message ?? "Error al conectar con el servidor.");
    btn.disabled = false;
    btn.textContent = "Ingresar";
  }
});

function showError(msg) {
  const div = document.getElementById("errorMsg");
  div.textContent = msg;
  div.style.display = "block";
}
