package managers;

import exceptions.LengthMismatchException;
import neuralNet.Net;
import neuralNet.staticTopologyNet.StaticNet;
import trainers.Trainer;

public class Governor {

    private Net[] population;

    public Governor(Net[] population) {
        this.population = population;
        for(Net net :population) {
            net.init();
        }
        log("created to govern "+population.length+" nets");
    }

    public Governor(){
    }

//    public Governor(SimpleNetWindow snWind) {
//    }

    public void breedPopulation(Trainer trainer, int maxGeneration) {
        for(int generation=0; generation<maxGeneration; generation++) {
            log("breeding generation "+(generation+1));
            breedGeneration(trainer);
        }
    }

    public void breedGeneration(Trainer trainer) {
        int goodIndex = 0, badIndex = population.length - 1;
        while(goodIndex < badIndex) {
            //population[badIndex].setWeights(population[goodIndex]);
            //todo fix this
            population[badIndex].mutate();
            goodIndex++;
            badIndex--;
        }
        testPopulation(trainer, false);

        sortPopulation(false);
    }

    public void runNet(Net NetToTest, float[] input) {
        String strInputs = "";
        for(float singleInput:input) {
            strInputs += singleInput+", ";
        }
        strInputs = strInputs.substring(0, strInputs.length()-2);

        log("running net "+ NetToTest.getId()+" with inputs: "+strInputs);
        try {
            NetToTest.run(input, false);
        } catch (LengthMismatchException e) {
            log("failed to run due to length of input not matching length of input layer");
            e.printStackTrace();
            return;
        }

        String strOuts = "";
        float[] outs = NetToTest.getOutput();
        for(float actualOut:outs) {
            strOuts += actualOut+", ";
        }
        strOuts = strOuts.substring(0, strOuts.length()-2);
        log("net "+ NetToTest.getId()+" run results:" +
                "\nconcluded: "+strOuts);
    }

    public Net getNetByRank(int rank) {
        if(rank < 1 || rank > population.length) {
            log("rank out of range");
            return null;
        }
        return population[rank-1];
    }

    public Net getNetById(int id) {
        Net NetToFind =null;
        for(Net Net :population) {
            if(Net.getId() == id) {
                NetToFind = Net;
                break;
            }
        }
        if(NetToFind ==null) {
            log("net with id "+id+" not found");
            return null;
        }

        return NetToFind;
    }

    public void bkpByGenerations(Trainer trainer, int totalGenerations, boolean shouldLog) {

        int generation = 0;
        int barProgress = 0;
        final int totalProgressBars = 30;

        if(!shouldLog) {
            StringBuilder bar = new StringBuilder();
            for(int i=0; i<totalProgressBars; i++){
                bar.append("_");
            }
            System.out.println(bar);
        }

        while (generation <= totalGenerations) {
            if(shouldLog) {
                log("bkp generation: " + (generation + 1));
            }else if((float)barProgress / (float)totalProgressBars <= (float)generation / (float)totalGenerations) {
                System.out.print("|");
//                System.out.println((float)barProgress / (float)totalProgressBars);
//                System.out.println((float)generation / (float)totalGenerations);
                barProgress++;
            }

            bkpOneGeneration(trainer, shouldLog);

//            sortPopulation(true);
            setBestWorst(shouldLog);

            generation++;
        }
        // adds new line
        System.out.print("\n");
//        System.out.print("||\n"); // dunno why, but missing 2 bars
    }

    public void bkpOneGeneration(Trainer trainer, boolean shouldLog) {
        float[][][] changes;
        float[][][] desiredChanges;

        float[][] trainingSet = trainer.getTrainingSet();
        float[][] ansSet = trainer.getAnswerSet(trainingSet);

        //one generation
        for(Net member : population) {
            StaticNet net = (StaticNet) member;
            if(shouldLog) {
                log(net.toString());
            }

            // test net and update costs
            trainer.testNet(net, false);

            //sum desired changes
            changes = net.getDesiredChanges(ansSet[0]);
            for(int setIndex=1; setIndex<trainingSet.length; setIndex++){
                desiredChanges = net.getDesiredChanges(ansSet[setIndex]);
                for(int layIndex=1; layIndex<changes.length; layIndex++) {
                    for(int nIndex=0; nIndex<changes[layIndex].length; nIndex++) {
                        for(int wIndex=0; wIndex<changes[layIndex][nIndex].length; wIndex++) {
                            changes[layIndex][nIndex][wIndex]
                                    += desiredChanges[layIndex][nIndex][wIndex];
                        }
                    }
                }
            }

            //divide by training set size (get average)
            for(int layIndex=1; layIndex<changes.length; layIndex++) {
                for(int nIndex=0; nIndex<changes[layIndex].length; nIndex++) {
                    for(int wIndex=0; wIndex<changes[layIndex][nIndex].length; wIndex++) {
                        changes[layIndex][nIndex][wIndex]
                                /= (float)trainingSet.length;
                    }
                }
            }

            //apply changes
            net.shiftWeights(changes, shouldLog);

            //test changes and update cost
            trainer.testNet(net, false);
        }
    }

