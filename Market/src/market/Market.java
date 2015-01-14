package market;

import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import java.util.Random;
import com.tictactec.ta.lib.Core;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.stream.DoubleStream;


public class Market {
	static GenerationalEvolutionEngine<Animal> engine;
	static CandidateFactory<Animal> candidateFactory;
	static List<EvolutionaryOperator<Animal>> operators = new LinkedList<EvolutionaryOperator<Animal>>();
	static FitnessEvaluator<Animal> fitnessEvaluator;
	static SelectionStrategy<Object> selectionStrategy;
	static Random rng;
	static DataBase data;
	static Core c;
	static List<Signal> signals;
	public static void main(String[] args) {
		makeSignals();
		data = new DataBase();
		c = new Core();
		rng = new Random();
		candidateFactory = new Factory();
		operators = new LinkedList<EvolutionaryOperator<Animal>>();
		operators.add(new AnimalCrossover());
		operators.add(new AnimalMutation());
		selectionStrategy = new RouletteWheelSelection();
		EvolutionaryOperator<Animal> pipeline = new EvolutionPipeline<Animal>(operators);
		
		engine = new GenerationalEvolutionEngine<Animal>(candidateFactory, pipeline, fitnessEvaluator, selectionStrategy, rng);

	}
	public static void makeSignals(){
		StockData data = new StockData(DataBase.getTestData());
		IndicatorValuesGenerator g = new IndicatorValuesGenerator();
		signals = g.generateSignal(data);
	}
	

}

class Animal{ //these are the things that are going to be evolved into an efficient stock trading animal.
	double[] chromosones = new double[4];
	double[] buyRange;
	double[] sellRange;
	double portfolio;
	double liquid;
	double invested;
	int number_of_stocks;
	List<Signal> signals;
	public Animal(double[] chro){
		this.chromosones = chro;  //[0] - [1] is buy range,   [2] - [3] is sell range. They can't overlap.
		sellRange = new double[2];
		buyRange = new double[2];
		double temp1;
		double temp2;
		double temp3;
		Arrays.sort(sellRange);
		Arrays.sort(buyRange);  //lowest to highest
		if((sellRange[0] < buyRange[0]) && (buyRange[0] < sellRange[1])) buyRange[0] = sellRange[1];   //check if lower limit of buyRange is in sellRange.
		if((sellRange[0] < buyRange[1]) && (buyRange[1] < sellRange[1])) buyRange[1] = sellRange[0];
		if((sellRange[0] < buyRange[0]) && (buyRange[0] < sellRange[1]) && (sellRange[0] < buyRange[1]) && (buyRange[1] < sellRange[1])){ //if buyrange is inside of sellrange
			temp1 = buyRange[0]; //set this as sellrange[1];
			temp2 = buyRange[1]; //reverse buyRange.
			temp3 = sellRange[1];
			sellRange[1] = temp1;
			buyRange[0] = temp2;
			buyRange[1] = temp3; //yay
		}
		if((buyRange[0] < sellRange[0]) && (sellRange[0] < buyRange[1]) && (buyRange[0] < sellRange[1]) && (sellRange[1] < buyRange[1])){
			temp1 = sellRange[0];
			temp2 = sellRange[1];
			temp3 = buyRange[1];
			buyRange[1] = temp1;
			sellRange[0] = temp2;
			sellRange[1] = temp3;
		}
		    
		signals = Market.signals;
		portfolio = 1000; //this gives animal 1000 to invest.
		liquid = 1000; //thats 1000$ 0$ in stocks.
		number_of_stocks = 0;
		invested = 0;
		
	}
	public void simulate(){
		Signal osc = signals.get(0);
		while(osc.next() != -1);
		osc.back();
		while(osc.ready()){
			
		}
	}
	public double getProfit(){ //TODO this.
		return 0.0;
	}
}

class Factory extends AbstractCandidateFactory<Animal>{ //this is going to create new animals for testing.
	public Factory(){
		
	}
@Override
    public Animal generateRandomCandidate(Random r) {
	    DoubleStream dubbs = r.doubles(4);
	    double[] rands = dubbs.toArray();
	    for(int x = 0; x < rands.length; x++)
	    	rands[x] *= 100; //because signal is 1-100 not 0-1.
	    return new Animal(rands);
    }	
}

class AnimalCrossover implements EvolutionaryOperator<Animal>{ //these are the operators which evolve the animals.
	public AnimalCrossover(){
		
	}

	@Override
	public List<Animal> apply(List<Animal> arg0, Random arg1) {
		
		return null;
	}
}

class AnimalMutation implements EvolutionaryOperator<Animal>{
	public AnimalMutation(){
		
	}

	@Override
	public List<Animal> apply(List<Animal> arg0, Random arg1) {
		
		return null;
	}
}

class AnimalEvaluator implements FitnessEvaluator<Animal>{
	public AnimalEvaluator(){
		
	}
	@Override
	public double getFitness(Animal animal, List<? extends Animal> arg1) {
		animal.simulate(); //makes sense I think. 
		return animal.getProfit();
	}
	@Override
	public boolean isNatural() {
		return true;
	}
}
