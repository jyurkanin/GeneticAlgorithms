package market;

import com.tictactec.ta.lib.*;

import java.util.ArrayList;
import java.util.List;


public class IndicatorValuesGenerator {
	Core c;
	public IndicatorValuesGenerator(){
		c = new Core();
	}
	public List<Signal> generateSignal(StockData d){ //just returns the ultOsc for now.
		List<Signal> signals = new ArrayList<>();
		int inLen = d.getHighs().length;
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		int lookback = c.ultOscLookback(7, 14, 28);
		int outLen = inLen - lookback;
		double[] outReal = new double[outLen];
		c.ultOsc(0, inLen - 1, d.getHighs(), d.getLows(), d.getCloses(), 7, 14, 28, outBegIdx, outNBElement, outReal);
		double[] ultOscs = new double[inLen];
		for(int x = 0; x < inLen; x++){
			ultOscs[x] = -1;  //just for the initialization
		}
		System.arraycopy(outReal, 0, ultOscs, outBegIdx.value, outReal.length);
		signals.add(new Signal(ultOscs));
		signals.add(new Signal(d.getHighs()));
		signals.add(new Signal(d.getCloses()));
		signals.add(new Signal(d.getLows()));
		signals.add(new Signal(d.getOpens()));
		signals.add(new Signal(d.getVolumes()));
		return signals;
	}
}

class Signal{  //sort of like a stream.
	double[] Data;
	int Index;
	public Signal(double[] buf){
		Data = buf;
		Index = 0;
	}
	public double next(){
		double buf = Data[Index];
		Index++;
		return buf;
	}
	public void back(){
		Index--;
	}
	public boolean ready(){
		return Index < Data.length;
	}
}