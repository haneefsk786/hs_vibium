package main

import (
	"fmt"
	"os"
	"strconv"

	"github.com/spf13/cobra"
)

func newPageCloseCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "page-close [index]",
		Short: "Close a browser page by index (default: current page)",
		Example: `  vibium page-close
  # Close current page (index 0)

  vibium page-close 1
  # Close page at index 1`,
		Args: cobra.MaximumNArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			toolArgs := map[string]interface{}{}
			if len(args) == 1 {
				idx, err := strconv.Atoi(args[0])
				if err != nil {
					fmt.Fprintf(os.Stderr, "Error: invalid page index: %s\n", args[0])
					os.Exit(1)
				}
				toolArgs["index"] = float64(idx)
			}

			result, err := daemonCall("browser_close_page", toolArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
}
