import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class POP_server {
    private static int port = 110;
    Socket socket = null;
    private boolean debug=true;

    public static void main(String[] args) throws UnknownHostException, IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("请输入你想使用的邮箱类型（例如 pop3.163.com)");
        String server = input.next();
        System.out.println("请输入你的邮箱账号：");
        String user = input.next();
        System.out.println("请输入你的授权码：");
        String password = input.next();

        POP_server pop3Client=new POP_server();
        pop3Client.init(server,port);
        pop3Client.recieveMail(user,password);
    }

    /*初始化函数*/
    public void init(String server, int port){
        try{
            socket=new Socket(server,port);//在新建socket的时候就已经与服务器建立了连接
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            System.out.println("建立连接！");
        }
    }


    //接收邮件程序
    public boolean recieveMail(String user,String password){

        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));//得到向客户端输入流
            BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));//得到客户端输出流
            Scanner input = new Scanner(System.in);
            user(user,in,out);//输入用户名
            //System.out.println("user 命令执行完毕！");
            pass(password,in,out);//输入密码
            //System.out.println("pass 命令执行完毕！");
            int number = stat(in,out);
            System.out.println("共有"+number+"封邮件");
            list(in,out);
            //System.out.println("list 命令执行完毕！");
            System.out.println("请输入邮件标识符:");
            int mailNum = Integer.parseInt(input.next());
            retr(mailNum,in,out);
            System.out.println("邮件内容显示完毕！");
            quit(in,out);
            System.out.println("已断开连接！");
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //得到服务器返回的一行命令
    public String getReturn(BufferedReader in){
        String line="";
        try{
            line=in.readLine();
            if(debug){
                System.out.println("服务器返回状态:"+line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return line;
    }

    //从返回的命令中得到第一个字段,也就是服务器的返回状态码(+OK或者-ERR)
    public String getResult(String line){
        StringTokenizer st=new StringTokenizer(line," ");
        return st.nextToken();
    }

    //发送命令
    private String sendServer(String str,BufferedReader in,BufferedWriter out) throws IOException{
        out.write(str);//发送命令
        out.newLine();//发送空行
        out.flush();//清空缓冲区

        if(debug){
            System.out.println("已发送命令:"+str);
        }
        return getReturn(in);
    }

    //user命令
    public void user(String user,BufferedReader in,BufferedWriter out) throws IOException{

        String result = null;
        result=getResult(getReturn(in));//先检测连接服务器是否已经成功
        if(!"+OK".equals(result)){
            throw new IOException("连接服务器失败!");
        }
        result=getResult(sendServer("user "+user,in,out));//发送user命令

        if(!"+OK".equals(result)){
            throw new IOException("用户名错误!");
        }
    }

    //pass命令
    public void pass(String password,BufferedReader in,BufferedWriter out) throws IOException{
        String result = null;
        result = getResult(sendServer("pass "+password,in,out));

        if(!"+OK".equals(result)){
            throw new IOException("密码错误!");
        }
    }


    //stat命令

    public int stat(BufferedReader in,BufferedWriter out) throws IOException{//返回邮件总数

        String result = null;
        String line = null;
        int mailNum = 0;//统计邮件总数

        line=sendServer("stat",in,out);

        StringTokenizer st=new StringTokenizer(line," ");//将发送STAT命令后得到的字符串分解
        result=st.nextToken();

        if(st.hasMoreTokens())
            mailNum=Integer.parseInt(st.nextToken());
        else{
            mailNum=0;
        }

        if(!"+OK".equals(result)){
            throw new IOException("查看邮箱状态出错!");
        }

        return mailNum;
    }

    //无参数list命令
    public void list(BufferedReader in,BufferedWriter out) throws IOException{

        String message = "";
        String line = null;
        line=sendServer("list",in,out);

        while(!".".equalsIgnoreCase(line)){
            message=message+line+"\n";
            line=in.readLine().toString();
        }

        System.out.println(message);
    }

    //带参数list命令
    public void list_one(int mailNumber ,BufferedReader in,BufferedWriter out) throws IOException{

        String result = null;
        result = getResult(sendServer("list "+mailNumber,in,out));

        if(!"+OK".equals(result)){
            throw new IOException("list错误!");
        }
    }

    //得到邮件详细信息

    public String getMessagedetail(BufferedReader in) throws UnsupportedEncodingException{

        String message = "";
        String line = null;

        try{
            line=in.readLine().toString();
            while(!".".equalsIgnoreCase(line)){
                message=message+line+"\n";
                line=in.readLine().toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return message;
    }

    //retr命令
    public void retr(int mailNum,BufferedReader in,BufferedWriter out) throws IOException, InterruptedException{

        String result;
        result=getResult(sendServer("retr "+mailNum,in,out));

        if(!"+OK".equals(result)){
            throw new IOException("接收邮件出错!");
        }

        System.out.println("第"+mailNum+"封");
        System.out.println(getMessagedetail(in));
        Thread.sleep(3000);
    }

    //退出
    public void quit(BufferedReader in,BufferedWriter out) throws IOException{

        String result;
        result=getResult(sendServer("QUIT",in,out));

        if(!"+OK".equals(result)){
            throw new IOException("未能正确退出");
        }
    }

}
