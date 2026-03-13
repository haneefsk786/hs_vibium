package main

import (
	"strconv"

	"github.com/spf13/cobra"
)

func newPageSwitchCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "page-switch [index or url]",
		Short: "Switch to a browser page by index or URL substring",
		Example: `  vibium page-switch 1
  # Switch to page at index 1

  vibium page-switch google.com
  # Switch to page containing "google.com" in URL`,
		Args: cobra.ExactArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			toolArgs := map[string]interface{}{}

			// Try to parse as integer index
			if idx, err := strconv.Atoi(args[0]); err == nil {
				toolArgs["index"] = float64(idx)
			} else {
				toolArgs["url"] = args[0]
			}

			result, err := daemonCall("browser_switch_page", toolArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
}
