# KardExoTools #

# Features #

## Server management ##

* Scheduled saving
* Scheduled backup

## Comfort features ##

* Print death location upon respawn
* Crops can be harvested with a right-click when fully grown

## Commands ##

Command     | Feature                                                             | Permissions
----------- | ------------------------------------------------------------------- | -----------
backup      | Force world backup                                                  | Admin
bases       | Configure bases (options depend on ownership and permission level)  | Everyone
calc        | Calculate mathematical expressions                                  | Everyone
home        | Teleport to home location                                           | Everyone
kardexo     | Print version and commands depending on your permission level       | Everyone
moonphase   | Print current moon phase                                            | Everyone
places      | Configure places (options depend on ownership and permission level) | Everyone
resource    | Calculate resources needed for specified area                       | Everyone
sethome     | Set home location                                                   | Everyone
spawn       | Teleport to spawn location                                          | Everyone
pack        | Calculate boxes/stacks/items needed for a given item count          | Everyone
undo        | Undo last veinmine                                                  | Everyone
veinminer   | Toggle veinminer and display vein blocks                            | Everyone
whereis     | Locate players                                                      | Everyone
worldtime   | Print current world time                                            | Everyone

## Property ##

A property is an abstract object for a specified area. It can either be a base or place with at least one creator and a set of owners.

### Place ###

Places are used by whereis to provide a more specific context of the given position. 

### Base ###

Bases can be used by players to receive a notification when a player enters or leaves their base. It is also used by whereis to provide a more specific context of the given position. The notifications can be customized or turned off by each player individually for a specific base. Creators and owners will not trigger a notification.

## Veinminer ##

Once activated you can mine veins of blocks (configurable) by sneaking. It only works with an appropiate tool that has more than one durability and the blocks are in radius of the configured size. There is also a maximum (configurable) amount of blocks you can mine at once. You can undo the last (configurable amount) veinmine as long as you have the applicable blocks in your inventory and there is no living entity or block (except fluids) in the area you mined out. Durability will not be refunded.

# Building #

## Prerequisites ##

1. Java 17

## Build ##

1. Run `gradlew build`
2. The mod jar can be found in `build/libs/`

## Configure (Optional) ##

1. Run the server once
2. Edit generated json files in `config/kardexotools/`
