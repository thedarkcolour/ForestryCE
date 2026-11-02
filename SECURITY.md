# Security Policy

Forestry: Community Edition (ForestryCE) is a community-maintained Minecraft mod. We take security reports seriously, especially issues that could affect multiplayer servers, player data, or world integrity.

## Supported Versions

Security fixes are prioritized for the **latest development build of each officially maintained Minecraft and mod-loader target**. Older builds do not automatically receive backported security fixes; updating to the latest compatible build is recommended.

| Minecraft version | Mod loader | Status |
| --- | --- | --- |
| 1.21.1 | NeoForge | Supported (active development) |
| 1.20.1 | Forge | Supported |
| 1.19.2 | Forge | Supported |

Development and pre-release builds (for example `3.0.0-alpha`) may receive fixes at the maintainers' discretion. See the [README](README.md), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/forestry-community-edition) and [Modrinth](https://modrinth.com/mod/forestry) listings for the current support matrix and available versions.

## Reporting a Vulnerability

**Please do not report suspected security vulnerabilities through public GitHub issues, pull requests, discussions, or Discord channels.** Public reports may expose affected servers and players before a fix is available.

Instead, submit a confidential report using GitHub's **[Report a vulnerability](https://github.com/thedarkcolour/ForestryCE/security/advisories/new)** feature. This allows maintainers to investigate and discuss the issue privately.

Please include as much of the following as you can:

- A concise description of the vulnerability and its potential impact.
- The affected ForestryCE version, Minecraft version, and mod loader (including loader version if known).
- Whether the issue affects dedicated servers, integrated servers, clients, or a particular configuration.
- Reproduction steps or a minimal proof of concept, including any required permissions or other mods.
- Relevant logs, screenshots, or code references, with private information removed.
- Whether the vulnerability has already been disclosed or is known to affect other projects.

You do not need a complete exploit to submit a report. If you are unsure whether an issue is security-sensitive, reporting it privately is appropriate.

If GitHub's private reporting feature is unavailable, contact a maintainer privately through a contact method listed in the [README](README.md). Do not send exploit details to a public channel.

## What to Expect

1. **Acknowledgment:** We aim to acknowledge reports within **7 days**, although responses may take longer because ForestryCE is maintained by volunteers.
2. **Triage:** We will assess reproducibility, impact, affected versions, and whether the issue originates in ForestryCE. We may ask for more information or a minimal reproduction.
3. **Remediation:** Confirmed vulnerabilities will be prioritized according to their severity and practical impact. Fixes may be developed and tested privately to avoid premature disclosure.
4. **Release and disclosure:** When appropriate, we will publish a fixed build and a GitHub Security Advisory identifying affected versions, available fixes, and recommended mitigation steps.

These are goals, not guaranteed response or resolution deadlines. Reports that cannot be reproduced or are outside ForestryCE's scope may be closed with an explanation.

## Coordinated Disclosure

Please give maintainers a reasonable opportunity to investigate and address the issue before publishing exploit details. We ask reporters to coordinate the timing of public disclosure with the project maintainers.

We will avoid intentionally publishing sensitive technical details before a fix or coordinated disclosure. When an advisory is published, we can credit the reporter with their consent. A CVE identifier may be requested where appropriate, but is not guaranteed for every report.

## Responsible Security Research

- Test only on systems and Minecraft worlds you own or have explicit permission to assess.
- Avoid disrupting public servers, accessing other players' data, or performing destructive tests.
- Share only the information needed to demonstrate the issue, and redact credentials, tokens, IP addresses, and personal information from logs.
- Do not publish a working exploit or a public patch that reveals the vulnerability before coordinated disclosure.

## Scope

This policy applies to **official Forestry: Community Edition releases and code maintained in [thedarkcolour/ForestryCE](https://github.com/thedarkcolour/ForestryCE)**. It does not automatically cover the original Forestry project, unofficial forks, third-party addons, or unrelated Minecraft mods. If an issue originates in another project, please report it to that project's maintainers as well.

For ordinary bugs, crashes without a security impact, compatibility problems, or feature requests, please use the [public issue tracker](https://github.com/thedarkcolour/ForestryCE/issues/new/choose) instead.
