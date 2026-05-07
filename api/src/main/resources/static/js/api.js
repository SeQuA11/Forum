// API конфигурация
const API_BASE_URL = window.location.origin + '/api/v1';

// Состояние приложения
const AppState = {
    jwtToken: localStorage.getItem('jwtToken') || null,
    currentUser: JSON.parse(localStorage.getItem('currentUser')) || null,

    saveAuth(token, user) {
        this.jwtToken = token;
        this.currentUser = user;
        localStorage.setItem('jwtToken', token);
        localStorage.setItem('currentUser', JSON.stringify(user));
    },

    clearAuth() {
        this.jwtToken = null;
        this.currentUser = null;
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('currentUser');
    }
};

// API методы
const ForumAPI = {
    // Вспомогательный метод для запросов
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (AppState.jwtToken) {
            headers['Authorization'] = `Bearer ${AppState.jwtToken}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (!response.ok) {
                const errorText = await response.text();
                let errorMessage = `HTTP ${response.status}`;
                try {
                    const errorJson = JSON.parse(errorText);
                    errorMessage = errorJson.message || errorMessage;
                } catch (e) {
                    errorMessage = errorText || errorMessage;
                }
                throw new Error(errorMessage);
            }

            // Для ответов без содержимого (204)
            if (response.status === 204) {
                return null;
            }

            const text = await response.text();
            if (!text) return null;

            return JSON.parse(text);
        } catch (error) {
            if (error.message.includes('Failed to fetch')) {
                throw new Error('Не удалось подключиться к серверу. Убедитесь, что API запущен на порту 8080.');
            }
            throw error;
        }
    },

    // ========== Аутентификация ==========

    async login(username, password) {
        const data = await this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        return data;
    },

    async register(username, password) {
        const data = await this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        return data;
    },

    // ========== Категории ==========

    async getCategories() {
        return await this.request('/categories');
    },

    async createCategory(name, description, slug, sortOrder = 0, isActive = true) {
        return await this.request('/categories', {
            method: 'POST',
            body: JSON.stringify({ name, description, slug, sortOrder, isActive })
        });
    },

    async deleteCategory(id) {
        return await this.request(`/categories/${id}`, {
            method: 'DELETE'
        });
    },

    // ========== Темы ==========

    async getThreads(page = 0, size = 100) {
        return await this.request(`/threads?page=${page}&size=${size}`);
    },

    async getThreadById(id) {
        return await this.request(`/threads/${id}`);
    },

    async viewThread(id) {
        return await this.request(`/threads/${id}/view`);
    },

    async getThreadsByCategory(categoryId, page = 0, size = 100) {
        return await this.request(`/threads/category/${categoryId}?page=${page}&size=${size}`);
    },

    async createThread(title, content, categoryId) {
        return await this.request('/threads', {
            method: 'POST',
            body: JSON.stringify({ title, content, categoryId })
        });
    },

    async deleteThread(id) {
        return await this.request(`/threads/${id}`, {
            method: 'DELETE'
        });
    },

    async updateThreadStatus(id, status) {
        return await this.request(`/threads/${id}/status?status=${status}`, {
            method: 'PATCH'
        });
    },

    async viewThread(id) {
        return await this.request(`/threads/${id}/view`);
    },

    // ========== Сообщения ==========

    async getPosts(threadId, page = 0, size = 100) {
        return await this.request(`/threads/${threadId}/posts?page=${page}&size=${size}`);
    },

    async createPost(threadId, content) {
        return await this.request(`/threads/${threadId}/posts`, {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    }
};