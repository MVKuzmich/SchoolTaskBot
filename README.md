# SchoolBot

Мультимодульный Maven-проект Telegram-бота с переиспользуемым ядром.

## Структура проекта

```
SchoolBot/
├── pom.xml                    # Родительский POM (schoolbot-parent)
├── bot-core/                   # Переиспользуемое ядро бота
│   ├── pom.xml
│   └── src/main/java/com/kuzmich/schoolbot/core/
│       ├── bot/                # AbstractTelegramBot — базовый класс бота
│       ├── config/             # CoreBotConfig — роутеры команд и callback
│       ├── handler/
│       │   ├── command/        # CommandHandler, CommandProcessingHandler
│       │   └── callback/       # CallbackQueryHandler, CallbackQueryProcessingHandler
│       └── service/            # UserStateService (интерфейс)
└── school-bot/                 # Специфичный бот (точка входа и команды)
    ├── pom.xml
    └── src/main/java/com/kuzmich/schoolbot/
        ├── SchoolBotApplication.java
        └── handler/            # StartCommandHandler и др.
```

## Артефакты

- **groupId:** `com.kuzmich.schoolbot`
- **Модули:**
  - `schoolbot-parent` — родитель, управление версиями и модулями
  - `bot-core` — ядро (базовый класс, обработчики, роутеры)
  - `school-bot` — приложение SchoolBot (зависит от bot-core)

## Сборка и запуск

```bash
# Сборка всего проекта
mvn clean install

# Запуск приложения (модуль school-bot)
cd school-bot && mvn spring-boot:run
```

**Конфигурация (токен и username):**

- Удобно хранить в файле `.env` в корне проекта (файл в `.gitignore`, не попадёт в репозиторий).
- Скопируйте `.env.example` в `.env` и заполните `TELEGRAM_BOT_TOKEN` и при необходимости `TELEGRAM_BOT_USERNAME`.
- Spring Boot читает переменные окружения: `TELEGRAM_BOT_TOKEN` и `TELEGRAM_BOT_USERNAME` подхватываются из `application.properties`. Чтобы они подхватились из `.env`, нужно либо задать их в окружении перед запуском (например, в IDE указать env file для конфигурации запуска), либо выполнить в терминале: `set -a && source .env && set +a` (Linux/macOS) перед `mvn spring-boot:run`.

Либо задайте свойства явно: `telegram.bot.token`, `telegram.bot.username`.

## Переиспользование bot-core

В другом проекте добавьте зависимость:

```xml
<dependency>
    <groupId>com.kuzmich.schoolbot</groupId>
    <artifactId>bot-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Наследуйте `AbstractTelegramBot`, зарегистрируйте свои `CommandHandler` и `CallbackQueryHandler` — Spring соберёт их в роутеры через `CoreBotConfig`.
