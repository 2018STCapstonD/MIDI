import java.io.*;
import java.net.*;

public class Server {
	 public static void main(String[] args) {
	      ServerSocket ss;
	      Socket sc;
	      BufferedReader in;
	      PrintWriter out;

	      try {
	         ss = new ServerSocket(37771);
	         sc = ss.accept();
	         System.out.println(sc.getInetAddress() +"server connect");

	         in = new BufferedReader(new InputStreamReader(sc.getInputStream())); 
	         out = new PrintWriter(sc.getOutputStream(), true); 
	         String inLine = in.readLine();
	         System.out.println(inLine);

	         in.close();
	         out.close();
	         sc.close();
	         ss.close();
	      } catch(IOException ex) {
	         ex.printStackTrace();
	      }
	   }
}
