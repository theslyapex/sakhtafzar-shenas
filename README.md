# سخت‌افزار شناس | Hardware Shenas

**Know Your Device. Inside Out.**

اپلیکیشن بومی اندروید برای مشاهده مشخصات سخت‌افزاری، پایش وضعیت دستگاه و اجرای آزمون‌های تشخیصی.

> POWERED BY ALISHAMS DIGITAL STUDIO

## وضعیت توسعه
داشبورد و ماژول‌های تشخیص سخت‌افزار، پایش سنسورها، GPU، عملکرد CPU و RAM، بنچمارک آزمایشی، باتری، نمایشگر، شبکه و مشخصات دوربین‌ها در حال توسعه هستند.

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


## Camera diagnostics

- Reads Camera2 metadata for each camera exposed by Android: facing, hardware support level, sensor pixel-array dimensions, reported focal lengths and apertures, physical sensor size, flash, autofocus, optical/video stabilization modes, and supported capabilities.
- Lists representative JPEG photo and MediaRecorder video output sizes when the device reports them.
- Metadata-only: no camera preview, photo, or video is captured, and the screen does not request the CAMERA runtime permission.
- Camera metadata and output sizes are manufacturer-reported capabilities. Missing values are shown as unavailable rather than inferred.


## Live camera test

- Optional CameraX preview for front and rear cameras, only after the user explicitly grants the CAMERA permission.
- Tap-to-focus uses CameraX focus/metering where supported; torch toggle is shown only when the selected camera reports a flash unit.
- Preview-only: this test does not capture, save, or upload photos or videos.
