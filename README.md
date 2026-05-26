# Go Board Game

A fully playable 19×19 Go board game built in Java using the [Processing](https://processing.org) library.

![logo](logo.png)

## Overview

This project implements the ancient strategy game of Go as a standalone desktop application. It enforces Chinese ruleset scoring, handles all edge-case game logic (ko, suicide, group capture), and renders smooth 3D-style stones with real-time hover feedback — built on top of Processing's Java2D rendering pipeline.

## Features

- **Chinese rules** — area scoring (stones + territory) with 7.5 komi for White
- **Capture detection** — BFS flood-fill liberty checking for individual stones and connected groups
- **Ko rule** — rejects moves that recreate the previous board position
- **Suicide prevention** — illegal self-capture moves are blocked before placement
- **Pass & Reset** — pass your turn or restart the game at any time
- **Game end** — two consecutive passes trigger automatic scoring and declare a winner
- **Territory visualisation** — empty intersections are marked at game end to show each player's scored regions
- **3D stone rendering** — each stone is drawn with shadow, radial gradient, and specular highlight layers
- **Hover preview** — semi-transparent ghost stone follows the cursor for precise placement
- **App icon** — custom logo displayed on the window, taskbar, and macOS Dock

## Tech

- **Language:** Java 17
- **Rendering:** [Processing](https://processing.org) (PApplet / Java2D)

## Requirements

Java 17 or later

## Running

Download `Go.jar` from [Releases](https://github.com/TheYellowDuck/go-board-game/releases) and run:

```sh
java -jar Go.jar
```

## Building from Source

```sh
javac --release 17 -cp "lib/core.jar:lib/gluegen-rt.jar:lib/jogl-all.jar" -d bin src/Go/Go.java src/Go/Sketch.java
```
