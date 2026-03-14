package main

import (
	"time"

	"github.com/spf13/cobra"
	"github.com/vibium/clicker/internal/api"
)

func newWaitCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "wait [selector]",
		Short: "Wait for an element, URL, text, page load, or JS condition",
		Example: `  vibium wait "div.loaded"
  # Wait for element to exist in DOM

  vibium wait "div.loaded" --state visible
  # Wait for element to be visible

  vibium wait "div.spinner" --state hidden --timeout 5000
  # Wait for spinner to disappear`,
		Args: cobra.ExactArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			selector := args[0]
			state, _ := cmd.Flags().GetString("state")
			timeoutMs, _ := cmd.Flags().GetInt("timeout")

			toolArgs := map[string]interface{}{
				"selector": selector,
				"state":    state,
				"timeout":  float64(timeoutMs),
			}

			result, err := daemonCall("browser_wait", toolArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
	cmd.Flags().String("state", "attached", "State to wait for: attached, visible, hidden")
	cmd.Flags().Int("timeout", int(api.DefaultTimeout/time.Millisecond), "Timeout in milliseconds")

	urlCmd := &cobra.Command{
		Use:   "url [pattern]",
		Short: "Wait until the page URL contains a substring",
		Example: `  vibium wait url "/dashboard"
  # Wait until URL contains "/dashboard"

  vibium wait url "success" --timeout 10000
  # Wait up to 10 seconds`,
		Args: cobra.ExactArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			pattern := args[0]
			timeout, _ := cmd.Flags().GetInt("timeout")

			toolArgs := map[string]interface{}{"pattern": pattern}
			if cmd.Flags().Changed("timeout") {
				toolArgs["timeout"] = float64(timeout)
			}

			result, err := daemonCall("browser_wait_for_url", toolArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
	urlCmd.Flags().Int("timeout", 30000, "Timeout in milliseconds")

	textCmd := &cobra.Command{
		Use:   "text [text]",
		Short: "Wait until text appears on the page",
		Example: `  vibium wait text "Welcome"
  # Waits until "Welcome" appears on the page

  vibium wait text "Success" --timeout 10000
  # Wait with custom timeout (10 seconds)`,
		Args: cobra.ExactArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			text := args[0]
			timeout, _ := cmd.Flags().GetFloat64("timeout")

			callArgs := map[string]interface{}{"text": text}
			if timeout > 0 {
				callArgs["timeout"] = timeout
			}
			result, err := daemonCall("browser_wait_for_text", callArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
	textCmd.Flags().Float64("timeout", 30000, "Timeout in milliseconds")

	loadCmd := &cobra.Command{
		Use:   "load",
		Short: "Wait until the page is fully loaded",
		Example: `  vibium wait load
  # Wait until document.readyState is "complete"

  vibium wait load --timeout 10000
  # Wait up to 10 seconds`,
		Args: cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			timeout, _ := cmd.Flags().GetInt("timeout")

			toolArgs := map[string]interface{}{}
			if cmd.Flags().Changed("timeout") {
				toolArgs["timeout"] = float64(timeout)
			}

			result, err := daemonCall("browser_wait_for_load", toolArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
	loadCmd.Flags().Int("timeout", 30000, "Timeout in milliseconds")

	fnCmd := &cobra.Command{
		Use:   "fn [expression]",
		Short: "Wait until a JS expression returns truthy",
		Example: `  vibium wait fn "document.readyState === 'complete'"
  # Wait for page to be fully loaded

  vibium wait fn "window.ready === true" --timeout 10000
  # Wait for custom condition with timeout`,
		Args: cobra.ExactArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			expression := args[0]
			timeout, _ := cmd.Flags().GetFloat64("timeout")

			callArgs := map[string]interface{}{"expression": expression}
			if timeout > 0 {
				callArgs["timeout"] = timeout
			}
			result, err := daemonCall("browser_wait_for_fn", callArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
	fnCmd.Flags().Float64("timeout", 30000, "Timeout in milliseconds")

	cmd.AddCommand(urlCmd)
	cmd.AddCommand(textCmd)
	cmd.AddCommand(loadCmd)
	cmd.AddCommand(fnCmd)
	return cmd
}
