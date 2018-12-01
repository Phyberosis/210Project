package trainers;

import tetris.BlockHandler;
import tetris.TetrisBlocks;
import tetris.TetrisGUI;
import tetris.TetrisPlayer;

import java.util.LinkedList;

public class TrainerTetris extends Trainer {

    private final int MAX_HISTORY = 10;
    private LinkedList<float[]> history;

    private final int FAIL_SCORE = -1000;
    private final int CLEAR_BONUS = 2;
    private final float yFactor = 7f ;
    private final float sFactor = 15f;
    private final float fFactor = 3f;

    private TetrisGUI ui;
    public TrainerTetris(TetrisGUI tetrisGUI){
        history = new LinkedList<>();
        ui = tetrisGUI;
    }

    @Override
    public float[][] getTrainingSet() {
        float[][] toRet = new float[history.size()][];
        return history.toArray(toRet);
    }

    public void addToHistory(float[] input){
        float[] copy = new float[input.length];
        System.arraycopy(input, 0, copy, 0, input.length);
        history.addLast(copy);
        if(history.size() > MAX_HISTORY){
            history.removeFirst();
        }
    }

    @Override
    public float[] getAnswer(float[] input) {
        int offset = TetrisPlayer.OFFSET;
        int rawBlockData = getRawID(input, offset);
        TetrisBlocks block = getBlock(rawBlockData);
        int blockRot = getRot(rawBlockData);
        boolean[][] board = getBoard(input, offset);

        Scenario snro = new Scenario(block, 0, board);

        //debug
//        System.out.println("trainer");
//        BlockHandler b = new BlockHandler();
//        boolean[][] originalBlock = block.getGeometry();
//        for(int i=0; i<blockRot; i++){
//            originalBlock = b.rotateCCW(originalBlock);
//        }
//        System.out.println("raw"+rawBlockData);
//        System.out.println("r"+blockRot);
//        System.out.println("id"+block);
//        ScreenToInput.printBlock(originalBlock);

        LinkedList<XYSFR> lo_XYSFR = getBestMoves(snro);
        XYSFR best = lo_XYSFR.getFirst();
        for(XYSFR move : lo_XYSFR){
            if(move.y*yFactor + move.s*sFactor + move.f*fFactor
                    > best.y*yFactor + best.s*sFactor + best.f*fFactor) {
                best = move;
            }
        }

        //debug
//        System.out.println(best.x+" "+best.r);
//        System.out.println("\n" +
//                "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +
//                "\nfinal:");
        BlockHandler bbb = new BlockHandler();
        boolean[][] bb = snro.type.getGeometry();
        for(int i=0; i<best.r; i++){
            bb = bbb.rotateCCW(bb);
        }
        //debug
        overlay(bb, board, best.x, best.y);

        return parseOut(best, blockRot);
    }

    private float[] parseOut(XYSFR move, int natRot){
        int r = move.r - natRot;
        r = r<0? r+4 : r;
        if(r == 1)
            r = 3;
        else if(r == 3){
            r = 1;
        }

        int rotDataIndex = 10;
        float[] out = new float[rotDataIndex+4];
        for(int x=0; x<rotDataIndex; x++){
            if(x==move.x)
                out[x] = 1f;
            else
                out[x] = 0f;
        }
        for(int x=rotDataIndex; x<rotDataIndex+4; x++){
            if(x==rotDataIndex+r)
                out[x] = 1f;
            else
                out[x] = 0f;
        }

        //debug
//        System.out.println("@@"+r);
//        StringBuilder sb = new StringBuilder();
//        for(float f : out){
//            sb.append(f).append(" ");
//        }
//        System.out.println(sb);

        return out;
    }

