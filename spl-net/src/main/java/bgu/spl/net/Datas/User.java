package bgu.spl.net.Datas;

import bgu.spl.net.api.Bidi.Messages.Message;

import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {
    private String userName;
    private String password;
    private String birthDay;
    private boolean loggedIn = false;
    private Queue<User> following;
    private Queue<User> followers;
//    private Queue<String> outMsg;
    private Queue<String> PMMsg;
    private Queue<String> posts;

    public Queue<Message> getWaitingMessages() {
        return waitingMessages;
    }

    private Queue<Message> waitingMessages;
    private int connectionId;

    public User(String userName, String password, String birthDay){
        this.userName = userName;
        this.password = password;
        this.birthDay = birthDay;
//        this.outMsg = new ConcurrentLinkedQueue<String>();
        this.PMMsg = new ConcurrentLinkedQueue<>();
        this.posts = new ConcurrentLinkedQueue<>();
        this.following = new ConcurrentLinkedQueue<>();
        this.followers = new ConcurrentLinkedQueue<>();
        this.waitingMessages = new ConcurrentLinkedQueue<>();
    }

    public String getUserName() {
        return userName;
    }

    public boolean confirmPassword(String password) {
        return this.password == password;
    }

    public String getBirthDay() {
        return birthDay;
    }

//    public Queue<String> getOutMsg() {
//        return outMsg;
//    }

    public Queue<String> getPosts() {
        return posts;
    }

    public Queue<String> getPMMsg() {
        return PMMsg;
    }

    public void addPost(String post){
        this.posts.add(post);
    }

//    public void addOutMsg(String outMsg){
//        this.outMsg.add(outMsg);
//    }

    public void addPMMsg(String inMsg){
        this.PMMsg.add(inMsg);
    }

    public void logIn(){
        this.loggedIn = true;
    }

    public void logOut(){
        this.loggedIn = false;
    }

    public Queue<User> getFollowing() {
        return following;
    }

    public Queue<User> getFollowers() {
        return followers;
    }

    public boolean getLog(){
        return this.loggedIn;
    }

    public int getConnectionId(){
        return this.connectionId;
    }

    public void addFollower(User follower){
        this.followers.add(follower);
    }

    public void addFollowing(User following){
        this.following.add(following);
    }

    public void addWaitingMsg(Message message){
        this.waitingMessages.add(message);
    }

    public void removeFollower(User follower){
        this.followers.remove(follower);
    }

    public  void removeFollowing(User following){
        this.following.remove(following);
    }

    public void removeWaitingMsg(Message message){
        this.waitingMessages.remove(message);
    }

    public boolean isFollowing(User user){return this.following.contains(user);}

    public boolean isFollowed(User user){return this.followers.contains(user);}

    public short getAge(){
        Calendar bday = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        bday.set(Integer.parseInt(this.birthDay.substring(0,2)), Integer.parseInt(this.birthDay.substring(3,5)), Integer.parseInt(this.birthDay.substring(6))); //very bad way

        int age = now.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

        if (now.get(Calendar.DAY_OF_YEAR) < bday.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        return (short)age;
    }

    public short getNumOfFollowers(){
        return (short)(this.followers.size());
    }

    public short getNumOfFollowing(){
        return (short)this.following.size();
    }
}
