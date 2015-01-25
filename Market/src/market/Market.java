package market;

import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.UserAbort;

import org.uncommons.maths.random.MersenneTwisterRNG;
import com.tictactec.ta.lib.Core;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.DoubleStream;


public class Market {
	static GenerationalEvolutionEngine<Animal> engine;
	static CandidateFactory<Animal> candidateFactory;
	static List<EvolutionaryOperator<Animal>> operators = new LinkedList<EvolutionaryOperator<Animal>>();
	static FitnessEvaluator<Animal> fitnessEvaluator;
	static SelectionStrategy<Object> selectionStrategy;
	static Random rng;
	static Observer JaneGoodall;
	static DataBase data;
	static Core c;
	static List<Signal> signals;
	static Animal Best; //fittest animal after simulation.
	public static void main(String[] args) {
		makeSignals();
		data = new DataBase();
		c = new Core();
		rng = new MersenneTwisterRNG();
		candidateFactory = new Factory();
		operators = new LinkedList<EvolutionaryOperator<Animal>>();
		operators.add(new AnimalCrossover());
		operators.add(new AnimalMutation());
		selectionStrategy = new RouletteWheelSelection();
		fitnessEvaluator = new AnimalEvaluator();
		JaneGoodall = new Observer(); //this just prints some useful data
		EvolutionaryOperator<Animal> pipeline = new EvolutionPipeline<Animal>(operators);
		engine = new GenerationalEvolutionEngine<Animal>(candidateFactory, pipeline, fitnessEvaluator, selectionStrategy, rng);
		engine.addEvolutionObserver(JaneGoodall);
		Scanner scan = new Scanner(System.in);
		String buf;
		engine.setSingleThreaded(true); //Because then it runs multiple animal at once and it gets confusing to debug. I think.
		engine.evolve(100, 20, new GenerationCount(100), new UserAbort()); //for now I guess
		int gs;
		int pop;
		int elites;
		while(true){
			System.out.println("Evolve The Animals? y/n");
			buf = scan.next();
			if(buf.equals("n")) break;
			System.out.println("Evolution Settings: ");
			System.out.print("Initial Population ");
			pop = Integer.parseInt(scan.next());
			System.out.print("Elites ");
			elites = Integer.parseInt(scan.next());
			System.out.print("max generations");
			gs = Integer.parseInt(scan.next());
			engine.evolve(pop, elites, new GenerationCount(gs), new UserAbort());
			while(true){
				System.out.println("b to break");
				buf = scan.next();
				if(buf.equals("b"));
				
			}
		}
		scan.close();

	}
	public static void makeSignals(){
		StockData data = new StockData(DataBase.getTestData()); //so this all is good in this line.
		IndicatorValuesGenerator g = new IndicatorValuesGenerator();
		signals = g.generateSignal(data);
	}
	

}

