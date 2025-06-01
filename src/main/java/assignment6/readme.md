Assignment 6 task 1:


This drag-to-rotate function lets users intuitively rotate a 3D model (like a cube) using simple mouse movements. When the user drags the mouse, the function calculates the Euclidean distance (straight-line distance) between the current and previous mouse positions. This distance reflects how far the mouse was moved and is used to scale the rotation angle — the farther you drag, the more the object rotates. The rotation axis is set to a vector perpendicular to the drag direction in screen space (Point3D(dy, dx, 0)), giving natural-feeling rotations around the X and Y axes. Together, this allows smooth, responsive rotation of the 3D object based on 2D mouse gestures. Since the mouse movement is only in the  2d space, we only take x axis and y axis into consideration.










The provided code sets up an InvalidationListener on the innerGroup to ensure that the entire group of loaded 3D mesh models is always centered in the scene. Whenever the children of innerGroup change—such as when new OBJ files are loaded—the listener is triggered. It calculates the bounding box of all current meshes using innerGroup.getBoundsInLocal(), which returns the minimum and maximum coordinates that enclose all the child nodes. From this bounding box, the code computes the geometric center by averaging the min and max values for each axis (X, Y, Z). This center represents the middle point of all models currently in the group. To bring the entire group to the center of the scene, a Translate transform is applied using the negative of these center coordinates. This effectively shifts the group so that its center aligns with the origin (0, 0, 0) of the coordinate system. Centering the group like this is important because it ensures that rotation and zoom operations (which are applied to the parent contentGroup) behave intuitively, making the entire anatomical structure appear well-positioned and visually balanced within the 3D viewer.