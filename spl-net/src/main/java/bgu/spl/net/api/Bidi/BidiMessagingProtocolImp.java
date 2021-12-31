package bgu.spl.net.api.Bidi;

import bgu.spl.net.Datas.DataBase;
import bgu.spl.net.Datas.User;
import bgu.spl.net.api.Bidi.BidiMessagingProtocol;
import bgu.spl.net.api.Bidi.Connections;
import bgu.spl.net.api.Bidi.Messages.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BidiMessagingProtocolImp implements BidiMessagingProtocol {
    private boolean shouldTerminate = false;
    private DataBase dataBase;
    private int myId;
    private Connections connections;
    private String userName;

    public  BidiMessagingProtocolImp(DataBase data){
        this.dataBase = data;
    }

    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.myId = connectionId;
        userName = "";
    }

    @Override
    public void process(Object message) {
        Message msg = (Message) message;
        msg.act(this);
    }

    public void register(short opcode, String userName, String password, String bday){

        Message ans;
        boolean work  = this.dataBase.register(userName,password,bday);
        if (work){
            ans = new Ack(opcode);
        }
        else{
            ans = new ErrorMessage(opcode);
        }
        this.connections.send(this.myId, ans);
    }

    public void logout(short opcode){
        Message ans;
        boolean work = this.dataBase.logOutRe(this.userName);
        if (work){
            ans = new Ack(opcode);
        }
        else{
            ans = new ErrorMessage(opcode);
        }

        this.connections.send(this.myId, ans);
    }

    public void LogStat(short opcode){
        while (!dataBase.connectedUsers().isEmpty()) { //if users getting loged all the time we will not get out of this loop!
            User user = dataBase.connectedUsers().poll();
            Message userDataAck = new AckUserInfo(user.getAge(), user.getNumOfFollowers(),user.getNumOfFollowing());
            connections.send(this.myId, userDataAck);
        }
    }

    public void post(short opcode, String content){

        if (dataBase.isRegister(userName) && dataBase.isLogged(userName)){
            User user = dataBase.getUser(userName);
            user.addPost(content);
            Message post = new NotificationMessage((byte)(1), userName, content);

            //sending to followers
            for (User follower: user.getFollowers()){
                if (follower.getLog()){
                    connections.send(follower.getConnectionId(),post);
                }
                else{
                    follower.addWaitingMsg(post);
                }
            }

            //sending to tagged
            Queue<String> tagged = tagged(content);
            for (String name : tagged){
                User tagUser = dataBase.getUser(name);
                if (tagUser != null && !user.getFollowers().contains(tagUser)){
                    if (tagUser.getLog()){
                        connections.send(tagUser.getConnectionId(),post);
                    }
                    else{
                        tagUser.addWaitingMsg(post);
                    }
                }
            }
        }
    }

    private Queue<String> tagged(String content){
        Queue<String> tag = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < content.length(); i++){
            if (content.indexOf(i) == '@'){
                String name = getTaggedName(content, i);
                tag.add(name);
                i = i + name.length();
            }
        }
        return tag;
    }

    private String getTaggedName(String content, int idx){
        String name = "";
        int i = idx + 1;
        while (i < content.length()){
            if (content.indexOf(i) == ' '){
                return name;
            }
            else{
                name = name + content.indexOf(i);
                i++;
            }
        }
        return name;
    }

    public void logIn(String userName,String password){
        this.userName = userName;
        boolean work = true;
        Integer opcode = 2;
        work = dataBase.logInRe(userName,password);
        if(work) {
            connections.send(myId, new Ack(opcode.shortValue()));
            User user =this.dataBase.getUser(this.userName);
            for(Message msg:user.getWaitingMessages()){
                connections.send(myId, new Ack(msg.getOpcode()));
            }
        }
        else
            connections.send(myId,new Error());
    }

    public void follow(byte follow, String userName) {
        boolean work = true;
        Integer opcode = 4;
        work = dataBase.follow(follow,this.userName,userName);
        if(work)
            connections.send(myId,new ACKFollow(opcode.shortValue(),userName));
        else
            connections.send(myId,new Error());
    }

    public void PM(short opcode,String userName,String content) {
        boolean work = true;
        String newContent = filter(content);
        work = dataBase.addPM(this.userName,userName,content);
        //sending to otherUser
        if(work) {
            Message PM = new NotificationMessage((byte) (0), userName, newContent);
            User user = this.dataBase.getUser(userName);
            if(user.getLog())
                connections.send(myId,PM);
            else
                user.addWaitingMsg(PM);

        }
        else
            this.connections.send(myId,new ErrorMessage(opcode));
    }

    public String filter(String content){
        List<String> filtered = this.dataBase.getFilteredWords();
        String ans = content;
        for(String word:filtered){
            ans = ans.replace(word,"<filtered>");
        }
        return ans;
    }

    public void stat() {

    }

    public void block() {

    }


    @Override
    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }


    public static void main(String[] args) {
        DataBase data = new DataBase();
        data.addFilteredWords("abc");
        String ans = "abc kk abc lmno";
        BidiMessagingProtocolImp bidiMessagingProtocol = new BidiMessagingProtocolImp(data);
        ans = bidiMessagingProtocol.filter(ans);
        System.out.println(ans);
    }
}