class Animal{ //these are the things that are going to be evolved into an efficient stock trading animal.
	final double STARTING_MONEY = 1000;
	public double[] chromosones;
	double[] buyRange;
	double[] sellRange;
	double[] buyHowMuch;
	double portfolio;
	double liquid;
	double invested;
	double number_of_stocks;
	List<Signal> signals;
	double SignalInput;
	double price;
	final boolean DEBUG = false;  //because it works now.
	public Animal(double[] chro){ //as far as you can tell this constructor is flawless. damn right.
		this.chromosones = chro;  //[0] - [1] is buy range,   [2] - [3] is sell range. They can't overlap. //and thanks to this constructor they don't
		sellRange = new double[2];
		buyRange = new double[2];
		buyHowMuch = new double[20];
		sellRange[0] = chromosones[0];
		sellRange[1] = chromosones[1];
		buyRange[0] = chromosones[2];
		buyRange[1] = chromosones[3];
		for(int x = 4; x <  chromosones.length; x++){
			buyHowMuch[x-4] = chromosones[x];
		}
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
		if((buyRange[0] < sellRange[0]) && (sellRange[0] < buyRange[1]) && (buyRange[0] < sellRange[1]) && (sellRange[1] < buyRange[1])){ //if sellrange is inside of buyrange
			temp1 = sellRange[0];     //its like this    [   [  ]   ]  --->    [   ]    [   ]
			temp2 = sellRange[1];
			temp3 = buyRange[1];
			buyRange[1] = temp1;
			sellRange[0] = temp2;
			sellRange[1] = temp3;
		}
		signals = Market.signals;
		portfolio = STARTING_MONEY; //this gives animal 1000 to invest.
		liquid = 1000; //thats 1000$ liquid,  0$ in stocks.
		number_of_stocks = 0;
		invested = 0;
	}
	public void simulate(){ //problem because somehow portfolio equals like 8000. Which makes no sense at all.
		System.out.print("$");
		Signal osc = signals.get(0); //this block needs to reset before the loop.
		Signal prices = signals.get(2);
		for(int i = 0; i < signals.size(); i++) signals.get(i).reset(); //kay bruh
		while(osc.next() == -1); //this was it I think. Before you fixed it, it was osc.next() != -1.
		osc.back();  
		prices.Index = osc.Index;
		if(DEBUG) System.out.println("==================Beginning to Simulate Animal==================");
		boolean changed = false;
		portfolio = STARTING_MONEY; //this is for resetting everything. You think this causes a problem.
		liquid = STARTING_MONEY;
		number_of_stocks = 0;
		invested = 0;
		
		while(osc.ready()){ //each turn of the loop represents a single market day.
			SignalInput = osc.next();
			price = prices.next();   
			invested = number_of_stocks * price; 
			portfolio = liquid + invested; 
			if(changed){
				//System.out.println("Portfolio " + portfolio);
				changed = false;
			}
			//if(DEBUG) System.out.println("Signal Input " + SignalInput);
			if(canBuy(price) && (buyRange[0] < SignalInput) && (SignalInput < buyRange[1])){changed = true; buy();}
			else if(canSell() && (sellRange[0] < SignalInput) && (SignalInput < sellRange[1])){changed = true; sell();}
			else hold();
		}
		if(DEBUG) System.out.println("==================Ending Simulation============");
	}
	public boolean canSell(){
		return number_of_stocks > 0;
	}
	public boolean canBuy(double p){
		return p < liquid;
	}
	public void buy(){ //this is fucking up somehow. Dont know how.
		//not important if(DEBUG) System.out.println("begin of buy  stock l["+ liquid +"] inv[" + invested + "] po[" + portfolio + "] N[" + number_of_stocks + "] Pr[" + price + "]");
		//how much
		int howmuch = 0;
		//System.out.println("howmuch " + Arrays.toString(buyHowMuch));
		for(int x = 1; x < buyHowMuch.length; x++){
			if((x == 0) && (SignalInput < buyHowMuch[0])){ //this is a bit of a special case.
				howmuch = x + 1;
				break;
			}
			else if((buyHowMuch[x-1] < SignalInput) && (SignalInput < buyHowMuch[x])){
				howmuch = x +1;
				//if(DEBUG) System.out.println("howmuch " + howmuch);
				break;
			}
		}
		while(liquid < (howmuch * price)){
			howmuch --;
			if(howmuch == 0) return;
		}
		liquid -= ( price * howmuch); //
		invested += price * howmuch; //
		number_of_stocks += howmuch; //
		portfolio = liquid + invested; //finally fixed.
		if(DEBUG) System.out.println("buying " + howmuch + " stock l["+ liquid +"] inv[" +invested+ "] po[" + portfolio + "] N[" + number_of_stocks + "] Pr[" + price + "]");
	}
	public void sell(){
		liquid += price * number_of_stocks;  //this just indiscriminately sells everything.
		invested -= price * number_of_stocks;
		number_of_stocks = 0;
		portfolio = invested + liquid;
		if(DEBUG) System.out.println("selling all l["+ liquid +"] inv[" + invested + "] p[" + portfolio + "] N[" + number_of_stocks + "] Pr [" + price + "]");
	}
	public void hold(){
		//holding;
		//if(DEBUG) System.out.println("holding");
		if(DEBUG) System.out.println("holding stock l["+ liquid +"] inv[" +invested+ "] po[" + portfolio + "] N[" + number_of_stocks + "] Pr[" + price + "]");

		return;
	}
	public double getProfit(){
		return portfolio; //note. Number can be negative if you make negative gains. 
		                                   //Evolution engine doesn't handle negative fitness. Shit.
		                                   //Ill just return the portfolio value then. Good idea.
	}
}

