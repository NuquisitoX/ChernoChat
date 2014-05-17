package com.TheCherno.ChernoChat.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
		try {
			System.out.println("Server started on " + InetAddress.getLocalHost() + ":" + port);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	private void sendToAll(String message) {
		for(ServerClient client: clients) {
			send(message.getBytes(), client.ip, client.port);
		}
	}
	
	private void send(String message, InetAddress address, int port) {
		message += "/e/";
		send(message.getBytes(), address, port);
	}
	private void send(final byte[] data, final InetAddress ip, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
	
	
	private void process(DatagramPacket packet) {
		String string = new String(packet.getData());
		if (string.startsWith("/c/")) {
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(string.split("/c/|/e/")[1], packet.getAddress(), packet.getPort(), id));
			System.out.println("ID: " + id + ", added " + string.split("/c/|/e/")[1] + " to the serverlist!");
			String idConfirm = "/c/" + id;
			send(idConfirm, packet.getAddress(), packet.getPort());
		} else if(string.startsWith("/d/")) {
			int id = Integer.parseInt(string.split("/d/|/e/")[1]);
			disconnect(id, true);
		} else if(string.startsWith("/m/")) {
			sendToAll(string);
		} else {
			System.out.println(string);
		}
	}
	
	private void disconnect(int id, boolean status) {
		ServerClient c = null;
		for(ServerClient client: clients) {
			if(client.getID() == id) {
				c = client;
				clients.remove(client);
				break;
			}
		}
		String message = "";
		if (status) {
			message = "Client " + c.name + " (" + c.getID() + ") @ " + c.ip.toString() + ":" + c.port + " disconnected.";
		} else {
			message = "Client " + c.name + " (" + c.getID() + ") @ " + c.ip.toString() + ":" + c.port + " timed out.";
		}
		System.out.println(message);
	}
	
}
