import com.google.gson.Gson;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Main {
  private class Question {
    public int id;
    public String question;
    public String A;
    public String B;
    public String C;
    public String answer;
  }

  public static void main(String[] args) throws IOException {
    String serverName = "localhost";
    int port = 11451;
    Socket client = null;
    String token = "";
    try {
      System.out.println("欢迎来到党史答题系统！");
      System.out.println("连接主机：" + serverName + " ，端口号：" + port);
      client = new Socket(serverName, port);
      System.out.println("连接成功！远程主机地址：" + client.getRemoteSocketAddress() + "\n");
      while (true) {
        System.out.println("请选择操作：1.登录；2.注册");
        String method = "";
        Scanner scan = new Scanner(System.in);
        method = scan.nextLine();
        while (!method.equals("1") && !method.equals("2")) {
          System.out.println("请输入1或2 ！！");
          method = scan.nextLine();
        }
        OutputStream outToServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);

        System.out.print("请输入账号：");
        String username = scan.nextLine();
        System.out.print("请输入密码：");
        String password = scan.nextLine();
        String loginRequest = """
          /login?data={"type":"%s","username":"%s","password":"%s"}""";
        if (method.equals("1")) {
          out.writeUTF(String.format(loginRequest, "login", username, password));
          InputStream inFromServer = client.getInputStream();
          DataInputStream in = new DataInputStream(inFromServer);
          String response = in.readUTF();
          if (response.startsWith("200")) {
            System.out.println("登录成功！欢迎" + username);
            token = response.replaceFirst("200 OK,Token:", "");
          } else {
            System.out.println("登录失败！请检查账号密码！");
            client.close();
            client = new Socket(serverName, port);
            continue;
          }
        } else {
          out.writeUTF(String.format(loginRequest, "register", username, password));
          InputStream inFromServer = client.getInputStream();
          DataInputStream in = new DataInputStream(inFromServer);
          String response = in.readUTF();
          if (response.startsWith("200")) {
            System.out.println("注册成功！");
          } else {
            System.out.println("注册失败！可能是该用户名已被使用！");
          }
          client.close();
          client = new Socket(serverName, port);
          continue;
        }
        client.close();
        System.out.println("开始答题！\n");
        int[] questions = new int[5];
        String[] userRecord = new String[5];
        String[] answerRecord = new String[5];
        int correct=0;
        Gson gson = new Gson();
        for (int i = 0; i < 5; i++) {
          client = new Socket(serverName, port);
          out = new DataOutputStream(client.getOutputStream());
          out.writeUTF("/getQuestion");
          InputStream inFromServer = client.getInputStream();
          DataInputStream in = new DataInputStream(inFromServer);
          String response = in.readUTF();
          Question question = gson.fromJson(response, Question.class);
          System.out.print("问题：" + question.question + "\nA:" + question.A + "\nB:" + question.B + "\nC:" + question.C + "\n请作答：");
          String userAnswer = scan.nextLine();
          while (!(userAnswer.equals("A") || userAnswer.equals("B") || userAnswer.equals("C"))) {
            System.out.print("请输入A,B或C！\n请作答：");
            userAnswer = scan.nextLine();
          }
          if (userAnswer.equals(question.answer)) {
            System.out.println("回答正确！\n");
            correct++;
          } else {
            System.out.println("回答错误，正确答案是：" + question.answer+"\n");
          }
          questions[i]=question.id;
          answerRecord[i]=question.answer;
          userRecord[i] = userAnswer;
        }
        System.out.println("作答结束，共5道题目，您回答正确了"+correct+"道！\n");
      }
    } catch (ConnectException e) {
      System.out.println("连接服务器失败！请确认服务器端状态！");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      assert client != null;
      client.close();
    }
  }
}