class Factory extends AbstractCandidateFactory<Animal>{ //this is going to create new animals for testing.
	public Factory(){
		
	}
@Override
    public Animal generateRandomCandidate(Random r) {
	    DoubleStream dubbs = r.doubles(24); //4 for sell range buy range and the rest is the amount to buy.
	    double[] rands = dubbs.toArray();
	    for(int x = 0; x < 4; x++){
	    	rands[x] *= 100; //because signal is 1-100 not 0-1.
	    }
	    double temp[] = new double[2];
	    temp[0] = rands[2];
	    temp[1] = rands[3];
	    Arrays.sort(temp);  //[0]is biggest [1] is smallest
	    double scale = temp[0] - temp[1];  //multipoly it by the dubbs
	    double temp2[] = new double[20];
	    for(int x = 4; x < 24; x++){
	    	temp2[x-4] = (temp[1] + (rands[x] * scale));	
	    }
	    Arrays.sort(temp2);
	    for(int x = 0; x < temp2.length; x++){
	    	rands[x+4] = temp2[x];
	    }
	    double[] ret = new double[24];
	    for(int x = 0; x < ret.length; x++){
	    	ret[x] = rands[x]; //lets just copy this array into one that is size 24 just in case rands is actually really big.
	    }
	    return new Animal(ret);
    }	
    
}

class AnimalCrossover implements EvolutionaryOperator<Animal>{ //these are the operators which evolve the animals.
	final int NUMBER_OF_OFFSPRING = 2;
	final boolean DEBUG = false;  //I think everything works here so no need to debug.
	public AnimalCrossover(){
		
	}

	@Override
	public List<Animal> apply(List<Animal> Animals, Random rand) {
	    if(DEBUG) System.out.println("# of animals before breeding " + Animals.size());
		List<Animal> parents = new ArrayList<>();
		List<Animal> offspring;
		List<Animal> generation = new ArrayList<>(); 
		int which = 0;
		while(true){
			offspring = new ArrayList<>(); //because we call .add() method. Which would keep adding them 
			//and never delete the list. And each loop it adds every element of this list to the new generation. 
			//So offspring would increse each loop and old animals would get added to generation.
			//This was the bug that caused exponential population growth. Now growth is stable.
			if((which+1) >= Animals.size()) break; //cant have .get return error.
			parents.add(Animals.get(which));
			parents.add(Animals.get(which+1));
			which += 2;
			for(int x = 0; x < NUMBER_OF_OFFSPRING; x++) //so each set of parents makes whatever amount of babies.
				offspring.add(breed(parents, rand)); //breed returns only one animal.
			for(Animal baby : offspring){
				generation.add(baby);
			}
		}
	    if(DEBUG) System.out.println("# of animals after breeding " + generation.size());
		return generation;
	}
	public Animal breed(List<Animal> parents, Random rand){
		DoubleStream r = rand.doubles(24); //4 for sell range buy range and the rest is the amount to buy.
	    double[] dubbs = r.toArray();
		double[] mom = parents.get(0).chromosones;
		double[] dad = parents.get(1).chromosones;
		double[] baby = new double[mom.length];
		for(int x = 0; x < mom.length; x++){
	        dubbs[x] *= 2;
	        if(dubbs[x] < 1)       baby[x] = dad[x];  //50% chance you get mom genes.
	        else if(dubbs[x] >= 1) baby[x] = mom[x];  //50% chance you get dad genes.
		}
		double[] howmuch = new double[20];         //this sorts the howmuch part of the chromosone because else weird shit might happen. I dont know what
	    System.arraycopy(baby, 4, howmuch, 0, 20);
	    Arrays.sort(howmuch);
	    System.arraycopy(howmuch, 0, baby, 4, 20);
	    if(DEBUG) System.out.println("Offspring Chromosone " + Arrays.toString(baby));  
		return new Animal(baby); //thankfully constructor sorts out the chromosone.
	}
}

class AnimalMutation implements EvolutionaryOperator<Animal>{ //idea. Instead of mutating, it will just get a whole new random animal. Yeah.
	final double CHANCE_OF_MUTATION = .90; //my idea idea is high elitism and high mutation.
	final boolean DEBUG = false; //everything is good, no need to debug
	int n;
	public AnimalMutation(){
		
	}

