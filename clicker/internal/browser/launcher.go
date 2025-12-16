package browser

import (
	"bufio"
	"fmt"
	"io"
	"os/exec"
	"regexp"
	"strings"

	"github.com/vibium/clicker/internal/paths"
	"github.com/vibium/clicker/internal/process"
)

// LaunchOptions contains options for launching Chrome.
type LaunchOptions struct {
	Headless bool
}

// LaunchResult contains the result of launching Chrome.
type LaunchResult struct {
	WebSocketURL string
	Cmd          *exec.Cmd
}

// LaunchChrome launches Chrome with BiDi flags and returns the WebSocket URL.
func LaunchChrome(opts LaunchOptions) (*LaunchResult, error) {
	chromePath, err := paths.GetChromeExecutable()
	if err != nil {
		return nil, fmt.Errorf("Chrome not found: %w", err)
	}

	args := []string{
		"--remote-debugging-port=0",
		"--no-first-run",
		"--no-default-browser-check",
		"--disable-background-networking",
		"--disable-background-timer-throttling",
		"--disable-backgrounding-occluded-windows",
		"--disable-breakpad",
		"--disable-component-extensions-with-background-pages",
		"--disable-component-update",
		"--disable-default-apps",
		"--disable-dev-shm-usage",
		"--disable-extensions",
		"--disable-features=TranslateUI",
		"--disable-hang-monitor",
		"--disable-ipc-flooding-protection",
		"--disable-popup-blocking",
		"--disable-prompt-on-repost",
		"--disable-renderer-backgrounding",
		"--disable-sync",
		"--enable-features=NetworkService,NetworkServiceInProcess",
		"--force-color-profile=srgb",
		"--metrics-recording-only",
		"--password-store=basic",
		"--use-mock-keychain",
		"--enable-blink-features=IdleDetection",
	}

	if opts.Headless {
		args = append(args, "--headless=new")
	}

	// Add a blank page to start
	args = append(args, "about:blank")

	cmd := exec.Command(chromePath, args...)

	// Capture stderr to find the WebSocket URL
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return nil, fmt.Errorf("failed to get stderr pipe: %w", err)
	}

	if err := cmd.Start(); err != nil {
		return nil, fmt.Errorf("failed to start Chrome: %w", err)
	}

	// Parse stderr for the DevTools WebSocket URL
	wsURL, err := parseWebSocketURL(stderr)
	if err != nil {
		cmd.Process.Kill()
		return nil, fmt.Errorf("failed to get WebSocket URL: %w", err)
	}

	// Track the process for cleanup
	process.Track(cmd)

	return &LaunchResult{
		WebSocketURL: wsURL,
		Cmd:          cmd,
	}, nil
}

// parseWebSocketURL reads from stderr and extracts the DevTools WebSocket URL.
func parseWebSocketURL(stderr io.Reader) (string, error) {
	// DevTools listening on ws://127.0.0.1:XXXXX/devtools/browser/GUID
	wsRegex := regexp.MustCompile(`DevTools listening on (ws://[^\s]+)`)

	scanner := bufio.NewScanner(stderr)
	for scanner.Scan() {
		line := scanner.Text()
		if matches := wsRegex.FindStringSubmatch(line); matches != nil {
			return matches[1], nil
		}
		// Also check for errors
		if strings.Contains(line, "error") || strings.Contains(line, "Error") {
			// Continue scanning, errors might be non-fatal
		}
	}

	if err := scanner.Err(); err != nil {
		return "", err
	}

	return "", fmt.Errorf("WebSocket URL not found in Chrome output")
}
