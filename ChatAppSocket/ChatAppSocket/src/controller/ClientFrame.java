/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager; 
import javax.swing.UnsupportedLookAndFeelException;
import model.User;
import view.ClientPanel;
import view.LoginPanel;
import view.PrivateChat;
import view.RoomPanel;
import view.SignUpPanel;
import view.WelcomePanel;
/**
 *
 * @author Hi
 */
public class ClientFrame extends JFrame implements Runnable {
    
    
    String serverHost;
    public static final String NICKNAME_EXIST = "This nickname is already login in another place! Please using another nickname";
    public static final String NICKNAME_VALID = "This nickname is OK";
    public static final String NICKNAME_INVALID = "Nickname or password is incorrect";
    public static final String SIGNUP_SUCCESS = "Sign up successful!";
    public static final String ACCOUNT_EXIST = "This nickname has been used! Please use another nickname!";
    public static final String PASSWORD_CONFIRM = "Passwords don't match!";
    
    
    User user;
    String name;
    String room;
    Socket socketOfClient;
    BufferedWriter bw;
    BufferedReader br;
    
    JPanel mainPanel;
    LoginPanel loginPanel;
    ClientPanel clientPanel;
    WelcomePanel welcomePanel;
    SignUpPanel signUpPanel;
    RoomPanel roomPanel;
    
    Thread clientThread;
    boolean isRunning;
    
    JMenuBar menuBar;
    JMenu menuShareFile;
    JMenuItem itemSendFile;
    JMenu menuAccount;
    JMenuItem itemLeaveRoom, itemLogout, itemChangePass;
    
    SendFileFrame sendFileFrame;
    
    StringTokenizer tokenizer;
    String myDownloadFolder;
    
    Socket socketOfSender, socketOfReceiver;
    
    DefaultListModel<String> listModel, listModelThisRoom, listModel_rp;
        
    boolean isConnectToServer;
    
    int timeClicked = 0;    ///biến này để kiểm tra xem người dùng vừa click hay vừa double-click vào jList.
    //Thuật toán kiểm tra rất đơn giản: khi click thì tăng biến này lên 1 và đếm 500ms, nếu trong 500ms đó 
    //người dùng click tiếp thì đó là double-click, nếu ko thì chỉ là click
    
    Hashtable<String, PrivateChat> listReceiver;
    
    public ClientFrame(String name) {
        this.name = name;
        socketOfClient = null;
        bw = null;
        br = null;
        serverHost="localhost";
        isRunning = true;
        listModel = new DefaultListModel<>();
        listModelThisRoom = new DefaultListModel<>();
        listModel_rp = new DefaultListModel<>();
        isConnectToServer = false;
        listReceiver = new Hashtable<>();
    }
    
    
    
