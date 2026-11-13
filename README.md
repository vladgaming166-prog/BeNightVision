# BeNightVision

A Paper plugin that grants permanent Night Vision to players.

- **Name:** BeNightVision
- **Version:** 1.0.0
- **Author:** Besina
- **Main class:** `ro.besina.benightvision.BeNightVision`
- **API:** Paper
- **Java:** 21

## Compatibility

The plugin is compiled against **Paper 1.21.11** (`paper-api 1.21.11-R0.1-SNAPSHOT`) with `api-version: 1.20`.

Supported runtimes:

| Server | Status |
| --- | --- |
| Paper 1.20.x | Supported (Bukkit/Paper API only, no NMS) |
| Paper 1.21.x, including 1.21.1 and 1.21.11 | Supported |
| Minecraft / Paper **1.26.3** | Does not exist |
| Paper **26.3** | The current Paper line after 1.21.11. The plugin JAR is Java 21 bytecode and uses only stable Bukkit potion APIs, so it can load on Paper 26.3 running **Java 25**. Paper 26.3 itself cannot be compiled against with Java 21. |

Minecraft switched from `1.21.x` to year-based versions in 2026 (`26.1`, `26.2`, `26.3`). There is no `1.26.3` release.

## Build

```bash
mvn clean package
```

The compiled plugin is written to:

```text
target/BeNightVision-1.0.0.jar
```

## Install

1. Build the plugin or use the generated JAR.
2. Copy `target/BeNightVision-1.0.0.jar` into your Paper server's `plugins` folder.
3. Start or restart the server.
4. Edit `plugins/BeNightVision/config.yml` if you want custom messages or prefix colors.
5. Use `/bnv` in-game or from the console.

## Commands

`/bnv` and `/benightvision` do the same thing.

| Command | Description |
| --- | --- |
| `/bnv` | Show help |
| `/bnv on all` | Enable Night Vision for every online player and persist global mode |
| `/bnv on <player>` | Enable Night Vision for one online player |
| `/bnv off all` | Disable Night Vision for every online player and persist global mode off |
| `/bnv off <player>` | Disable Night Vision for one online player |

Tab completion offers `on` / `off`, then `all` plus online player names.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `benightvision.admin` | OP | All commands |
| `benightvision.use` | true | View help |

## Global state

`/bnv on all` stores `global-night-vision: true` in `config.yml`. After a restart, every online player and every new join receives Night Vision until `/bnv off all` is used.
