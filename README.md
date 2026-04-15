# Lexxie TV

Android TV приложение для просмотра фильмов и сериалов с TMDB.

## Возможности

- 🎬 Фильмы (TMDB)
- 📺 Сериалы с выбором сезонов и эпизодов
- 🎮 Видеоплеер с поддержкой HLS (m3u8)
- 🔍 Поиск контента
- 📱 Android TV оптимизация

## Источники

- Метаданные: [TMDB API](https://www.themoviedb.org/)
- Видео: flixer.su (через WebView сниффинг)

## Технологии

- Kotlin
- ExoPlayer (Media3)
- Leanback UI
- Cicerone (Navigation)
- Quill DI

## Сборка

```bash
./gradlew :app-tv:assembleRelease
```

## APK

Готовый APK: `app-tv/build/outputs/apk/release/Lexxie_TV_v1.2.0.apk`

## Конфигурация

Для подписи release версии необходим keystore в `app-tv/keystore/`.

## Лицензия

Проект основан на [AniLibria](https://github.com/anilibria/anilibria-app)
GPL License
