package market;

import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;


public class Market {
	static GenerationalEvolutionEngine<Animal> engine;
	static CandidateFactory<Animal> candidateFactory;
	static List<EvolutionaryOperator<Animal>> operators = new LinkedList<EvolutionaryOperator<Animal>>();
	static FitnessEvaluator<Animal> fitnessEvaluator;
	static SelectionStrategy<Animal> selectionStrategy;
	static DataInterface port;
	public static void main(String[] args) {
		port = new DataInterface();
		candidateFactory = new Factory();
		operators = new LinkedList<EvolutionaryOperator<Animal>>();
		operators.add(new AnimalCrossover());
		operators.add(new AnimalMutation());
		EvolutionaryOperator<Animal> pipeline = new EvolutionPipeline<Animal>(operators);
		
		engine = new GenerationalEvolutionEngine<Animal>(candidateFactory, pipeline, fitnessEvaluator, selectionStrategy, rng);;

	}
	

}

class Animal{ //these are the things that are going to be evolved into an efficient stock trading animal.
	public Animal(){
		
	}
}

class Factory implements CandidateFactory<Animal>{ //this is going to create new animals for testing.
	public Factory(){
		
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
}

class DataInterface{
    PrintWriter output;
    BufferedReader input;
    Socket sock;
    final boolean DEBUG = false;
    public DataInterface(){
        sock = new Socket();
        try{
            sock = new Socket("127.0.0.1", 1025);
            input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            output = new PrintWriter(sock.getOutputStream(), true);
        }
        catch(IOException e){}
    }
    public String[] query(String ticker){ //way this works is
    	try{                              //it querys server and
    		               //server always responds with the last 100 
    		               //data points in the form in the form
    		               //Date,Open,High,Low,Close,Volume,Adj Close
    		               //last one is not important... Dont even know what adj close is.
    		output.println("ticker,"+getStringDate());
    		String[] response = new String[100];
    		int index = 0;
    		while(input.ready() && (index <= 100)){
    			response[index] = input.readLine();
    			index++;
    		}
    		return response;
    	}
    	catch(IOException e){
            return null;
    	}
    	
    }
    public static String getStringDate(){
        DateFormat df = new SimpleDateFormat("yy-MM-dd");
        Date d = new Date();
        String dt = df.format(d);
        return dt;
    }  //returns yy/mm/dd
}
