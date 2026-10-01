# Студия звукозаписи

Консольная информационная система для учёта клиентов студии и заказов на студийную запись.

## Стек технологий

- Java 17
- Maven
- PostgreSQL
- JDBC
- Apache POI (экспорт в Excel)

## Архитектура

Приложение построено по многослойной схеме:

    Console UI  ->  Service  ->  Repository / JDBC  ->  PostgreSQL

- **UI** — `com.studio.ui.ConsoleUI`, `InputReader`, `ExceptionHandler`
- **Service** — `com.studio.service.ClientService`, `RecordingOrderService`
- **Repository** — `com.studio.repository.*` и `com.studio.repository.impl.*`
- **Database** — `com.studio.util.DatabaseManager`, `db/schema.sql`

## Структура проекта

    recording-studio/
    ├── pom.xml
    ├── README.md
    ├── build-and-run.bat
    ├── db/
    │   └── schema.sql
    └── src/main/
        ├── java/com/studio/
        │   ├── Main.java
        │   ├── model/          классы-сущности и перечисления
        │   ├── repository/     интерфейсы и JDBC-реализации
        │   ├── service/        бизнес-логика
        │   ├── exception/      собственные исключения
        │   ├── ui/             консольный интерфейс
        │   └── util/           DatabaseManager, ExcelExporter
        └── resources/
            └── db.properties

## База данных

Две связанные таблицы:

- `clients` — клиенты студии
- `recording_orders` — заказы на запись

Связь: `recording_orders.client_id -> clients.id` (один-ко-многим).

Ограничения: PRIMARY KEY, FOREIGN KEY, NOT NULL, UNIQUE, CHECK.

## Функционал

- CRUD для клиентов и заказов
- Поиск заказов по названию и описанию
- Фильтрация по статусу, типу записи, приоритету, диапазону дат
- Сортировка по дате записи, стоимости, дате создания
- Статистика по заказам (7 показателей)
- Экспорт заказов в Excel (.xlsx)
- Бизнес-правила: проверка полей, валидность email и телефона, запрет некорректных переходов статусов, автоматический расчёт стоимости
- Обработка ошибок ввода и ошибок БД

## Требования

- Java 17 или выше
- Maven 3.8 или выше
- PostgreSQL 13 или выше

## Настройка базы данных

1. Создать базу данных:

       psql -U postgres -c "CREATE DATABASE recording_studio;"

2. Применить схему:

       psql -U postgres -d recording_studio -f db/schema.sql

3. Проверить параметры подключения в `src/main/resources/db.properties`:

       db.driver=org.postgresql.Driver
       db.url=jdbc:postgresql://localhost:5432/recording_studio?characterEncoding=UTF-8
       db.user=postgres
       db.password=postgres

## Запуск

### Способ 1. Через готовый скрипт (Windows, самый простой)

В корне проекта лежит `build-and-run.bat`. Он собирает проект и сразу запускает приложение.

**Вариант А. Двойной клик** по файлу `build-and-run.bat` в Проводнике.

**Вариант Б. Из cmd:**

    build-and-run.bat

**Вариант В. Из PowerShell:**

    .\build-and-run.bat

**Вариант Г. Из Git Bash:**

    ./build-and-run.bat

### Способ 2. Вручную через Maven

1. Собрать проект:

       mvn clean package

2. Запустить приложение:

       java -jar target/recording-studio-1.0.0.jar

### Способ 3. Запуск на Windows с правильной кодировкой

Русский ввод в классической консоли Windows работает корректно только при кодировке CP866. Перед запуском выполните:

**cmd:**

    chcp 866
    java -jar target\recording-studio-1.0.0.jar

**PowerShell:**

    chcp 866
    java -jar target/recording-studio-1.0.0.jar

**Git Bash:**

    chcp 866
    java -jar target/recording-studio-1.0.0.jar

Скрипт `build-and-run.bat` делает это автоматически — его можно использовать вместо ручного запуска.

### Способ 4. Запуск через Windows Terminal (рекомендуется)

Windows Terminal корректно работает с кириллицей в UTF-8, никаких `chcp` не требуется:

    java -jar target/recording-studio-1.0.0.jar

### Полный список команд для запуска

| Оболочка | Команда |
|---|---|
| Проводник | двойной клик по `build-and-run.bat` |
| cmd | `build-and-run.bat` |
| PowerShell | `.\build-and-run.bat` |
| Git Bash | `./build-and-run.bat` |
| Ручной запуск (cmd) | `chcp 866` затем `java -jar target\recording-studio-1.0.0.jar` |
| Ручной запуск (PowerShell) | `chcp 866` затем `java -jar target/recording-studio-1.0.0.jar` |
| Windows Terminal | `java -jar target/recording-studio-1.0.0.jar` |

## Главное меню

    1. Клиенты
    2. Заказы на запись
    3. Поиск
    4. Фильтрация
    5. Сортировка
    6. Статистика
    7. Экспорт данных
    8. Вывести таблицы базы данных
    0. Выход
