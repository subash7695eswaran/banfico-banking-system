import api from './axiosClient';

const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8081';
const REALM = 'banfico-realm';
const CLIENT_ID = 'banfico-banking-app';

export const AuthService = {
  /**
   * Authenticate user against Keycloak IAM via Direct Grant
   */
  login: async (username, password) => {
    // Call backend Keycloak IAM Gateway endpoint
    const response = await api.post('/auth/login', { username, password });
    const data = response.data || response;

    if (data.accessToken) {
      localStorage.setItem('banfico_jwt_token', data.accessToken);
      localStorage.setItem('banfico_refresh_token', data.refreshToken || '');
      localStorage.setItem('banfico_current_role', data.primaryRole || 'CUSTOMER');
      localStorage.setItem('banfico_current_user', data.email || data.username);
      localStorage.setItem('banfico_auth_user', JSON.stringify(data));
    }
    return data;
  },

  /**
   * Self-register new customer in Keycloak and auto-provision banking profile
   */
  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    const data = response.data || response;

    if (data.accessToken) {
      localStorage.setItem('banfico_jwt_token', data.accessToken);
      localStorage.setItem('banfico_refresh_token', data.refreshToken || '');
      localStorage.setItem('banfico_current_role', data.primaryRole || 'CUSTOMER');
      localStorage.setItem('banfico_current_user', data.email || data.username);
      localStorage.setItem('banfico_auth_user', JSON.stringify(data));
    }
    return data;
  },

  /**
   * Fetch current authenticated identity profile
   */
  getProfile: async () => {
    const response = await api.get('/auth/me');
    return response.data || response;
  },

  /**
   * Redirect to Keycloak Hosted Login (Standard OpenID Connect Flow)
   */
  loginWithKeycloakSSO: () => {
    const redirectUri = encodeURIComponent(`${window.location.origin}`);
    window.location.href = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/auth?client_id=${CLIENT_ID}&response_type=code&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;
  },

  /**
   * Redirect to Keycloak Hosted Registration Page
   */
  registerWithKeycloakSSO: () => {
    const redirectUri = encodeURIComponent(`${window.location.origin}`);
    window.location.href = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/registrations?client_id=${CLIENT_ID}&response_type=code&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;
  },

  /**
   * Clear local session and logout
   */
  logout: () => {
    localStorage.removeItem('banfico_jwt_token');
    localStorage.removeItem('banfico_refresh_token');
    localStorage.removeItem('banfico_auth_user');
    localStorage.removeItem('banfico_current_role');
    localStorage.removeItem('banfico_current_user');
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: () => {
    const token = localStorage.getItem('banfico_jwt_token');
    return !!token;
  },

  /**
   * Get stored auth user object
   */
  getStoredUser: () => {
    const userStr = localStorage.getItem('banfico_auth_user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        return null;
      }
    }
    return null;
  }
};

export default AuthService;
