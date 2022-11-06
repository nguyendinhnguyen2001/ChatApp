/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/*
-đăng nhập đăng kí
-gửi,nhận tin nhắn 1-1
-gửi nhận tin nhắn group
-gửi,nhận icon,file,ảnh
-tìm và kết bạn
-tìm group chat
-tạo group
-out khỏi group
 */
import dao.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JTextArea;
import model.Group;
import model.Member;
import model.Message;
import model.User;

/**
 *
 * @author Hi
 */
public class ServerThread extends Thread {

    Socket socketOfServer;
    BufferedWriter bw;
    BufferedReader br;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    String nameClient, passClient, clientRoom;
    List<Group> clientGroupList;

    public static Hashtable<String, ServerThread> listUser = new Hashtable<>();
    public static final String NICKNAME_EXIST = "This username is already! Please using another username";
    public static final String NICKNAME_VALID = "This nickname is OK";
    public static final String NICKNAME_INVALID = "Nickname or password is incorrect";
    public static final String SIGNUP_SUCCESS = "Sign up successful!";
    public static final String ACCOUNT_EXIST = "This nickname has been used! Please use another nickname!";

    public JTextArea taServer;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    StringTokenizer tokenizer;
    private final int BUFFER_SIZE = 1024;
    String privateSenderName, privateReceiverName;
    List<String> publicSenderNames, publicReceiverNames;
    static Socket privateSenderSocket, privateReceivedSocket;
    static List<Socket> publicSenderSockets, publicReceiverSockets;
    UserDAO userDAO;
    GroupDAO groupDAO;
    MemberDAO memberDAO;
    MessageDAO messageDAO;
    MutualDAO mutualDAO;
    static boolean isBusy = false;

    public ServerThread(Socket socketOfServer) {
        this.socketOfServer = socketOfServer;
        this.bw = null;
        this.br = null;
        this.ois = null;
        this.oos = null;
        nameClient = "";
        passClient = "";
        userDAO = new UserDAO();
        groupDAO = new GroupDAO();
        memberDAO = new MemberDAO();
        messageDAO = new MessageDAO();
        mutualDAO = new MutualDAO();
    }

    public void appendMessage(String message) {
        taServer.append(message);
        taServer.setCaretPosition(taServer.getText().length() - 1);     //thiết lập vị trí con trỏ ngay sau đoạn text vừa chèn vào
    }

//    public String recieveFromClient() {
//        try {
//            return br.readLine();
//        } catch (IOException ex) {
//            System.out.println(nameClient + " is disconnected!");
//        }
//        return null;
//    }

    public void sendToClient(Object response) {     //chỉ gửi tin tới client gắn kết với thread này
        try {
              
            oos.writeObject(response);
            //bw.newLine();
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
        }
    }

    public void sendToSpecificClient(ServerThread socketOfClient, Object response) {     //chỉ gửi tin tới client cụ thể nào đó
        try {
            
            oos.writeObject(response);
//            writer.newLine();
//            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
        }
    }

    public void sendToSpecificClient(Socket socket, Object response) {     //chỉ gửi tin tới client cụ thể nào đó
        try {
            //oos=new ObjectOutputStream(socketOfServer.getOutputStream());
            oos.writeObject(response);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
        }
    }

    public void notifyToAllUsers(Object message) {
        //nguyên tắc hoạt động: giả sử client A gửi tin tới server, và đang có các client B,C,D khác cũng đang kết nối tới server
        //đầu tiên server lấy socketOfClient trong listUser, socketOfClient lấy tương ứng với tên A
        //server sẽ lấy tên và message từ thằng client A, sau đó gửi bản tin có nột dung: "A: message" tới tất cả client khác thông
        //qua các socketOfServer của chúng
        //tóm lại server gửi bản tin "A: message" tới A,B,C,D thông qua 4 socket: socketOfServer của A, socketOfServer của B, socketOfServer của C, socketOfServer của D
        //các socketOfServer lưu trong listUser

        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
//        BufferedWriter writer;

        while (clients.hasMoreElements()) {
            st = clients.nextElement();
//             = st.bw;
            try {
                st.oos.writeObject(message);
//                writer.newLine();
//                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
            }
        }
    }

