// Основное приложение форума
document.addEventListener('DOMContentLoaded', () => {
    // Инициализация
    initAuth();
    loadCategories();
    loadThreads();
    setupEventListeners();
    
    // Показываем панель модерации в модальном окне для авторизованных пользователей
    updateThreadModerationPanel();
});

// Глобальные переменные
let currentCategory = null;
let currentThread = null;
let selectedThreadId = null;

// ========== Инициализация аутентификации ==========
function initAuth() {
    const userInfo = document.getElementById('userInfo');
    const authButtons = document.getElementById('authButtons');
    const welcomeMessage = document.getElementById('welcomeMessage');
    const roleBadge = document.getElementById('roleBadge');

    if (AppState.currentUser) {
        userInfo.style.display = 'flex';
        authButtons.style.display = 'none';
        welcomeMessage.textContent = `Добро пожаловать, ${AppState.currentUser.username}!`;
        roleBadge.textContent = AppState.currentUser.role;
        roleBadge.className = `badge ${AppState.currentUser.role}`;

        // Показываем админ панель для модераторов и админов
        const isAdminOrMod = ['ROLE_ADMIN', 'ROLE_MODERATOR'].includes(AppState.currentUser.role);
        document.getElementById('adminPanel').style.display = isAdminOrMod ? 'block' : 'none';

        // Показываем кнопку удаления темы для модераторов и админов
        document.getElementById('deleteThreadBtn').style.display = isAdminOrMod ? 'inline-block' : 'none';
    } else {
        userInfo.style.display = 'none';
        authButtons.style.display = 'flex';
        document.getElementById('adminPanel').style.display = 'none';
        document.getElementById('deleteThreadBtn').style.display = 'none';
    }
    
    // Обновляем панель модерации в модальном окне
    updateThreadModerationPanel();
}

// ========== Загрузка категорий ==========
async function loadCategories() {
    try {
        const categories = await ForumAPI.getCategories();
        const categoryList = document.getElementById('categoryList');
        const threadCategory = document.getElementById('threadCategory');

        // Очищаем список перед заполнением, чтобы избежать дублирования
        categoryList.innerHTML = '';
        
        // Создаем элемент "Все категории" с обработчиком
        const allLi = document.createElement('li');
        allLi.textContent = 'Все категории';
        allLi.dataset.id = 'all';
        allLi.classList.add('active');
        allLi.addEventListener('click', selectAllCategories);
        categoryList.appendChild(allLi);
        
        threadCategory.innerHTML = '';

        if (categories && categories.length > 0) {
            categories.forEach(cat => {
                // Добавляем в список категорий
                const li = document.createElement('li');
                li.textContent = cat.name;
                li.dataset.id = cat.id;
                li.addEventListener('click', () => selectCategory(cat));
                categoryList.appendChild(li);

                // Добавляем в select для создания темы
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.name;
                threadCategory.appendChild(option);
            });
        } else {
            categoryList.innerHTML += '<li class="no-data">Нет доступных категорий</li>';
            threadCategory.innerHTML = '<option value="">Нет категорий</option>';
        }
    } catch (error) {
        console.error('Ошибка загрузки категорий:', error);
        showError('Ошибка загрузки категорий: проверьте подключение к API');
        const categoryList = document.getElementById('categoryList');
        categoryList.innerHTML = '<li class="no-data">Ошибка загрузки</li>';
    }
}

// ========== Выбор категории ==========
function selectCategory(category) {
    currentCategory = category;

    // Обновляем активный элемент в списке
    document.querySelectorAll('#categoryList li').forEach(li => {
        li.classList.remove('active');
    });
    const activeLi = document.querySelector(`#categoryList li[data-id="${category.id}"]`);
    if (activeLi) {
        activeLi.classList.add('active');
    } else {
        // Если категория не найдена по id, активируем "Все категории"
        document.querySelector('#categoryList li[data-id="all"]').classList.add('active');
    }

    document.getElementById('currentCategoryTitle').textContent = `Категория: ${category.name}`;

    loadThreadsByCategory(category.id);
}

