# vShulkerBox

vShulkerBox is a simple and powerful Minecraft plugin that allows you to open Shulker Boxes directly from your inventory without placing them. It offers high configurability and is 99.99% dupe-proof, making it a must-have for any server!

## Features

- **Open Shulker Boxes from Inventory**: Open your ShulkerBox without placing it on the ground.
- **Virtual ShulkerBox**: To prevent item duplication, the items inside the real ShulkerBox are temporarily removed and stored in a virtual ShulkerBox. Even in case of server crashes, the plugin automatically restores your items.
- **Highly Configurable**: You can adjust the plugin to suit your needs. For example, you can enable the option to open ShulkerBoxes only with Shift + Right Click, or activate the "Anywhere" feature, which lets you open the ShulkerBox by right-clicking anywhere in the inventory without even being in-game.
- **Open Source**: vShulkerBox is fully open-source and built with security in mind.

## How It Works

1. **Click to Open**: When you click on a ShulkerBox, an identical GUI opens showing all your items.
2. **Anti-Dupe Mechanism**: The real items in the ShulkerBox are temporarily removed, preventing duplication, and stored in a Virtual-ShulkerBox.
3. **Automatic Restoration**: Even if the server crashes, the ShulkerBox automatically helps you restore all your items.

## Configuration

vShulkerBox offers a variety of configuration options to fit your preferences:

- **Shift + Right Click**: You can enable this setting to allow ShulkerBoxes to be opened only with Shift + Right Click.
- **'Anywhere' Feature**: Activate the "Anywhere" feature, allowing you to open the ShulkerBox by right-clicking anywhere in your inventory without needing to be in-game!

## Build Instructions

To build this plugin, you will need to have Gradle installed. Follow the steps below to compile the plugin:

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/vShulkerBox.git
2. Navigate to the project directory:
   ```bash
   cd vShulkerBox
3. Run the Gradle build command:
  ```bash
  ./gradlew build
```
4. After the build completes, the .jar file can be found in the build/libs/ directory. This file is the plugin you can install on your Minecraft server.

[LICENSE](LICENSE)
