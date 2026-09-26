-- Скрипт создания схемы БД "Студия звукозаписи"
-- База recording_studio должна быть уже создана.
SET client_encoding = 'UTF8';

DROP TABLE IF EXISTS recording_orders CASCADE;
DROP TABLE IF EXISTS clients CASCADE;

CREATE TABLE clients (
    id          SERIAL PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    phone       VARCHAR(20)  NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL
                CHECK (role IN ('SOLO_ARTIST','BAND','PRODUCER','LABEL')),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recording_orders (
    id              SERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL
                    CHECK (status IN ('CREATED','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED')),
    type            VARCHAR(20)  NOT NULL
                    CHECK (type IN ('SINGLE','ALBUM','JINGLE','VOICEOVER','MIXING')),
    priority        VARCHAR(10)  NOT NULL
                    CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    client_id       INT          NOT NULL,
    recording_date  DATE         NOT NULL,
    hours           INT          NOT NULL CHECK (hours BETWEEN 1 AND 12),
    price           INT          NOT NULL CHECK (price >= 0),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_client
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Тестовые данные: клиенты
INSERT INTO clients (full_name, email, phone, role) VALUES
('Иван Морозов',    'morozov@mail.ru',  '+79001112233', 'SOLO_ARTIST'),
('Группа Звёзды',   'stars@mail.ru',    '+79002223344', 'BAND'),
('Ольга Светлова',  'svetlova@mail.ru', '+79003334455', 'PRODUCER'),
('Лейбл Мечта',     'dream@mail.ru',    '+79004445566', 'LABEL'),
('Пётр Волков',     'volkov@mail.ru',   '+79005556677', 'SOLO_ARTIST');

-- Тестовые данные: заказы
INSERT INTO recording_orders
(title, description, status, type, priority, client_id, recording_date, hours, price) VALUES
('Запись сингла',        'Запись вокальной партии',           'CREATED',     'SINGLE',    'HIGH',   1, '2026-02-15', 4,  14400),
('Альбом группы',        'Запись 10 треков',                  'IN_PROGRESS', 'ALBUM',     'HIGH',   2, '2026-02-20', 10, 30000),
('Реклама кофе',         'Озвучка рекламного ролика',         'COMPLETED',   'VOICEOVER', 'MEDIUM', 3, '2026-01-10', 2,  4000),
('Джингл для радио',     'Короткий музыкальный джингл',       'CONFIRMED',   'JINGLE',    'MEDIUM', 4, '2026-03-05', 3,  12000),
('Сведение трека',       'Сведение и мастеринг',              'IN_PROGRESS', 'MIXING',    'HIGH',   5, '2026-02-25', 6,  25200),
('Запись бэк-вокала',    'Запись бэк-вокала для альбома',     'CREATED',     'SINGLE',    'LOW',    1, '2026-03-10', 2,  6000),
('Аудиокнига',           'Озвучка главы книги',               'COMPLETED',   'VOICEOVER', 'LOW',    3, '2026-01-20', 5,  10000),
('Мини-альбом',          'Запись EP из 5 треков',             'CANCELLED',   'ALBUM',     'MEDIUM', 2, '2026-02-01', 8,  0),
('Рекламный джингл',     'Джингл для интернет-магазина',      'CONFIRMED',   'JINGLE',    'HIGH',   4, '2026-03-15', 3,  14400),
('Сведение альбома',     'Мастеринг 8 треков',                'IN_PROGRESS', 'MIXING',    'MEDIUM', 5, '2026-03-01', 9,  31500),
('Запись подкаста',      'Запись выпуска подкаста',           'CREATED',     'VOICEOVER', 'LOW',    1, '2026-03-20', 2,  4000),
('Запись вокала',        'Запись вокала для сингла',          'COMPLETED',   'SINGLE',    'MEDIUM', 5, '2026-01-05', 3,  9000);