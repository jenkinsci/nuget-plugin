Nuget plugin
====================

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/nuget.svg)](https://plugins.jenkins.io/nuget)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/nuget.svg?color=blue)](https://plugins.jenkins.io/nuget)

## About this plugin

The Claim plugin is meant to integrated Nuget into Jenkins.

Supported features

* **trigger jobs** when a NuGet dependency is updated
* **publish packages** to NuGet repositories

## Usage

### Installing and enabling the plugin

The Nuget plugin can be installed from any Jenkins installation connected to the Internet using the **Plugin Manager** screen.

The path to `NuGet.exe` is configurable.
`NuGet.exe` uses config file in `%APPDATA%\Nuget\Nuget.config` (location depending on actual user used by Jenkins.

NuGet repositories can be configured in global configuration along with corresponding API keys. These repositories can then be used in jobs to publish packages.

## License

[MIT License](./LICENSE.md)

## More information

[Changelog](./CHANGELOG.md)