// ========== Выбор всех категорий ==========
function selectAllCategories() {
    currentCategory = null;

    // Обновляем активный элемент в списке
    document.querySelectorAll('#categoryList li').forEach(li => {
        li.classList.remove('active');
    });
    document.querySelector('#categoryList li[data-id="all"]').classList.add('active');

    document.getElementById('currentCategoryTitle').textContent = 'Все темы';

    loadThreads();
}

// ========== Загрузка тем ==========
async function loadThreads() {
    try {
        const pageData = await ForumAPI.getThreads(0, 20);
        renderThreads(pageData.content || pageData);
    } catch (error) {
        console.error('Ошибка загрузки тем:', error);
        showError('Ошибка загрузки тем: проверьте подключение к API');
        const tbody = document.getElementById('threadsTableBody');
        tbody.innerHTML = '<tr><td colspan="6" class="no-data">Ошибка загрузки тем</td></tr>';
    }
}

async function loadThreadsByCategory(categoryId) {
    try {
        const pageData = await ForumAPI.getThreadsByCategory(categoryId, 0, 20);
        renderThreads(pageData.content || pageData);
    } catch (error) {
        console.error('Ошибка загрузки тем категории:', error);
        showError('Ошибка загрузки тем: проверьте подключение к API');
        const tbody = document.getElementById('threadsTableBody');
        tbody.innerHTML = '<tr><td colspan="6" class="no-data">Ошибка загрузки тем</td></tr>';
    }
}

