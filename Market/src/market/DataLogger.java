package market;
//so usually this program runs in a separate machine altogether but right now it doesn't.
//TODO make this whole program run as a thread in the main program.
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
/**
 * @author root
 */
public class DataLogger implements Runnable{
    static String tickers[];
    static volatile String flag = "";
    final static String dir = "/home/Justin/data/";
    static Port port;
    static boolean NetworkConnection = true;
    public static void main(String args[]){
        try{
            tickers = getTickers(); 
            port = new Port();
            //this tests network connection
            try(Socket sock = new Socket("www.yahoo.com" , 80)){
                sock.setSoTimeout(2000);
                NetworkConnection  = sock.isConnected();
            }
            catch(Exception e){NetworkConnection = false;}
            
            for(int x = 0; x < tickers.length; x++){
                //skip updating from yahoo if we have no network connection.
                if (tickers[x].equals("") || 
                    !NetworkConnection || 
                    Arrays.equals(getLastDateInFile(tickers[x]), getDate())) continue;  //if the files are up to date for that ticker try the next one.
                System.out.println("Getting historical prices");
                if(port.readFile(tickers[x]).length() < 1){
                    writeFile(tickers[x], readAPI(tickers[x], new int[]{0,0,0}, getDate()));
                }
                else{
                    String str;
                    str = readAPI(tickers[x], incrementDate(getLastDateInFile(tickers[x])), getDate());
                    appendFile(tickers[x], str);
                }
            }
            System.out.println("we Just read stuff");
            if(!NetworkConnection) System.out.println("Except we didn't because theres no network connection. :(");
            
            Thread console = new Thread( new DataLogger() );
            Thread SocketInterface = new Thread(port);
            console.setDaemon(true);
            SocketInterface.setDaemon(true);
            console.start();
            SocketInterface.start();
            loop();
        }
        catch(Exception e){e.printStackTrace();}
    }
    public static void loop() {
        int lastDate = getDate()[1];
        int currentDate;
        String line = "";
        try{
            while(!flag.equals("quit")){
                currentDate = getDate()[1];
                if(lastDate != currentDate){ //checks to see if a day has gone by.
                    lastDate = getDate()[1];
                    for(int x = 0; x < tickers.length; x++){
                        do{
                            if(!line.equals("")) Thread.sleep(1800000); //checks every 30 minutes after midnight if we can get new info for today.
                            line = readAPI(tickers[x], getDate(), getDate());  //gets one line of datas for a single date.
                        }while(!line.split(",")[0].equals("20" + getStringDate()));
                        appendFile(tickers[x], line);
                        line = "";
                    }
                }
                else{
                    System.out.println("Database Is Up To Date");
                }
                Thread.sleep(600000); //this sleeps for 10 minutes.
            }
        }
        catch(InterruptedException e){}
        System.out.println("Closing");
        port.close();
    }
    public void run(){
        Scanner scan = new Scanner(System.in);
        String in;
        System.out.println("Console:");
        while(true){
            in = scan.nextLine();
            if(in.equals("quit")){
                System.out.println("Quitting");
                port.close();
                flag = "quit";
            }
            else if(in.equals("ls") && (port.sock.getInetAddress() != null)) System.out.println(port.sock.getInetAddress());
        }
    }
    public static int[] getDate(){
        DateFormat df = new SimpleDateFormat("yy-MM-dd");
        Date d = new Date();
        String dt = df.format(d);
        String dateString[] = dt.split("-");
        int dateInts[] = new int[dateString.length];
        for(int x = 0; x < dateString.length; x++){
            dateInts[x] = Integer.parseInt(dateString[x]);
        }
        dateInts[1]--;  //because someone decided to make the months zero indexed...
        return dateInts;
    }
    public static int[] incrementDate(int d[]){ // d is yy/mm/dd. Remember. but is also can be yy-m-d which throws exception.....
        String split[] = new String[3];
        split[0] = String.valueOf(d[0]);  //unrolled iteration. takes up same space anyways.
        split[1] = String.valueOf(d[1]+1);
        split[2] = String.valueOf(d[2]);
        //this converts it to a string which can be used with this function.
        if(split[0].length() == 2) split[0] = "20" + split[0]; //these conditionals add leading zeroes.
        if(split[1].length() == 1) split[1] = "0" + split[1];  //Because the function is pretty finicky and only accepts certain stuff.
        if(split[2].length() == 1) split[2] = "0" + split[2];
        String nextDate = (LocalDate.parse(split[0]+"-"+split[1]+"-"+split[2]))  //because d only has the last two digits of the year. 14. in 2014.
                .plusDays(1)
                .toString();//this makes the date object.
        split = nextDate.split("-");
        if(split[0].length() == 4) split[0] = split[0].replace("20", ""); //this converts the date back into yy-m-d or yy-mm-dd
        if(split[1].charAt(0) == '0') split[1] = String.valueOf(split[1].charAt(1)); //meaning it gets id of stupid leading zeroes.
        if(split[2].charAt(0) == '0') split[2] = String.valueOf(split[2].charAt(1));
         //converts strings to ints. Because thats what readapi needs.
        d[0] = Integer.parseInt(split[0]);
        d[1] = Integer.parseInt(split[1]) - 1;
        d[2] = Integer.parseInt(split[2]);
        return d; 
    }
    public static String getStringDate(){
        DateFormat df = new SimpleDateFormat("yy-MM-dd");
        Date d = new Date();
        String dt = df.format(d);
        return dt;
    }  //returns yy/mm/dd
    public static String readAPI(String ticker, int date[], int todate[]){//[0] is yy; [1] is mm; [2] is dd;
        try{
            URL finance;
            BufferedReader url;
            String r = "";
            String s = "";
            boolean flag1 = Arrays.equals(todate, getDate());
            int list[] = new int[3];
            if(!Arrays.equals(date, getDate())){
                finance = new URL("http://ichart.yahoo.com/table.csv?s="+ticker+"&a="+date[1]+"&b="+date[2]+"&c="+date[0]+"&d="+todate[1]+"&e="+todate[2]+"&f="+todate[0]);
                url = new BufferedReader(new InputStreamReader(finance.openStream()));
                url.readLine();  //this gets rid of the high low close volume test that yahoo returns.
                if(url.ready()) r = fixDate(url.readLine());  //this is for line formatting so that we don't have extra \n's on the ends.
                /*list[0] = Integer.parseInt(s.split(",")[0].split("-")[0].replace("20", ""));
                list[1] = Integer.parseInt(s.split(",")[0].split("-")[1]) - 1;
                list[2] = Integer.parseInt(s.split(",")[0].split("-")[2]);
                if(Arrays.equals(list, getDate())){
                    flag1 = false;
                    r = s;
                }*/
                while(url.ready()){
                    s = url.readLine();
                    r = (fixDate(s) + "\n" + r);
                } //note due to the nature of the api, this cycles from oldest to newest which was causing problems earlier.
                url.close();
            }
            /*if(flag1){ //I hate these special cases.
                //System.out.println("Current Date Data is Also Being Retrieved \nBecause yahoo api is retarded thus \n neccessitating a special case for current data.");
                finance = new URL("http://download.finance.yahoo.com/d/quotes.csv?s="+ticker+"&f=ohgl1v&e=.csv");
                url = new BufferedReader(new InputStreamReader(finance.openStream()));
                s = fixDate(url.readLine());
                r = (r + "\n" + s);
            }*/
            return r;
        }
        catch(Exception e){e.printStackTrace();}
        return "";     
    }   
    public static int[] getLastDateInFile(String name){
        try(BufferedReader in = new BufferedReader(new FileReader(dir+name)))
        {
            String line = "";
            String last = "";
            //String lines[] = new String[5];
            while(in.ready()){
                last = line;
                line = in.readLine();
            }
            if(line.length() < 5) line = last;
            System.out.println("Last Date in DataBase: " + line);
            int list[] = new int[3];
            list[0] = Integer.parseInt(line.split(",")[0].split("-")[0].replace("20", ""));
            list[1] = Integer.parseInt(line.split(",")[0].split("-")[1]) - 1;
            list[2] = Integer.parseInt(line.split(",")[0].split("-")[2]);
            return list;  //this gets the date.
        }
        catch(Exception e){e.printStackTrace();}
        return new int[]{0,0,0};
    }
    public static double getClose(String data){
        return Double.parseDouble(data.split(",")[4]); //4 is closes.
    }
    public static double getHigh(String data){
        return Double.parseDouble(data.split(",")[3]); //3 is high.
    }
    public static double getLow(String data){
        return Double.parseDouble(data.split(",")[2]); //2 is lows
    }
    public static double getVolume(String data){
        return Double.parseDouble(data.split(",")[5]); //5 is volume.
    }
    public static void writeFile(String filename, String data){
        try(PrintWriter outputWire = new PrintWriter(dir+filename, "UTF-8"))
        {
            outputWire.print(data);
            outputWire.close();
        }
        catch(Exception e){}
    }
    public static void appendFile(String filename, String data){
        System.out.println("APpending to file");
        System.out.println(data);
        String old = port.readFile(filename);
        String dat[];
        try(PrintWriter outputWire = new PrintWriter(dir+filename, "UTF-8"))
        {
            outputWire.append(old);
            dat = data.split("\n");
            /*data = "";
            dat[0] = "";  //because.
            for(int x = 0; x < dat.length-1; x++){
                data += (dat[x]+"\n");
            }
            data += dat[dat.length-1];*/
            outputWire.append("\n" + data);
            outputWire.close();
        }
        catch(Exception e){}
    }
    public static String[] getTickers(){
        List<String> t = new ArrayList<>(); 
        String line;
        try(BufferedReader file = new BufferedReader(new FileReader(dir+"TICKERS"));){
            while(file.ready()){
                line = file.readLine();
                if(line.length() > 0) t.add(line); //I might accidentaly have an extra \n at the end of file which would create a "" ticker.
            }
            if(t.isEmpty()) return null;
            else return t.toArray(new String[1]); //since the array that gets passed to function is too small function is smart and returns bigger one            
        }
        catch(Exception e){}
        return null;
    }
    public static String fixDate(String unparsed){  //this gets rid of leading zeroes. 2014-04-05 is replaced with 2014-4-5
                     //this is to handle the special case because retard data 
                     //has no date prefixed so it will loop once and today's date 
                     //will be added and then it will loop again an it will be corrected.
        String date[] = unparsed.split(",")[0].split("-");
        if(date.length == 3){
            if(date[1].charAt(0) == '0') date[1] = String.valueOf(date[1].charAt(1));
            if(date[2].charAt(0) == '0') date[2] = String.valueOf(date[2].charAt(1));
            String d =  date[0] + "-" + date[1] + "-" + date[2];
            for(int x = 1; x < unparsed.split(",").length; x++){
                d += ("," + unparsed.split(",")[x]);
            }
            return d;
        }
        else{
            String s = getStringDate();
            unparsed =  "20" + s + "," + unparsed;
            return fixDate(unparsed); //woah dude
        }
    }
    public static boolean isMarketDay(){
        return false;
    }
}

