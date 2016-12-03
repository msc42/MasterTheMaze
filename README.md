# Master the maze #
This repository contains the source code for the Android app Master the maze. The app is available on the Play Store (https://play.google.com/store/apps/details?id=io.github.msc42.masterthemaze).

## App description ##
Master the maze is a game.

The levels respectively the mazes of the game are displayed on a connected Bluetooth LED display. The required specification for the Bluetooth LED display is available at http://www.teco.edu/wp-content/uploads/2014/10/teco_led_matrix_protocol.pdf. There is an emulator (written in Python), which emulates a connected Bluetooth LED display, available at https://github.com/vince-d/cm-emulator.

The Android device is responsible for the user input, the computation of the game and sending the levels to the connected Bluetooth LED display.

The levels consist of a point that is controlled by the player (there are two input modes: accelerometer control or touchscreen buttons control), border points, teleport points, next level points and moving enemy points.

The levels are JSON files. It is possible to add further levels. For this purpose, add files with the name "Level", the number of the level and the extension ".json" to the directory "MasterTheMaze", which have to be in the home directory of the Android device. Start with “Level1.json”, then “Level2.json” and so on.
