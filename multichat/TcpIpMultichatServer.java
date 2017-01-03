package multichat;

import java.io.*;
import java.net.*;
import java.util.*;

public class TcpIpMultichatServer {
	HashMap clients;

	TcpIpMultichatServer() {
		clients = new HashMap();
		Collections.synchronizedMap(clients);	//다수의 클라이언트 동기화
	}

	public void start() {
		ServerSocket serverSocket = null;	//한 포트에 하나의 ServerSocket만 연결 가능
		Socket socket = null;	//inputStream과 OutputStream으로 프로세스간 통신

		try {
			serverSocket = new ServerSocket(7777);
			System.out.println("서버가 시작되었습니다.");

			while (true) {
				socket = serverSocket.accept();	//포트와 연결되어 외부의 연결요청을 기다리다 Socket을 생성
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하였습니다.");
				ServerReceiver thread = new ServerReceiver(socket);	//socket 하나 당 하나의 thread 생성하여 동작
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void sendToAll(String msg) {
		Iterator it = clients.keySet().iterator();	//접속된 clients의 키를 순환하는 iterator

		while (it.hasNext()) {
			try {
				DataOutputStream out = (DataOutputStream) clients.get(it.next());
				out.writeUTF(msg);		
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		new TcpIpMultichatServer().start();
	}

	class ServerReceiver extends Thread {
		Socket socket;
		DataInputStream in;
		DataOutputStream out;

		ServerReceiver(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
			}
		}

		public void run() {
			String name = "";
			try {
				name = in.readUTF();
				sendToAll("#" + name + "님이 들어오셨습니다.");

				clients.put(name, out);
				System.out.print("현재 서버접속자 수는 " + clients.size() + "입니다.");

				while (in != null) {
					sendToAll(in.readUTF());
				}
			} catch (IOException e) {

			} finally {
				sendToAll("#" + name + "님이 나가셨습니다.");
				clients.remove(name);
				System.out.print("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하였습니다.");
				System.out.print("현재 서버접속자 수는 " + clients.size() + "입니다.");
			}
		}
	}
}
