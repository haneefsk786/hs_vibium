# Plan: README Improvements

## 1. Add hero GIF

Record a short (~10s) terminal GIF showing:
```
vibium go https://var.parts && vibium map && vibium click @e1 && vibium diff map
```

Place at top of README, right after the tagline and before "Why Vibium?". Use a simple `![demo](docs/assets/demo.gif)`.

Tool: `vhs` (Charm CLI) or `asciinema` + `agg` for terminal recording, or screen capture of the actual browser + terminal side by side.


## 5. Add inline quick start

Add a 3-line "try it now" block right after "New here?" so people can copy-paste without reading a tutorial:

```markdown
**Try it now:**
```bash
npm install -g vibium
vibium go https://var.parts && vibium map
```

---

## Priority

| # | Change | Effort | Impact |
|---|--------|--------|--------|
| 1 | Hero GIF | 30 min | Biggest visual impact |
| 2 | Inline quick start | 5 min | Reduces time to "aha" |
