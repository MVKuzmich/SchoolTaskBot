# Шрифты для PDF

Для корректного отображения **кириллицы** в сгенерированных PDF приложение использует TTF-шрифт из этой папки.

**По умолчанию ожидается файл:** `DejaVuSans.ttf`

- Скачать: [DejaVu Fonts](https://github.com/dejavu-fonts/dejavu-fonts/releases) → архив `dejavu-fonts-ttf-*.tar.bz2` → из папки `ttf` взять `DejaVuSans.ttf`.
- Или скопировать из Maven-зависимости `jasperreports-fonts` (jar → `net/sf/jasperreports/fonts/dejavu/DejaVuSans.ttf`).

Положите `DejaVuSans.ttf` в эту папку (`src/main/resources/fonts/`). Путь задаётся в `application.properties`: `pdf.font.path=classpath:/fonts/DejaVuSans.ttf` (значение по умолчанию). Если файла в этой папке нет, приложение попробует взять DejaVu Sans из зависимости `jasperreports-fonts`.

Можно использовать другой TTF: положите файл сюда и укажите в настройках, например `pdf.font.path=classpath:/fonts/ИмяШрифта.ttf`, или путь к файлу на диске / переменная окружения `PDF_FONT_PATH`.

Если шрифт не найден, используется Helvetica (кириллица отображаться не будет).
