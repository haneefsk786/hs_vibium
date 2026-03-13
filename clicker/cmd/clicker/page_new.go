package main

import (
	"github.com/spf13/cobra"
)

func newPageNewCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "page-new [url]",
		Short: "Open a new browser page",
		Example: `  vibium page-new
  # Open a blank new page

  vibium page-new https://example.com
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
}
