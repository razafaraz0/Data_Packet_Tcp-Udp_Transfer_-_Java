import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class SecuritySystemClient {
    public static void main(String args[]) throws IOException {
        int i = 0;

        int port = 1234;//Integer.parseInt(args[0]);
        int type;
        String username = "bilkent";//args[1];
        String password = "cs421";//args[2];
        String data = username + ":" + password;
        boolean exit = false;
        System.out.println("whatever");
        //   try {
        Socket client = new Socket("localhost", port);

        //the output stream is connected to the input stream
        //send the authentication message
        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(client.getInputStream()));//data coming from server

        //first we send the first byte which in case of authorize is 0
        outToServer.writeByte(0);
        //then we send data size

        //now we pass the actual date which is username and password
        //first convert them to US-ASCII standard
        byte[] us = data.getBytes(StandardCharsets.US_ASCII);
        short length = (short) data.length();
        outToServer.writeShort(length);
        outToServer.write(us); //data send
        outToServer.flush();

        //getting response from server
        type = inFromServer.readByte();
        length = inFromServer.readShort();
        if (type == 2) {
            System.out.println("Authenication Successfull");
            while (exit == false)
            {
                System.out.println("Back here");
                type = inFromServer.readByte();
                System.out.println("Type is " + type);
                length = inFromServer.readShort();

                i++;
                if (type == 1) {
                    int retardedType;
                    //sending keep alive message back to the server
                    byte sendType = 1;
                    short sendLen = 0;
                    System.out.println("sending KeepAlive Signal ; "+ sendType);
                    outToServer.writeByte(sendType);

                    try { Thread.sleep(100);}
                    catch(Exception e){}
                    outToServer.writeShort(sendLen);

                    outToServer.flush();

                   // outToServer.flush();

                    //waitiing for the server to send client
                    retardedType = inFromServer.readByte();
                    length = inFromServer.readShort();
                    System.out.println("retarded type From server " + retardedType);
                    System.out.println("length inside " + length);

                    if (retardedType == 2) {
                        System.out.println("Recieved OK\n-----------");
                        //System.out.println("type inside  " + type);
                        //System.out.println("length inside " + length);
                    } else if (retardedType == 3) {
                        System.out.println("Invalid");
                        System.out.println("-----------");
                    }

                } else if (type == 4) {
                    System.out.println("in 4444");
                } else if (type == 7) {
                    System.out.println("------- exit is : " + exit);
                    System.out.println("EXITING NOW");
                    exit = true;
                    //   client.close();
                } else {
                    System.out.println("Nothing recieved");
                }
                System.out.println("printing again");

            }
        } else if (type == 3) {
            System.out.println("Invalid");
        } else {
            System.out.println("not 1 not 3");
        }

        // InputStream reply = client.getInputStream();
        //   DataInputStream in = new DataInputStream(reply);
        //    System.out.println("Server says " + in.readUTF());


        // }catch (IOException e){
        // e.printStackTrace();
    }

}

//}