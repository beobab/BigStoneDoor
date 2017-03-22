BigStoneDoor Plugin

/door create <name> [location [size]]
/door location x y z
/door size x y z (one of these must be 1, max is 50)
/door middle x y z (must be inside the door whilst it is closed)

/door demo vanish 
/door demo portcullis
/door demo sliding
/door demo opening
/door demo drawbridge

/door style (VANISH|SLIDING|OPENING)
/door direction (UP|DOWN|LEFT|RIGHT|OUT|IRIS)
/door delay <ticks> (delay per voxel movement)

/door emptyblock <blocktype> (AIR|STRUCTURE_VOID|LAVA|WATER)

/door setOwner <player>

/door info [name]
The door is called <name> and is owned by <player>
The door is located at: x,y,z to x,y,z
It is currently OPEN.
It opens with a SLIDING style, from the bottom UP.
The door can be opened and closed by pressing the button at x,y,z
                          		  by moving the lever at x,y,z
It is replaced by AIR when it opens.
This door can be opened by <owner [,other_player]>

Name        : <name>
Owner       : <player>
------------ -------------------------------
Description : A sliding door which opens from the middle outwards.
            : A vanishing door.
            : An opening door which opens from the top downwards.
Orientation : The top of the door is (up|north|etc), and it will open (up|north|etc)wards.
Location    : x,y,z to x,y,z 
Trigger     : Button at x,y,z
Style       : SLIDING
Direction   : OUT
   (middle) : x = 435 z = 64
Empty Block : AIR
Locked      : UNLOCKED | LOCKED | AUTOLOCK

Allow List  : (everyone)
Deny List   : (no-one)

(ops can see all doors - otherwise you see doors you have permissions to)
/door list
Name                     Owner
------------------------ --------------------
mountainway              LiztherWiz
dragondoors              Minecraft_101

MVP: 
Door which vanishes and re-appears made of stone at 0,100,0 to 5, 105, 0 on command open and close
Door which you can set location
Door which you can set size.
Door which you can trigger from a button.
/door demo vanish

STAGE 1:
Door which has uses already existing blocks.
Door which you can change the empty block.
Door which you can set to slide up.
Door which you can change the delay
/door demo portcullis

STAGE 2:
Door which you can set to open from left.
Door which you can set to open from right.


Stage 3:
Door which you can set to open from middle.
Door which you can adjust the middle left and right some amount?
/door demo sliding

STAGE 3:
Door which you can set to open like a drawbridge.
/door demo drawbridge
