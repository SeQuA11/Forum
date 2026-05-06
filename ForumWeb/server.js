const http = require('http');
const fs = require('fs');
const path = require('path');

const PROXY_TARGET = 'http://localhost:8080';
const PORT = 3000;

const mimeTypes = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
    // Добавляем заголовки CORS для ВСЕХ ответов
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    // Обработка preflight запросов (OPTIONS)
    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    // Если запрос к API -> проксируем на Java backend
    if (req.url.startsWith('/api')) {
        const options = {
            hostname: 'localhost',
            port: 8080,
            path: req.url,
            method: req.method,
            headers: req.headers
        };

        const proxyReq = http.request(options, (proxyRes) => {
            res.writeHead(proxyRes.statusCode, proxyRes.headers);
            proxyRes.pipe(res);
        });

        proxyReq.on('error', (e) => {
            console.error(`Ошибка прокси: ${e.message}`);
            res.writeHead(500);
            res.end('Ошибка подключения к API серверу');
        });

        req.pipe(proxyReq);
        return;
    }

    // Иначе отдаем статические файлы
    let filePath = '.' + req.url;
    if (filePath === './') {
        filePath = './index.html';
    }

    const extname = String(path.extname(filePath)).toLowerCase();
    const contentType = mimeTypes[extname] || 'application/octet-stream';

    fs.readFile(filePath, (error, content) => {
        if (error) {
            if (error.code == 'ENOENT') {
                res.writeHead(404);
                res.end('Файл не найден: ' + req.url);
            } else {
                res.writeHead(500);
                res.end('Ошибка сервера: ' + error.code);
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content, 'utf-8');
        }
    });
});

server.listen(PORT, () => {
    console.log(`Сервер запущен на http://localhost:${PORT}`);
    console.log(`API проксируется на ${PROXY_TARGET}`);
});