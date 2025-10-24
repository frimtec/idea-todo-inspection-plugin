# Idea TODO Inspection Plugin

[![JetBrains Plugins][jetbrains-plugin-release-shield]][jetbrains-plugin]
![Plugin Downloads][jetbrains-plugin-download-shield]

![Project Maintenance][maintenance-shield]
[![License][license-shield]][license]

[![Build Status][build-status-shield]][build-status]
[![Deploy Status][deploy-status-shield]][deploy-status]

![Icon](plugin/src/main/resources/META-INF/pluginIcon.svg)

## 💡 Overview

<!-- Plugin description -->
The `TODO Inspection Plugin` is an IntelliJ-IDEA plugin, designed to enhance the standard TODO inspection mechanism by
integrating with a Jira ticketing system.

It solves a common problem that developers leaving `TODO`-comments in the code, referencing tickets, only for that
ticket to be closed later, leaving the technical debt forgotten. 
This plugin ensures that your `TODO`'s are always relevant and actionable, surfacing warnings when a linked ticket is already complete.
<!-- Plugin description end -->

## ✨ Features

This plugin provides real-time feedback by marking `TODO` or `FIXME` comments in the editor if:
* No Jira ticket is referenced from the `TODO` comment.
* The referenced Jira ticket is already closed.
* Jira is not accessible due to configuration issues or system unavailability.

![Warnings](images/warning.png)

Examples:

| Code Comment                                 | Status in JIRA | Plugin Behavior                                           |
|----------------------------------------------|----------------|-----------------------------------------------------------|
| `// TODO PROJECT-123 Fix race condition`     | In Progress    | No Warning                                                |
| `// TODO PROJECT-123 Fix race condition`     | Closed         | Warning: `TODO references a ticket which is already done` |
| `// TODO PROJECT-1239999 Fix race condition` | n/a            | Warning:  `TODO references a ticket that does not exist`  |
| `// TODO Update API`                         | n/a            | Warning: `TODO does not reference a ticket`               |

### ⬇️ Installation
- Using IDE built-in plugin system:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Todo Inspection"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:
  Download the [latest release][latest-release] and install it manually using
  <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>⚙</kbd> > <kbd>Install plugin from disk...</kbd>

### 🛠️ Configuration

The configuration is done under <kbd>Settings</kbd> > <kbd>Editor</kbd> > <kbd>Inspections</kbd> > <kbd>General</kbd> > <kbd>TODO comment (enhanced)</kbd>:
![Configuration](images/config.png)

The following settings are available:

| Setting            | Comment                                                                                               | Default Value        |
|--------------------|-------------------------------------------------------------------------------------------------------|----------------------|
| Allow FIXME        | If unchecked all `FIXME`'s are marked as warning, otherwise `FIXME`'s are handled the same way as `TODO`'s. | `unchecked`          |
| Jira URL           | URL of your Jira instance.                                                                            |                      |
| Jira Username      | Username of a Jira account (requires read access to tickets only).                                    |                      |
| Jira API-Token     | API token or password of used Jira account.                                                           |                      |
| Jira Project Keys  | Comma seperated list of all Jira project IDs to be used.                                              |                      |
| Jira Closed States | Comma seperated list of Jira states treated as closed.                                                | Closed,Done,Resolved |

[license-shield]: https://img.shields.io/github/license/frimtec/idea-todo-inspection-plugin.svg
[license]: https://opensource.org/licenses/Apache-2.0
[maintenance-shield]: https://img.shields.io/maintenance/yes/2025.svg
[build-status-shield]: https://github.com/frimtec/idea-todo-inspection-plugin/workflows/Build/badge.svg
[build-status]: https://github.com/frimtec/idea-todo-inspection-plugin/actions?query=workflow%3ABuild
[deploy-status-shield]: https://github.com/frimtec/idea-todo-inspection-plugin/actions/workflows/release.yml/badge.svg
[deploy-status]: https://github.com/frimtec/idea-todo-inspection-plugin/actions/workflows/release.yml
[jetbrains-plugin-release-shield]: https://img.shields.io/jetbrains/plugin/v/28829
[jetbrains-plugin-download-shield]: https://img.shields.io/jetbrains/plugin/d/28829
[jetbrains-plugin]: https://plugins.jetbrains.com/plugin/28829-todo-inspection
[latest-release]: https://github.com/frimtec/idea-todo-inspection-plugin/releases/latest