    public void notifyToUsersInGroup(int gid,String message) {
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;

        while (clients.hasMoreElements()) {
            st = clients.nextElement();
            //st.clientGroupList=st.groupDAO.getListGroup(st.userDAO.findbyUsername(st.nameClient).getUserId());
            int check=0;
            for(Group g:st.clientGroupList){
                if(gid==g.getGroupId()){
                    check=1;
                    break;
                }
            }
            if (check==1) {     //gửi tin cho những thằng (st.clientRoom) có room trùng với room của thằng gửi tin (this.clientRoom)
                try {
                    st.oos.writeObject(message);
                    System.out.println("WRITE TO CLIENT: " + st.toString());
//                    writer.write(message);
//                    writer.newLine();
//                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
                }
            }
        }
    }

//    public void notifyToUsersInRoom(String room, String message) {      //gửi bản tin message tới phòng room
//        Enumeration<ServerThread> clients = listUser.elements();
//        ServerThread st;
//        BufferedWriter writer;
//
//        while (clients.hasMoreElements()) {
//            st = clients.nextElement();
//            if (st.clientRoom.equals(room)) {
//                writer = st.bw;
//
//                try {
//                    oos.writeObject(message);
////                    writer.write(message);
////                    writer.newLine();
////                    writer.flush();
//                } catch (IOException ex) {
//                    Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
//                }
//            }
//        }
//    }

    public void closeServerThread() {
        try {            
            br.close();
            bw.close();
            socketOfServer.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.ERROR, null, ex);
        }
    }

//    public String getAllUsers() {
//        StringBuffer kq = new StringBuffer();
//        String temp = null;
//
//        Enumeration<String> keys = listUser.keys();
//        if (keys.hasMoreElements()) {
//            String str = keys.nextElement();
//            kq.append(str);
//        }
//
//        while (keys.hasMoreElements()) {
//            temp = keys.nextElement();
//            kq.append("|").append(temp);
//        }
//
//        return kq.toString();
//    }
//
//    public String getUsersThisRoom() {
//        StringBuffer kq = new StringBuffer();
//        String temp = null;
//        ServerThread st;
//        Enumeration<String> keys = listUser.keys();
//
//        while (keys.hasMoreElements()) {
//            temp = keys.nextElement();
//            st = listUser.get(temp);
//            if (st.clientRoom.equals(this.clientRoom)) {
//                kq.append("|").append(temp);
//            }
//        }
//
//        if (kq.equals("")) {
//            return "|";
//        }
//        return kq.toString();   //Chú ý kq bắt đầu bằng '|' nhé, ví dụ: kq = "|anh tu|huy|toan|nguyen"
//    }

    public String getUsersAtRoom(String room) {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        ServerThread st;
        Enumeration<String> keys = listUser.keys();

        while (keys.hasMoreElements()) {
            temp = keys.nextElement();
            st = listUser.get(temp);
            if (st.clientRoom.equals(room)) {
                kq.append("|").append(temp);
            }
        }

        if (kq.equals("")) {
            return "|";
        }
        return kq.toString();   //Chú ý kq bắt đầu bằng '|' nhé, ví dụ: kq = "|anh tu|huy|toan|nguyen"
    }

    public void clientQuit() {
        //khi gửi file, ta sẽ tạo 1 socket mới để gửi file, và khi gửi xong socket đó tự động close
        //do socket đó ta tạo ra ko có tên của client nên socket_đó.clientName == null, do đó ko cần in
        //thông tin socket_đó close ra màn hình
        if (nameClient != null) {

            this.appendMessage("\n[" + sdf.format(new Date()) + "] Client \"" + nameClient + "\" is disconnected!");
            listUser.remove(nameClient);
            if (listUser.isEmpty()) {
                this.appendMessage("\n[" + sdf.format(new Date()) + "] Now there's no one is connecting to server\n");
            }
//            notifyToAllUsers("CMD_ONLINE_USERS|" + getAllUsers());
//            notifyToUsersInGroup("CMD_ONLINE_THIS_ROOM" + getUsersThisRoom());
 //           notifyToUsersInGroup(nameClient + " has quitted");
        }
    }

