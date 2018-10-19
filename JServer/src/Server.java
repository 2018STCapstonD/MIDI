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
import java.util.Arrays;

public class Server {
	public static void main(String[] args) {
		try {
			ServerSocket ss;
			//초 단위 실행
			TrainThread trThread = new TrainThread(60);
			trThread.start();
			ss = new ServerSocket(37771);
			System.out.println("Server wait...");
			while(true) {
				Socket sock = ss.accept();
				TCPServerThread srvThread = new TCPServerThread(sock);
				srvThread.start();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}

class TrainThread extends Thread{
	private String path = null;
	private int time = 0;
	
	public TrainThread(int time) {
		//초단위
		this.time = time*1000;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while(true) {
				path = Paths.get(this.getClass().getResource("").toURI()).toString();
				ProcessBuilder pb = new ProcessBuilder("python",path +"\\..\\..\\PServer\\train.py");
				pb.start();
				System.out.println("Training...");
				sleep(time);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}

class TCPServerThread extends Thread{
	private Socket sock;
	String s = "";
	String kakao_id = "";
	String path = "";
	String musicIn = "";
	BufferedReader bufRd = null;
	BufferedWriter bufWr = null;
	BufferedReader in = null;
	PrintWriter out = null;
	InetAddress inetAddr = null;
	public TCPServerThread(Socket sock) {
		this.sock = sock;
		inetAddr = sock.getInetAddress();
	}
	public void run() {
		try {
			
			path = Paths.get(this.getClass().getResource("").toURI()).toString();
			bufRd = Files.newBufferedReader(Paths.get(path+"\\..\\..\\PServer\\userRecs.csv"));
			bufWr = Files.newBufferedWriter(Paths.get(path+"\\..\\..\\PServer\\tempdata.csv"));
			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			out = new PrintWriter(sock.getOutputStream(), true);
			
			System.out.println("접속 : " + inetAddr.getHostAddress());

			bufWr.write("kakao_id"+"\t"+"title"+"\t"+"album"+"\t"+"artist"+"\t"+"rating"+"\t"+"musicID"+"\n");
			while((s = in.readLine()) != null){
				String[] data = s.split("\t");
				if(!(data[0].equals(null) | data[1].equals(null) | data[2].equals(null) | data[3].equals(null) | data[4].equals(null) | data[4].equals(null))) {
					int to_hash = (data[1]+data[2]).hashCode(); 
					bufWr.write(s+"\t"+to_hash+"\n");
					bufWr.flush();
					System.out.println(s);
				}
				kakao_id = data[0];
				while((musicIn = bufRd.readLine()) != null) {
					String[] musicInData = musicIn.split("\t");
					if(musicInData[0].equals(kakao_id)) {
						System.out.println(musicInData[1]+"\t"+musicInData[2]+"\t"+musicInData[3]);
						out.println(musicInData[1]+"\t"+musicInData[2]+"\t"+musicInData[3]);
					}
				}
				out.println("end");
			}
			in.close();
			out.close();
			bufWr.close();
			sock.close();
		}catch(Exception e) {
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
				ProcessBuilder pb = new ProcessBuilder("python",path +"\\..\\..\\PServer\\preproceses.py");
				pb.start();
			}
			catch(IOException e) {}
		}
	}
}