    // based on overhang
    private LinkedList<XYSFR> getBestMoves(Scenario snro){
        boolean[][] board = snro.board;

        LinkedList<XYSFR> XYSFR = new LinkedList<>();
        XYSFR.add(new XYSFR(0,0,0,0, 0));

        for(int x=0; x<board.length; x++){
            for(int r=0; r<4; r++){
                snro.rot = r;

                int score[] = evalColumn(snro, x);
                XYSFR.add(new XYSFR(x, score[1], score[0], score[2], r));
            }
        }

        return XYSFR;
    }

    // check every y position at x = xBoard, given block rotation in snro
    private int[] evalColumn(Scenario snro, int xBoard) {
        boolean[][] blockGeometry = snro.type.getGeometry();
        BlockHandler b = new BlockHandler();
        for (int i = 0; i < snro.rot; i++) {
            blockGeometry = b.rotateCCW(blockGeometry);
        }

        int w = blockGeometry.length, h = blockGeometry[0].length;
        boolean[][] board = snro.board;

        if (xBoard + w > board.length)
            return new int[]{FAIL_SCORE, 0, 0};

        int bestScore = Integer.MIN_VALUE;
        int bestY = -1;
        int bestFit = -1;
        for (int yBoard=4; yBoard <= board[0].length; yBoard++) {

            if (yBoard+h > board[0].length)
                continue;

            int score = evalPosition(board, blockGeometry, xBoard, yBoard);
            if(score>bestScore) {
                bestScore = score;
                bestY = yBoard;
                bestFit = evalFit(board, blockGeometry, xBoard, yBoard);
            }
        }

        //debug
//        overlay(blockGeometry, board, xBoard, bestY);
//        System.out.println(xBoard+" "+bestY);
//        System.out.println(bestScore + ", " + bestY);
//        System.out.println("\n\n");

        return new int[] {bestScore, bestY, bestFit};
    }

    // check specific position at coordinate (xBoard, yBoard)
    private int evalPosition(boolean[][] board, boolean[][] blockGeometry, int xBoard, int yBoard){
        int w = blockGeometry.length;

        int score = 0;
        for (int x = xBoard; x < xBoard+w; x++) {
            score += evalLineAtPosition(board[x], blockGeometry[x-xBoard], yBoard);
        }
        return score;
    }

    //check left-right fit at coordinate (xBoard, yBoard)
    private int evalFit(boolean[][] board, boolean[][] block, int blockX, int blockY) {
        int score = 0;

        for (int y = blockY; y < blockY + block[0].length; y++) {
            boolean lastWasBoard = false;
            boolean lastWasBlock = false;
            boolean fullLine = true;
            int rowScore = 0;
            for (int x = blockX - 1; x <= blockX + block.length; x++) {
                boolean isBoard = true;
                if (inBounds(board, x, y))
                    isBoard = board[x][y];

                boolean isBlock = false;
                if (inBounds(block, x - blockX, y - blockY)) {
                    isBlock = block[x - blockX][y - blockY];
                }

                if (lastWasBoard && isBlock)
                    rowScore++;

                if (lastWasBlock && isBoard)
                    rowScore++;

                if (!isBlock && !isBoard)
                    fullLine = false;

                lastWasBlock = isBlock;
                lastWasBoard = isBoard;
            }
            score += rowScore;
            if (fullLine)
                score += CLEAR_BONUS;
        }

        return score;
    }

    // helper - eval score of specific column of block
    private int evalLineAtPosition(boolean[] board, boolean[] block, int blockY){
        for(int y = blockY; y<block.length+blockY; y++){
            if (board[y] && block[y-blockY]){
                return FAIL_SCORE;
            }
        }

        //check position unreachable
        boolean blockFound = false;
        for(int y=blockY+block.length-1; y>=4; y--){
            if(board[y] && blockFound)
                return FAIL_SCORE;

            if(y-blockY < block.length && y-blockY >= 0) {
                if(block[y - blockY])
                    blockFound = true;
            }
        }

        int gapCount = 0;
        boolean prevWasBoard  = true;
        for(int y = board.length-1; y>=blockY; y--){
            if(board[y]){
                gapCount = 0;
                continue;
            }

            if(y-blockY >= block.length){
                if(!board[y])
                    gapCount++;
            }else{
                if(block[y-blockY]) {
                    if (prevWasBoard && gapCount != 0)  {
                        gapCount--; // reward for tight fit
                    }
                    break;
                }

                if(!block[y-blockY] && !board[y]){
                    gapCount++;
                }
            }

            prevWasBoard = board[y];
        }

        return -gapCount;
    }

