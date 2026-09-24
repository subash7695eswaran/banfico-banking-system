import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// Interceptor to attach Keycloak JWT bearer token if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('banfico_jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Global response interceptor
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || error.message || 'An error occurred';
    return Promise.reject({ ...error, message });
  }
);

export const HealthApi = {
  getHealth: () => axios.get('/health').then(res => res.data),
  getInfo: () => api.get('/info')
};

export const CustomerApi = {
  getAll: () => api.get('/customers'),
  getById: (id) => api.get(`/customers/${id}`),
  create: (data) => api.post('/customers', data),
  getAccounts: (id) => api.get(`/customers/${id}/accounts`),
  getConsents: (id) => api.get(`/customers/${id}/consents`)
};

export const AccountApi = {
  getAll: () => api.get('/accounts'),
  getById: (id) => api.get(`/accounts/${id}`),
  getByNumber: (accNum) => api.get(`/accounts/number/${accNum}`),
  create: (data) => api.post('/accounts', data)
};

export const TransactionApi = {
  getAll: () => api.get('/transactions'),
  getByAccount: (accountId) => api.get(`/accounts/${accountId}/transactions`),
  execute: (accountId, data) => api.post(`/accounts/${accountId}/transactions`, data)
};

export const BeneficiaryApi = {
  getAll: () => api.get('/beneficiaries'),
  getByCustomer: (customerId) => api.get(`/beneficiaries/customer/${customerId}`),
  create: (data) => api.post('/beneficiaries', data),
  delete: (id) => api.delete(`/beneficiaries/${id}`)
};

export const ConsentApi = {
  getAll: () => api.get('/consents'),
  getById: (id) => api.get(`/consents/${id}`),
  getByCustomer: (customerId) => api.get(`/consents/customer/${customerId}`),
  create: (data) => api.post('/consents', data),
  approve: (id) => api.post(`/consents/${id}/approve`),
  reject: (id) => api.post(`/consents/${id}/reject`),
  updateStatus: (id, status) => api.put(`/consents/${id}/status`, { status })
};

export default api;
