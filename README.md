# KardExoTools #

# Features #

* Server management
	* Scheduled saving
	* Scheduled backup
* Comfort features
	* Print death location upon respawn
	* Only one player is required to sleep
* Commands

	Command     | Feature                                                       | Permissions
	----------- | ------------------------------------------------------------- | -----------
	backup      | Fource world backup                                           | Admin
	bases       | Configure bases                                               | Everyone
	calc        | Calculate mathematical expressions                            | Everyone
	home        | Teleport to home location                                     | Everyone
	kardexo     | Print version and commands depending on your permission level | Everyone
	moonphase   | Print current moon phase                                      | Everyone
	places      | Configure places                                              | Everyone
	resource    | Calculate resources needed for specified area                 | Everyone
	sethome     | Set home location                                             | Everyone
	spawn       | Teleport to spawn location                                    | Everyone
	undo        | Undo last veinmine                                            | Everyone
	veinminer   | Toggle veinminer and display vein blocks                      | Everyone
	whereis     | Locate players                                                | Everyone
	worldtime   | Print current world time                                      | Everyone

# Concepts #

## Property ##

A property is an abstract object for a specified area. It can either be a base or place with at least one creator and a set of owners.

### Place ###

Places are used by whereis to provide a more specific context of the given position. 

### Base ###

Bases can be used by players to receive a notification when a player enters or leaves their base. It is also used by whereis to provide a more specific context of the given position. The notifications can be customized or turned off by each player individually for a specific base. Creators and owners will not trigger a notification.

## Veinminer ##

Once activated you can mine veins of blocks (configurable) by sneaking. It only works with an appropiate tool that has more than one durability and the blocks are in radius of the configured size. There is also a maximum (configurable) amount of blocks you can mine at once. You can undo the last (configurable amount) veinmine as long as you have the applicable blocks in your inventory and there is no living entity or block (except fluids) in the area you mined out. Durability will not be refunded.

# Installation #

## Prerequisites ##

1. Java SE 8 Update 92
2. Java Development Kit 8 Update 92

## Setup jarmod-buildsystem-2 ##

1. Download [jarmod-buildsystem-2](https://github.com/Earthcomputer/jarmod-buildsystem-2)
2. Delete the folders `patches` and `src` and the file `conf/settings.json`
3. Copy the folders `patches` and `src` and the file `conf/settings.json` from this repo
4. Run `gradlew setup`

## Configure (Optional) ##

General config file: `src/main/java/exopandora/kardexo/kardexotools/config/Config.java`

## Build ##

1. Run `gradlew releaseProject`
2. Compiled sources can be found in `build/distributions/`

## Install ##

1. Open a vanilla `minecraft_server.jar` as a zip file
2. Copy all files from `build/distributions/KardExoTools_VERSION_Server.zip` into the jar
3. Run the server
