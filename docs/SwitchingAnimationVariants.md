### Switching between grids with and without animations enabled

#### (A) Create 50x50 (2500 cells) grid by selecting it in the initial window

```
2022-09-10T21:47:31.616890Z – Grid -> null
2022-09-10T21:47:33.607908Z – Enable animations -> OFF
2022-09-10T21:47:39.865347Z – Grid -> 50x50 (2500 cells)
2022-09-10T21:47:41.031984Z – 5000 cells drawn in 333.62ms from 2022-09-10T21:47:39.685506Z to 2022-09-10T21:47:40.019126Z
2022-09-10T21:47:49.491669Z – Grid -> null
2022-09-10T21:47:53.619966Z – Enable animations -> ON 
2022-09-10T21:47:57.813194Z – Grid -> 50x50 (2500 cells)
2022-09-10T21:48:01.281118Z – 5000 cells drawn in 2.793244s from 2022-09-10T21:47:57.453426Z to 2022-09-10T21:48:00.246670Z
```

#### (B) Switch animations on 50x50 (2500 cells) grid, hiding the grid temporarily

```
2022-09-10T21:48:37.975564Z – Enable animations -> OFF
2022-09-10T21:48:40.319497Z – 37500 cells drawn in 1.369423s from 2022-09-10T21:48:37.938119Z to 2022-09-10T21:48:39.307542Z
2022-09-10T21:48:44.573528Z – Enable animations -> ON 
2022-09-10T21:48:49.491237Z – 7500 cells drawn in 3.967818s from 2022-09-10T21:48:44.498855Z to 2022-09-10T21:48:48.466673Z
```

#### (C) Switch animations on 50x50 (2500 cells) grid, without hiding the grid temporarily

```
2022-09-10T21:49:39.871883Z – Enable animations -> OFF
2022-09-10T21:49:51.419328Z – 37500 cells drawn in 10.536078s from 2022-09-10T21:49:39.831093Z to 2022-09-10T21:49:50.367171Z
2022-09-10T21:49:55.901524Z – Enable animations -> ON 
2022-09-10T21:50:14.673802Z – 32500 cells drawn in 17.810506s from 2022-09-10T21:49:55.810575Z to 2022-09-10T21:50:13.621081Z
```

#### Profiling

* When switching animations while _not_ hiding the `Grid`, the majority of time is spent in slot table operations:
    * androidx.compose.runtime.CompositionImpl.lambda 'fastForEach' in applyChangesInLocked()
        * androidx.compose.runtime.ComposerImpl$recordInsert$2.invoke(Object, Object, Object)
            * androidx.compose.runtime.ComposerImpl$recordInsert$2.invoke(Applier, SlotWriter, RememberManager)
                * androidx.compose.runtime.SlotWriter.moveFrom(SlotTable, int)
        * androidx.compose.runtime.ComposerKt$removeCurrentGroupInstance$1.invoke(Object, Object, Object)
            * androidx.compose.runtime.ComposerKt$removeCurrentGroupInstance$1.invoke(Applier, SlotWriter,
              RememberManager)
                * androidx.compose.runtime.ComposerKt.removeCurrentGroup(SlotWriter, RememberManager)


#### Full log

The log shows the number of cell drawing operations between idle periods of 1 second or more.

These actions cause drawing operations:
* Selecting a grid on the initial scene.
* Toggling "Enable animations".
* Moving the mouse, causing hover animations over buttons and checkboxes.
* Toggling a checkbox, causing a ripple effect.

> Because the entire UI is drawn on each frame, subtle animations, hover indicators, can cause thousands of cell drawing operations. Ripple animations cause a multiple of that.

This is the complete log file, from which the above data has been extracted:

```
2022-09-10T21:46:14.983788Z – Pause on each step (100ms) -> OFF
2022-09-10T21:46:14.987241Z – Update top row only -> OFF
2022-09-10T21:46:14.987344Z – Enable animations -> OFF
2022-09-10T21:46:14.987513Z – Highlight recompositions -> OFF
2022-09-10T21:46:14.987607Z – Force top-level recomposition -> OFF
2022-09-10T21:46:14.987709Z – Force row-level recomposition -> OFF
2022-09-10T21:46:14.987797Z – Force cell-level recomposition -> OFF
2022-09-10T21:46:14.987934Z – Draw cell text -> ON 
2022-09-10T21:46:14.988339Z – Track drawing -> ON 
2022-09-10T21:46:14.991622Z – Hide grid temporarily when switching animations -> ON 
2022-09-10T21:46:15.271789Z – Grid -> null
2022-09-10T21:46:45.155424Z – Grid -> 50x50 (2500 cells)
2022-09-10T21:46:46.369431Z – 5000 cells drawn in 533.05ms from 2022-09-10T21:46:44.813553Z to 2022-09-10T21:46:45.346603Z
2022-09-10T21:46:52.864318Z – 12500 cells drawn in 138.692ms from 2022-09-10T21:46:51.700311Z to 2022-09-10T21:46:51.839003Z
2022-09-10T21:46:58.786203Z – Grid -> null
2022-09-10T21:46:59.590604Z – 2500 cells drawn in 15.476ms from 2022-09-10T21:46:58.497732Z to 2022-09-10T21:46:58.513208Z
2022-09-10T21:47:06.144643Z – Enable animations -> ON 
2022-09-10T21:47:13.033987Z – Grid -> 50x50 (2500 cells)
2022-09-10T21:47:16.817249Z – 5000 cells drawn in 3.096053s from 2022-09-10T21:47:12.664521Z to 2022-09-10T21:47:15.760574Z
2022-09-10T21:47:31.616890Z – Grid -> null
2022-09-10T21:47:31.918087Z – 12500 cells drawn in 992.977ms from 2022-09-10T21:47:29.891993Z to 2022-09-10T21:47:30.884970Z
2022-09-10T21:47:33.607908Z – Enable animations -> OFF
2022-09-10T21:47:39.865347Z – Grid -> 50x50 (2500 cells)
2022-09-10T21:47:41.031984Z – 5000 cells drawn in 333.62ms from 2022-09-10T21:47:39.685506Z to 2022-09-10T21:47:40.019126Z
2022-09-10T21:47:46.888147Z – 15000 cells drawn in 132.002ms from 2022-09-10T21:47:45.741427Z to 2022-09-10T21:47:45.873429Z
2022-09-10T21:47:49.491669Z – Grid -> null
2022-09-10T21:47:50.293995Z – 2500 cells drawn in 20.797ms from 2022-09-10T21:47:49.241834Z to 2022-09-10T21:47:49.262631Z
2022-09-10T21:47:53.619966Z – Enable animations -> ON 
2022-09-10T21:47:57.813194Z – Grid -> 50x50 (2500 cells)
2022-09-10T21:48:01.281118Z – 5000 cells drawn in 2.793244s from 2022-09-10T21:47:57.453426Z to 2022-09-10T21:48:00.246670Z
2022-09-10T21:48:14.106020Z – 42500 cells drawn in 1.957566s from 2022-09-10T21:48:11.072607Z to 2022-09-10T21:48:13.030173Z
2022-09-10T21:48:19.572855Z – 2500 cells drawn in 12.409ms from 2022-09-10T21:48:18.537180Z to 2022-09-10T21:48:18.549589Z
2022-09-10T21:48:19.720524Z – Enable animations -> OFF
2022-09-10T21:48:22.093751Z – 7500 cells drawn in 1.408573s from 2022-09-10T21:48:19.673952Z to 2022-09-10T21:48:21.082525Z
2022-09-10T21:48:29.746544Z – Enable animations -> ON 
2022-09-10T21:48:34.910817Z – 27500 cells drawn in 4.182232s from 2022-09-10T21:48:29.691941Z to 2022-09-10T21:48:33.874173Z
2022-09-10T21:48:37.975564Z – Enable animations -> OFF
2022-09-10T21:48:40.319497Z – 37500 cells drawn in 1.369423s from 2022-09-10T21:48:37.938119Z to 2022-09-10T21:48:39.307542Z
2022-09-10T21:48:44.573528Z – Enable animations -> ON 
2022-09-10T21:48:49.491237Z – 7500 cells drawn in 3.967818s from 2022-09-10T21:48:44.498855Z to 2022-09-10T21:48:48.466673Z
2022-09-10T21:48:54.142785Z – 5000 cells drawn in 59.681ms from 2022-09-10T21:48:53.038091Z to 2022-09-10T21:48:53.097772Z
2022-09-10T21:48:55.179124Z – Hide grid temporarily when switching animations -> OFF
2022-09-10T21:48:57.413539Z – 42500 cells drawn in 1.877356s from 2022-09-10T21:48:54.453755Z to 2022-09-10T21:48:56.331111Z
2022-09-10T21:49:03.842115Z – Enable animations -> OFF
2022-09-10T21:49:15.113693Z – 40000 cells drawn in 10.263847s from 2022-09-10T21:49:03.800329Z to 2022-09-10T21:49:14.064176Z
2022-09-10T21:49:19.170576Z – Enable animations -> ON 
2022-09-10T21:49:37.700329Z – 30000 cells drawn in 17.576870s from 2022-09-10T21:49:19.080698Z to 2022-09-10T21:49:36.657568Z
2022-09-10T21:49:39.871883Z – Enable animations -> OFF
2022-09-10T21:49:51.419328Z – 37500 cells drawn in 10.536078s from 2022-09-10T21:49:39.831093Z to 2022-09-10T21:49:50.367171Z
2022-09-10T21:49:55.901524Z – Enable animations -> ON 
2022-09-10T21:50:14.673802Z – 32500 cells drawn in 17.810506s from 2022-09-10T21:49:55.810575Z to 2022-09-10T21:50:13.621081Z
2022-09-10T21:50:20.715283Z – 5000 cells drawn in 59.997ms from 2022-09-10T21:50:19.627001Z to 2022-09-10T21:50:19.686998Z
```
