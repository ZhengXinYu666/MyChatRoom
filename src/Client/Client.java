package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java聊天室客户端
 *
 * @author 郑黑脸
 * @version 1.0
 */

//继承Thread类，使用多线程
public class Client extends Thread {
    //创建客户端Socket对象
    public Socket c_socket;
    private Client_chatFrame c_chatFrame;
    private Client_enterFrame c_enterFrame;
    private Client_singleFrame c_singleFrame;
    //创建数据输入流对象
    public DataInputStream dis = null;
    //创建数据输出流对象
    public DataOutputStream dos = null;
    //退出标记
    private boolean flag_exit = false;
    //定义线程id
    private int threadID;

    //Map集合存储单人聊天界面
    public Map<String, Client_singleFrame> c_singleFrames;
    //List集合存储即时在线用户信息
    public List<String> username_online;
    //List集合存储用户id
    public List<Integer> clientuserid;
    //初始化用户名
    public String username = null;
    //读到的信息
    public String chat_re;

    //getter, setter方法
    public Client_chatFrame getC_chatFrame() {
        return c_chatFrame;
    }

    public Client_singleFrame getC_singlFrame() {
        return c_singleFrame;
    }

    public void setC_singlFrame(Client_singleFrame c_singlFrame) {
        this.c_singleFrame = c_singlFrame;
    }

    public void setC_chatFrame(Client_chatFrame c_chatFrame) {
        this.c_chatFrame = c_chatFrame;
    }

    public Client_enterFrame getC_enterFrame() {
        return c_enterFrame;
    }

    public void setC_enterFrame(Client_enterFrame c_enterFrame) {
        this.c_enterFrame = c_enterFrame;
    }

    public int getThreadID() {
        return threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public Client() {
        //创建集合对象
        c_singleFrames = new HashMap<String, Client_singleFrame>();
        username_online = new ArrayList<String>();
        clientuserid = new ArrayList<Integer>();
//		signlechatuse = new ArrayList<String>();
    }

    public static void main(String[] args) {
        Client client = new Client();
        Client_enterFrame c_enterFrame = new Client_enterFrame(client);
        client.setC_enterFrame(c_enterFrame);
        //显示窗体
        c_enterFrame.setVisible(true);
    }

    /**登录
     *
     * @param username  用户名
     * @param hostIp    主机IP
     * @param hostPort  主机端口
     * @return
     *
     * 登录模块
     * 加入登录错误提示功能
     * 可提示的内容有：端口号错误，主机地址错误，连接服务异常
     * 如果三项都输入正确，则返回true
     *
     */
    public String login(String username, String hostIp, String hostPort) {
        this.username = username;
        String login_mess = null;
        try {
            c_socket = new Socket(hostIp, Integer.parseInt(hostPort));
        } catch (NumberFormatException e) {
            login_mess = "连接的服务器端口号port为整数,取值范围为：1024<port<65535";
            return login_mess;
        } catch (UnknownHostException e) {
            login_mess = "主机地址错误";
            return login_mess;
        } catch (IOException e) {
            login_mess = "连接服务其失败，请稍后再试";
            return login_mess;
        }
        return "true";
    }

    //显示聊天窗体
    public void showChatFrame(String username) {
        getDataInit();
        c_chatFrame = new Client_chatFrame(this, username);
        c_chatFrame.setVisible(true);
        //将退出标记置为true
        flag_exit = true;
        //启动该聊天的线程
        this.start();

    }

    //获取数据单元模块
    //从输入流中获取数据，封装到通道
    private void getDataInit() {
        try {
            dis = new DataInputStream(c_socket.getInputStream());
            dos = new DataOutputStream(c_socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 重写run方法
     * 加入判断功能
     */
    public void run() {
        while (flag_exit) {
            try {
                //从所包含的输入流中读取此操作的字节数
                chat_re = dis.readUTF();
            } catch (IOException e) {
                flag_exit = false;
                if (!chat_re.contains("@serverexit")) {
                    chat_re = null;
                }
            }
            if (chat_re != null) {
                if (chat_re.contains("@clientThread")) {
                    int local = chat_re.indexOf("@clientThread");
                    setThreadID(Integer.parseInt(chat_re.substring(0, local)));
                    try {
                        dos.writeUTF(username + "@login" + getThreadID() + "@login");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (chat_re.contains("@userlist")) {
                        c_chatFrame.setDisUsers(chat_re);
                    } else {
                        if (chat_re.contains("@chat")) {
                            c_chatFrame.setDisMess(chat_re);
                        } else {
                            if (chat_re.contains("@serverexit")) {
                                c_chatFrame.closeClient();
                            } else {
                                if (chat_re.contains("@single")) {
                                    c_chatFrame.setSingleFrame(chat_re);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //传输错误
    public void transMess(String mess) {
        try {
            dos.writeUTF(username + "@chat" + getThreadID() + "@chat" + mess + "@chat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //退出聊天
    public void exitChat() {
        try {
            dos.writeUTF(username + "@exit" + getThreadID() + "@exit");
            flag_exit = false;
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //退出登录
    public void exitLogin() {
        System.exit(0);
    }

    //退出客户端
    public void exitClient() {
        flag_exit = false;
        System.exit(0);
    }
}
