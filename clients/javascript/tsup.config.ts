import { defineConfig } from "tsup";

export default defineConfig([
  // Main entry
  {
    entry: ["src/index.ts"],
    format: ["cjs", "esm"],
    dts: true,
    clean: true,
  },
  // Worker entry (CJS only, bundled standalone)
  {
    entry: ["src/sync/worker.ts"],
    format: ["cjs"],
    outDir: "dist",
    clean: false, // Don't clean, main build already did
    noExternal: [/.*/], // Bundle all dependencies
  },
]);