function renderThreads(threads) {
    const tbody = document.getElementById('threadsTableBody');
    tbody.innerHTML = '';

    if (!threads || threads.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="no-data">Темы не найдены</td></tr>';
        return;
    }

    threads.forEach(thread => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
        <td>${thread.id}</td>
        <td>${escapeHtml(thread.title)}</td>
        <td>${escapeHtml(thread.authorUsername || 'Неизвестно')}</td>
        <td><span class="badge">${thread.status || 'ACTIVE'}</span></td>
        <td>${thread.viewsCount || 0}</td>
        <td>${formatDate(thread.createdAt)}</td>
        `;
        tr.addEventListener('click', () => openThreadDetail(thread.id));
        tbody.appendChild(tr);
    });
}

// ========== Открытие детали темы ==========
async function openThreadDetail(threadId) {
    try {
        // Просмотр темы (увеличиваем счетчик просмотров)
        const thread = await ForumAPI.viewThread(threadId);
        currentThread = thread;

        // Загружаем посты
        const postsData = await ForumAPI.getPosts(threadId);
        const posts = postsData.content || postsData;

        // Заполняем модальное окно
        document.getElementById('viewThreadTitle').textContent = thread.title;
        document.getElementById('viewThreadAuthor').textContent = `Автор: ${thread.authorUsername}`;
        document.getElementById('viewThreadStatus').textContent = thread.status;
        document.getElementById('viewThreadDate').textContent = formatDate(thread.createdAt);
        document.getElementById('viewThreadContent').innerHTML = `<p>${escapeHtml(thread.content)}</p>`;

        // Отображаем посты
        const postsList = document.getElementById('postsList');
        postsList.innerHTML = '';
        posts.forEach(post => {
            const postDiv = document.createElement('div');
            postDiv.className = 'post';
            postDiv.innerHTML = `
            <div class="post-author">${escapeHtml(post.authorUsername)}</div>
            <div class="post-date">${formatDate(post.createdAt)}</div>
            <div class="post-content">${escapeHtml(post.content)}</div>
            `;
            postsList.appendChild(postDiv);
        });

        // Показываем секцию ответа если пользователь авторизован
        document.getElementById('replySection').style.display = AppState.currentUser ? 'block' : 'none';

        // Обновляем панель модерации в модальном окне
        updateThreadModerationPanel();

        // Открываем модальное окно
        document.getElementById('viewThreadModal').style.display = 'block';
    } catch (error) {
        showError('Ошибка открытия темы: ' + error.message);
    }
}

// ========== Настройка обработчиков событий ==========
function setupEventListeners() {
    // Кнопки аутентификации
    document.getElementById('loginBtn').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'block';
    });

    document.getElementById('registerBtn').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'block';
    });

    document.getElementById('logoutBtn').addEventListener('click', logout);

    // Кнопки создания
    document.getElementById('createThreadBtn').addEventListener('click', () => {
        if (!AppState.currentUser) {
            showError('Сначала войдите в систему');
            return;
        }
        document.getElementById('threadModal').style.display = 'block';
    });

    document.getElementById('createCategoryBtn').addEventListener('click', () => {
        document.getElementById('categoryModal').style.display = 'block';
    });

    // Кнопки управления темами
    document.getElementById('deleteThreadBtn').addEventListener('click', deleteSelectedThread);
    document.getElementById('deleteThreadModalBtn').addEventListener('click', deleteSelectedThread);
    document.getElementById('refreshBtn').addEventListener('click', () => {
        if (currentCategory) {
            loadThreadsByCategory(currentCategory.id);
        } else {
            loadThreads();
        }
    });

    // Модерация
    document.getElementById('pinThreadBtn').addEventListener('click', () => changeThreadStatus('PINNED'));
    document.getElementById('closeThreadBtn').addEventListener('click', () => changeThreadStatus('CLOSED'));
    document.getElementById('hideThreadBtn').addEventListener('click', () => changeThreadStatus('HIDDEN'));

    // Формы
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    document.getElementById('threadForm').addEventListener('submit', handleCreateThread);
    document.getElementById('categoryForm').addEventListener('submit', handleCreateCategory);
    document.getElementById('replyForm').addEventListener('submit', handleCreateReply);

    // Закрытие модальных окон
    document.querySelectorAll('.close').forEach(btn => {
        btn.addEventListener('click', function() {
            this.closest('.modal').style.display = 'none';
        });
    });

    // Закрытие по клику вне окна
    window.addEventListener('click', (event) => {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    });
}

// ========== Обработчики форм ==========
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await ForumAPI.login(username, password);
        AppState.saveAuth(response.token, {
            id: response.userId,
            username: response.username,
            role: response.role
        });

        document.getElementById('loginModal').style.display = 'none';
        document.getElementById('loginForm').reset();
        initAuth();
        showSuccess('Вход выполнен успешно!');
        // Перезагружаем данные после входа
        loadCategories();
        loadThreads();
    } catch (error) {
        console.error('Ошибка входа:', error);
        showError('Ошибка входа: проверьте логин/пароль или подключение к API');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;

    try {
        await ForumAPI.register(username, password);
        document.getElementById('registerModal').style.display = 'none';
        document.getElementById('registerForm').reset();
        showSuccess('Регистрация успешна! Теперь войдите.');
        document.getElementById('loginModal').style.display = 'block';
    } catch (error) {
        console.error('Ошибка регистрации:', error);
        if (error.message.includes('409')) {
            showError('Ошибка регистрации: пользователь с таким именем уже существует');
        } else {
            showError('Ошибка регистрации: проверьте подключение к API');
        }
    }
}

async function handleCreateThread(e) {
    e.preventDefault();
    const title = document.getElementById('threadTitle').value;
    const content = document.getElementById('threadContent').value;
    const categoryId = document.getElementById('threadCategory').value;

    try {
        await ForumAPI.createThread(title, content, parseInt(categoryId));
        document.getElementById('threadModal').style.display = 'none';
        document.getElementById('threadForm').reset();
        showSuccess('Тема создана!');

        if (currentCategory) {
            loadThreadsByCategory(currentCategory.id);
        } else {
            loadThreads();
        }
    } catch (error) {
        showError('Ошибка создания темы: ' + error.message);
    }
}

async function handleCreateCategory(e) {
    e.preventDefault();
    const name = document.getElementById('categoryName').value;
    const description = document.getElementById('categoryDescription').value;
    const slug = document.getElementById('categorySlug').value;

    try {
        await ForumAPI.createCategory(name, description, slug);
        document.getElementById('categoryModal').style.display = 'none';
        document.getElementById('categoryForm').reset();
        showSuccess('Категория создана!');
        loadCategories();
    } catch (error) {
        showError('Ошибка создания категории: ' + error.message);
    }
}

async function handleCreateReply(e) {
    e.preventDefault();
    const content = document.getElementById('replyContent').value;

    try {
        await ForumAPI.createPost(currentThread.id, content);
        document.getElementById('replyForm').reset();
        showSuccess('Ответ добавлен!');

        // Перезагружаем посты
        const postsData = await ForumAPI.getPosts(currentThread.id);
        const posts = postsData.content || postsData;

        const postsList = document.getElementById('postsList');
        postsList.innerHTML = '';
        posts.forEach(post => {
            const postDiv = document.createElement('div');
            postDiv.className = 'post';
            postDiv.innerHTML = `
            <div class="post-author">${escapeHtml(post.authorUsername)}</div>
            <div class="post-date">${formatDate(post.createdAt)}</div>
            <div class="post-content">${escapeHtml(post.content)}</div>
            `;
            postsList.appendChild(postDiv);
        });
    } catch (error) {
        showError('Ошибка добавления ответа: ' + error.message);
    }
}

// ========== Удаление темы ==========
async function deleteSelectedThread() {
    // В реальной реализации нужно выбрать тему из таблицы
    // Для простоты используем last opened thread или добавим выделение
    if (!currentThread) {
        showError('Выберите тему для удаления (откройте тему)');
        return;
    }

    if (!confirm(`Вы уверены, что хотите удалить тему "${currentThread.title}"?`)) {
        return;
    }

    try {
        await ForumAPI.deleteThread(currentThread.id);
        document.getElementById('viewThreadModal').style.display = 'none';
        showSuccess('Тема удалена!');

        if (currentCategory) {
            loadThreadsByCategory(currentCategory.id);
        } else {
            loadThreads();
        }
    } catch (error) {
        showError('Ошибка удаления темы: ' + error.message);
    }
}

// ========== Изменение статуса темы ==========
async function changeThreadStatus(status) {
    if (!currentThread) {
        showError('Выберите тему для модерации');
        return;
    }

    try {
        await ForumAPI.updateThreadStatus(currentThread.id, status);
        showSuccess(`Статус темы изменен на ${status}`);

        // Обновляем отображение
        document.getElementById('viewThreadStatus').textContent = status;
        currentThread.status = status;

        if (currentCategory) {
            loadThreadsByCategory(currentCategory.id);
        } else {
            loadThreads();
        }
    } catch (error) {
        showError('Ошибка изменения статуса: ' + error.message);
    }
}

// ========== Обновление панели модерации в модальном окне ==========
function updateThreadModerationPanel() {
    const moderationPanel = document.getElementById('threadModerationPanel');
    if (!moderationPanel) return;
    
    const isAdminOrMod = AppState.currentUser && ['ROLE_ADMIN', 'ROLE_MODERATOR'].includes(AppState.currentUser.role);
    moderationPanel.style.display = isAdminOrMod ? 'flex' : 'none';
    
    // Обновляем кнопку удаления в модальном окне
    const deleteBtnModal = document.getElementById('deleteThreadModalBtn');
    if (deleteBtnModal) {
        deleteBtnModal.style.display = isAdminOrMod ? 'inline-block' : 'none';
    }
}

// ========== Выход ==========
function logout() {
    AppState.clearAuth();
    initAuth();
    showSuccess('Выход выполнен успешно');
}

// ========== Утилиты ==========
function showError(message) {
    console.error('Error:', message);
    // Создаем красивое уведомление об ошибке
    const notification = document.createElement('div');
    notification.className = 'notification error';
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 10);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

function showSuccess(message) {
    console.log('Success:', message);
    // Создаем красивое уведомление об успехе
    const notification = document.createElement('div');
    notification.className = 'notification success';
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 10);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}