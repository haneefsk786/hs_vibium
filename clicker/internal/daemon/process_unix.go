//go:build !windows

package daemon

import "syscall"

// ProcessExists checks if a process with the given PID exists.
func ProcessExists(pid int) bool {
	err := syscall.Kill(pid, 0)
	return err == nil || err == syscall.EPERM
}
