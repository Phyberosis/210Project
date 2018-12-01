package tetris;

public enum TetrisBlocks {
    LINE(1, getLine()),
    MIR_L(2, getMirL()),
    L(3, getL()),
    SQUARE(4, getSquare()),
    S(5, getS()),
    Z(6, getZ()),
    T(7, getT()),
    UNK(-1, new boolean[0][]);

    private final int val;
    private boolean[][] geometry;

    TetrisBlocks(int i, boolean[][] g) {
        val = i;
        geometry = g;
    }

    public int getVal(){
        return val;
    }

    public boolean[][] getGeometry(){
        return geometry;
    }

    private static boolean[][] getL(){
        return new boolean[][]{{true, true, true}, {false, false, true}};
    }

    private static boolean[][] getMirL(){
        return new boolean[][]{{false, false, true}, {true, true, true}};
    }

    private static boolean[][] getS(){
        return new boolean[][]{{false, true}, {true, true}, {true, false}};
    }

    private static boolean[][] getZ(){
        return new boolean[][]{{true, false}, {true, true}, {false, true}};
    }

    private static boolean[][] getSquare(){
        return new boolean[][]{{true, true}, {true, true}};
    }

    private static boolean[][] getLine(){
        return new boolean[][]{{true, true, true, true}};
    }

    private static boolean[][] getT(){
        return new boolean[][]{{true, false}, {true, true}, {true, false}};
    }
}
