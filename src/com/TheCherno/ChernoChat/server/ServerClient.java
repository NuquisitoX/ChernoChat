package com.TheCherno.ChernoChat.server;

import java.net.InetAddress;

public class ServerClient {

	public String name;
	public InetAddress ip;
	public int port;
	private final int ID;
	public int attempt = 0;
	
	public ServerClient(String name, InetAddress ip, int port, final int ID) {
		this.port = port;
		this.ip = ip;
		this.name = name;
		this.ID = ID;
	}
	
	public int getID() {
		return ID;
	}
	
}
