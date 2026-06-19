# ScreenRotator

Плавающая кнопка поворота экрана для Android.

## Сборка APK

### Вариант 1 — Android Studio (проще всего)
1. File → Open → выбрать папку `ScreenRotator`.
2. Дождаться синхронизации Gradle (скачает зависимости из Google Maven — нужен интернет).
3. Build → Build Bundle(s)/APK(s) → Build APK(s).
4. Готовый файл: `app/build/outputs/apk/debug/app-debug.apk`.

### Вариант 2 — командная строка
В корне проекта:
    ./gradlew assembleDebug        # Linux/macOS
    gradlew.bat assembleDebug      # Windows

Требуется установленный Android SDK и переменная ANDROID_HOME (или local.properties с sdk.dir).

## Установка на телефон
1. Скопировать app-debug.apk на устройство.
2. Разрешить установку из неизвестных источников.
3. Открыть приложение, выдать оба разрешения:
   - «Поверх других приложений» (overlay)
   - «Изменение системных настроек» (WRITE_SETTINGS)
4. Нажать «Запустить кнопку».

Тап по плавающей кнопке — циклическая смена ориентации (0/90/180/270).
Перетаскивание — перемещение кнопки по экрану.

## Заметки
- minSdk 23, targetSdk 34.
- Жёстко фиксированные по ориентации приложения (android:screenOrientation)
  игнорируют системный USER_ROTATION — это ограничение Android.
- На MIUI/EMUI overlay может требовать отдельного тумблера в настройках.
