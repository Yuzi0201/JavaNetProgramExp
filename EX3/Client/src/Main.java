import com.google.gson.Gson;

import java.net.*;
import java.io.*;
import java.util.Arrays;
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

  public class Record {
    public String[] questions;
    public class records{
      public String[] questionIDs;
      public String[] correctAnswers;
      public String[] userAnswers;
    }
    public records[] recordData;

    @Override
    public String toString() {
      String[] list = new String[recordData.length];
      for (int i = 0; i < recordData.length; i++) {
        list[i] = "\n 题目序号: " + Arrays.toString(recordData[i].questionIDs) + "\n 正确答案: " + Arrays.toString(recordData[i].correctAnswers) + "\n 你的答案: " + Arrays.toString(recordData[i].userAnswers) + "\n";
      }
      return "题目：" + Arrays.toString(questions).replace(", ", "\n").replace("[", "\n").replace("]", "") + "\n记录：" + Arrays.toString(list);
    }
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
        while (!method.equals("1") && !method.equals("2") && !method.equals("3")) {
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
            break;
          } else {
            System.out.println("登录失败！请检查账号密码！");
            client.close();
            client = new Socket(serverName, port);
          }
        } else if (method.equals("2")) {
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
        }
      }
      while (true){
        System.out.println("请选择操作：1.查询成绩；2.答题；3.退出");
        String method = "";
        Scanner scan = new Scanner(System.in);
        method = scan.nextLine();
        while (!method.equals("1") && !method.equals("2") && !method.equals("3")) {
          System.out.println("请输入1或2或3 ！！");
          method = scan.nextLine();
        }
        if (method.equals("1")){
          String getRecordStr = """
          /record?type=GET&data={"token":"%s"}""";
          client = new Socket(serverName, port);
          DataOutputStream out = new DataOutputStream(client.getOutputStream());
          out.writeUTF(String.format(getRecordStr, token));
          InputStream inFromServer = client.getInputStream();
          DataInputStream in = new DataInputStream(inFromServer);
          String response = in.readUTF();
          Gson gson = new Gson();
          Record record = gson.fromJson(response, Record.class);
          System.out.println(record);
        } else if (method.equals("2")) {
          System.out.println("开始答题！\n");
          int[] questions = new int[5];
          String[] userRecord = new String[5];
          String[] answerRecord = new String[5];
          int correct=0;
          Gson gson = new Gson();
          for (int i = 0; i < 5; i++) {
            client = new Socket(serverName, port);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
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
          String questionIDStr = Arrays.toString(questions).replace(" ", "");
          String answerRecordStr = Arrays.toString(answerRecord).replace(" ", "").replace("A", "\"A\"").replace("B", "\"B\"").replace("C", "\"C\"");
          String userRecordStr = Arrays.toString(userRecord).replace(" ", "").replace("A", "\"A\"").replace("B", "\"B\"").replace("C", "\"C\"");
          String pushRecordStr = """
          /record?type=PUT&data={"userRecord":{"questionIDs":%s,"correctAnswers":%s,"userAnswers":%s},"token":"%s"}""";
          client = new Socket(serverName, port);
          DataOutputStream out = new DataOutputStream(client.getOutputStream());
          out.writeUTF(String.format(pushRecordStr, questionIDStr, answerRecordStr, userRecordStr, token));
          InputStream inFromServer = client.getInputStream();
          DataInputStream in = new DataInputStream(inFromServer);
          String response = in.readUTF();
        } else {
          System.exit(0);
        }
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