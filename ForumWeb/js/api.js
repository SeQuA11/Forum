// API конфигурация
     2	const API_BASE_URL = 'http://localhost:3000/api/v1';
     3	
     4	// Состояние приложения
     5	const AppState = {
     6	    jwtToken: localStorage.getItem('jwtToken') || null,
     7	    currentUser: JSON.parse(localStorage.getItem('currentUser')) || null,
     8	    
     9	    saveAuth(token, user) {
    10	        this.jwtToken = token;
    11	        this.currentUser = user;
    12	        localStorage.setItem('jwtToken', token);
    13	        localStorage.setItem('currentUser', JSON.stringify(user));
    14	    },
    15	    
    16	    clearAuth() {
    17	        this.jwtToken = null;
    18	        this.currentUser = null;
    19	        localStorage.removeItem('jwtToken');
    20	        localStorage.removeItem('currentUser');
    21	    }
    22	};
    23	
    24	// API методы
    25	const ForumAPI = {
    26	    // Вспомогательный метод для запросов
    27	    async request(endpoint, options = {}) {
    28	        const url = `${API_BASE_URL}${endpoint}`;
    29	        const headers = {
    30	            'Content-Type': 'application/json',
    31	            ...options.headers
    32	        };
    33	
    34	        if (AppState.jwtToken) {
    35	            headers['Authorization'] = `Bearer ${AppState.jwtToken}`;
    36	        }
    37	
    38	        try {
    39	            const response = await fetch(url, {
    40	                ...options,
    41	                headers
    42	            });
    43	
    44	            if (!response.ok) {
    45	                const errorText = await response.text();
    46	                let errorMessage = `HTTP ${response.status}`;
    47	                try {
    48	                    const errorJson = JSON.parse(errorText);
    49	                    errorMessage = errorJson.message || errorMessage;
    50	                } catch (e) {
    51	                    errorMessage = errorText || errorMessage;
    52	                }
    53	                throw new Error(errorMessage);
    54	            }
    55	
    56	            // Для ответов без содержимого (204)
    57	            if (response.status === 204) {
    58	                return null;
    59	            }
    60	
    61	            const text = await response.text();
    62	            if (!text) return null;
    63	            
    64	            return JSON.parse(text);
    65	        } catch (error) {
    66	            if (error.message.includes('Failed to fetch')) {
    67	                throw new Error('Не удалось подключиться к серверу. Убедитесь, что API запущен на порту 8080.');
    68	            }
    69	            throw error;
    70	        }
    71	    },
    72	
    73	    // ========== Аутентификация ==========
    74	    
    75	    async login(username, password) {
    76	        const data = await this.request('/auth/login', {
    77	            method: 'POST',
    78	            body: JSON.stringify({ username, password })
    79	        });
    80	        return data;
    81	    },
    82	
    83	    async register(username, password) {
    84	        const data = await this.request('/auth/register', {
    85	            method: 'POST',
    86	            body: JSON.stringify({ username, password })
    87	        });
    88	        return data;
    89	    },
    90	
    91	    // ========== Категории ==========
    92	    
    93	    async getCategories() {
    94	        return await this.request('/categories');
    95	    },
    96	
    97	    async createCategory(name, description, slug, sortOrder = 0, isActive = true) {
    98	        return await this.request('/categories', {
    99	            method: 'POST',
   100	            body: JSON.stringify({ name, description, slug, sortOrder, isActive })
   101	        });
   102	    },
   103	
   104	    async deleteCategory(id) {
   105	        return await this.request(`/categories/${id}`, {
   106	            method: 'DELETE'
   107	        });
   108	    },
   109	
   110	    // ========== Темы ==========
   111	    
   112	    async getThreads(page = 0, size = 100) {
   113	        return await this.request(`/threads?page=${page}&size=${size}`);
   114	    },
   115	
   116	    async getThreadById(id) {
   117	        return await this.request(`/threads/${id}`);
   118	    },
   119	
   120	    async getThreadsByCategory(categoryId, page = 0, size = 100) {
   121	        return await this.request(`/threads/category/${categoryId}?page=${page}&size=${size}`);
   122	    },
   123	
   124	    async createThread(title, content, categoryId) {
   125	        return await this.request('/threads', {
   126	            method: 'POST',
   127	            body: JSON.stringify({ title, content, categoryId })
   128	        });
   129	    },
   130	
   131	    async deleteThread(id) {
   132	        return await this.request(`/threads/${id}`, {
   133	            method: 'DELETE'
   134	        });
   135	    },
   136	
   137	    async updateThreadStatus(id, status) {
   138	        return await this.request(`/threads/${id}/status?status=${status}`, {
   139	            method: 'PATCH'
   140	        });
   141	    },
   142	
   143	    async viewThread(id) {
   144	        return await this.request(`/threads/${id}/view`);
   145	    },
   146	
   147	    // ========== Сообщения ==========
   148	    
   149	    async getPosts(threadId, page = 0, size = 100) {
   150	        return await this.request(`/threads/${threadId}/posts?page=${page}&size=${size}`);
   151	    },
   152	
   153	    async createPost(threadId, content) {
   154	        return await this.request(`/threads/${threadId}/posts`, {
   155	            method: 'POST',
   156	            body: JSON.stringify({ content })
   157	        });
   158	    }
   159	};