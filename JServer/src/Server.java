import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
	public static void main(String[] args) {
		try {
			ServerSocket ss;
			ss = new ServerSocket(37771);
			System.out.println("Server wait...");
			while(true) {
				Socket sock = ss.accept();
				TCPServerThread thr = new TCPServerThread(sock);
				thr.start();
			}

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}

class TCPServerThread extends Thread{
	private Socket sock;
	public TCPServerThread(Socket sock) {
		this.sock = sock;
	}
	public void run() {
		try {
			String s = "";
			String path = Paths.get(this.getClass().getResource("").toURI()).toString();
			System.out.println(path);
			BufferedWriter bufWr = Files.newBufferedWriter(Paths.get(path+"\\..\\..\\PServer\\tempdata.csv"));
			
			InetAddress inetAddr = sock.getInetAddress();
			System.out.println("접속 : " + inetAddr.getHostAddress());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			bufWr.write("kakao_id"+"\t"+"title"+"\t"+"album"+"\t"+"artist"+"\t"+"rating"+"\t"+"musicID"+"\n");
			while((s = in.readLine()) != null){
				String[] data = s.split("\t");
				if(!(data[0].equals(null) | data[1].equals(null) | data[2].equals(null) | data[3].equals(null) | data[4].equals(null) | data[4].equals(null))) {
					int to_hash = (data[1]+data[2]).hashCode(); 
					bufWr.write(s+"\t"+to_hash+"\n");
					System.out.println(s+"\n");
				}
			}
			out.close();
			in.close();
			bufWr.close();
			sock.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
	            String path = null;
				try {
					path = Paths.get(this.getClass().getResource("").toURI()).toString();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ProcessBuilder pb = new ProcessBuilder("python",path +"\\..\\..\\PServer\\preprocess.py");
	            pb.start();
			}
			catch(IOException e) {}
		}
	}
}