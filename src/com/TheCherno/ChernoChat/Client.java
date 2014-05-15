package com.TheCherno.ChernoChat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private String name;
	private String address;
	private int port;

	private JTextField txtMessage;
	private JTextArea txtHistory;
	
	private DatagramSocket socket;
	private InetAddress ip;
	
	private Thread send;

	public Client(String name, String address, int port) {
		setTitle("Cherno Chat - " + name);
		this.name = name;
		this.address = address;
		this.port = port;
		boolean connect = openConnection(address);
		createWindow();
		if(!connect){
			System.err.println("Connection failed!");
			console("Connection failed!");
		} else {
			console("Attempting a connection to " + address + ":" + port + ", user: " + name);
			String connection = "/c/" + name;
			send(connection.getBytes());
		}
	}
	
	private boolean openConnection(String address) {
		try {
			socket = new DatagramSocket();
			ip = InetAddress.getByName(address);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String receive() {
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String message = new String(packet.getData());
		return message;
	}

	private void send(final byte[] data) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
	
	private void createWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 550);
		setLocationRelativeTo(null);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 28, 815, 30, 7 };
		gbl_contentPane.rowHeights = new int[] { 35, 475, 40 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0 };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		txtHistory = new JTextArea();
		txtHistory.setEditable(false);
		JScrollPane scroll = new JScrollPane(txtHistory);
		GridBagConstraints gbc_scroll = new GridBagConstraints();
		gbc_scroll.insets = new Insets(0, 5, 5, 5);
		gbc_scroll.fill = GridBagConstraints.BOTH;
		gbc_scroll.gridx = 0;
		gbc_scroll.gridy = 0;
		gbc_scroll.gridwidth = 3;
		gbc_scroll.gridheight = 2;
		contentPane.add(scroll, gbc_scroll);

		txtMessage = new JTextField();
		txtMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessage.getText());
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 5, 5, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessage.getText());
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		contentPane.add(btnSend, gbc_btnSend);

		setVisible(true);
		txtMessage.requestFocusInWindow();
	}

	private void send(String message) {
		if (message.equals("") || message.length() == 0)
			return;
		message = name + ": " + message;
		console(message);
		send(message.getBytes());
		txtMessage.setText("");
	}

	public void console(String message) {
		txtHistory.append(message + "\n\r");
		txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
	}

}
