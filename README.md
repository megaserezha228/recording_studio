# Студия звукозаписи — консольная информационная система

Контрольная работа №1.
Стек: Java 17, Maven, PostgreSQL, JDBC, Apache POI.

## Архитектура
Console UI → Service → Repository / JDBC → PostgreSQL


- **UI** — `com.studio.ui.ConsoleUI`, `InputReader`, `ExceptionHandler`
- **Service** — `com.studio.service.ClientService`, `RecordingOrderService`
- **Repository** — `com.studio.repository.*` + `impl/*`
- **DB** — `com.studio.util.DatabaseManager`, `db/schema.sql`

## Требования

- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Настройка БД

1. Создайте базу и таблицы:
psql -U postgres -f db/schema.sql

2. В `src/main/resources/db.properties` укажите свои параметры:
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/recording_studio
db.user=postgres
db.password=postgres


## Сборка и запуск

```bash
mvn clean package
java -jar target/recording-studio-1.0.0.jar

```
## Или через Maven:

- mvn exec:java -Dexec.mainClass=com.studio.Main

## Структура проекта:

src/main/java/com/studio/
├── Main.java
├── model/          # POJO + enum
├── repository/     # интерфейсы + JDBC-реализации
├── service/        # бизнес-логика
├── exception/      # собственные исключения
├── ui/             # консольное меню
└── util/           # DatabaseManager, ExcelExporter

## Функционал:

- CRUD по клиентам и заказам на запись

- Поиск по названию и описанию заказа

- Фильтрация по статусу / типу / приоритету / диапазону дат

- Сортировка по дате записи / стоимости / дате создания

- Статистика по заказам

- Экспорт заказов в .xlsx

- Бизнес-правила (валидация в сервисах, переходы статусов)

- Обработка ошибок ввода и БД


---

# ✅ Итог по Части 4

## 🆕 Создано заново

| Файл |
|---|
| `src/main/java/com/studio/util/ExcelExporter.java` |
| `README.md` |

## ✏️ Изменено (был создан в Части 1 как заглушка)

| Файл |
|---|
| `src/main/java/com/studio/ui/ConsoleUI.java` (полная версия) |
| `src/main/java/com/studio/Main.java` (полная версия) |

---

## 🧾 Все команды Части 4 одной пачкой

### PowerShell

```powershell
cd recording-studio

# Excel-экспортёр
mkdir src\main\java\com\studio\util
New-Item src\main\java\com\studio\util\ExcelExporter.java -ItemType File

# UI и точка входа (файлы уже были — перезаписываем содержимое)
New-Item src\main\java\com\studio\ui\ConsoleUI.java -ItemType File -Force
New-Item src\main\java\com\studio\Main.java          -ItemType File -Force

# README
New-Item README.md -ItemType File

code .
```

## bash

cd recording-studio

mkdir -p src/main/java/com/studio/util
touch src/main/java/com/studio/util/ExcelExporter.java

touch src/main/java/com/studio/ui/ConsoleUI.java
touch src/main/java/com/studio/Main.java
touch README.md

code .



