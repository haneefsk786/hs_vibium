package main

import (
	"fmt"
	"os"
	"strconv"

	"github.com/spf13/cobra"
)

func newViewportCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "viewport [width] [height]",
		Short: "Get or set the browser viewport size",
		Example: `  vibium viewport
  # {"width":1280,"height":720,"devicePixelRatio":1}

  vibium viewport 1280 720
  # Set viewport to 1280x720

  vibium viewport 375 812 --dpr 3
  # Simulate iPhone X viewport`,
		Args: cobra.RangeArgs(0, 2),
		Run: func(cmd *cobra.Command, args []string) {
			if len(args) == 0 {
				// Get viewport
				result, err := daemonCall("browser_get_viewport", map[string]interface{}{})
				if err != nil {
					printError(err)
					return
				}
				printResult(result)
				return
			}

			if len(args) == 1 {
				fmt.Fprintf(os.Stderr, "Error: provide both width and height\n")
				os.Exit(1)
			}

			// Set viewport
			width, err := strconv.Atoi(args[0])
			if err != nil {
				fmt.Fprintf(os.Stderr, "Error: invalid width: %s\n", args[0])
				os.Exit(1)
			}
			height, err := strconv.Atoi(args[1])
			if err != nil {
				fmt.Fprintf(os.Stderr, "Error: invalid height: %s\n", args[1])
				os.Exit(1)
			}

			dpr, _ := cmd.Flags().GetFloat64("dpr")

			callArgs := map[string]interface{}{
				"width":  float64(width),
				"height": float64(height),
			}
			if dpr > 0 {
				callArgs["devicePixelRatio"] = dpr
			}
			result, err := daemonCall("browser_set_viewport", callArgs)
			if err != nil {
				printError(err)
				return
			}
			printResult(result)
		},
	}
	cmd.Flags().Float64("dpr", 0, "Device pixel ratio (e.g., 2 for Retina)")
	return cmd
}
