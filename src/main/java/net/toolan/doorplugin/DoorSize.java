package net.toolan.doorplugin;

public class DoorSize {
    public int X;
    public int Y;
    public int Z;

    DoorSize(int x, int y, int z) {
        X = x;
        Y = y;
        // "At most one thick" criteria enforced.
        if (x > 1 && y > 1 && z > 1)
            Z = 1;
        else if (x < -1 && y < -1 && z < -1)
            Z = -1;
        else
            Z = z;
    }

    @Override
    public String toString() {
        return "X: " + Integer.toString(X) +
               ", Y: " + Integer.toString(Y) +
               ", Z: " + Integer.toString(Z);
    }
}
