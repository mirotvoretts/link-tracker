# LinkTracker

> Distributed microservice platform for tracking web resource updates and delivering notifications via Telegram.

[English](#english) | [Русский](#русский)

---

## English

### Overview

LinkTracker is a Telegram bot that monitors GitHub repositories and Stack Overflow questions for new activity and sends you real-time notifications. Under the hood it is a distributed microservice system with intelligent update processing: spam filtering, summarization, and priority ranking before anything reaches your Telegram chat.

### Architecture

```
         User (Telegram)
              ↓
        Telegram Bot API
              ↓
    ┌─────────────────┐
    │   Bot Service   │  ← receives commands, sends notifications
    └────────▲────────┘
             │
     link.processed-updates  (Kafka)
             │
    ┌────────┴────────┐
    │   AI Agent      │  ← filters spam, summarizes, prioritizes
    │   Service       │
    └────────▲────────┘
             │
       link.raw-updates  (Kafka)
             │
    ┌────────┴────────┐
    │    Scrapper     │  ← polls GitHub & Stack Overflow
    │    Service      │
    └─────────────────┘
```

### Services

| Service | Port | Responsibility |
|---------|------|----------------|
| **Bot** | 8080 | Telegram interface — accepts commands, delivers notifications |
| **Scrapper** | 8083 | Polls GitHub / Stack Overflow, publishes raw events to Kafka |
| **AI Agent** | — | Filters, summarizes and prioritizes events; publishes processed events |

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 25, Spring Boot 4.x |
| Database | PostgreSQL 18 + Liquibase migrations |
| Cache | Redis 8 (with TTL-based invalidation) |
| Messaging | Apache Kafka 4 + Confluent Schema Registry (Avro) |
| Resilience | Resilience4j — retry, circuit breaker, rate limiter |
| Testing | Testcontainers (Kafka, PostgreSQL, Redis) |
| Build | Maven 3.9+ with Spotless, SpotBugs, PMD |

### Getting Started

#### Prerequisites

- JDK 25+
- Docker & Docker Compose
- Maven 3.9.12+ (or use the bundled `mvnw`)

#### Build

```bash
# Unix / macOS / WSL
./mvnw clean verify

# Windows
mvnw.cmd clean verify
```

#### Configuration

Secrets are stored in `.env` files at each module root (never commit them).

**bot/.env**
```properties
TELEGRAM_TOKEN=<your-bot-token>
SCRAPPER_BASE_URL=http://localhost:8083
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**scrapper/.env**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/scrapper
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
GITHUB_TOKEN=<your-github-token>
STACKOVERFLOW_KEY=<your-stackoverflow-key>
STACKOVERFLOW_ACCESS_KEY=<your-stackoverflow-access-key>
```

#### Useful Commands

```bash
# Format code
./mvnw spotless:apply

# Run tests only
./mvnw test

# Run all linters
./mvnw clean compile spotless:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check

# Dependency tree
./mvnw dependency:tree
```

### Bot Commands

| Command | Description |
|---------|-------------|
| `/start` | Register and get a welcome message |
| `/track <url> [tags...]` | Start tracking a link |
| `/untrack <url>` | Stop tracking a link |
| `/list [tag]` | Show all tracked links (optionally filtered by tag) |
| `/help` | Show available commands |

### Project Structure

```
link-tracker/
├── bot/                  # Telegram Bot service
├── scrapper/             # Link monitoring service
├── ai-agent/             # Intelligent processing service
├── build-report-aggregate/  # Build report aggregation
├── pom.xml               # Parent POM
├── pmd.xml               # PMD rules
└── spotbugs-excludes.xml # SpotBugs exclusions
```

### Data Flow

1. User adds a link via Telegram (`/track`)
2. Scrapper polls the source periodically
3. On new activity → raw event published to `link.raw-updates`
4. AI Agent filters spam, summarizes long content, assigns priority
5. Processed event published to `link.processed-updates`
6. Bot receives the event and sends a formatted Telegram message

---

## Русский

### Обзор

LinkTracker — Telegram-бот для мониторинга GitHub-репозиториев и вопросов на Stack Overflow. При появлении новой активности система отправляет персональные уведомления в ваш Telegram-чат. Это распределённая микросервисная платформа с интеллектуальной обработкой обновлений: фильтрация спама, суммаризация и приоритизация происходят до того, как что-либо достигнет пользователя.

### Архитектура

```
         Пользователь (Telegram)
                ↓
          Telegram Bot API
                ↓
    ┌───────────────────┐
    │   Bot Service     │  ← принимает команды, отправляет уведомления
    └─────────▲─────────┘
              │
    link.processed-updates  (Kafka)
              │
    ┌─────────┴─────────┐
    │   AI Agent        │  ← фильтрует спам, суммаризирует, приоритизирует
    │   Service         │
    └─────────▲─────────┘
              │
       link.raw-updates  (Kafka)
              │
    ┌─────────┴─────────┐
    │   Scrapper        │  ← опрашивает GitHub и Stack Overflow
    │   Service         │
    └───────────────────┘
```

### Сервисы

| Сервис | Порт | Ответственность |
|--------|------|----------------|
| **Bot** | 8080 | Telegram-интерфейс — принимает команды, доставляет уведомления |
| **Scrapper** | 8083 | Опрашивает GitHub / Stack Overflow, публикует сырые события в Kafka |
| **AI Agent** | — | Фильтрует, суммаризирует и приоритизирует; публикует обработанные события |

### Стек технологий

| Слой | Технология |
|------|-----------|
| Runtime | Java 25, Spring Boot 4.x |
| База данных | PostgreSQL 18 + Liquibase миграции |
| Кэш | Redis 8 (TTL-инвалидация) |
| Очередь сообщений | Apache Kafka 4 + Confluent Schema Registry (Avro) |
| Отказоустойчивость | Resilience4j — retry, circuit breaker, rate limiter |
| Тестирование | Testcontainers (Kafka, PostgreSQL, Redis) |
| Сборка | Maven 3.9+ со Spotless, SpotBugs, PMD |

### Быстрый старт

#### Требования

- JDK 25+
- Docker & Docker Compose
- Maven 3.9.12+ (или встроенный `mvnw`)

#### Сборка

```bash
# Unix / macOS / WSL
./mvnw clean verify

# Windows
mvnw.cmd clean verify
```

#### Конфигурация

Секреты хранятся в `.env`-файлах в корне каждого модуля (не коммитить в репозиторий).

**bot/.env**
```properties
TELEGRAM_TOKEN=<токен-бота>
SCRAPPER_BASE_URL=http://localhost:8083
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**scrapper/.env**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/scrapper
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
GITHUB_TOKEN=<токен-github>
STACKOVERFLOW_KEY=<ключ-stackoverflow>
STACKOVERFLOW_ACCESS_KEY=<access-key-stackoverflow>
```

#### Полезные команды

```bash
# Автоформатирование кода
./mvnw spotless:apply

# Только тесты
./mvnw test

# Все линтеры
./mvnw clean compile spotless:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check

# Дерево зависимостей
./mvnw dependency:tree
```

### Команды бота

| Команда | Описание |
|---------|----------|
| `/start` | Регистрация и приветственное сообщение |
| `/track <url> [теги...]` | Начать отслеживание ссылки |
| `/untrack <url>` | Прекратить отслеживание ссылки |
| `/list [тег]` | Показать список ссылок (опционально — с фильтром по тегу) |
| `/help` | Показать доступные команды |

### Структура проекта

```
link-tracker/
├── bot/                     # Сервис Telegram-бота
├── scrapper/                # Сервис мониторинга ссылок
├── ai-agent/                # Сервис интеллектуальной обработки
├── build-report-aggregate/  # Агрегация отчётов сборки
├── pom.xml                  # Родительский POM
├── pmd.xml                  # Правила PMD
└── spotbugs-excludes.xml    # Исключения SpotBugs
```

### Поток данных

1. Пользователь добавляет ссылку через Telegram (`/track`)
2. Scrapper периодически опрашивает источник
3. При новой активности → сырое событие публикуется в `link.raw-updates`
4. AI Agent фильтрует спам, суммаризирует длинный контент, назначает приоритет
5. Обработанное событие публикуется в `link.processed-updates`
6. Bot получает событие и отправляет форматированное сообщение в Telegram
