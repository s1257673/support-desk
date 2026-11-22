// ============================================================
// api.js — Centralized API communication layer
//
// Every API call in the app goes through this file.
// Benefits:
//  - JWT token is attached automatically on every request
//  - Error handling is consistent everywhere
//  - If the API base URL changes, we change it in one place
// ============================================================

const API_BASE = '';  // empty = same host (Spring Boot serves frontend too)

// Storage keys — use constants so a typo doesn't create a silent bug
const TOKEN_KEY = 'sd_token';
const USER_KEY  = 'sd_user';

// ---- Token management ----

export function saveAuth(authResponse) {
    // localStorage persists across page loads and browser sessions
    // until explicitly cleared (unlike sessionStorage which clears on tab close)
    localStorage.setItem(TOKEN_KEY, authResponse.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
        email:     authResponse.email,
        firstName: authResponse.firstName,
        lastName:  authResponse.lastName,
        role:      authResponse.role
    }));
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function getCurrentUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
}

export function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

export function isLoggedIn() {
    return !!getToken();
}

// ---- Core request function ----

// async function: returns a Promise
// await pauses execution until the Promise resolves — no .then() chains needed
export async function request(endpoint, options = {}) {

    const token = getToken();

    // Build headers — always include Content-Type and Authorization if logged in
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers  // allow overrides
    };

    if (token) {
        // This is what JwtAuthFilter reads on every request
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        // fetch() returns a Promise<Response>
        // await unwraps it into the actual Response object
        const response = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            headers
        });

        // response.json() reads the body and parses JSON
        // also returns a Promise — await it too
        const data = await response.json();

        // HTTP 2xx = success
        if (!response.ok) {
            // Our ApiResponse always has a 'message' field
            throw new Error(data.message || `HTTP ${response.status}`);
        }

        return data;  // This is our ApiResponse<T> object

    } catch (error) {
        // Network failure (no internet, server down)
        if (error.name === 'TypeError') {
            throw new Error('Cannot connect to server. Please check your connection.');
        }
        throw error;
    }
}

// ---- Convenience wrappers ----

export const api = {
    get:    (url)         => request(url, { method: 'GET' }),
    post:   (url, body)   => request(url, { method: 'POST',   body: JSON.stringify(body) }),
    put:    (url, body)   => request(url, { method: 'PUT',    body: JSON.stringify(body) }),
    patch:  (url, body)   => request(url, { method: 'PATCH',  body: JSON.stringify(body) }),
    delete: (url)         => request(url, { method: 'DELETE' })
};