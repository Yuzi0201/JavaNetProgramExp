import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class Server extends Thread {
  String request;
  Socket client;
  Statement stmt;

  private class LoginData {
    public String type;
    public String username;
    public String password;
  }

  private class Record {
    public class userRecord {
      ArrayList<Integer> questionIDs;
      ArrayList<String> userAnswers;
    }

    public userRecord userRecord;
    public String token;
  }

  private class GetRecord {
    public ArrayList<String> questions = new ArrayList<>();

    public class recordData {
      int[] questionIDs;
      String[] correctAnswers;
      String[] userAnswers;
    }

    public recordData recordData = new recordData();
  }

  public Server(Socket client, Statement stmt) {
    this.client = client;
    this.stmt = stmt;
  }

  @Override
  public void run() {
    try {
      request = new DataInputStream(client.getInputStream()).readUTF();
      String requestType = "";
      if (request.equals("/getQuestion")) {
        requestType = request;
      } else if (request.startsWith("/login")) {
        requestType = "/login";
      } else if (request.startsWith("/record")) {
        requestType = "/record";
      }
      switch (requestType) {
        case "/getQuestion" -> {
          Random random = new Random();
          int id = random.nextInt(5) + 1;
          ResultSet rs = stmt.executeQuery("SELECT id, question, A, B, C, answer FROM questions where id=" + id);
          while (rs.next()) {
            // 通过字段检索
            String question = rs.getString("question");
            String answer = rs.getString("answer");
            String A = rs.getString("A");
            String B = rs.getString("B");
            String C = rs.getString("C");
            // 输出数据
            String Json = "{\"id\":" + id + ",\"question\":\"" + question + "\",\"A\":\"" + A + "\",\"B\":\"" + B + "\",\"C\":\"" + C + "\",\"answer\":\"" + answer + "\"}";
            //System.out.println(Json);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeUTF(Json);
            client.close();
          }
        }
        case "/login" -> {//请求格式：/login?data={"type":"login或register","username":"xxx","password":"xxx"}
          Gson gson = new Gson();
          LoginData loginData = gson.fromJson(request.replaceFirst("/login\\?data=", ""), LoginData.class);
          ResultSet rs = stmt.executeQuery("select * from users where username='" + loginData.username + "' and password='" + loginData.password + "'");
          DataOutputStream out = new DataOutputStream(client.getOutputStream());
          if (loginData.type == null) {
            out.writeUTF("400 Bad Request");
            break;
          }
          switch (loginData.type) {
            case "login" -> {
              if (rs.next()) {
                String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                Random random = new Random();
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < 10; i++) {
                  int number = random.nextInt(62);
                  stringBuffer.append(str.charAt(number));
                }
                String token = stringBuffer.toString();
                stmt.executeUpdate("update users set token='" + token + "' where username='" + loginData.username + "'");
                out.writeUTF("200 OK,Token:" + token);
              } else {
                out.writeUTF("403 Forbidden");
              }
            }
            case "register" -> {
              if (rs.next()) {
                out.writeUTF("403 Forbidden");
              } else {
                stmt.executeUpdate("insert into users (username, password) values ('" + loginData.username + "','" + loginData.password + "')");
                out.writeUTF("200 OK");
              }
            }
            case default -> {
              out.writeUTF("400 Bad Request");
            }
          }
          client.close();
        }
        case "/record" -> {
          //请求格式：/record?type=GET&data={"token":"tokenxxx"}返回:{"questions":[题目,...],"records":[{"correctAnswers":["A","B","C","B","A"],"userAnswers":["A","B","C","B","A"]},{...}]}
          // 或/record?type=PUT&data={"userRecord":{"questionIDs":[1,2,3,4,5],"userAnswers":["A","B","C","B","A"]},"token":"tokenxxx"}
          //TODO 记录成绩
          Gson gson = new Gson();
          DataOutputStream out = new DataOutputStream(client.getOutputStream());
          if (request.startsWith("/record?type=GET")) {
            String token = request.replace("/record?type=GET&data={\"token\":\"", "").replace("\"}", "");
            System.out.println(token);
            ResultSet rs = stmt.executeQuery("select username from users where token='" + token + "'");
            if (rs.next()) {
              String username = rs.getString("username");
              GetRecord response = new GetRecord();
              rs = stmt.executeQuery("select * from questions order by id");
              while (rs.next()) {
                response.questions.add(rs.getString("question"));
              }
              System.out.println(response.questions);
              rs = stmt.executeQuery("select * from record where username='" + username + "'");
              while (rs.next()) {
                String[] qustionIDs = rs.getString("questionID").split(",");
                response.recordData.questionIDs = Arrays.stream(qustionIDs).mapToInt(Integer::parseInt).toArray();
                response.recordData.userAnswers = rs.getString("userAnswer").split(",");
                response.recordData.correctAnswers = rs.getString("correctAnswer").split(",");
              }
              out.writeUTF(gson.toJson(response));
            } else {
              out.writeUTF("403 Forbidden");
            }
          } else if (request.startsWith("/record?type=PUT")) {
            Record record = gson.fromJson(request.replaceFirst("/record\\?type=PUT&data=", ""), Record.class);

          } else {
            out.writeUTF("400 Bad Request");
          }
        }
        default -> {
          new DataOutputStream(client.getOutputStream()).writeUTF("400 Bad Request");
          client.close();
        }
      }
    } catch (SQLException | IOException e) {
      try {
        new DataOutputStream(client.getOutputStream()).writeUTF("500 Server Error");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException(e);
    }
  }
}

public class Main {
  static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
  static final String DB_URL = "jdbc:mysql://localhost:3306/HONGDB?useSSL=false&allowPublicKeyRetrieval=true";
  static final String USER = "root";
  static final String PASS = "ren1919810";

  public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
    Class.forName(JDBC_DRIVER);
    Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
    Statement stmt = conn.createStatement();
    ServerSocket socket = new ServerSocket(11451);
    Socket client = null;
    while (true) {
      client = socket.accept();
      Server server = new Server(client, stmt);
      server.start();
    }
  }
}