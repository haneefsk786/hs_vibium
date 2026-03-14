package main

import (
	"fmt"
	"os"
	"strconv"

	"github.com/spf13/cobra"
)

func newPageCmd() *cobra.Command {
	pageCmd := &cobra.Command{
		Use:   "page",
		Short: "Manage browser pages (new, close, switch)",
		Args:  cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			cmd.Help()
		},
	}

	newCmd := &cobra.Command{
		Use:   "new [url]",
		Short: "Open a new browser page",
		Example: `  vibium page new
  # Open a blank new page

  vibium page new https://example.com
  # Open a new page and navigate to URL`,
		Args: cobra.MaximumNArgs(1),
		Run: func(cmd *cobra.Command, args []string) {
			toolArgs := map[string]interface{}{}
			if len(args) == 1 {
				toolArgs["url"] = args[0]
			}

			result, err := daemonCall("browser_new_page", toolArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}

	closeCmd := &cobra.Command{
		Use:   "close [index]",
		Short: "Close a browser page by index (default: current page)",
		Example: `  vibium page close
  # Close current page (index 0)

  vibium page close 1
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

	switchCmd := &cobra.Command{
		Use:   "switch [index or url]",
		Short: "Switch to a browser page by index or URL substring",
		Example: `  vibium page switch 1
  # Switch to page at index 1

  vibium page switch google.com
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

	pageCmd.AddCommand(newCmd)
	pageCmd.AddCommand(closeCmd)
	pageCmd.AddCommand(switchCmd)
	return pageCmd
}
