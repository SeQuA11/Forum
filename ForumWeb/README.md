# ForumWeb - Веб-версия форума

Веб-клиент для форума, использующий API из проекта `api` и функционал из `forumD`.

## Структура проекта

```
ForumWeb/
├── index.html          # Главная страница
├── css/
│   └── styles.css      # Стили приложения
├── js/
│   ├── api.js          # API клиент для работы с backend
│   └── app.js          # Основная логика приложения
└── lib/                # Библиотеки (при необходимости)
```

## Функционал

### Для всех пользователей:
- Просмотр списка категорий
- Просмотр тем по категориям
- Просмотр деталей темы с сообщениями
- Регистрация нового пользователя

### Для авторизованных пользователей:
- Создание новых тем
- Добавление ответов в темы
- Личный кабинет с информацией о пользователе

### Для модераторов и администраторов:
- Создание категорий
- Удаление тем
- Изменение статуса тем (закрепить, закрыть, скрыть)
- Модерация контента

## API Endpoints

Приложение использует следующие endpoints API:

### Аутентификация
- `POST /api/v1/auth/register` - Регистрация
- `POST /api/v1/auth/login` - Вход

### Категории
- `GET /api/v1/categories` - Получить список категорий
- `POST /api/v1/categories` - Создать категорию (только ADMIN)
- `DELETE /api/v1/categories/{id}` - Удалить категорию (только ADMIN)

### Темы
- `GET /api/v1/threads` - Получить список тем
- `GET /api/v1/threads/{id}` - Получить тему по ID
- `GET /api/v1/threads/category/{categoryId}` - Темы по категории
- `POST /api/v1/threads` - Создать тему
- `DELETE /api/v1/threads/{id}` - Удалить тему
- `PATCH /api/v1/threads/{id}/status` - Изменить статус темы
- `GET /api/v1/threads/{id}/view` - Просмотр темы

### Сообщения
- `GET /api/v1/threads/{threadId}/posts` - Получить сообщения темы
- `POST /api/v1/threads/{threadId}/posts` - Создать сообщение

## Запуск

1. Убедитесь, что backend API запущен на `http://localhost:8080`
2. Откройте `index.html` в браузере

Или используйте локальный веб-сервер:

```bash
# Python 3
python -m http.server 8000

# Node.js (требуется http-server)
npx http-server -p 8000
```

Затем откройте `http://localhost:8000` в браузере.

## Настройка

Если API находится на другом порту или хосте, измените константу в `js/api.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080/api/v1';
```

## Технологии

- HTML5
- CSS3 (Flexbox, Grid)
- Vanilla JavaScript (ES6+)
- Fetch API для HTTP запросов
- LocalStorage для хранения токена аутентификации

## Безопасность

- JWT токены хранятся в LocalStorage
- Все запросы к API отправляются с заголовком Authorization
- Реализована проверка прав доступа на уровне UI
