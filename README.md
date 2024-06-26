### Compose Counting Grid

A simple application to check **Compose for Desktop** and **Compose for Web (Canvas)** drawing speeds when drawing grids (or tables) with larger numbers of cells.

#### Initial Scene

<p style="margin-left: 24px">
<img alt="Initial Scene" src="docs/initial-scene.png">
</p>

#### Desktop with Recomposition Highlighting

<p style="margin-left: 24px">
<img alt="Desktop with Highlighting" src="docs/desktop-highlighting.png">
</p>

#### Desktop High-Frequency Update (Top Row Only)

* This animated GIF ⚠️ **not suitable for persons with photosensitive epilepsy** ⚠️ demonstrates increasing FPS as the window shrinks: [Animation with high-frequency updates](docs/top-row-only-updates-resizing.gif).

    > This effect appears with the desktop application, not the web application.

#### How It Operates

Given a grid of cells: Choose a random cell. Increase its single-digit count. Repeat.

Unless pausing is enabled, updates will be drawn as fast as possible. The desktop application will even go beyond your display's vsync frequency (which Compose/Skia normally would not do, as it makes no sense other than to check the speed).

#### How To Build And Run

JVM desktop application: `./gradlew runRelease`

Js browser application: `./gradlew -Dapplication.useJs=true jsBrowserProductionRun` (requires some patience for bundles to load)

Wasm browser application: `./gradlew wasmJsBrowserProductionRun` (requires some patience for bundles to load)

* If you'd like Wasm even faster, try aggressive optimization: `./gradlew -Dapplication.optimize=true wasmJsBrowserProductionRun`

#### What To Try

* Try everything without animations first.
* Resize the window so that only the top row of counters is visible.
* Highlight re-compositions.
* Toggle "Force top-level recomposition".
* Browser: Compare Js and Wasm speed and download file size differences (Skiko size will not change).

#### Remarks

* This application does not simulate any real-world scenario as it uses a very simple layout with fixed-size cells.
* Compose for Web on Js/Canvas is experimental and might be deprecated at some point, favoring the upcoming WebAssembly target. However, it is useful in the transition period until full library support is available for the WebAssembly target.
* Compose for Web on WebAssembly is experimental, but most probably the future. It currently requires carefully selected libraries, a specific Compose plugin and some hack to bridge an implementation gap regarding Node module imports.

#### Changes

##### 2024-03-30

* Migrated to 1.9.23, Compose 1.6.1

##### 2023-11-07

* Unified UI across platforms, using Compose scrollbars and `MaterialTheme` everywhere.
* Added aggressive Wasm optimization via `-Dapplication.optimize=true`, increasing speed by another 20%, reducing `app.wasm` size by 24% (to 473 KB). In certain scenarios (50x50 grid, default configuration), K/Wasm on Chrome now seems to be at 84% of desktop/JVM performance (even if the optimization is applied to app+Compose only, not to Skiko).

##### 2023-11-06

* Added Wasm support via Compose 1.5.10-dev-wasm02.
* Web/Wasm FPS now at about 200% of Web/Js FPS (use larger grids and/or animations to check, otherwise you'll hit the display frame rate ceiling).
* Web/Wasm compressed transfer size for `app.wasm` (formerly `frontendWasm.wasm`) now at 623 kB. Total transfer size still at 4.0 MB (Web/Wasm) vs. 3.8 MB (Web/Js) with `skiko.wasm` being the largest contributor as before (3.2 MB).

##### 2023-11-01

* Migrated to Kotlin 1.9.20, Compose 1.5.10
* Wasm not ready yet, waiting for [KT-62872 – K/Wasm: (re)publish compose-mp for wasm-js with 1.9.20-RC2 (or newer if available)](https://youtrack.jetbrains.com/issue/KT-62872)
* Replaced `BrowserViewportWindow` with `CanvasBasedWindow` (now a fully featured replacement for Js and Wasm-Js, part of Compose Multiplatform since 1.5.0-beta02)

##### 2023-07-30

* Web/Js: `BrowserViewportWindow` avoids adding multiple event listeners whenever the window is resized.
* Web/Js: Use Compose Multiplatform 1.5.0-beta01.

##### 2023-07-12

* Web/Js: `BrowserViewportWindow` no longer crashes when resizing the window on Kotlin 1.9.0.
* Web/Wasm: Enabled optimization with binaryen, shrinking the (compressed) transfer size of `frontendWasm.wasm` from 1.2 MB to 612 kB, both in addition to Skiko's transfer size of 3.2 MB. Total transfer size with optimized Wasm is now 4.0 MB (with Web/Js at 3.8 MB). 
* Web/Wasm: Fixed node module import hack.

##### 2023-07-11

* Migrated to Kotlin 1.9.0
* Web/Wasm: Added a full WebAssembly target with Wasm-compiled frontend code (in addition to Skiko/Skia, which were always Wasm-compilations).
* Web/Js: With Kotlin 1.9.0, BrowserViewportWindow no longer reacts to resizing as the Kotlin/Js target now refuses access to non-public symbols.
* Removed non-current documentation.

##### 2022-09-16

* Added options to speed up animation switching:
    * Enable grid generations
    * Enable BoxWithConstraints per row
* Updated timing results

##### 2022-09-12

* Added instrumentation to analyze UI responsiveness when switching animations:
    * Added console logging with timestamps.
    * Added configuration settings:
        * Draw cell text
        * Track drawing
        * Hide grid temporarily when switching animations
* Updated observations in README.
* Added animation switching analysis in `docs/SwitchingAnimationVariants.md`.
* Refactored GridScene to avoid unnecessary grid recompositions when controls update.

##### 2022-09-02

* Redesigned Web/Canvas integration thanks to @langara

##### 2022-08-23
 
* Added options to force row-level and cell-level recompositions. Revised conclusions regarding recomposition and layout impact.
* Improved UI responsiveness when toggling options for recomposition highlighting and animations.
