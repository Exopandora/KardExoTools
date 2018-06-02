# KardExoTools #

# Features #

* Server Management
	* Scheduled saving
	* Scheduled backup
* Comfort features
	* Print death location upon respawn
	* Only one player is required to sleep
* Commands

	Command   | Feature
	--------- | -------------------------------------------
	bases     | Configure bases
	calc      | Calculate mathematical expressions
	forcesave | Force world save
	home      | Teleport to home location
	moonphase | Print current moon phase
	places    | Configure places
	resource  | Calculate resources need for specified area
	sethome   | Set home locataion
	spawn     | Teleport to spawn location
	undo      | Undo last veinmine
	veinminer | Toggle veinminer
	whereis   | Find players
	worldtime | Prints current world time

# Concepts #

## Property

A property is an abstract object for a specified area. It can either be a base or place with at least one creator and a set of owners.

### Place

Places are used by whereis to provide more specific context of the given position. 

### Base

Bases can be used by players to receive a notification when a player enters their base. It is also used by whereis to provide more specific context of the given position. The notifications can be customized or turned off by each player individually for a specific base. Creators and owners will not trigger a notification.

## Veinminer

Once activated you can mine veins of blocks (configurable) by sneaking. It only works with an appropiate tool that has more than 1 durability and the block is in the radius of a configured size. There is also a maximum (configurable) amount of blocks you can mine at once. You can undo the last (configurable amount) veinmine as long as you still have the blocks in your inventory and there is no entity or blocks (except fluids) in the area you mined out. Durability will not be refunded.

# Setup #

1. Download MCP
2. Download 'minecraft_server.jar'
3. Rename it to 'minecraft_server.jar'
4. Put it inside the folder 'jars'
5. Run 'decompile --server'
6. Clone this repo into the MCP folder
7. Run 'patch'

# Configure #

General: exopandora/kardexo/kardexotools/Config.java  
Veinminer: exopandora/kardexo/kardexotools/Veinminer.java

# Recompile #

1. Run 'recompile'
2. Run 'reobfuscate'
3. Compiled sources can be found in 'rebof/minecraft_server'

# Install #

1. Open a vanilla 'minecraft_server.jar' as a zip file
2. Copy all files from 'rebof/minecraft_server' into the jar
3. Run the server