    Runnable counting = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            timeClicked = 0;
        }
    };
    
    private void labelRoomEvent() { // khi người dùng vào 1 phòng chat thì sẽ gửi đến server để thêm vô phòng
        this.clientPanel.getTpMessage().setText("");
        this.sendToServer("CMD_ROOM|"+this.room);
        try {
            Thread.sleep(200);      //chờ tý cho nó đỡ lỗi!
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.roomPanel.setVisible(false);
        this.clientPanel.setVisible(true);
        this.setTitle("\""+this.name+"\" - "+this.room);
        clientPanel.getLbRoom().setText(this.room);
    }
    
    private void leaveRoom() {// khi người dùng rời phòng sẽ gửi đến sẽ gửi đến server để xoá ra khỏi phòng
        this.sendToServer("CMD_LEAVE_ROOM|"+this.room);
        try {
            Thread.sleep(200);      //chờ tý cho nó đỡ lỗi!
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.roomPanel.setVisible(true);
        this.clientPanel.setVisible(false);
        //clear the textPane message:
        clientPanel.getTpMessage().setText("");
        this.setTitle("\""+this.name+"\"");
    }
    
    
    ////////////////////////Events////////////////////////////
    private String btOkEvent(User user) {//login
        
        this.serverHost = "localhost";
        
        if(user.getUserName().equals("") || user.getPassWord().equals("")) {
            return "Please fill up all fields";
        }
        if(!isConnectToServer) {
            isConnectToServer = true;   //nghĩa là khi chạy file ClientFrame.java này thì chỉ connect tới server duy nhất 1 lần thôi,
                                        //sau đó ko phải connect nữa vì đã có kết nối tới server rồi. Nếu cứ connect nhiều lần (do nhập sai account)
                                        //thì sẽ tạo ra nhiều socket kết nối tới server mỗi lần bấm OK, sau đó socket tự ý close dẫn tới việc lỗi!
            this.connectToServer(serverHost); //tạo 1 socket kết nối tói server
        }    
        this.sendToServer("CMD_CHECK_NAME|" +user.getUserName()+"|"+user.getPassWord());       //sau đó gửi tên đến để yêu cầu đăng nhập =  tên đó
        
        //server phản hồi rằng tên vừa nhập có hợp lệ hay ko:
        String response = this.recieveFromServer();
        if(response != null) {
            if (response.equals(NICKNAME_EXIST) || response.equals(NICKNAME_INVALID)) {
                return response;
            } else {

                //1 thread của client đc tạo ra để giao tiếp với server, chú ý rằng nhiệm vụ của thread này chỉ chờ server gửi tin tới
                //và in kq lên textarea, còn việc gửi tin tới server dùng sự kiện của btSend
                clientThread = new Thread(this);
                clientThread.start();
                this.sendToServer("CMD_ROOM|"+this.room);     //yêu cầu ds các user đang online để có thể chat private

                System.out.println("this is \""+name+"\"");
                //loginPanel.getBtOK().setText("OK");
            }
        } else System.out.println("[btOkEvent()] Server is not open yet, or already closed!");
        return null;
    }
    
    private String btSignUpEvent(User user,String confirmPass) {
        if(!user.getPassWord().equals(confirmPass)) {
            return PASSWORD_CONFIRM;
        } else {
            if(user.getName().equals("")|| user.getUserName().equals("")|| user.getAddress().equals("")||user.getPassWord().equals("")||confirmPass.equals("")) {
                return "Please fill up all fields";
            }
            if(!isConnectToServer) {
                isConnectToServer = true;   //nghĩa là khi chạy file ClientFrame.java này thì chỉ connect tới server duy nhất 1 lần thôi,
                                            //sau đó ko phải connect nữa vì đã có kết nối tới server rồi. Nếu cứ connect nhiều lần (do nhập sai account)
                                            //thì sẽ tạo ra nhiều socket kết nối tới server mỗi lần bấm OK, sau đó socket tự ý close dẫn tới việc lỗi!
                this.connectToServer(serverHost); //tạo 1 socket kết nối tói server
            }    
            this.sendToServer("CMD_SIGN_UP|" +user.getName()+"|"+user.getAddress()+"|"+user.getUserName()+"|"+user.getPassWord());       //sau đó gửi tên đến để yêu cầu đăng nhập =  tên đó
        
            String response = this.recieveFromServer();
            if(response != null) {
                if(response.equals(NICKNAME_EXIST) || response.equals(ACCOUNT_EXIST)) {
                    return response;
                } else {
                    return "Success";
                }
            }
        }
        return "Error";
    }
            
    private void btSendEvent() {
        String message = clientPanel.getTaInput().getText().trim();
        if(message.equals("")) clientPanel.getTaInput().setText("");
        else {
            this.sendToServer("CMD_CHAT|" + message);       //gửi data tới server
            this.btClearEvent();
        }
        //chú ý rằng việc chờ server phản hồi thực hiện trong hàm run của thread chứ ko phải ở đây
    }

    private void btClearEvent() {
        clientPanel.getTaInput().setText("");
    }

    private void btExitEvent() {
        try {
            isRunning = false;
            //this.disconnect();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    private void openSendFileFrame() {
//        sendFileFrame = new SendFileFrame();
//        
//        //gửi tất cả thông tin của client này sang frame đó:
//        //sendFileFrame.name = this.name;
//        //sendFileFrame.socketOfClient = this.socketOfClient;
////        sendFileFrame.bw = this.bw;
////        sendFileFrame.br = this.br;
//        
//        sendFileFrame.setVisible(true);
//        sendFileFrame.setLocation(450, 250);
//        sendFileFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//    }
    
    ////////////////////////End of Events////////////////////////////   
    
    public void connectToServer(String hostAddress) {   //mỗi lần connect tới server là khởi tạo lại thuộc tính socketOfClient
        try {
            socketOfClient = new Socket("192.168.0.101", 9999);
            bw = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));
            
        } catch (java.net.UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Host IP is not correct.\nPlease try again!", "Failed to connect to server", JOptionPane.ERROR_MESSAGE);
        } catch (java.net.ConnectException e) {
            JOptionPane.showMessageDialog(this, "Server is unreachable, maybe server is not open yet, or can't find this host.\nPlease try again!", "Failed to connect to server", JOptionPane.ERROR_MESSAGE);
        } catch(java.net.NoRouteToHostException e) {
            JOptionPane.showMessageDialog(this, "Can't find this host!\nPlease try again!", "Failed to connect to server", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
    
    public void sendToServer(String line) {
        try {
            this.bw.write(line);
            this.bw.newLine();   //phải có newLine thì mới dùng đc hàm readLine()
            this.bw.flush();
        } catch (java.net.SocketException e) {
            JOptionPane.showMessageDialog(this, "Server is closed, can't send message!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.lang.NullPointerException e) {
            System.out.println("[sendToServer()] Server is not open yet, or already closed!");
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String recieveFromServer() {
        try {
            return this.br.readLine();  //chú ý rằng chỉ nhận 1 dòng từ server gửi về thôi, nếu server gửi nhiều dòng thì các dòng sau ko đọc
        } catch (java.lang.NullPointerException e) {
            System.out.println("[recieveFromServer()] Server is not open yet, or already closed!");
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void disconnect() {
        System.out.println("disconnect()");
        try {
            if(br!=null) this.br.close();
            if(bw!=null) this.bw.close();
            if(socketOfClient!=null) this.socketOfClient.close();
            System.out.println("trong khoi try catch");
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        ClientFrame client = new ClientFrame(null);
        client.setVisible(true);
    }

    @Override
    public void run() {
        String response;
        String sender, receiver, fileName, thePersonIamChattingWith, thePersonSendFile;
        String msg;
        String cmd, icon;
        PrivateChat pc;
        
        while(isRunning) {
            response = this.recieveFromServer();   //nhận phản hồi từ server sau khi đã gửi data ở trên
            tokenizer = new StringTokenizer(response, "|");
            cmd = tokenizer.nextToken(); 
            switch (cmd) {
                case "CMD_CHAT":    //giả sử nhận được gói tin: CMD_CHAT|anh tu: today is very cool!
                    sender = tokenizer.nextToken();
                    msg = response.substring(cmd.length()+sender.length()+2, response.length());
                    
                    if(sender.equals(this.name)) this.clientPanel.appendMessage(sender+": ", msg, Color.BLACK, new Color(0, 102, 204));
                    else this.clientPanel.appendMessage(sender+": ", msg, Color.MAGENTA, new Color(56, 224, 0));
                    
                    //phải lằng nhằng như trên vì tránh trường hợp tin nhắn có ký tự |, nếu cứ dùng StringTokenizer và lấy ký tự '|' làm cái phân chia thì tin nhắn ko thể hiển thị kí tự | đc
                    break;
                    
                case "CMD_PRIVATECHAT":     //khi server gửi message của sender, đứng ở góc nhìn của thằng client này
                    //thì thằng gửi tới đó chính là thằng nhận, vì thằng client này chat với thằng gửi đó,
                    //nên thằng gửi đó sẽ nhận tin từ thằng này
                    sender = tokenizer.nextToken();
                    msg = response.substring(cmd.length()+sender.length()+2, response.length());
                    
                    pc = listReceiver.get(sender);
                    
                    
                    if(pc == null) {
                        pc = new PrivateChat(name, sender, serverHost, bw, br);
                        pc.sender = name;
                        pc.receiver = sender;
                        pc.serverHost = this.serverHost;
                        pc.bw = ClientFrame.this.bw;
                        pc.br = ClientFrame.this.br;

                        pc.getLbReceiver().setText("Private chat with \""+pc.receiver+"\"");
                        pc.setTitle(pc.receiver);
                        pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        pc.setVisible(true);    //nếu cái pc đó đang Visible rồi thì lệnh này cho focus vào cái frame này

                        listReceiver.put(sender, pc);
                    } else {
                        pc.setVisible(true);
                    }
                    //pc.appendMessage(sender+": ", msg, Color.CYAN, Color.GREEN);
                    pc.appendMessage_Left(sender+": ", msg);
                    break;
                    
                case "CMD_ONLINE_USERS":
                    listModel.clear();
                    listModel_rp.clear();
                    while(tokenizer.hasMoreTokens()) {
                        cmd = tokenizer.nextToken();
                        listModel.addElement(cmd);
                        listModel_rp.addElement(cmd);
                    }
                    clientPanel.getOnlineList().setModel(listModel);
                    
                    listModel_rp.removeElement(this.name);
                    roomPanel.getOnlineList_rp().setModel(listModel_rp);
                    break;
                    
                case "CMD_ONLINE_THIS_ROOM":
                    listModelThisRoom.clear();
                    while(tokenizer.hasMoreTokens()) {
                        cmd = tokenizer.nextToken();
                        listModelThisRoom.addElement(cmd);
                    }
                    clientPanel.getOnlineListThisRoom().setModel(listModelThisRoom);
                    break;
                    
//                case "CMD_SERVERISBUSY":
//                    JOptionPane.showMessageDialog(this, "Server is busy, please try to send file later", "Info", JOptionPane.INFORMATION_MESSAGE);
//                    break;
                    
                case "CMD_FILEAVAILABLE":
                    System.out.println("file available");
                    fileName = tokenizer.nextToken();
                    thePersonIamChattingWith = tokenizer.nextToken();
                    thePersonSendFile = tokenizer.nextToken();
                    
                    pc = listReceiver.get(thePersonIamChattingWith);
                    
                    if(pc == null) {
                        sender = this.name;
                        receiver = thePersonIamChattingWith;
                        pc = new PrivateChat(sender, receiver, serverHost, bw, br);
                        
                        pc.getLbReceiver().setText("Private chat with \""+pc.receiver+"\"");
                        pc.setTitle(pc.receiver);
                        pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        
                        listReceiver.put(receiver, pc);
                    }
                    
                    pc.setVisible(true);    //nếu cái pc đó đang Visible rồi thì lệnh này cho focus vào cái frame này
                    pc.insertButton(thePersonSendFile, fileName);
                    break;
                    
                case "CMD_ICON":
                    icon = tokenizer.nextToken();
                    cmd = tokenizer.nextToken();    //cmd = sender
                    
                    if(cmd.equals(this.name)) this.clientPanel.appendMessage(cmd+": ", "\n  ", Color.BLACK, Color.BLACK);
                    else this.clientPanel.appendMessage(cmd+": ", "\n   ", Color.MAGENTA, Color.MAGENTA);
                    
                    switch (icon) {
                        case "LIKE":
                            this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/like2.png")));
                            break;
                            
                        case "DISLIKE":
                            this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/dislike.png")));
                            break;
                            
                        case "PAC_MAN":
                            this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/pacman.png")));
                            break;
                            
                        case "SMILE":
                            this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/smile.png")));
                            break;
                            
                        case "GRIN":
                            this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/grin.png")));
                            break;
                            
                        case "CRY":
                            this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/cry.png")));
                            break;
                            
                        default:
                            throw new AssertionError("The icon is invalid, or can't find icon!");
                    }
                    
                    break;
                    
                default:
                    if(!response.startsWith("CMD_")) {      //trường hợp response chỉ là 1 tin nhắn thông thường
                        if(response.equals("Warnning: Server has been closed!")) {
                            this.clientPanel.appendMessage(response, Color.RED);
                        }
                        else this.clientPanel.appendMessage(response, new Color(153, 153, 153));
                    }
                    //do bên server có hàm notifyToAllUsers(clientName+" has just entered!"); 
                    //hàm trên ko có định dạng nào cả, tức là ko có CMD_ ở đầu message, nên ta chỉ cần in ra thông điệp server gửi tới là đc
                    
            }
        }
        System.out.println("Disconnected to server!");
    }


}
/*
Chat trong room: khi 1 thằng ấn send thì nội dung tin nhắn đc gửi đến server, sau đó server gửi lại tin đó
cho tất cả các thằng trong room đó, bao gồm cả thằng gửi, sau đó các thằng nhận đc tin nhắn mới hiển thị tin 
lên textpane, nghĩa là thằng gửi tin cũng phải chờ server phản hồi về mới hiển thị tin nhắn nó vừa gửi lên
cái textpane của nó.
Chat private giữa 2 thằng: sender hiển thị tin nhắn của nó lên textpane sau đó mới gửi tin tới server, server chỉ
gửi tin lại thằng nhận thôi, ko gửi lại cho sender nữa. Khi gửi tin thì dùng PrivateChat để gửi, nhưng khi nhận tin thì
dùng ClientFrame để nhận, sau đó ClientFrame lấy cái JFrame PrivateChattìm từ cái tên đứa gửi trong listReceiver và 
hiển thị tin lên cái frame đó. Chú ý là 1 người có thể có nhiều Frame PrivateChat để chat riêng với các người khác nhau,
do đó cần listReceiver để lưu các Frame tương ứng với người đó lại
Vậy listReceiver là danh sách các đối tượng đang chat riêng với người dùng đó, với key là tên của người chat và value là 
cái Frame chat ứng với người chat đó
*/