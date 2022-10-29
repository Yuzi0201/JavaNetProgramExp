import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Random;

class Server extends Thread {
  String request;
  Socket client;
  Statement stmt;

  private class LoginData{
    public String type;
    public String username;
    public String password;
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
      } else if (request.startsWith("/record")){
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
        case "/login" -> {//请求格式：/login&data={"type":"login或register","username":"xxx","password":"xxx"}
          Gson gson=new Gson();
          LoginData loginData=gson.fromJson(request.replaceFirst("/login&data=",""),LoginData.class);
          //TODO 登录
          ResultSet rs= stmt.executeQuery("select * from users where username='"+loginData.username+"' and password='"+loginData.password+"'");
          DataOutputStream out = new DataOutputStream(client.getOutputStream());
          if(loginData.type==null){
            out.writeUTF("400 Bad Request");
            break;
          }
          switch (loginData.type){
            case "login" -> {
              if(rs.next()){
                String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                Random random=new Random();
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < 10; i++) {
                  int number = random.nextInt(62);
                  stringBuffer.append(str.charAt(number));
                }
                String token=stringBuffer.toString();
                stmt.executeUpdate("update users set token='"+token+"' where username='"+loginData.username+"'");
                out.writeUTF("200 OK,Token:"+token);
              }else {
                out.writeUTF("403 Forbidden");
              }
            }
            case "register" -> {
              if(rs.next()){
                out.writeUTF("403 Forbidden");
              }else {
                stmt.executeUpdate("insert into users (username, password) values ('"+loginData.username+"','"+loginData.password+"')");
                out.writeUTF("200 OK");
              }
            }
            case default ->{
              out.writeUTF("400 Bad Request");
            }
          }
          client.close();
        }
        case "/record" -> {//请求格式：/record?data={"type":"GET"}或/record?data={"type":"PUT",record:[]}
          //TODO 记录成绩
        }
        default -> {
          new DataOutputStream(client.getOutputStream()).writeUTF("400 Bad Request");
          client.close();
        }
      }
    } catch (SQLException | IOException e) {
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