package com.midi.eclipse;

import java.io.*;
import java.net.*;


public class MIDI_server {
	 public static void main(String[] args) {
	      ServerSocket ss;
	      Socket sc;
	      BufferedReader in; // �������� �Է�
	      PrintWriter out;  // Ŭ���̾�Ʈ���� ���

	      try {
	         // ��Ʈ ��ȣ�� 37771�� ����ϴ� ServerSocket�� ����
	         ss = new ServerSocket(37771);
	         // Ŭ���̾�Ʈ�� ������ ��ٸ�
	         sc = ss.accept();
	         System.out.println(sc.getInetAddress() +"server connect");

	         // ����� ��Ʈ���� ����
	         in = new BufferedReader(new InputStreamReader(sc.getInputStream())); 
	         out = new PrintWriter(sc.getOutputStream(), true); 
	         String inLine = in.readLine(); // �����͸� �о� ����
	         System.out.println(inLine);

	         // ����, ��Ʈ��, ���� ������ �ݴ´�.
	         in.close();
	         out.close();
	         sc.close();
	         ss.close();
	      } catch(IOException ex) {
	         ex.printStackTrace();
	      }
	   }


}
