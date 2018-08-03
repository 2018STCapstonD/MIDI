import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
			BufferedWriter bufWr = Files.newBufferedWriter(Paths.get("..\\..\\PServer\\tempdata.csv"));
			
			InetAddress inetAddr = sock.getInetAddress();
			System.out.println("접속 : " + inetAddr.getHostAddress());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			bufWr.write("kakao_id"+"\t"+"title"+"\t"+"album"+"\t"+"artist"+"\t"+"rating"+"\t"+"musicID"+"\n");
			while((s = in.readLine()) != null){
				String[] data = s.split("\t");
				int to_hash = (data[1]+data[2]).hashCode(); 
				bufWr.write(s+"\t"+to_hash+"\n");
				System.out.println(s+"\n");
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
	            ProcessBuilder pb = new ProcessBuilder("python","C:\\Users\\ITS_1\\Documents\\MIDI\\PServer\\preprocess.py");
	            pb.start();
			}
			catch(IOException e) {}
		}
	}
}