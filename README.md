# Passagecraft
Passagecraft is a bukkit plugin which allows administrators to setup "Passagecraft Mazes" - in which joined users will navigate and fight to the death to reach the "nukeroom" and survive the bombings!

Designed and Developed by MeshCollider

CONFIGURATION
=============
PLEASE USE THE HELP COMMAND IN GAME FOR SPECIFIC COMMAND USAGE.

On the server, the administrator should contruct a maze with a central room as a goal for the maze. The central room is the nukeroom, and players must reach this room to survive the map.

First, setup the HUB for the world using the /pchub command - this is where all players will be teleported to after the map has ended.

Secondly, create the map using /pccreate. The map should be given a name to identify it. View current maps by typing /pcmaps.

Thirdly, setup the lobby and spawnpoints for the map, using the /pclobby and /pcspawnpoint commands respectively. The lobby is where players are TPed to when they join, while waiting. Players will be set to creative in this area, but their inventories will be cleared. The players will then be sent to a random spawnpoint of the 4 set when the map starts. These must be set individually using /pcspawnpoint mapname number, where number is 1 to 4. When the map begins, the players will be given a kit if a kit is specified for the map.

Fourth, setup the nukeroom. First type /pcnukeroom mapname, then you will be given a selection tool. right click once with it to select a corner of the nukeroom, and a second time on the OPPOSITE corner, to encapsulate the entire room. Then setup the protection region in the same manner - use /pcprotect mapname, and select the 2 corners of the maze to protect it from unprivileged players.

Then setup kits for the map, use /pckits to see available kits and /pckit to add a kit to a map. Kits can be configured in kits.yml, and killstreak kits are also available. to view a kits details, type /pckitdetails kitname. The killstreak kit must have the name KILLSTREAK.

All other settings can be configured in config.yml including colors and messages.

AFTER THE MAP IS SETUP, use /pcpublish mapname to broadcast a message to all players advertising it. you can use /pcdetails mapname to view the details of a map.

Players can join a map using the /pcjoin command, also available are /pcforcejoin and /pcvipjoin for privileged players. Signs can be set up for maps by typing on the sign in this format:

PASSAGECRAFT mapname The sign will then change color and display the current status information. Right clicking on the sign will join the map.

To quit a map you are currently in, type /pcquit. You will then be teleported to the hub. Each map has a time limit (configurable in config.yml), after which all players who are NOT IN THE NUKEROOM will be killed. A winning message is broadcast and players are teleported to the hub.

Players in the nukeroom are invincible and cannot leave again until the map is over.

Each map has a scoreboard to display player kills. To view the scoreboard, type /pcscores mapname.

To delete a map, type /pcdelete mapname. All signs assosiated with the map will not be deleted, but will display a "map doesn't exist" message.

Bonus commands include: - Explode, which creates an explosion around the specified player - Gotohub which teleports the player to the hub of the world

COMMANDS
========
pcscores: Displays the Passagecraft scoreboard for the specified map

pccreate: Creates a new PassageCraft map

pclobby: Sets the location of the lobby for the specified map

pcjoin: Joins the specified PassageCraft map

pcvipjoin: Joins the specified PassageCraft map irrelevant of its fullness

pcforcejoin: Joins the specified PassageCraft map even if it is currently in progress

pcspawnpoint: Sets the location of a specified spawn point in the specified map

pcspawnpoints: Sets the location of all 4 spawn points in the specified map

pcquit: Quits the current map

pchub: Sets the hub of the world

pcprotect: Turns editing mode on for the protecion region of the map

pcnukeroom: Turns editing mode on for the nukeroom of the map

pcmaps: Lists the created PassageCraft maps

pcpublish: Broadcasts a newly created map

pcdelete: Removes a specified map

pcdetails: Lists details of the specifed map

pcgotohub: Teleports to the HUB of the world

pckit: Sets the kit for the specified map

pckits: Lists the available kits

pckitdetails: Lists the details of the specified kit

pcexplode: Creates an explosion around the specified player

PERMISSIONS
passagecraft.basic: description: Basic Passagecraft command default: true

passagecraft.scores: description: Allows you to see the scores of a passagecraft map default: true

passagecraft.tphub: description: Allows you to see the scores of a passagecraft map default: op

passagecraft.join: description: Allows you to join a passagecraft map default: true

passagecraft.vipjoin: description: Allows you to join a passagecraft map irrevelvant of its fullness default: op

passagecraft.forcejoin: description: Allows you to force join a passagecraft map currently in progress default: op

passagecraft.create: description: Allows you to create a new passagecraft map default: op

passagecraft.list: description: Allows you to view the created maps default: op

passagecraft.publish: description: Allows you to broadcast a newly created map default: op

passagecraft.delete: description: Allows you to delete a passagecraft map default: op

passagecraft.hub: description: Allows you to set the hub of the world default: op

passagecraft.details: description: Allows you to list details about a specific map default: op

passagecraft.breakprotected: description: Allows you to break a block in a protected region default: op

passagecraft.placeprotected: description: Allows you to place a block in a protected region default: op

passagecraft.setkit: description: Allows you to set the kit for a map default: op

passagecraft.explode: description: Allows you to create an explosion default: op
