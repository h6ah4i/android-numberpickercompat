NumberPickerCompat
===============

This library is a port of the [NumberPicker](https://developer.android.com/reference/android/widget/NumberPicker.html) widget of Android framework. The ported widget has been re-written with the support library features so it can provide the same Material Design look and feel for all devices.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.h6ah4i.android.widget/numberpickercompat/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.h6ah4i.android.widget/numberpickercompat)

---

Screenshot
---

<img src="./pic/screenshot.png?raw=true" alt="NumberPickerCompat" width="200" />

---

Target platforms
---

- API level 9 or later


Latest version
---

- Version 1.0.0  (September 25, 2018)   ([RELEASE NOTES](./RELEASE-NOTES.md))

Getting started
---

This library is published on Maven Central. Just add these lines to `build.gradle`.

```diff
dependencies {
    compile 'com.h6ah4i.android.widget:numberpickercompat:1.0.0'
}
repositories {
+     mavenCentral()
}

dependencies {
+     implementation 'com.h6ah4i.android.widget:numberpickercompat:1.0.0'
}
```

Usage
---

```xml
<com.h6ah4i.android.widget.numberpickercompat.NumberPicker
    android:layout_width="200dp"
    android:layout_height="200dp" />

```

License
---

This library is licensed under the [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

See [`LICENSE`](LICENSE) for full of the license text.

    Copyright (C) 2017 Haruki Hasegawa
    Copyright (C) 2008 The Android Open Source Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
