import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Test {
  public static void main(String[] args) throws IOException {
    String serverName = "localhost";
    int port = 11451;
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(10000);
      String requestStr="/login?data={\"type\":\"login\",\"username\":\"yuzi\",\"password\":\"19198100\"}";
      DatagramPacket request = new DatagramPacket(requestStr.getBytes(), requestStr.getBytes().length, InetAddress.getByName(serverName), port);
      DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
      socket.send(request);
      socket.receive(response);
      String result = new String(response.getData(), 0, response.getLength(),
        StandardCharsets.UTF_8);
      System.out.println(result);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
