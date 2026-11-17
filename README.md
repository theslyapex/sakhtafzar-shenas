# سخت‌افزار شناس | Hardware Shenas

**Know Your Device. Inside Out.**

اپلیکیشن بومی اندروید برای مشاهده مشخصات سخت‌افزاری، پایش وضعیت دستگاه و اجرای آزمون‌های تشخیصی.

> POWERED BY ALISHAMS DIGITAL STUDIO

## وضعیت توسعه
نسخه فعلی، اسکلت اولیه پروژه است. داشبورد و جمع‌آوری اطلاعات پایه دستگاه پیاده‌سازی شده‌اند و ماژول‌های تخصصی در مراحل بعدی توسعه اضافه می‌شوند.

## فناوری‌ها
- Kotlin
- Android Native
- Jetpack Compose و Material 3
- حداقل نسخه اندروید: Android 8.0 (API 26)

## ساخت پروژه
پروژه را با Android Studio باز کنید و اجازه دهید Gradle Sync کامل شود. سپس پیکربندی `app` را اجرا کنید.

## اصول صحت داده
اطلاعاتی که اندروید یا سازنده دستگاه در اختیار برنامه قرار نمی‌دهد، به‌عنوان مقدار قطعی نمایش داده نمی‌شوند. مقادیر تخمینی، محدودشده یا غیرقابل‌دسترسی باید از داده‌های اندازه‌گیری‌شده تفکیک شوند.


## Display diagnostics

- Full-screen solid-color sequence for visual inspection of stuck/dead pixels and panel uniformity.
- Current and supported display refresh modes reported by Android.
- Multi-touch test that records the maximum simultaneous pointer count observed.

These checks are diagnostic aids. Pixel defects and color accuracy require visual inspection or external measurement; the app does not claim automatic pixel detection or colorimeter-grade calibration.