    private boolean inBounds(boolean[][] block, int x, int y){
        return x>=0 && y>=0 && x<block.length && y<block[0].length;
    }

    // does not separate rotation data
    private int getRawID(float[] in, int offset){
        int rawBlockData = 0;


        for(int i=0; i<offset; i++){
            if(in[i] == 1f) {
                rawBlockData = i;
                break;
            }
        }

        return rawBlockData;
    }

    private TetrisBlocks getBlock(int rawBlockData){
        return TetrisBlocks.values()[rawBlockData / 4];
    }

    //debug "draw" board with block in given position
    private void overlay(boolean[][] block, boolean[][] board, int xOff, int yOff){
        StringBuilder sb = new StringBuilder();
        for(int y=0; y<board[0].length; y++){
            for(int x=0; x<board.length; x++){
                String tile = "[ ]";
                if(board[x][y]){
                    tile = "[O]";
                }
                if(x-xOff >=0 && x-xOff < block.length &&
                        y-yOff >=0 && y-yOff < block[0].length){
                    if(board[x][y] && block[x-xOff][y-yOff]){
                        tile = "[@]";
                    }else if(block[x-xOff][y-yOff]){
                        tile = "[X]";
                    }
                }
                sb.append(tile);
            }
            if(y<board[0].length-1)
                sb.append("\n");
        }
        System.out.println(sb);
        if(ui!=null)
            ui.setTextArea(sb.toString());
    }

    private int getRot(int rawBlockData){
        // 7 blocks in tetris
        return rawBlockData % 4;
    }

    private boolean[][] getBoard(float[] in, int offset){
        int boardArea = 200;
        int boardWidth = 10;
        boolean[][] board = new boolean[boardWidth][boardArea/boardWidth];
        for(int i = 0; i<boardArea; i++) {
            int x = i % boardWidth;
            int y = i / boardWidth;

            board[x][y] = in[i + offset] == 1f;
        }

        return board;
    }

    private Scenario toScenario(float[] in){
        int rawBlockData = 0;
        int offset = 21;

        for(int i=0; i<offset; i++){
            if(in[i] == 1f) {
                rawBlockData = i;
                break;
            }
        }

        // 7 blocks in tetris
        int r = rawBlockData / 7;
        TetrisBlocks type = TetrisBlocks.values()[rawBlockData % 7];

        int boardArea = 200;
        int boardWidth = 10;
        boolean[][] board = new boolean[boardWidth][boardArea/boardWidth];
        for(int i = 0; i<boardArea; i++) {
            int x = i % boardWidth;
            int y = i / boardWidth;

            board[x][y] = in[i + offset] == 1f;
        }

        return new Scenario(type, r, board);
    }

    // X, Y, SCORE, FIT, ROTATION
    private class XYSFR {
        int x;
        int y;
        int s;
        int f;
        int r;

        private XYSFR(int xx, int yy, int ss, int ff, int rr){
            x = xx;
            y = yy;
            s = ss;
            f = ff;
            r = rr;
        }
    }

    private class Scenario {
        private TetrisBlocks type;
        private int rot;
        private boolean[][] board;

        private Scenario(TetrisBlocks t, int r, boolean[][] b){
            type = t;
            rot = r;
            board = new boolean[b.length][b[0].length];
            for(int x = 0; x<b.length; x++){
                System.arraycopy(b[x], 0, board[x], 0, b[0].length);
            }
        }
    }
}