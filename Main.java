import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        int choice;
        Scanner input = new Scanner(System.in);
        System.out.println("请选择服务：\n" +
                        "1. 发邮件\n" +
                        "2. 接收邮件"
                        );
        choice = input.nextInt();
        while (true){
            if (choice == 2){
                POP_server pop_server = new POP_server();
                pop_server.main(args);
            }
            else if (choice == 1){
                SMTP_server smtp_server = new SMTP_server();
                smtp_server.main(args);
            }
            else {
                System.out.println("请输入正确的选项！");
            }
        }

    }
}
