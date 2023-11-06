import { instantiate } from './app.uninstantiated.mjs';

await wasmSetup;

instantiate({ skia: Module['asm'] });
