package com.TheCherno.ChernoChat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ClientWindow extends JFrame implements Runnable{
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea txtHistory;
	
	private Client client;
	
	private Thread run, listen;
	private boolean running = false;
	
	public ClientWindow(String name, String address, int port) {
		setTitle("Cherno Chat - " + name);
		client = new Client(name, address, port);
		boolean connect = client.openConnection(address);
		createWindow();
		if(!connect){
			System.err.println("Connection failed!");
			console("Connection failed!");
		} else {
			console("Attempting a connection to " + address + ":" + port + ", user: " + name);
			String connection = "/c/" + name + "/e/";
			client.send(connection.getBytes());
			running = true;
			run = new Thread(this, "Running");
			run.start();
		}
	}
	
	public void run() {
		listen();
	}
	
	private void send(String message) {
		if (message.equals("") || message.length() == 0)
			return;
		message = "/m/" + client.getName() + ": " + message + "/e/";
		client.send(message.getBytes());
		txtMessage.setText("");
	}

	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				while(running) {
					String message = client.receive();
					if(message.startsWith("/c/")) {
						client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
						console("Succesfully connected to server! ID: " + client.getID());
					} else if(message.startsWith("/m/")) {
						String text = message.substring(3).split("/e/")[0];
						console(text);
					}
				}
			}
		};
		listen.start();
	}
	
	public void console(String message) {
		txtHistory.append(message + "\n\r");
		txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
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

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String disconnect = "/d/" + client.getID() + "/e/";
				client.send(disconnect.getBytes());
				running = false;
				client.close();
			}
		});
		
		setVisible(true);
		txtMessage.requestFocusInWindow();
	}
}
