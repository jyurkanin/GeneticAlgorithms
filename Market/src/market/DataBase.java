package market;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

class DataBase{
     static URL api;
     static BufferedReader input;
	static String[] getIntraData(String ticker){ //this will automatically get the intra-day information for the past 15 days
		try{
			List<String> response = new ArrayList<String>();
			api = new URL("chartapi.finance.yahoo.com/instrument/1.0/" + ticker + "/chartdata;type=quote;range=15d/csv");
			input = new BufferedReader(new InputStreamReader(api.openStream()));
			do{
				if(input.readLine().split(":")[0].equals("volume")) break; //this parses to the start of the data.
			}while(input.ready());
			
			while(input.ready()){
				response.add(input.readLine());
			}
			return response.toArray(new String[1]); //function is smart enough to realize input is wrong size and it makes a bigger one.
			 
		}
		catch(MalformedURLException e){}
		catch(IOException e){}
		return null;
	}
	static String[] getTestData(){ //this works just fine.
		try{
			List<String> response = new ArrayList<>();
			BufferedReader input = new BufferedReader(new FileReader("/home/Justin/data/TEST"));
			do{
				response.add(input.readLine());
			}while(input.ready());
			input.close();
			return response.toArray(new String[response.size()]);
		}
		catch(IOException e){}
		return null;
	}
	
    static String getStringDate(){
        DateFormat df = new SimpleDateFormat("yy-MM-dd");
        Date d = new Date();
        String dt = df.format(d);
        return dt;
    }  //returns yy/mm/dd
}
