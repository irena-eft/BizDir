# BizDir

**BizDir** е Android апликација (Java) која претставува бизнис директориум.
Корисникот може да прегледува, додава, менува и брише компании поделени по
категории, како и да добие известување кога физички се наоѓа во близина на
некоја од нив.

## Функционалности

- Преглед на компании по четири категории: Сервиси, Забава, Индустрија, Едукација
- Свајпање или клик за менување меѓу табови
- Пребарување по име во рамките на тековната категорија
- Внес на нова компанија со име, адреса, координати, e-mail, телефон, веб
  страна и една или повеќе категории
- Прикачување логотип од галеријата на телефонот
- Автоматско читање на тековната локација преку GPS
- Детален приказ на компанија со опции за **уредување** и **бришење**
- Pull-to-refresh за освежување на листата
- Toast известување кога корисникот е на помалку од 50 м од некоја компанија

## Архитектура

Android апликацијата комуницира со PHP веб сервиси хостирани на Render,
кои потоа се обраќаат до Supabase (PostgreSQL базата и Storage за сликите).
Сите тајни клучеви се чуваат на серверот, а не во APK-то.

```
Android app  ──HTTPS──▶  PHP веб сервиси  ──HTTPS──▶  Supabase
 (Retrofit)              (Render)                     (PostgreSQL + Storage)
```

## Автори

- Ирена Ефтимова

## Користени библиотеки

- [Retrofit](https://square.github.io/retrofit/) — HTTP клиент
- [OkHttp](https://square.github.io/okhttp/) — испраќање на сликите
- [Gson Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/gson) — JSON парсирање
- [Glide](https://github.com/bumptech/glide) — приказ на слики од интернет
- [Google Play Services Location](https://developer.android.com/training/location) — геолокација
- [AndroidX AppCompat, ViewPager2, SwipeRefreshLayout, ConstraintLayout](https://developer.android.com/jetpack/androidx)
- [Material Components](https://github.com/material-components/material-components-android) — Toolbar, TabLayout

## Користени сервиси

- [Supabase](https://supabase.com) — база на податоци (PostgreSQL) и складиште за слики
- [Render](https://render.com) — хостинг на PHP веб сервисите
