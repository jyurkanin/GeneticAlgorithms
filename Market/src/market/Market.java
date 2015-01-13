package market;

import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import com.tictactec.ta.lib.CoreAnnotated;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.DoubleStream;


public class Market {
	static GenerationalEvolutionEngine<Animal> engine;
	static CandidateFactory<Animal> candidateFactory;
	static List<EvolutionaryOperator<Animal>> operators = new LinkedList<EvolutionaryOperator<Animal>>();
	static FitnessEvaluator<Animal> fitnessEvaluator;
	static SelectionStrategy<Object> selectionStrategy;
	static DataBase data;
	static CoreAnnotated c;
	public static void main(String[] args) {
		data = new DataBase();
		c = new CoreAnnotated();
		c.o
		candidateFactory = new Factory();
		operators = new LinkedList<EvolutionaryOperator<Animal>>();
		operators.add(new AnimalCrossover());
		operators.add(new AnimalMutation());
		selectionStrategy = new RouletteWheelSelection();
		EvolutionaryOperator<Animal> pipeline = new EvolutionPipeline<Animal>(operators);
		
		engine = new GenerationalEvolutionEngine<Animal>(candidateFactory, pipeline, fitnessEvaluator, selectionStrategy, rng);;

	}
	
	public static List<IndicatorValues> generate(IStockData data){
		return null;
	}
	

}

class Animal{ //these are the things that are going to be evolved into an efficient stock trading animal.
	double[] thresholds = new double[4];
	public Animal(double a, double b, double c, double d){
		thresholds[0] = a;
		thresholds[1] = b;
		thresholds[2] = c;
		thresholds[3] = d;
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
	    return new Animal(rands[0], rands[1], rands[2], rands[3]);
    }	
}

class AnimalCrossover implements EvolutionaryOperator<Animal>{ //these are the operators which evolve the animals.
	public AnimalCrossover(){
		
	}
}

class AnimalMutation implements EvolutionaryOperator<Animal>{
	public AnimalMutation(){
		
	}
}

class AnimalEvaluator implements FitnessEvaluator<Animal>{
	public AnimalEvaluator(){
		
	}
	@Override
	public double getFitness(Animal animal, List<? extends Animal> arg1) {
		return animal.getProfit(); //makes sense I think.
	}
	@Override
	public boolean isNatural() {
		return true;
	}
}
