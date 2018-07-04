package com.midi.eclipse;

import java.io.*;
import java.net.*;


public class MIDI_server {
	 public static void main(String[] args) {
	      ServerSocket ss;
	      Socket sc;
	      BufferedReader in; // 서버로의 입력
	      PrintWriter out;  // 클라이언트로의 출력

	      try {
	         // 포트 번호로 37771를 사용하는 ServerSocket을 생성
	         ss = new ServerSocket(37771);
	         // 클라이언트의 접속을 기다림
	         sc = ss.accept();
	         System.out.println(sc.getInetAddress() +"server connect");

	         // 입출력 스트림을 얻음
	         in = new BufferedReader(new InputStreamReader(sc.getInputStream())); 
	         out = new PrintWriter(sc.getOutputStream(), true); 
	         String inLine = in.readLine(); // 데이터를 읽어 들임
	         System.out.println(inLine);

	         // 소켓, 스트림, 서버 소켓을 닫는다.
	         in.close();
	         out.close();
	         sc.close();
	         ss.close();
	      } catch(IOException ex) {
	         ex.printStackTrace();
	      }
	   }


}
