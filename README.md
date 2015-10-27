# dream-iso-droid
It's an OpenGL isometric perspective game engine for android.
I wrote this as my tesis work at university, and since than I didn't changed it, but it can be a good starting point anyone who want to dive in how to make an isometric engine.

# Sprites
This engine is using only sprites, but to calculate what should be the order of the drawing, it places every object in a 3d box virtualy. The basics of this technique is borrowed from OpenTTD's source code. (So if some of you from OpenTTD team reads this, Thanks a lot! :) )

# Animation
The engine also contains an animation system, where you can define timing of each freams of an animated sprited, and you can define different animation and call them to action by it's name.

# Collisions
Also contains a collision system which also works with the virtual 3d boxes of the objects, and quadral-trees.

# Path finding
There's an A* path finding system, works on grid base.

# Fog of war
The engine contains a fog of war system, what based on texture painting technique.

# Test game
It also contains a test game, what was created to an Asus Pad Transformer 1. Now it has problem with mouse mapping becouse of resolution changes, but it can be a good start. If you can change it and fix it, you already know how you can use this engine. :) 
