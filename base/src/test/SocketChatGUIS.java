package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SocketChatGUIS extends JFrame {
    private JTextArea logArea;
    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private ServerSocket serverSocket;
    private boolean isListening;
    private Vector<ClientHandler> clients;
    private JButton startButton, stopButton;
    private JTextField portField;

    public SocketChatGUIS() {
        super("Socket聊天室服务器端");
        initializeGUI();
        clients = new Vector<>();
    }

    private void initializeGUI() {
        // 主面板设置
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        // 顶部控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("端口号:"));
        portField = new JTextField("9999", 10);
        controlPanel.add(portField);

        startButton = new JButton("开始监听");
        stopButton = new JButton("停止监听");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        // 中间内容区域
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("服务器日志"));

        // 用户列表
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("在线用户列表"));

        splitPane.setLeftComponent(logScroll);
        splitPane.setRightComponent(userScroll);

        // 布局安排
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void startServer() {
        int port;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            log("端口号格式错误!");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            isListening = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);

            log("开始监听 " + port + " 端口!");

            // 启动接受客户端连接的线程
            new Thread(this::acceptClients).start();

        } catch (IOException e) {
            log("启动服务器失败: " + e.getMessage());
        }
    }

    private void stopServer() {
        isListening = false;

        // 关闭所有客户端连接
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        listModel.clear();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        portField.setEnabled(true);
        log("服务器已停止");
    }

    private void acceptClients() {
        while (isListening) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket);
                clients.add(client);
                new Thread(client).start();

            } catch (IOException e) {
                if (isListening) {
                    log("接受客户端连接错误: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(new Date() + ": " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void refreshUserList() {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client.getUsername() != null) {
                        listModel.addElement(client.getUsername());
                    }
                }
            }
        });
    }

    // 广播消息给所有客户端
    private void broadcast(String message) {
        synchronized (clients) {
            Iterator<ClientHandler> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientHandler client = iterator.next();
                if (!client.sendMessage(message)) {
                    iterator.remove();
                    refreshUserList();
                }
            }
        }
    }

    // 客户端处理类
    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getUsername() {
            return username;
        }

        public boolean sendMessage(String message) {
            if (out != null) {
                out.println(message);
                return !out.checkError();
            }
            return false;
        }

        public void close() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 读取用户名
                username = in.readLine();
                log("用户 " + username + " 连接成功");
                refreshUserList();

                // 广播用户上线消息
                broadcast("SYSTEM:" + username + " 加入了聊天室");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    // 广播聊天消息（不包含"我"标记）
                    broadcast("MSG:" + username + ":" + message);
                    log(username + ": " + message);
                }

            } catch (IOException e) {
                log("客户端连接异常: " + e.getMessage());
            } finally {
                close();
                clients.remove(this);
                if (username != null) {
                    log("用户 " + username + " 断开连接");
                    broadcast("SYSTEM:" + username + " 离开了聊天室");
                    refreshUserList();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SocketChatGUIS().setVisible(true);
        });
    }
}