//    public void changeUserRoom() {      //cập nhật lại room cho chính đối tượng thuộc class này trong listUser
//        ServerThread st = listUser.get(this.nameClient);
//        st.clientRoom = this.clientRoom;
//        listUser.put(this.nameClient, st);    //st chính là đối tượng serverThread gắn với client yêu cầu thay đổi room
//        
//        /*
//        Để ý rằng đối tượng gắn với client yêu cầu thay đổi room chính là đối tượng thuộc lớp này, do đó chỉ cần 1 lệnh sau cũng 
//        đủ để thay thế 3 lệnh trên:
//        listUser.put(clientName, this);     //tham số thứ 2 là this, gắn với client muốn đổi room, và nó cũng thay đổi room rồi, do đó
//        lệnh này update value có key=clientName trong hashtable đó thôi
//        */
//    }
//    public void removeUserRoom() {
//        ServerThread st = listUser.get(this.nameClient);
//        st.clientRoom = this.clientRoom;
//        listUser.put(this.nameClient, st);
//        //  Tương tự hàm trên, chỉ cần 1 lệnh này là đủ: listUser.put(clientName, this);
//    }
    @Override
    public void run() {
        try {
            bw = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            oos = new ObjectOutputStream(socketOfServer.getOutputStream());
            ois = new ObjectInputStream(socketOfServer.getInputStream());

            boolean isUserExist = true;
            String message = "", sender, receiver, fileName;
            StringBuffer str;
            String cmd, icon;
            
            while (true) {
                try {
                    message = (String) ois.readObject();
                    System.out.println("mess: " + message);
                } catch (Exception e) {
                    System.out.println(e);
                }
                tokenizer = new StringTokenizer(message, "|");
                cmd = tokenizer.nextToken();

                switch (cmd) {
                    case "CMD_CHECK_NAME":
                        
                        nameClient = tokenizer.nextToken();
                        passClient = tokenizer.nextToken();
                        this.clientGroupList=this.groupDAO.getListGroup(this.userDAO.findbyUsername(this.nameClient).getUserId());
                        User user = userDAO.findbyUsername(nameClient);
                        user.setActiveStatus(1);
                        userDAO.setStatus(user);
                        System.out.println("RECEIVE: "+user);
                        List<User> listFriend = mutualDAO.getFriendList(user.getUserId());
                        for(User i: listFriend) System.out.println("FRIENDS: " + i);
                        user.setListFriend(listFriend);
                        List<Group> listGroup=groupDAO.getListGroup(user.getUserId());
                        for(Group g:listGroup){
                            g.setListMembers((ArrayList<Member>)memberDAO.getAllMembersfromGroup(g.getGroupId()));
                            g.setListUser((ArrayList<User>) memberDAO.getAllUsersfromGroup(g.getGroupId()));
                            g.setListMessages((ArrayList<Message>)messageDAO.getAllMessagesFromGroup(g));
                        }
                        user.setListGroup(listGroup);
                        //isUserExist = listUser.containsKey(nameClient);
                        System.out.println(nameClient + "is conecting...");
                        System.out.println(nameClient + "-" + passClient);
//                        if(isUserExist) {  //nickname is exist, nghĩa là đang có người khác đăng nhập với nick đó rồi
//                            sendToClient(NICKNAME_VALID);
//                        }
                        //else {  //nickname vẫn chưa có ai đăng nhập
                        boolean kq = userDAO.checkLogin(user);
                        if (kq == true) {
                            sendToClient(user);
                            //sau đó nếu tên hợp lệ thì cho nick đó vào Hashtable và chát với client:
                            this.appendMessage("\n[" + sdf.format(new Date()) + "] Client \"" + nameClient + "\" is connecting to server");
                            listUser.put(nameClient, this);     //thêm tên của đối tượng này và thêm cả đối tượng này vào listUser
                        } else {
                            sendToClient(NICKNAME_INVALID);
                        }
                        System.out.println(listUser);
                        //}
                        break;
                    
                    case "CMD_SEARCH_FRIEND":
                        int userID=Integer.parseInt(tokenizer.nextToken());
                        String key_csf=tokenizer.nextToken();
                        List<User> listFriend_csf=new ArrayList<>();
                        for(User u:mutualDAO.getFriendList(userID)){
                            if(u.getName().contains(key_csf)){
                                listFriend_csf.add(u);
                            }
                        }
                        sendToClient(listFriend_csf);
                        System.out.println(listFriend_csf);
                        break;
                    case "CMD_CONFIRM_ADD_FRIEND":
                        int user_id2=Integer.parseInt(tokenizer.nextToken());
                        int user_id1=Integer.parseInt(tokenizer.nextToken());
                        mutualDAO.acceptFriend(user_id2, user_id1);
                        break;
                    
                    case "CMD_SEARCH_USER":
//                        int uid=Integer.parseInt(tokenizer.nextToken());
                        String search=tokenizer.nextToken();
                        List<User> listu=userDAO.search(search);
                        sendToClient(listu);
//                        Map<String,Integer> listSearchMutual=new HashMap<>();
//                        List<User> listMutual_0=mutualDAO.getWaitFriendList(uid, 0);
//                        List<User> listMutual_1=mutualDAO.getWaitFriendList(uid, 1);
//                        for(User u:listMutual_0){
//                            for(User v:listu){
//                                if(v.getUserId()==u.getUserId()){
//                                    listSearchMutual.put(u.getName(), 0);
//                                }
//                            }
//                        }
//                        for(User u:listMutual_1){
//                            for(User v:listu){
//                                if(v.getUserId()==u.getUserId()){
//                                    listSearchMutual.put(u.getName(), 1);
//                                }
//                            }
//                        }
//                        sendToClient(listSearchMutual);
                        break;
                    case "CMD_FRIEND_REQUEST":
                        int u_id=Integer.parseInt(tokenizer.nextToken());
                        sendToClient(mutualDAO.getWaitFriendList(u_id, 0));
                        break;
                    case "CMD_DELETE_FRIEND":
                        int u1_id=Integer.parseInt(tokenizer.nextToken());
                        int u2_id=Integer.parseInt(tokenizer.nextToken());
                        mutualDAO.removeFriend(u1_id, u2_id);
                        sendToClient("FRIEND IS REMOVED");
                        break;
                    case "CMD_ADD_FRIEND":
                        int u1_id_add=Integer.parseInt(tokenizer.nextToken());
                        int u2_id_add=Integer.parseInt(tokenizer.nextToken());
                        mutualDAO.addFriend(u1_id_add, u2_id_add);
                        break;
                    case "CMD_SIGN_UP":
                        String name = tokenizer.nextToken();
                        String address = tokenizer.nextToken();
                        String username = tokenizer.nextToken();
                        String password = tokenizer.nextToken();
                        User u = new User(name, username, password, address, 0);
                        if (userDAO.checkRegister(username) == true) {
                            userDAO.register(u);
                            sendToClient(SIGNUP_SUCCESS);
                        } else {
                            sendToClient(ACCOUNT_EXIST);
                        }
                        break;
                    case "CMD_CREATE_GROUP":
                        int uadmin=Integer.parseInt(tokenizer.nextToken());
                        Group g=new Group();
                        String grname = userDAO.findbyID(uadmin).getName() + "'s group" ;
                        g.setNameGroup(grname);
                        int gid=groupDAO.createGroup(g);
                        g.setGroupId(gid);
                        memberDAO.addMemberToGroup(uadmin, gid,1);
                        sendToClient(g);
                        System.out.println(g);
                        break;
                    case "CMD_ADD_MEMBER":
                        int gid_add=Integer.parseInt(tokenizer.nextToken());
                        int memberid=Integer.parseInt(tokenizer.nextToken());
                        memberDAO.addMemberToGroup(memberid, gid_add,0);
                        sendToClient("User is added to group");
                        break;
                    case "CMD_REMOVE_GROUP":
                        int gid_remove=Integer.parseInt(tokenizer.nextToken());
                        memberDAO.removeAllMember(gid_remove);
                        groupDAO.removeGroup(gid_remove);
                        sendToClient("Group is removed");
                        break;
                    case "CMD_SAVE_GROUP":
                        int gid_save=Integer.parseInt(tokenizer.nextToken());
                        String nameGroup=tokenizer.nextToken();
                        boolean check=groupDAO.setNameGroup(gid_save, nameGroup);
                        if(check==true){
                            sendToClient("Group is updated");
                        }else sendToClient("Erorr update group,please try again");
                        break;
                    case "CMD_REMOVE_MEMBER":
                        int gid_remove_member=Integer.parseInt(tokenizer.nextToken());
                        int mid_remove=Integer.parseInt(tokenizer.nextToken());
                        boolean check_remove=memberDAO.removeMember(gid_remove_member,mid_remove);
                        if(check_remove==true){
                            sendToClient("Remove sucessfull");
                        }else sendToClient("remove fail,try again!");
                        break;
                    case "CMD_SEND_MESSAGE":
                        int g_id=Integer.parseInt(tokenizer.nextToken());
                        User u_sender=userDAO.findbyUsername(nameClient);
                        String mess=tokenizer.nextToken();
                        messageDAO.insert(u_sender, mess, g_id);
                        Message m=messageDAO.getLastestMessage(memberDAO.findMember(u_sender.getUserId(), g_id));
                        System.out.println(m);
                        System.out.println("CMD_CHAT|"+String.valueOf(m.getMessId())+"|"+String.valueOf(m.getUser().getUserId())+"|"+m.getMessage()+"|"+String.valueOf(m.getGroupId()));
                        notifyToUsersInGroup(g_id,"CMD_CHAT|"+String.valueOf(m.getMessId())+"|"+String.valueOf(m.getUser().getUserId())+"|"+m.getMessage()+"|"+String.valueOf(m.getGroupId()));
                        break;
                    case "CMD_LOGIN_CHAT":
                        int client_id_login=Integer.parseInt(tokenizer.nextToken());
                        userDAO.setStatus(client_id_login,1);
                        notifyToAllUsers("CMD_UPDATE_STATUS_CLIENT"+"|"+userDAO.findbyID(client_id_login).getUserId()+"|1");
                        break;
                    case "CMD_QUIT_CHAT":
                        int client_id=Integer.parseInt(tokenizer.nextToken());
                        userDAO.setStatus(client_id,0);
                        notifyToAllUsers("CMD_UPDATE_STATUS_CLIENT"+"|"+userDAO.findbyID(client_id).getUserId()+"|0");
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } catch (IOException ex) {
            clientQuit();
            java.util.logging.Logger.getLogger(ServerThread.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
