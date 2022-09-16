### Switching between grids with and without animations enabled

#### Timing Results

Duration of cell drawing operations from switching _Enable animations_ until the next idle period of 1 second or more.

| Enable grid generations | BoxWithConstraints per row | Hide grid temporarily | Animations -> ON | Animations -> OFF |
|:-----------------------:|:--------------------------:|:---------------------:|-----------------:|------------------:|
|                         |                            |                       |           19.1 s |            10.4 s |
|            ✅            |                            |                       |            4.5 s |             1.8 s |
|                         |                            |           ✅           |            3.7 s |             1.3 s |
|            ✅            |                            |           ✅           |            3.7 s |             1.3 s |
|                         |             ✅              |                       |            2.3 s |             1.7 s |
|            ✅            |             ✅              |                       |            2.3 s |             1.6 s |

> NOTE: In each case, measurements were taken from the last switching activity.

#### Full log

The log shows the number of cell drawing operations between idle periods of 1 second or more.

These actions cause drawing operations:
* Selecting a grid on the initial scene.
* Toggling "Enable animations".
* Moving the mouse, causing hover animations over buttons and checkboxes.
* Toggling a checkbox, causing a ripple effect.

> Because the entire UI is drawn on each frame, subtle animations, hover indicators, can cause thousands of cell drawing operations. Ripple animations cause a multiple of that.

These are the complete log files, from which the above data has been extracted:

```
2022-09-16T14:45:47.472893Z – Pause on each step (100ms) -> OFF
2022-09-16T14:45:47.477023Z – Update top row only -> OFF
2022-09-16T14:45:47.477207Z – Enable grid generations -> ON 
2022-09-16T14:45:47.477598Z – Enable animations -> OFF
2022-09-16T14:45:47.478315Z – Highlight recompositions -> OFF
2022-09-16T14:45:47.478439Z – Force top-level recomposition -> OFF
2022-09-16T14:45:47.478546Z – Force row-level recomposition -> OFF
2022-09-16T14:45:47.478662Z – Force cell-level recomposition -> OFF
2022-09-16T14:45:47.478762Z – Draw cell text -> ON 
2022-09-16T14:45:47.479061Z – Track drawing -> ON 
2022-09-16T14:45:47.483120Z – Hide grid temporarily when switching animations -> OFF
2022-09-16T14:45:47.909434Z – Grid -> null
2022-09-16T14:45:53.868996Z – Grid -> 50x50 (2500 cells)
2022-09-16T14:45:56.739808Z – 85000 cells drawn in 2.113283s from 2022-09-16T14:45:53.589204Z to 2022-09-16T14:45:55.702487Z
2022-09-16T14:46:02.368059Z – 100000 cells drawn in 3.187074s from 2022-09-16T14:45:58.080794Z to 2022-09-16T14:46:01.267868Z
2022-09-16T14:46:05.839204Z – 12500 cells drawn in 439.459ms from 2022-09-16T14:46:04.306279Z to 2022-09-16T14:46:04.745738Z
2022-09-16T14:46:09.012740Z – Enable animations -> ON 
2022-09-16T14:46:14.449763Z – 30000 cells drawn in 4.458654s from 2022-09-16T14:46:08.958880Z to 2022-09-16T14:46:13.417534Z
2022-09-16T14:46:16.456554Z – Enable animations -> OFF
2022-09-16T14:46:19.305542Z – 40000 cells drawn in 1.811921s from 2022-09-16T14:46:16.418983Z to 2022-09-16T14:46:18.230904Z
2022-09-16T14:46:22.632043Z – Enable animations -> ON 
2022-09-16T14:46:28.197912Z – 30000 cells drawn in 4.583876s from 2022-09-16T14:46:22.570859Z to 2022-09-16T14:46:27.154735Z
2022-09-16T14:46:31.014203Z – Enable animations -> OFF
2022-09-16T14:46:33.804813Z – 40000 cells drawn in 1.756438s from 2022-09-16T14:46:30.974240Z to 2022-09-16T14:46:32.730678Z
2022-09-16T14:46:36.614592Z – Enable animations -> ON 
2022-09-16T14:46:42.120787Z – 27500 cells drawn in 4.512819s from 2022-09-16T14:46:36.579794Z to 2022-09-16T14:46:41.092613Z
2022-09-16T14:46:44.585035Z – Enable animations -> OFF
2022-09-16T14:46:47.409628Z – 40000 cells drawn in 1.811537s from 2022-09-16T14:46:44.546173Z to 2022-09-16T14:46:46.357710Z
2022-09-16T14:46:51.054986Z – Hide grid temporarily when switching animations -> ON 
2022-09-16T14:46:52.500963Z – 57500 cells drawn in 1.921731s from 2022-09-16T14:46:49.555397Z to 2022-09-16T14:46:51.477128Z
2022-09-16T14:46:53.611027Z – 5000 cells drawn in 34.131ms from 2022-09-16T14:46:52.502611Z to 2022-09-16T14:46:52.536742Z
2022-09-16T14:46:57.480761Z – Enable animations -> ON 
2022-09-16T14:47:02.572007Z – 27500 cells drawn in 4.037464s from 2022-09-16T14:46:57.446579Z to 2022-09-16T14:47:01.484043Z
2022-09-16T14:47:05.828434Z – Enable animations -> OFF
2022-09-16T14:47:08.243008Z – 37500 cells drawn in 1.360658s from 2022-09-16T14:47:05.789544Z to 2022-09-16T14:47:07.150202Z
2022-09-16T14:47:12.062794Z – Enable animations -> ON 
2022-09-16T14:47:16.746578Z – 10000 cells drawn in 3.700477s from 2022-09-16T14:47:12.008265Z to 2022-09-16T14:47:15.708742Z
2022-09-16T14:47:19.394269Z – Enable animations -> OFF
2022-09-16T14:47:21.737417Z – 37500 cells drawn in 1.288724s from 2022-09-16T14:47:19.354702Z to 2022-09-16T14:47:20.643426Z
2022-09-16T14:47:26.720263Z – Enable grid generations -> OFF
2022-09-16T14:47:29.667900Z – 72500 cells drawn in 3.245322s from 2022-09-16T14:47:25.402660Z to 2022-09-16T14:47:28.647982Z
2022-09-16T14:47:35.341447Z – Enable animations -> ON 
2022-09-16T14:47:39.936828Z – 10000 cells drawn in 3.640927s from 2022-09-16T14:47:35.281024Z to 2022-09-16T14:47:38.921951Z
2022-09-16T14:47:46.017295Z – Enable animations -> OFF
2022-09-16T14:47:48.360795Z – 7500 cells drawn in 1.376860s from 2022-09-16T14:47:45.971044Z to 2022-09-16T14:47:47.347904Z
2022-09-16T14:47:59.072784Z – Enable animations -> ON 
2022-09-16T14:48:03.706006Z – 20000 cells drawn in 3.664890s from 2022-09-16T14:47:58.994750Z to 2022-09-16T14:48:02.659640Z
2022-09-16T14:48:14.338749Z – Enable animations -> OFF
2022-09-16T14:48:16.604596Z – 7500 cells drawn in 1.305791s from 2022-09-16T14:48:14.286393Z to 2022-09-16T14:48:15.592184Z
2022-09-16T14:48:19.436782Z – 2500 cells drawn in 10.904ms from 2022-09-16T14:48:18.399825Z to 2022-09-16T14:48:18.410729Z
2022-09-16T14:48:20.685016Z – Hide grid temporarily when switching animations -> OFF
2022-09-16T14:48:23.165904Z – 72500 cells drawn in 2.404207s from 2022-09-16T14:48:19.666759Z to 2022-09-16T14:48:22.070966Z
2022-09-16T14:48:26.570760Z – Enable animations -> ON 
2022-09-16T14:48:46.600508Z – 30000 cells drawn in 19.073024s from 2022-09-16T14:48:26.499497Z to 2022-09-16T14:48:45.572521Z
2022-09-16T14:48:51.547203Z – Enable animations -> OFF
2022-09-16T14:49:03.007199Z – 37500 cells drawn in 10.433303s from 2022-09-16T14:48:51.503992Z to 2022-09-16T14:49:01.937295Z
2022-09-16T14:49:05.938542Z – Enable animations -> ON 
2022-09-16T14:49:26.041411Z – 30000 cells drawn in 19.114718s from 2022-09-16T14:49:05.878933Z to 2022-09-16T14:49:24.993651Z
2022-09-16T14:49:27.506790Z – Enable animations -> OFF
2022-09-16T14:49:38.882568Z – 40000 cells drawn in 10.380137s from 2022-09-16T14:49:27.461871Z to 2022-09-16T14:49:37.842008Z
2022-09-16T14:49:41.804038Z – 2500 cells drawn in 10.977ms from 2022-09-16T14:49:40.732508Z to 2022-09-16T14:49:40.743485Z
```

