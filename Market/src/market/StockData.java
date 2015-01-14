package market;

class StockData {
	double[] Highs;
	double[] Lows;
	double[] Volumes;
	double[] Opens;
	double[] Closes;
	int Len;
	public StockData(String[] list){
		Len = list.length; //also the length of everything else.
		int index = 0;
		double[] Closes = new double[Len];
		double[] Highs = new double[Len];
		double[] Lows = new double[Len];
		double[] Opens = new double[Len];
		double[] Volumes = new double[Len];
		
		String[] buf;
		for(String line : list){
			buf = line.split(",");  //[0] is time stamp which I think is useless right now.
			Closes[index] = Double.parseDouble(buf[1]);
			Highs[index] = Double.parseDouble(buf[2]);
			Lows[index] = Double.parseDouble(buf[3]);
			Opens[index] = Double.parseDouble(buf[4]);
			Volumes[index] = Double.parseDouble(buf[5]);
			index++;
		}
	}
	public double[] getHighs() {	return Highs;} //I like it inline.
	public double[] getLows() {		return Lows;}
	public double[] getVolumes() {	return Volumes;}
	public double[] getOpens() {	return Opens;}
	public double[] getCloses() {	return Closes;}
	public int getLen() {				return Len;}
}