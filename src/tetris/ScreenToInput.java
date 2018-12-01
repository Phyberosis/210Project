package tetris;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class ScreenToInput extends BlockHandler {

    private Robot sharedRobot;

    /**
     * may need to adjust screen area -> enter "capture" into textbox
     */

    //  note to self: 6 right 2 down to position
    //  x, y of top right, size is w, h
    private final int[] LEFT_BOARD = {224, 390};
    private final int[] RIGHT_BOARD = {1285, 414};
    private final int[] SIZE = {290, 600};


    private final int BOARD_WIDTH = 10;
    private final int PIXEL_WIDTH = SIZE[0] / BOARD_WIDTH;
    private final int BOARD_AREA = 200;

     //debug
    private boolean[][] block;
//    int r;
//    TetrisBlocks type;

    public ScreenToInput(Robot r){
        sharedRobot = r;
    }

    // does nothing if block not touching top
    // sets one of 21 to true -> 7 blocks x 3 rotations
    private boolean trySetCurrBlock(boolean[] data, int offset, int boardWidth){
        boolean[][] block = tryFindBlockIfNearTop(data, offset, boardWidth);
        if(block.length == 0){
            return false;
        }

        //debug
//        this.block = block;

        BlockIDData bData = getBlockID(block);
        if(!bData.getId().equals(TetrisBlocks.UNK)){
            TetrisBlocks blockID = bData.getId();
            int r = bData.getRotation();
            if(r == 1)
                r = 3;
            else if(r == 3)
                r = 1;

            int id = bData.getId().getVal();
            int pos = (id-1)*4 + r;// prev block spots * 3 + rot
            for(int i=0; i<offset; i++){
                data[i] = pos == i;
            }

            //debug
//            this.r = r;
            this.block = block;
//            this.type = blockID;

            return true;

        }else{
            return false;
        }

    }

    //returns empty array if not touching top
    private boolean[][] tryFindBlockIfNearTop(boolean[] data, int offset, int boardWidth){
        int leftBound = Integer.MAX_VALUE;
        int rightBound = Integer.MIN_VALUE;
        final int Y_RANGE = 4;

        for(int y=0; y<Y_RANGE; y++){
            boolean xFoundShape = false;
            for(int x = 0; x<boardWidth; x++){

                int i = y*boardWidth + x;
                if(data[i+offset]){
                    xFoundShape = true;

                    leftBound = Math.min(leftBound, x);
                    rightBound = Math.max(rightBound, x);
                }
            }

            if(y == 1 && !xFoundShape){
                return new boolean[0][];    // shape not near top -> no need to update
            }
        }


        int xRange = (rightBound - leftBound)+1;
        boolean[][] block = new boolean[xRange][Y_RANGE];
        for(int y=0; y<Y_RANGE; y++){
            for(int x = 0; x<xRange; x++){

//                if(x >= block.length){
//                    System.out.println(x+", "+block.length);
//                }

                int i = y*boardWidth + x + leftBound;
                block[x][y] = data[i+offset];
            }
        }

        block = trimBlock(block);

        return block;
    }

    private boolean[][] trimBlock(boolean[][] block){
        int topTrim = 0;
        boolean yFound = false;
        for(int y=0; y<block[0].length; y++){
            boolean isEmpty = true;
            for(int x=0; x<block.length; x++){
                if(block[x][y]) {
                    isEmpty = false;
                    yFound = true;
                }
            }
            // found and past -> is bot trim now
            if(yFound && isEmpty)
                break;

            if (isEmpty)
                topTrim++;
        }

        int botTrim  = 0;
        yFound = false;
        for(int y=block[0].length-1; y>=0; y--){
            boolean isEmpty = true;
            for(int x=0; x<block.length; x++){
                if(block[x][y]) {
                    isEmpty = false;
                    yFound = true;
                }
            }
            if(yFound && isEmpty)
                break;

            if (isEmpty)
                botTrim++;
        }

        boolean[][] trimmedBlock = new boolean[block.length][block[0].length - topTrim - botTrim];
        for(int y=0; y<block[0].length - botTrim - topTrim; y++){
            for(int x=0; x<block.length; x++){
                trimmedBlock[x][y] = block[x][y+topTrim];
            }
        }

        return trimmedBlock;
    }

    private float[] parseCapture(BufferedImage capture){
        int offset = 28;
        boolean[] data = new boolean[BOARD_AREA + offset];    // 7 pieces x 3 rotations
        //debug
//        System.out.println("size"+capture.getWidth()+", "+capture.getHeight());
//        System.out.println("pWid"+PIXEL_WIDTH);
        for(int i = 0; i< BOARD_AREA; i++){
            int x = i% BOARD_WIDTH;
            int y = i/ BOARD_WIDTH;

            int xCoord = x* PIXEL_WIDTH + PIXEL_WIDTH /2;
            int yCoord = y* PIXEL_WIDTH + PIXEL_WIDTH /2;

            Color clr = new Color(capture.getRGB(xCoord, yCoord));

            if(clr.getRed() == 0 && clr.getGreen()==0 && clr.getBlue() == 0){
                data[i+offset] = false;
            }else{
                data[i+offset] = true;
            }

            //debug
//            System.out.println(x+", "+y+": "+clr.toString());
//            capture.setRGB(xCoord, yCoord, Color.WHITE.getRGB());
        }

        boolean success = trySetCurrBlock(data, offset, BOARD_WIDTH);

        //debug
//        if(success) {
//            printBoard(data, offset, BOARD_AREA, BOARD_WIDTH);
//            printBlock(block);
//            printBlock(type.getGeometry());
//            System.out.println("r, t: "+r + " "+type.getVal());
//        }

        float[] toRet = new float[data.length];
        for(int i = 0; i<data.length; i++){
            if(data[i])
                toRet[i] = 1f;
            else
                toRet[i] = 0f;
        }
        return toRet;
    }

    //debug
    public String capture(){
        Rectangle screenRect = new Rectangle(LEFT_BOARD[0], LEFT_BOARD[1], SIZE[0], SIZE[1]);
        BufferedImage capture = sharedRobot.createScreenCapture(screenRect);
        String ext = "jpg";
        String fileName = "capture."+ext;

        for(int i = 0; i< BOARD_AREA; i++){
            int x = i% BOARD_WIDTH;
            int y = i/ BOARD_WIDTH;

            int xCoord = x* PIXEL_WIDTH + PIXEL_WIDTH /2;
            int yCoord = y* PIXEL_WIDTH + PIXEL_WIDTH /2;

            Color clr = new Color(capture.getRGB(xCoord, yCoord));

            System.out.println(x+", "+y+": "+clr.toString());
            capture.setRGB(xCoord, yCoord, Color.WHITE.getRGB());
        }

        try {
            ImageIO.write(capture, ext, new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return "CAPTURE FAILED: bad file location in ScreenToInput class";
        }

        return fileName;
    }

    //debug
    public static void printBlock(boolean[][] block) {
        int xMax = block.length;
        if(xMax == 0)
            return;
        int yMax = block[0].length;

        StringBuilder sb = new StringBuilder("\n");

        for(int y = 0; y<yMax; y++){
            for(int x = 0; x<xMax; x++){
                if(block[x][y]){
                    sb.append("[x]");
                }else{
                    sb.append("[ ]");
                }
            }
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    //debug
    public static void printBoard(boolean[] data, int offset, int boardArea, int boardWidth){
        int lastY = -1;
        StringBuilder sb = new StringBuilder("\n\n");
        for(int i = 0; i<boardArea; i++) {
            int y = i / boardWidth;

            String tile = "[ ]";
            if(data[i+offset])
                tile = "[X]";

            if(lastY != y){
                sb.append("\n");
                lastY = y;
            }
            sb.append(tile);
        }

        sb.append("\n");
        for (int i = 0; i<offset; i++){
            if(data[i]){
                sb.append("X");
            }else{
                sb.append("O");
            }
        }

        System.out.println(sb.toString());
    }

    // REQUIRES: interpretation of float[] must be consistent with net
    //              index 0-4 are boolean for block shape, rest is game space:
    //              x = (index - 4) % 10, y = floor( (index - 4) / 10 )
    //              true is occupied with block that is not apart of current block
    public float[] extractGameState(){
        Rectangle screenRect = new Rectangle(LEFT_BOARD[0], LEFT_BOARD[1], SIZE[0], SIZE[1]);
        return parseCapture(sharedRobot.createScreenCapture(screenRect));
    }
}