	@Override
	public List<Animal> apply(List<Animal> animals, Random rand) {
		n = 0;
		List<Animal> generation = new ArrayList<>();
		//if(DEBUG) System.out.println("Population before mutation " + animals.size()); this reveals that there is no change in population between mutations. good.
		for(Animal animal : animals){
			generation.add(mutate(rand, animal));
		}
		System.out.println("=================================================");
		System.out.println(">>>>>>>>>>Number Of Mutations " + n + "<<<<<<<<<<");
		System.out.println("=================================================");
		//if(DEBUG) System.out.println("Population after mutation " + generation.size());
		return generation;
	}
	public Animal mutate(Random rand, Animal animal){
		DoubleStream r = rand.doubles(24); //4 for sell range buy range and the rest is the amount to buy.
	    double[] dubbs = r.toArray();
	    
		int which;
		double[] chromosones = animal.chromosones;
		if(dubbs[0] <= CHANCE_OF_MUTATION){
			n++;
			which = (int) (dubbs[1]*animal.chromosones.length);
			if(DEBUG) System.out.println("Mutates");
			chromosones[which] = dubbs[2]; //a couple cases here. if which was in the buy and sell ranges then they need to be scaled by 100;
			                               //else if the which specifies one of the indexes that tells how much to buy then it needs to get scaled differently.
			if(which < 4){ chromosones[which] *= 100;} //scaled by 100.
			else{
				//chromosones[which]; //this is the one that will be mutated.
				double temp[] = new double[2];  //this will hold the buy range.
				
			    temp[0] = chromosones[2];
			    temp[1] = chromosones[3]; //this defines the buy range.
			    
			    double scale = temp[0] - temp[1];  //this is the length of the buy range.
			    
			   	chromosones[which] = (temp[1] + (chromosones[which] * scale)); //makes good sense.
			   	
			   	double[] howmuch = new double[20];
			    System.arraycopy(chromosones, 4, howmuch, 0, 20);
			    Arrays.sort(howmuch);
			    System.arraycopy(howmuch, 0, chromosones, 4, 20);
			}
			return new Animal(chromosones);
		}else return animal; //no mutation.
	}
}

class AnimalEvaluator implements FitnessEvaluator<Animal>{ //no errors here
	public AnimalEvaluator(){
		
	}
	@Override
	public double getFitness(Animal animal, List<? extends Animal> arg1) {
		animal.simulate(); //makes sense I think. 
		double buf = animal.getProfit();
		//System.out.println(buf);
		return buf;
	}
	@Override
	public boolean isNatural() {
		return true;
	}
}

class Observer implements EvolutionObserver<Animal>{
	PrintWriter file;
	final String filename = "/home/Justin/data/log";
	public Observer(){
		try{
			file = new PrintWriter(filename);
		}catch(Exception e){}
		
	}

	@Override
	public void populationUpdate(PopulationData<? extends Animal> animal) {
		Market.Best = animal.getBestCandidate();
		System.out.println("\n==============[EVOLUTION OBSERVER]===============");
		System.out.println("Fittest Candidate's Score: " + animal.getBestCandidateFitness());
		System.out.println("Generation "+animal.getGenerationNumber());
		System.out.println("Population " + animal.getPopulationSize());
		System.out.println("Elapsed Time " + animal.getElapsedTime());
		System.out.println("=================================================");
		System.out.println("=================================================");
	    System.out.println(">>>>>>>>>>>>>[Initiating Simulation]<<<<<<<<<<<<<");
	    System.out.println("=================================================");
	    file.println("\n==============[EVOLUTION OBSERVER]===============");
		file.println("Fittest Candidate's Score: " + animal.getBestCandidateFitness());
		file.println("Generation "+animal.getGenerationNumber());
		file.println("Population " + animal.getPopulationSize());
		file.println("Elapsed Time " + animal.getElapsedTime());
		file.println("=================================================");
		file.println("=================================================");
	    file.println(">>>>>>>>>>>>>[Initiating Simulation]<<<<<<<<<<<<<");
	    file.println("=================================================");
	    file.flush(); //it wasn't printing all of it.

	    
	}
}
