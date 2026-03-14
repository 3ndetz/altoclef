# AutoClef — multiplayer [altoclef](https://github.com/gaucho-matrero/altoclef)

*Plays block game. Allows multiplayer and python-side scripting support via py5j endpoint.*

*Powered by Baritone.*

![Kill&loot](https://github.com/3ndetz/autoclef/assets/30196290/7377ec79-1c3d-493b-9a1d-5d701f19d9c9)

![qwenie](https://github.com/user-attachments/assets/64b98492-ceca-410f-b3bc-efbd8ea09dcb)

[<img src="https://img.shields.io/badge/Habr-%D0%A7%D0%B8%D1%82%D0%B0%D1%82%D1%8C-%23000000?style=for-the-badge&link=https://habr.com/ru/articles/812387&logo=habr&logoColor=%23FFFFFF&labelColor=%2365A3BE"/>](https://habr.com/ru/articles/812387/#SkyWarsBot)

A**l**toClef could perform a full minecraft walkthrough. A**u**toClef enables it for multiplayer.

> [!CAUTION]
> <details><summary>RepoCodeDisclaimer ⚠️</summary>
>
> The "code" presented in the repository is mostly for prototyping. It should not be considered as a sample, it can be useful only to those who will be interested in repeating my experience, and not for "seekers of other people's mistakes" =)
>
> Furthermore, my experience in Java at the time of writing was extremely small (zero) and improving over time, so bugs or very silly things are possible. Unlike my other repositories, where I was in a hurry and would not really want to receive negative feedback, here it is the opposite, please report if you see something potentially bad in the code, because I still know Java not at a high level and would be happy to understand my mistakes!
> </details>

> [!TIP]
> This mod is an interface designed for use with an autonomous virtual streamer. It has a Python-callback interface to connect with Python-app and some functions to get info from the game to pass them to a streamer agent. Also it has a rich improved command system, compatible for using with LLM agent.

## Features

<details><summary>Features added on top of the orig AltoClef</summary>

- Smooth mouse look (for multiplayer anticheats killaura bypass)
  - buggy, but working!
- New tasks
  - SkyWarsTask for playing SkyWars
    - supports teammates
  - ThePitTask and others in development
- Support for connecting Python-scripts using Py4J library
  - two-way interface: send position to Python, send commands to Java
- Multi-version support (1.16.5 — 1.21.1) via preprocessor

</details>

## Versions

Available on **fabric** `1.21` for now. NOT `1.21.1 or 1.21.X`, ONLY `1.21`!

See `Releases` section and find the one with `Latest` tag.

> [!IMPORTANT]
> After installing, remove any old baritone configurations. Preexisting baritone configs will interfere with altoclef.
> You **do not** need to add baritone to your `mods` folder — it is already included.

## Demo GIFs

<details><summary>SkyWars bot autoplay Demo GIFs</summary>

### Looting chests
![Looting chests](https://github.com/3ndetz/autoclef/assets/30196290/aa44993e-a7e8-4285-bba6-a690b0ac29a2)

### Gapple & EnderPearl
![Gapple & EnderPearl](https://github.com/3ndetz/autoclef/assets/30196290/0d3e73d2-2e1f-40e7-a53b-be43d3d9335d)

### Kill & Loot
![Kill & Loot](https://github.com/3ndetz/autoclef/assets/30196290/7377ec79-1c3d-493b-9a1d-5d701f19d9c9)

### Bow master
![Bow](https://github.com/3ndetz/autoclef/assets/30196290/9bae7aee-f535-4704-83a3-3dd9ec885a80)

</details>


## Fork History

**[GauchoMatrero/altoclef](https://github.com/gaucho-matrero/altoclef)** → **[MarvionKirito/altoclef](https://github.com/MarvionKirito/altoclef)** → **[MiranCZ/altoclef](https://github.com/MiranCZ/altoclef)** → **this fork**

[MiranCZ](https://github.com/MiranCZ) added multi-version support via the [ReplayMod preprocessor](https://github.com/ReplayMod/preprocessor) and fixed many bugs — this fork uses that as its base and adds multiplayer/SkyWars capabilities from [3ndetz/AutoClef](https://github.com/3ndetz/autoclef).

## TODOs

- [x] Merge the MiranCZ's altoclef and 3ndetz's autoclef branches and make it work
- [ ] Rename the old repo to autoclef-old and this repo to the autoclef Because now it is the AutoClef.
- [ ] Improve PVP and movement system to support [Tungsten](https://github.com/Hackerokuz/Tungsten/tree/server-side) project for EXTRA-complex task. Implement Tungsten into the AutoClef project
