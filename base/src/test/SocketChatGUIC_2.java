package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class SocketChatGUIC_2 extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField, ipField, portField, nameField;
    private JButton connectButton, sendButton, disconnectButton, clearButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    private String username;

    public SocketChatGUIC_2() {
        super("聊天室客户端");
        initializeGUI();
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // 连接面板
        JPanel connectPanel = new JPanel(new FlowLayout());
        connectPanel.add(new JLabel("服务器IP:"));
        ipField = new JTextField("127.0.0.1", 10);
        connectPanel.add(ipField);

        connectPanel.add(new JLabel("端口:"));
        portField = new JTextField("9999", 5);
        connectPanel.add(portField);

        connectPanel.add(new JLabel("用户名:"));
        nameField = new JTextField("用户" + (int)(Math.random() * 1000), 8);
        connectPanel.add(nameField);

        connectButton = new JButton("连接");
        disconnectButton = new JButton("断开");
        disconnectButton.setEnabled(false);

        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnect());

        connectPanel.add(connectButton);
        connectPanel.add(disconnectButton);

        // 聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createTitledBorder("聊天记录"));

        // 消息发送区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("请输入文字:"), BorderLayout.WEST);
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        sendButton = new JButton("发送");
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());

        clearButton = new JButton("清空");
        clearButton.addActionListener(e -> {
            chatArea.setText("");
        });

        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // 布局安排
        setLayout(new BorderLayout());
        add(connectPanel, BorderLayout.NORTH);
        add(chatScroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }

    private void connectToServer() {
        String ip = ipField.getText();
        int port;
        username = nameField.getText().trim();

        if (username.isEmpty()) {
            appendMessage("系统", "用户名不能为空!");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            appendMessage("系统", "端口号格式错误!");
            return;
        }

        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // 发送用户名
            out.println(username);

            connected = true;
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            sendButton.setEnabled(true);
            ipField.setEnabled(false);
            portField.setEnabled(false);
            nameField.setEnabled(false);

            appendMessage("系统", "连接到 " + ip + ":" + port + " 端口");
            messageField.requestFocus();

            // 启动消息接收线程
            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            appendMessage("系统", "连接服务器失败: " + e.getMessage());
        }
    }

    private void disconnect() {
        connected = false;

        if (out != null) {
            out.println("/quit");
        }

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        sendButton.setEnabled(false);
        ipField.setEnabled(true);
        portField.setEnabled(true);
        nameField.setEnabled(true);

        appendMessage("系统", "已断开连接");
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && out != null) {
            // 立即在本地显示消息，显示为"用户名（我）"
            appendMessage(username + "（我）", message);

            // 发送消息到服务器
            out.println(message);
            messageField.setText("");
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (message.startsWith("MSG:")) {
                    // 普通聊天消息
                    String[] parts = message.substring(4).split(":", 2);
                    if (parts.length == 2) {
                        String sender = parts[0];
                        String content = parts[1];

                        // 如果消息不是自己发送的，才显示
                        // 自己发送的消息已经在sendMessage中显示了
                        if (!sender.equals(username)) {
                            appendMessage(sender, content);
                        }
                    }
                } else if (message.startsWith("SYSTEM:")) {
                    // 系统消息
                    appendMessage("系统", message.substring(7));
                }
            }
        } catch (IOException e) {
            if (connected) {
                appendMessage("系统", "连接异常: " + e.getMessage());
            }
        } finally {
            if (connected) {
                SwingUtilities.invokeLater(this::disconnect);
            }
        }
    }

    private void appendMessage(String sender, String content) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[" + new java.util.Date() + "] " + sender + ": " + content + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SocketChatGUIC_2().setVisible(true);
        });
    }
}