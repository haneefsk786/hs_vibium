package process

import (
	"os"
	"os/exec"
	"os/signal"
	"sync"
	"syscall"
)

// Manager tracks and manages spawned browser processes.
type Manager struct {
	mu       sync.Mutex
	browsers []*exec.Cmd
}

// Global manager instance
var defaultManager = &Manager{}

// Track adds a browser process to be tracked.
func Track(cmd *exec.Cmd) {
	defaultManager.mu.Lock()
	defer defaultManager.mu.Unlock()
	defaultManager.browsers = append(defaultManager.browsers, cmd)
}

// Untrack removes a browser process from tracking.
func Untrack(cmd *exec.Cmd) {
	defaultManager.mu.Lock()
	defer defaultManager.mu.Unlock()
	for i, c := range defaultManager.browsers {
		if c == cmd {
			defaultManager.browsers = append(defaultManager.browsers[:i], defaultManager.browsers[i+1:]...)
			return
		}
	}
}

// KillAll terminates all tracked browser processes and their children.
func KillAll() {
	defaultManager.mu.Lock()
	defer defaultManager.mu.Unlock()
	for _, cmd := range defaultManager.browsers {
		if cmd.Process != nil {
			// Kill the entire process group to get all child processes
			pgid, err := syscall.Getpgid(cmd.Process.Pid)
			if err == nil {
				syscall.Kill(-pgid, syscall.SIGKILL)
			} else {
				cmd.Process.Kill()
			}
		}
	}
	defaultManager.browsers = nil
}

// KillBrowser terminates a specific browser process.
func KillBrowser(cmd *exec.Cmd) error {
	if cmd == nil || cmd.Process == nil {
		return nil
	}
	Untrack(cmd)
	return cmd.Process.Kill()
}

// SetupSignalHandler sets up cleanup on SIGINT/SIGTERM.
func SetupSignalHandler() {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-c
		KillAll()
		os.Exit(0)
	}()
}

// WaitForSignal blocks until SIGINT/SIGTERM is received, then cleans up.
func WaitForSignal() {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	<-c
	KillAll()
}