```
2022-09-16T20:30:33.096808Z – Pause on each step (100ms) -> OFF
2022-09-16T20:30:33.102853Z – Update top row only -> OFF
2022-09-16T20:30:33.103007Z – Enable grid generations -> ON 
2022-09-16T20:30:33.103624Z – Enable animations -> OFF
2022-09-16T20:30:33.104915Z – Highlight recompositions -> OFF
2022-09-16T20:30:33.105064Z – Force top-level recomposition -> OFF
2022-09-16T20:30:33.105229Z – Force row-level recomposition -> OFF
2022-09-16T20:30:33.105360Z – Force cell-level recomposition -> OFF
2022-09-16T20:30:33.105528Z – Draw cell text -> ON 
2022-09-16T20:30:33.106031Z – Track drawing -> ON 
2022-09-16T20:30:33.111206Z – Hide grid temporarily when switching animations -> OFF
2022-09-16T20:30:33.111405Z – Enable BoxWithConstraints per row -> ON 
2022-09-16T20:30:33.431395Z – Grid -> null
2022-09-16T20:30:39.280197Z – Grid -> 50x50 (2500 cells)
2022-09-16T20:30:42.440878Z – 70000 cells drawn in 2.566414s from 2022-09-16T20:30:38.831075Z to 2022-09-16T20:30:41.397489Z
2022-09-16T20:30:43.905899Z – 5000 cells drawn in 146.622ms from 2022-09-16T20:30:42.706446Z to 2022-09-16T20:30:42.853068Z
2022-09-16T20:30:47.736127Z – 5000 cells drawn in 38.512ms from 2022-09-16T20:30:46.642317Z to 2022-09-16T20:30:46.680829Z
2022-09-16T20:30:51.865189Z – Enable animations -> ON 
2022-09-16T20:30:56.257282Z – 30000 cells drawn in 3.385817s from 2022-09-16T20:30:51.801650Z to 2022-09-16T20:30:55.187467Z
2022-09-16T20:30:58.027452Z – Enable animations -> OFF
2022-09-16T20:31:00.861822Z – 40000 cells drawn in 1.787853s from 2022-09-16T20:30:57.981453Z to 2022-09-16T20:30:59.769306Z
2022-09-16T20:31:04.380768Z – Enable animations -> ON 
2022-09-16T20:31:08.003942Z – 27500 cells drawn in 2.589024s from 2022-09-16T20:31:04.344746Z to 2022-09-16T20:31:06.933770Z
2022-09-16T20:31:10.589958Z – Enable animations -> OFF
2022-09-16T20:31:13.257392Z – 37500 cells drawn in 1.620087s from 2022-09-16T20:31:10.544630Z to 2022-09-16T20:31:12.164717Z
2022-09-16T20:31:16.887130Z – Enable animations -> ON 
2022-09-16T20:31:20.185150Z – 30000 cells drawn in 2.322666s from 2022-09-16T20:31:16.849974Z to 2022-09-16T20:31:19.172640Z
2022-09-16T20:31:23.023718Z – Enable animations -> OFF
2022-09-16T20:31:25.693851Z – 37500 cells drawn in 1.624419s from 2022-09-16T20:31:22.978021Z to 2022-09-16T20:31:24.602440Z
2022-09-16T20:31:32.673169Z – Enable grid generations -> OFF
2022-09-16T20:31:34.241693Z – 65000 cells drawn in 1.981797s from 2022-09-16T20:31:31.215742Z to 2022-09-16T20:31:33.197539Z
2022-09-16T20:31:35.448665Z – 2500 cells drawn in 19.924ms from 2022-09-16T20:31:34.386143Z to 2022-09-16T20:31:34.406067Z
2022-09-16T20:31:38.023828Z – Enable animations -> ON 
2022-09-16T20:31:41.629577Z – 27500 cells drawn in 2.567896s from 2022-09-16T20:31:37.984630Z to 2022-09-16T20:31:40.552526Z
2022-09-16T20:31:43.317156Z – Enable animations -> OFF
2022-09-16T20:31:46.010440Z – 37500 cells drawn in 1.697336s from 2022-09-16T20:31:43.244736Z to 2022-09-16T20:31:44.942072Z
2022-09-16T20:31:48.058349Z – Enable animations -> ON 
2022-09-16T20:31:51.282466Z – 27500 cells drawn in 2.267995s from 2022-09-16T20:31:47.997990Z to 2022-09-16T20:31:50.265985Z
2022-09-16T20:31:53.455908Z – Enable animations -> OFF
2022-09-16T20:31:56.116861Z – 30000 cells drawn in 1.677276s from 2022-09-16T20:31:53.407931Z to 2022-09-16T20:31:55.085207Z
2022-09-16T20:32:01.146693Z – 2500 cells drawn in 12.92ms from 2022-09-16T20:32:00.065087Z to 2022-09-16T20:32:00.078007Z
```
