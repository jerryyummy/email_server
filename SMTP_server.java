import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class SMTP_server {
    String mailserver;
    String password;
    String source;
    String destination;
    String subject;
    String content;
    String lineFeet = "\r\n";
    private int port =25;
    Socket client;
    BufferedReader bf;
    DataOutputStream dos;


    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSource() {
        return source;
    }


    public String getDestination() {
        return destination;
    }

    public String getSubject() {
        return subject;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getMailServer(){
        return mailserver;
    }

    public void setMailServer(String mailServer){
        this.mailserver = mailServer;
    }

    /**
     * 初始化连接
     * @return
     */
    private boolean init(){
        System.out.println("init be invoked");
        boolean flag = true;
        if(mailserver == null || "".equals(mailserver)){
            return false;
        }
        try{
            Socket client = new Socket(mailserver, port);
            bf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            dos = new DataOutputStream(client.getOutputStream());
            String isConnect = getResponse();
            if(isConnect.startsWith("220")){

            }else{
                System.out.println("建立连接失败： "+isConnect);
                flag = false;
            }

        }catch(UnknownHostException e){
            System.out.println("建立连接失败！");
            e.printStackTrace();
            flag = false;
        }catch(IOException e){
            System.out.println("读取流数据失败！");
            e.printStackTrace();
            flag = false;
        }
        System.out.println("init result = " +flag);
        return flag;
    }

    /**
     * 发送smtp指令
     * 并返回服务器响应信息
     * @param msg
     * @return
     */
    private String sendCommand(String msg){
        String result = null;
        try{
            dos.writeBytes(msg);
            dos.flush();//清空缓冲区，强制发送出去
            result = getResponse();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取服务器端响应信息
     * @return
     */
    private String getResponse(){
        String result = null;
        try{
            result = bf.readLine();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 关闭
     */
    private void close(){
        try{
            dos.close();
            bf.close();
//            client.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean sendMail(){
        //初始化
        if(client == null){
            if(init()){

            }else{
                return false;
            }
        }
        //判断 服务端和客户端地址
        if(source == null || source.isEmpty() || destination == null || destination.isEmpty()){
            return false;
        }
        //进行握手
        String result = sendCommand("HELO "+getSource() +lineFeet);
        if(isStartWith(result, "250")){
            System.out.println("握手结果："+true);
        }else{
            System.out.println("握手失败："+result);
            return false;
        }
//        验证发信人信息
        String auth = sendCommand("auth login"+lineFeet);
        if(isStartWith(auth,"334")){
            System.out.println("验证发信人信息结果："+true);
        }else{
            return false;
        }
        String user = sendCommand(new String(Base64.encode(source.getBytes()))+lineFeet);
        System.out.println("user = " +user);
        if(isStartWith(user, "334")){
            System.out.println("验证user信息结果："+true);
        }else{
            return false;
        }
        String pass = sendCommand(new String(Base64.encode(password.getBytes()))+lineFeet);
        System.out.println("pass = " +pass);
        if(isStartWith(pass, "235")){
            System.out.println("验证pass信息结果："+true);
        }else{
            System.out.println("验证pass信息结果："+false);
            return false;
        }

        //发送指令
        String f = sendCommand("Mail From: <"+source+">"+lineFeet);
        System.out.println("发送指令结果："+f);
        if(isStartWith(f,"250")){
            System.out.println("发送指令结果："+true);
        }else{
            System.out.println("发送指令结果："+false);
            return false;
        }
        String toStr = sendCommand("RCPT TO: <"+destination+">"+lineFeet);
        System.out.println("验证toStr结果："+toStr);
        if(isStartWith(toStr,"250")){
            System.out.println("验证toStr结果："+true);
        }else{
            return false;
        }

        String data = sendCommand("DATA"+lineFeet);
        if(isStartWith(data,"354")){
            System.out.println("验证data信息结果："+true);
        }else{
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("From: <"+source+">"+lineFeet);
        sb.append("To: <"+destination+">"+lineFeet);
        sb.append("Subject:" +subject+lineFeet);
        sb.append("Content-Type:text/plain;charset=\"GB2312\",\"UTF-8\""+lineFeet);
        sb.append(lineFeet);
        sb.append(content);
        sb.append(lineFeet+"."+lineFeet);

        String conStr = sendCommand(sb.toString());
        if(isStartWith(conStr,"250")){
            System.out.println("验证conStr信息结果："+true);
        }else{
            return false;
        }

        //quit
        String quit = sendCommand("QUIT"+lineFeet);
        if(isStartWith(quit,"221")){
            System.out.println("验证quit信息结果："+true);
        }else{
            return false;
        }
        close();
        return true;
    }

    /**
     *
     * 检查字符串开头
     */
    private boolean isStartWith(String res, String with){

        return res.startsWith(with);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SMTP_server mail_server = new SMTP_server();
        String temp;
        Scanner input = new Scanner(System.in);
        System.out.println("请输入你想使用的邮箱类型（例如 smtp.163.com)");
        temp = input.next();
        mail_server.setMailServer(temp);
        System.out.println("请输入你的邮箱账号：");
        temp = input.next();
        mail_server.setSource(temp);
        System.out.println("请输入授权码");
        temp = input.next();
        mail_server.setPassword(temp);
        System.out.println("请输入目标邮箱地址：");
        temp = input.next();
        mail_server.setDestination(temp);
        System.out.println("请输入邮件主题：");
        temp = input.next();
        mail_server.setSubject(temp);
        System.out.println("请输入邮件正文：");
        temp = input.next();
        mail_server.setContent(temp);
        boolean flag = mail_server.sendMail();
        if(flag){
            System.out.println("邮件发送成功");
        }else{
            System.out.println("邮件发送失败");
        }

    }

}
