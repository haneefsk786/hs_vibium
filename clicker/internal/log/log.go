// Package log provides structured logging for the clicker library.
package log

import (
	"io"
	"log/slog"
	"os"
)

// Level represents the logging level.
type Level int

const (
	LevelQuiet   Level = iota // No logging (default)
	LevelVerbose              // All logging (--verbose)
)

var logger *slog.Logger

func init() {
	// Default to quiet - no logs unless --verbose is used
	Setup(LevelQuiet)
}

// Setup configures the global logger with the specified level.
func Setup(level Level) {
	var handler slog.Handler

	switch level {
	case LevelVerbose:
		handler = slog.NewJSONHandler(os.Stderr, &slog.HandlerOptions{
			Level: slog.LevelDebug,
		})
	default: // LevelQuiet
		// Discard all logs
		handler = slog.NewJSONHandler(io.Discard, nil)
	}

	logger = slog.New(handler)
	slog.SetDefault(logger)
}

// Debug logs at debug level.
func Debug(msg string, args ...any) {
	logger.Debug(msg, args...)
}

// Info logs at info level.
func Info(msg string, args ...any) {
	logger.Info(msg, args...)
}

// Warn logs at warn level.
func Warn(msg string, args ...any) {
	logger.Warn(msg, args...)
}

// Error logs at error level.
func Error(msg string, args ...any) {
	logger.Error(msg, args...)
}

// With returns a logger with additional context.
func With(args ...any) *slog.Logger {
	return logger.With(args...)
}
