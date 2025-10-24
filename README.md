# Idea TODO Inspection Plugin
[![JetBrains Plugins][jetbrains-plugin-release-shield]][jetbrains-plugin]
![Plugin Downloads][jetbrains-plugin-download-shield]

![Project Maintenance][maintenance-shield]
[![License][license-shield]][license]

[![Build Status][build-status-shield]][build-status]
[![Deploy Status][deploy-status-shield]][deploy-status]

<!-- Plugin description -->
## 💡 Overview

The Idea TODO Inspection Plugin is a IntelliJ IDEA plugin designed to enhance the standard TODO inspection mechanism by integrating directly with your JIRA instance.

It solves a common problem: developers leaving ```// TODO comments``` in code, referencing a JIRA ticket, only for that ticket to be closed later, leaving the technical debt forgotten. This plugin ensures that your TODOs are always relevant and actionable, surfacing errors when a linked ticket is already complete.

## ✨ Features

* This plugin provides proactive feedback right in your editor:
** JIRA Ticket Validation: Automatically inspects your TODO comments to detect and validate structured JIRA ticket keys (e.g., PROJECT-123).
** Stale TODO Detection: Checks the status of the referenced JIRA ticket using the JIRA API.

* Real-time Error Highlighting:
** Highlights TODOs if the referenced JIRA ticket is marked as Closed, Done, or any other configured "final" status.
** Highlights TODOs if the ticket reference is syntactically invalid.
<!-- Plugin description end -->

## ⬇️ Installation
TODO

## 🛠️ Configuration
TODO

## ✍️ Usage

The inspector looks for TODO or FIXME comments containing a valid JIRA project key and number, such as: 
```// TODO PROJECT-45 Refactor this class before launching new feature X.```

Examples:
| Status in JIRA | Code Comment | Plugin Behavior |
| :--- | :---: | ---: |
| In Progress | // TODO PROJECT-123 Fix race condition | No Warning |
| Closed | // TODO PROJECT-123 Fix race condition | Warning Highlighted. (Stale debt) |
| Not existing | // TODO PROJECT-1239999 Fix race condition | Warning Highlighted. (Not found debt) |
| Invalid Key | // TODO PROJ_123 Update API | Warning Highlighted. (Invalid format) |
| No Key | // TODO Update API | Warning Highlighted. (Invalid format) |

When an issue is detected, the line will be underlined in the editor, and the problem will appear in the Problems tool window.

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