class Port implements Runnable{ 
    Socket sock;
    BufferedReader input;
    PrintWriter output;
    ServerSocket serverSocket;
    final String dir = "/home/Justin/data/";
    public Port() throws IOException{
        serverSocket = new ServerSocket(1025);
    }
    public void run(){
        try{
            while(true){
                sock = serverSocket.accept(); //it is so helpful that this blocks.
                System.out.println("Connection: " + sock.getInetAddress());
                input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                output = new PrintWriter(sock.getOutputStream());
                while(!sock.isClosed()){ //the counter helps it determine that an error has occured.
                    if(input.ready()){
                        handleRequest();
                        if(!sock.isConnected() || !sock.isBound()) sock.close();
                    }
                }
                System.out.println("disconnected");
            }
        }
        catch(IOException e){}
    }
    public void handleRequest(){
        System.out.println("Got a query.");
        String in[];
        String t;
        String st = "";
        String date;
        String temp;
        try{
            while(input.ready()){
                st = input.readLine();
            }
            if(st.equals("")){
                sock.close();
                System.out.println("Closed the sock.");
                return;
            }
            else if(st.equals("END_OF_TRANSMISSION")){
                sock.close();
                System.out.println("Overriding EOT Signal Recieved.");
                return;
            }
            else if(st.equals("LS_TICKERS")){
                temp = "[-";
                for(String tick : DataLogger.tickers){
                    temp += (tick + "-");
                }
                temp += "]";
                output.println(temp);
            }
        }
        catch(Exception e){}
        in = st.split(",");  //this is the input data from the socket.
        for(String s : in){
            System.out.println("Query " + s); //works up to here.
        }
        t = in[0];
        String buf[] = new String[3];
        buf[0] = in[1].split("-")[0];   //this gets the month because it needs to be decreased by one so that yahoo reads it correctly.
        buf[1] = in[1].split("-")[1];  //converts String to int, subtracts one, then bak to String.
        buf[2] = in[1].split("-")[2];
        in = buf;
        date = "";
        date += (buf[0] + "-");
        date += (buf[1] + "-");
        date += buf[2];
        date = "20" + date; //because it only gives me the last two digits of the actual year. 2014.
        System.out.println("Interpreted Date " + date);
        
        try{
            if(in.length == 0){
                System.out.println("There was no query.\nCLosing socket.");
                sock.close();
                return;
            }  //so in[0] == ticker. in[1] == todate. number of values before date == 100.
            
            String ticker = "";
            for(String name : DataLogger.tickers){
                if(t.equals(name)){  //It won't be able to read the files...
                    ticker = name;
                    break;
                }
            }
            //System.out.println("Ticker Requested " + ticker);   //This works. 30 MIght Change!!!
            String response[] = new String[100];  //so that it returns the specified number of data points.
            String dat;
            System.out.println("Going to Send: ");
            BufferedReader file = new BufferedReader(new FileReader(dir+ticker));
            boolean flag = false;
            while(file.ready()){
                dat = file.readLine();
                if(dat == null) continue;
                if(dat.equals("") || !dat.contains(",")) continue; //[0] is the date. yy/mm/dd
                response = shift(response, dat);
                //System.out.println("dat.split(-)[0] " + dat.split(",")[0]);
                //System.out.println("date " + date);
                if(dat.split(",")[0].equals(date)){
                    flag = true;
                    break; //break and return the list with the values from the past $X days.
                }//this tries to find the data with the right date.
            }
            file.close();
            
            for(String line : response){
                System.out.println("Line " + line);
                if(line == null) flag = false;
            }
            
            System.out.println("Was Sent");
            if(flag) print(response);
            else{ 
                print("No Data");
                System.out.println("No Data. Shit Fucked Up.");
            }
        }
        catch(Exception e){e.printStackTrace();}
    }
    public String[] shift(String in[], String add){ //ex; in = {4, 3} 4 is newest price. 3 is oldest.
        String out[] = new String[in.length];
        for(int x = 0; x < in.length-1; x++){  //first index should refer to the newest price.
            out[1+x] = in[x];    //out will then equal {add, 4}... Good.
        }
        out[0] = add;
        return out;
    } 
    public String read(){
        try{return input.readLine();}
        catch(IOException e){return null;}
    }
    public void print(String text){
        output.println(text);
        output.flush();
    }
    public void print(String text[]){
        for(String line : text){
            output.println(line);
        }
        output.flush();
    }
    public void close(){
        try{
            if(sock.isBound()) sock.close();
            serverSocket.close();
        }
        catch(IOException e){}
    }
    public String readFile(String file){
        String data = "";
        String line;
        try(BufferedReader inputWire = new BufferedReader(new FileReader(dir+file))){
            data = data + inputWire.readLine();
            while(inputWire.ready()){
                line = inputWire.readLine();
                //if(line.equals("\n")) continue;
                data  = (data + "\n" + line);
            }
            return data;
        }
        catch(Exception e){return "";}
    }
    public String readLineFile(String fn){
        try(BufferedReader buf = new BufferedReader(new FileReader(dir+fn))){
            return buf.readLine();
        }
        catch(Exception e){}
        return "";
    }
}