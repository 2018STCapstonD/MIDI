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
			ProcessBuilder pb = new ProcessBuilder("python","..\\..\\PServer\\preprocess.py");
			BufferedWriter bufWr = Files.newBufferedWriter(Paths.get("..\\..\\PServer\\tempdata.csv"));
			
			InetAddress inetAddr = sock.getInetAddress();
			System.out.println("접속 : " + inetAddr.getHostAddress());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			bufWr.write("kakao_id|title|album|artist|rating\n");
			while((s = in.readLine()) != null){
				bufWr.write(s+"\n");
				System.out.println(s);
			}
			pb.start();
			out.close();
			in.close();
			bufWr.close();
			sock.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}