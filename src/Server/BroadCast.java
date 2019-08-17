package Server;

import java.io.IOException;

/**
 * 广播线程的实现类
 *
 * @author 郑黑脸
 *
 * @version 1.0
 */

public class BroadCast extends Thread {
    //客户端线程
    ClientThread clientThread;
    //服务器端线程
    ServerThread serverThread;
    String str;
    private boolean flag_exit = false;

    public BroadCast(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    @Override
    public void run() {
        boolean flag = true;
        while (flag_exit) {
            synchronized (serverThread.messages) {
                if (serverThread.messages.isEmpty()) {
                    continue;
                } else {
                    str = (String) serverThread.messages.firstElement();
                    serverThread.messages.removeElement(str);
                    if (str.contains("@clientThread")) {
                        flag = false;
                    }
                }
            }
            synchronized (serverThread.clients) {
                for (int i = 0; i < serverThread.clients.size(); i++) {
                    clientThread = serverThread.clients.elementAt(i);
                    if (flag) {
                        try {
                            //向记录的每一个客户端发送数据信息
                            if (str.contains("@exit")) {
                                serverThread.clients.remove(i);
                                clientThread.closeClienthread(clientThread);
                                clientThread.dos.writeUTF(str);
                            }
                            if (str.contains("@chat") || str.contains("@userlist") || str.contains("@serverexit")) {
                                clientThread.dos.writeUTF(str);
                            }
                            if (str.contains("@single")) {
                                String[] info = str.split("@single");
                                int id_thread = Integer.parseInt(info[2]);
                                for (int j = 0; j < serverThread.clients.size(); j++) {
                                    if (id_thread == serverThread.clients.get(j).getId()) {
                                        serverThread.clients.get(j).dos.writeUTF(str);
                                        i = serverThread.clients.size();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException E) {
                        }
                    } else {
                        String value = serverThread.users.get((int) clientThread.getId());
                        if (value.equals("@login@")) {
                            flag = true;
                            try {
                                //向记录的每一个客户端发送数据信息
                                clientThread.dos.writeUTF(str);
                                if (str.contains("@exit")) {
                                    serverThread.clients.remove(i);
                                    clientThread.closeClienthread(clientThread);
                                }
                            } catch (IOException E) {
                            }
                            break;
                        }
                    }
                }
            }
            if (str.contains("@serverexit")) {
                serverThread.users.clear();
                flag_exit = false;
            }
        }
    }

    public void setFlag_exit(boolean b) {
        flag_exit = b;
    }


    public void stopBroadCase() {
        flag_exit = false;
    }
}
