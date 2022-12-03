import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class Server extends Thread {
  String request;
  DatagramSocket socket = null;
  DatagramPacket packet = null;
  Statement stmt;

  private class LoginData {
    public String type;
    public String username;
    public String password;
  }

  private class PutRecord {
    public class userRecord {
      int[] questionIDs;
      String[] userAnswers;
      String[] correctAnswers;
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

      public recordData(int[] questionIDs, String[] userAnswers, String[] correctAnswers) {
        this.questionIDs = questionIDs;
        this.userAnswers = userAnswers;
        this.correctAnswers = correctAnswers;
      }
    }

    public ArrayList<recordData> recordData = new ArrayList<>();

    public void AddData(int[] questionIDs, String[] userAnswers, String[] correctAnswers) {
      this.recordData.add(new recordData(questionIDs, userAnswers, correctAnswers));
    }
  }

  public Server(DatagramSocket socket, DatagramPacket packet, Statement stmt) {
    this.socket = socket;
    this.packet = packet;
    this.stmt = stmt;
  }

  @Override
  public void run() {
    try {
      //request = new DataInputStream(client.getInputStream()).readUTF();
      byte[] data = packet.getData();
      request = new String(data, 0, packet.getLength());
//      System.out.println(request);
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
            //DataOutputStream out = new DataOutputStream(client.getOutputStream());
            //out.writeUTF(Json);
            SendBack(Json);
          }
        }
        case "/login" -> {//请求格式：/login?data={"type":"login或register","username":"xxx","password":"xxx"}
          Gson gson = new Gson();
          LoginData loginData = gson.fromJson(request.replaceFirst("/login\\?data=", ""), LoginData.class);
          ResultSet rs = stmt.executeQuery("select * from users where username='" + loginData.username + "' and password='" + loginData.password + "'");
          if (loginData.type == null) {
            SendBack("400 Bad Request");
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
                SendBack("200 OK,Token:" + token);
              } else {
                SendBack("403 Forbidden");
              }
            }
            case "register" -> {
              if (rs.next()) {
                SendBack("403 Forbidden");
              } else {
                stmt.executeUpdate("insert into users (username, password) values ('" + loginData.username + "','" + loginData.password + "')");
                SendBack("200 OK");
              }
            }
            case default -> {
              SendBack("400 Bad Request");
            }
          }
        }
        case "/record" -> {
          //请求格式：/record?type=GET&data={"token":"tokenxxx"}返回:{"questions":[题目,...],"recordData":[{"correctAnswers":["A","B","C","B","A"],"userAnswers":["A","B","C","B","A"]},{...}]}
          // 或/record?type=PUT&data={"userRecord":{"questionIDs":[1,2,3,4,5],"correctAnswers":["A","B","C","B","A"],"userAnswers":["A","B","C","B","A"]},"token":"tokenxxx"}
          Gson gson = new Gson();
          if (request.startsWith("/record?type=GET")) {
            String token = request.replace("/record?type=GET&data={\"token\":\"", "").replace("\"}", "");
            //System.out.println(token);
            ResultSet rs = stmt.executeQuery("select username from users where token='" + token + "'");
            if (rs.next()) {
              String username = rs.getString("username");
              GetRecord response = new GetRecord();
              rs = stmt.executeQuery("select * from questions order by id");
              while (rs.next()) {
                response.questions.add(rs.getString("question"));
              }
              //System.out.println(response.questions);
              rs = stmt.executeQuery("select * from record where username='" + username + "'");
              int i=0;
              while (rs.next()) {
                String[] questionIDs = rs.getString("questionID").split(",");
                int[] questionIDs_INT = Arrays.stream(questionIDs).mapToInt(Integer::parseInt).toArray();
                String[] userAnswers = rs.getString("userAnswer").split(",");
                String[] correctAnswers = rs.getString("correctAnswer").split(",");
                response.AddData(questionIDs_INT,userAnswers,correctAnswers);
              }
              SendBack(gson.toJson(response));
            } else {
              SendBack("403 Forbidden");
            }
          } else if (request.startsWith("/record?type=PUT")) {
            PutRecord putRecord = gson.fromJson(request.replaceFirst("/record\\?type=PUT&data=", ""), PutRecord.class);
            ResultSet rs = stmt.executeQuery("select username from users where token='" + putRecord.token + "'");
            if (rs.next()) {
              String questionIDs= Arrays.toString(putRecord.userRecord.questionIDs).replace(" ","").replace("[","").replace("]","");
              String userAnswers= Arrays.toString(putRecord.userRecord.userAnswers).replace(" ","").replace("[","").replace("]","");
              String correctAnswers= Arrays.toString(putRecord.userRecord.correctAnswers).replace(" ","").replace("[","").replace("]","");
              //System.out.println("insert into record (username, questionID, userAnswer, correctAnswer) values ('"+rs.getString("username")+"','"+ questionIDs+"','"+userAnswers+"','"+correctAnswers+"')");
              stmt.executeUpdate("insert into record (username, questionID, userAnswer, correctAnswer) values ('"+rs.getString("username")+"','"+ questionIDs+"','"+userAnswers+"','"+correctAnswers+"')");
              SendBack("200 OK");
            } else {
              SendBack("403 Forbidden");
            }
          } else {
            SendBack("400 Bad Request");
          }
        }
        default -> {
          //new DataOutputStream(client.getOutputStream()).writeUTF("400 Bad Request");
          SendBack("400 Bad Request");
        }
      }
    } catch (SQLException | IOException e) {
      try {
        SendBack("500 Server Error");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException(e);
    } catch (JsonSyntaxException e){
      try {
        SendBack("400 Bad Request");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException(e);
    }
  }

  void SendBack(String msg) throws IOException {
    byte[] backbuf = msg.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(backbuf, backbuf.length,packet.getAddress(),packet.getPort()); //封装返回给客户端的数据
    socket.send(sendPacket);  //通过套接字反馈服务器数据
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
    //ServerSocket socket = new ServerSocket(11451);
    try(DatagramSocket socket=new DatagramSocket(11451)) {
      //Socket client = null;
      while (true) {
        DatagramPacket packet=new DatagramPacket(new byte[1024], 1024);
        //client = socket.accept();
        socket.receive(packet);
        Server server = new Server(socket,packet, stmt);
        server.start();
      }
    }catch (IOException e){
      e.printStackTrace();
    }
  }
}