package tetris;

public class BlockHandler {

    //y is + going down!

    public BlockIDData getBlockID(boolean[][] block){
        //you can only meaningfully rotate 90deg 3 times
        for(int r=0; r<4; r++){
            TetrisBlocks blockID = tryGetID(block);
            if(!blockID.equals(TetrisBlocks.UNK)){
                //there are only 7 blocks in Tetris
                return new BlockIDData(blockID, r);
            }else{
                block = rotateCCW(block);
            }
        }

        return new BlockIDData(TetrisBlocks.UNK, 0);
    }

    //only works with one orientation of block -> so rotate!
    private TetrisBlocks tryGetID(boolean[][] block){
        for(TetrisBlocks type : TetrisBlocks.values()){
            if(doBlocksMatch(type.getGeometry(), block))
                return type;
        }
        return TetrisBlocks.UNK;
    }

    public boolean[][] rotateCCW(boolean[][] block){
        int xMax = block.length, yMax = block[0].length;
        boolean[][] rBlock = new boolean[yMax][xMax];

        for(int x=0; x<xMax; x++){
            for(int y = 0; y<yMax; y++){
                rBlock[y][xMax - x - 1] = block[x][y];
            }
        }

        return rBlock;
    }

    private boolean doBlocksMatch(boolean[][] a, boolean[][] b){
        if(a.length == 0 || b.length == 0 || a[0].length == 0 || b[0].length == 0){
            return false;
        }

        if(a.length != b.length || a[0].length != b[0].length){
            return false;
        }

        for(int x=0; x<a.length; x++){
            for(int y=0; y<a[0].length; y++){
                if(a[x][y] != b[x][y])
                    return false;
            }
        }

        return true;
    }

    public class BlockIDData{
        private TetrisBlocks id;
        private int rotation;

        public BlockIDData(TetrisBlocks ID, int Rotation){
            id = ID;
            rotation = Rotation;
        }

        public TetrisBlocks getId() {
            return id;
        }

        public int getRotation(){
            return rotation;
        }
    }
}
