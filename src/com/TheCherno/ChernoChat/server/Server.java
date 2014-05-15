package com.TheCherno.ChernoChat.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{

	private List<ServerClient> clients = new ArrayList<ServerClient>();
	
	private DatagramSocket socket;
	private int port;
	
	private boolean running = false;
	private Thread run, manage, send, receive;
	
	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
		run = new Thread(this, "Server");
		run.start();
	}

	public void run() {
		running = true;
		manageClients();
		receive();
		System.out.println("Server started on port " + port);
	}
	
	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while(running) {
					// Managing
				}
			}
		};
		manage.start();
	}
	
	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while(running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();
	}
	
	private void process(DatagramPacket packet) {
		String string = new String(packet.getData());
		int end = string.indexOf(0);
		if (string.startsWith("/c/")) {
			String name = new String(string.substring(3, end));
			clients.add(new ServerClient(name, packet.getAddress(), packet.getPort(), 100));
			System.out.println(name + " connected to the server!");
		} else {
			System.out.println(string);
		}
	}
	
}
