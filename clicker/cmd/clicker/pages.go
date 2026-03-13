package main

import (
	"github.com/spf13/cobra"
)

func newPagesCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "pages",
		Short: "List all open browser pages",
		Example: `  vibium pages
  # [0] https://example.com
  # [1] https://google.com`,
		Args: cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			result, err := daemonCall("browser_list_pages", map[string]interface{}{})
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
}
