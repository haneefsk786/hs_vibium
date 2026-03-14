package main

import (
	"github.com/spf13/cobra"
)

func newCookiesCmd() *cobra.Command {
	cookiesCmd := &cobra.Command{
		Use:   "cookies [name] [value]",
		Short: "Manage browser cookies",
		Example: `  vibium cookies
  # List all cookies

  vibium cookies "session" "abc123"
  # Set a cookie with name and value`,
		Args: cobra.RangeArgs(0, 2),
		Run: func(cmd *cobra.Command, args []string) {
			if len(args) == 2 {
				// Set cookie
				result, err := daemonCall("browser_set_cookie", map[string]interface{}{
					"name":  args[0],
					"value": args[1],
				})
				if err != nil {
					printError(err)
					return
				}
				printResult(result)
				return
			}

			// Get cookies
			result, err := daemonCall("browser_get_cookies", map[string]interface{}{})
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}

	clearCmd := &cobra.Command{
		Use:   "clear",
		Short: "Clear all cookies",
		Example: `  vibium cookies clear
  # Delete all cookies`,
		Args: cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			result, err := daemonCall("browser_delete_cookies", map[string]interface{}{})
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}

	cookiesCmd.AddCommand(clearCmd)
	return cookiesCmd
}
