package main

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"github.com/vibium/clicker/internal/browser"
	"github.com/vibium/clicker/internal/paths"
)

var version = "0.1.0"

func main() {
	rootCmd := &cobra.Command{
		Use:   "clicker",
		Short: "Browser automation for AI agents and humans",
		Run: func(cmd *cobra.Command, args []string) {
			cmd.Help()
		},
	}

	rootCmd.AddCommand(&cobra.Command{
		Use:   "version",
		Short: "Print the version number",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Printf("Clicker v%s\n", version)
		},
	})

	rootCmd.AddCommand(&cobra.Command{
		Use:   "paths",
		Short: "Print browser and cache paths",
		Run: func(cmd *cobra.Command, args []string) {
			cacheDir, err := paths.GetCacheDir()
			if err != nil {
				fmt.Printf("Cache directory: error: %v\n", err)
			} else {
				fmt.Printf("Cache directory: %s\n", cacheDir)
			}

			chromePath, err := paths.GetChromeExecutable()
			if err != nil {
				fmt.Println("Chrome: not found")
			} else {
				fmt.Printf("Chrome: %s\n", chromePath)
			}

			chromedriverPath, err := paths.GetChromedriverPath()
			if err != nil {
				fmt.Println("Chromedriver: not found")
			} else {
				fmt.Printf("Chromedriver: %s\n", chromedriverPath)
			}
		},
	})

	rootCmd.AddCommand(&cobra.Command{
		Use:   "install",
		Short: "Download Chrome for Testing and chromedriver",
		Run: func(cmd *cobra.Command, args []string) {
			result, err := browser.Install()
			if err != nil {
				fmt.Fprintf(os.Stderr, "Error: %v\n", err)
				os.Exit(1)
			}

			fmt.Println("Installation complete!")
			fmt.Printf("Chrome: %s\n", result.ChromePath)
			fmt.Printf("Chromedriver: %s\n", result.ChromedriverPath)
			fmt.Printf("Version: %s\n", result.Version)
		},
	})

	rootCmd.Version = version
	rootCmd.SetVersionTemplate("Clicker v{{.Version}}\n")

	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