    public void bkpByThreshold(Trainer trainer, float threshold, float selectionThreshold, int iniGens, int tryGens) {
        int tryCount = 1;
        float bestCost;
        final int MAX_TRIES = 999;
        while(tryCount <= MAX_TRIES) {

            log("\n\nbkp attempt number "+tryCount);
            bkpByGenerations(trainer, iniGens, false);
            trainer.testNet(population[0], true);

            //Log(population[0].toString());

            if(population[0].getCost() < selectionThreshold) {
                int generation = 1;
                int generationsStuck = 0;
                float prevBest = population[0].getCost();
                while(true) {
                    log("generation: "+generation);
                    bkpOneGeneration(trainer, false);
                    log("best: "+population[0].getCost()
                            +", worst: "+population[population.length-1].getCost());
                    bestCost = population[0].getCost();
                    generation++;

                    if(Math.abs(prevBest - bestCost) < 0.01){
                        generationsStuck++;
                    }else{
                        generationsStuck = 0;
                    }
                    prevBest = bestCost;

                    if(generation > tryGens || generationsStuck > 5){
                        trainer.testNet(population[0], true);
                        log("bkp failed");
                        break;
                    }

                    if(bestCost < threshold) {
                        log("success");
                        return;
                    }
                }
            }

            for(Net Net :population) {
                Net.init();
            }

            tryCount++;
        }

        log("failed");
    }

    public void testPopulation(Trainer trainer, boolean shouldLog) {
        for (Net Net : population) {
            trainer.testNet(Net, shouldLog);
        }
    }

    private void setBestWorst(boolean shouldLog) {
        float best = Float.MAX_VALUE, worst = Float.MIN_VALUE;
        int bestIndex = 0, worstIndex = 0;

        for(int index=0; index<population.length; index++) {
            if(population[index].getCost() < best) {
                best = population[index].getCost();
                bestIndex = index;
            }else if(population[index].getCost() > worst) {
                worst = population[index].getCost();
                worstIndex = index;
            }
        }

        Net temp;
        if(bestIndex != 0) {
            temp = population[bestIndex];
            population[bestIndex] = population[0];
            population[0] = temp;
        }

        if(worstIndex != population.length-1) {
            temp = population[worstIndex];
            population[worstIndex] = population[population.length-1];
            population[population.length-1] = temp;
        }

        if(shouldLog) {
            log("best: " + population[0].getCost()
                    + ", worst: " + population[population.length - 1].getCost());
        }
    }

    private void sortPopulation(boolean shouldLog) {

        quickSortPop(0, population.length-1);

        if(shouldLog) {
            log("best: "+population[0].getCost()
                    +", worst: "+population[population.length-1].getCost());
        }
    }

    private void quickSortPop(int start, int end){
        if(start > end || start >= population.length || end < 0) {
            return; // section sorted
        }

        float pivot = population[end].getCost();
        int wallIndex = start;
        //end is pivot so stop at end-1
        Net temp;
        for(int i=start; i<end; i++) {
            if(population[i].getCost() < pivot) {
                temp = population[wallIndex];
                population[wallIndex] = population[i];
                population[i] = temp;
                wallIndex++;
            }
        }
        //place pivot at wall
        temp = population[wallIndex];
        population[wallIndex] = population[end];
        population[end] = temp;

        //pivot at wallIndex is in correct place
        quickSortPop(start, wallIndex-1);
        quickSortPop(wallIndex+1, end);
    }

    public String save(){
        StringBuilder save = new StringBuilder();

        save.append(population.length).append("\n");

        for(Net member:population){
            save.append(member.toString());
            save.append("\n");
        }

        return save.toString();
    }

    public void load(String save){

    }

    private void log(String msg) {
        managers.Log.print(this,msg);
    }
}
