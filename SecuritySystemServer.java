
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SecuritySystemServer {
    private static final String USERNAME = "bilkent";
    private static final String PASS = "cs421";
    private static final int HEADER_TYPE_BYTES = 1;
    private static final int HEADER_LENGTH_BYTES = 2;
    private static final String IMAGE_FILENAME_1 = "footage1";
    private static final String IMAGE_FILENAME_2 = "footage2";
    private static final long KEEPALIVE_INTERVAL = 3000L;
    private static final int RANDOM_MULTIPLIER = 3;
    private static final byte AUTHORIZE = 0;
    private static final byte KEEPALIVE = 1;
    private static final byte OK = 2;
    private static final byte INVALID = 3;
    private static final byte EMERGENCY = 4;
    private static final byte ALARM = 5;
    private static final byte DISCARD = 6;
    private static final byte EXIT = 7;

    public SecuritySystemServer() {
    }

    public static void main(String[] args) throws Throwable {
        int port = 0;

        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception var54) {
            System.out.println("Check the arguments.");
            System.exit(1);
        }

        try {
            Throwable var5 = null;
            Object var6 = null;

            try {
                ServerSocket serverSocket = new ServerSocket(port);

                try {
                    Socket clientSocket = serverSocket.accept();

                    try {
                        OutputStream out = clientSocket.getOutputStream();

                        try {
                            BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());

                            try {
                                ArrayList<byte[]> received = receiveMessage(in);
                                if (received == null) {
                                    sendError(out, "Invalid data length!");
                                } else {
                                    byte[] type = (byte[])received.get(0);
                                    byte[] data = (byte[])received.get(1);
                                    if (type[0] == 0) {
                                        if (data == null) {
                                            sendError(out, "No data!");
                                        } else {
                                            String dataStr = new String(data, StandardCharsets.US_ASCII);
                                            String[] userAndPass = dataStr.split(":");
                                            if (userAndPass.length != 2) {
                                                sendError(out, "Wrong data!");
                                            } else if (userAndPass[0].equals("bilkent") && userAndPass[1].equals("cs421")) {
                                                sendOK(out, "Authentication successful.");

                                                for(int i = 0; i < 2; ++i) {
                                                    keepAliveLoop(out, in);
                                                }

                                                emergencyLoop(out, in, "footage1");
                                                keepAliveLoop(out, in);
                                                emergencyLoop(out, in, "footage2");
                                                Thread.sleep(3000L);
                                                sendMessage(out, (byte)7, (byte[])null);
                                            } else {
                                                sendError(out, "Authentication failed!");
                                            }
                                        }
                                    } else {
                                        sendError(out, "Wrong message type!");
                                    }
                                }
                            } finally {
                                if (in != null) {
                                    in.close();
                                }

                            }
                        } catch (Throwable var56) {
                            if (var5 == null) {
                                var5 = var56;
                            } else if (var5 != var56) {
                                var5.addSuppressed(var56);
                            }

                            if (out != null) {
                                out.close();
                            }

                            throw var5;
                        }

                        if (out != null) {
                            out.close();
                        }
                    } catch (Throwable var57) {
                        if (var5 == null) {
                            var5 = var57;
                        } else if (var5 != var57) {
                            var5.addSuppressed(var57);
                        }

                        if (clientSocket != null) {
                            clientSocket.close();
                        }

                        throw var5;
                    }

                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (Throwable var58) {
                    if (var5 == null) {
                        var5 = var58;
                    } else if (var5 != var58) {
                        var5.addSuppressed(var58);
                    }

                    if (serverSocket != null) {
                        serverSocket.close();
                    }

                    throw var5;
                }

                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Throwable var59) {
                if (var5 == null) {
                    var5 = var59;
                } else if (var5 != var59) {
                    var5.addSuppressed(var59);
                }

                throw var5;
            }
        } catch (IOException var60) {
            var60.printStackTrace();
        } catch (InterruptedException var61) {
            var61.printStackTrace();
        }

    }



    public static ArrayList<byte[]> receiveMessage(BufferedInputStream in) throws IOException {
        ArrayList<byte[]> result = new ArrayList();
        byte[] type = new byte[1];
        in.read(type, 0, 1);
        result.add(type);
        byte[] length = new byte[2];
        in.read(length, 0, 2);
        int lengthInt = ByteBuffer.wrap(length).getChar();
        if (lengthInt < 0) {
            return null;
        } else {
            if (lengthInt == 0) {
                result.add((byte[])null); //CHANGED FROM (OBJECT)NULL TO BYTE(NULL)
            } else {
                byte[] data = new byte[lengthInt];
                in.read(data, 0, lengthInt);
                result.add(data);
            }

            return result;
        }
    }

    public static void sendError(OutputStream out, String errorMessage) throws IOException {
        System.out.println(errorMessage);

        try {
            sendMessage(out, (byte)3, (byte[])null);
        } catch (SocketException var3) {
            ;
        } catch (IOException var4) {
            throw var4;
        }

        System.exit(1);
    }

    public static void sendOK(OutputStream out, String message) throws IOException {
        System.out.println(message);
        sendMessage(out, (byte)2, (byte[])null);
    }

    public static void sendMessage(OutputStream out, byte type, byte[] data) throws IOException {
        byte[] sndBuffer = new byte[]{type};
        out.write(sndBuffer);
        if (data != null) {
            sndBuffer = ByteBuffer.allocate(2).putChar((char)data.length).array();
            out.write(sndBuffer);
            out.write(data);
        } else {
            sndBuffer = ByteBuffer.allocate(2).putShort((short)0).array();
            out.write(sndBuffer);
        }

    }
    public static void keepAliveLoop(OutputStream out, BufferedInputStream in) throws InterruptedException, IOException {
        Thread.sleep(3000L);
        sendMessage(out, (byte)1, (byte[])null);
        ArrayList<byte[]> received = receiveMessage(in);
        if (received == null) {
            sendError(out, "Invalid data length!");
        } else {
            byte[] type = (byte[])received.get(0);
            if (type[0] == 1) {
                System.out.println("type receieved in keep alive is "+ type[0]);
                sendOK(out, "Keepalive received.");
            } else {
                System.out.println("type receieved in keep alive is "+ type[0]);
                sendError(out, "Wrong message type!");
            }
        }

    }

    public static void emergencyLoop(OutputStream out, BufferedInputStream in, String imageFilename) throws InterruptedException, IOException {
        Thread.sleep((long)((Math.random() * 3.0D + 1.0D) * 1000.0D));
        BufferedReader fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(imageFilename), StandardCharsets.US_ASCII));
        String lines = "";

        for(String line = fileIn.readLine(); line != null; line = fileIn.readLine()) {
            lines = lines + line + "\n";
        }

        fileIn.close();
        byte[] data = lines.getBytes(StandardCharsets.US_ASCII);
        sendMessage(out, (byte)4, data);
        ArrayList<byte[]> received = receiveMessage(in);
        if (received == null) {
            sendError(out, "Invalid data length!");
        } else {
            byte[] type = (byte[])received.get(0);
            if (type[0] == 5) {
                sendOK(out, "Alarm rung!");
            } else if (type[0] == 6) {
                sendOK(out, "Nothing to worry about...");
            } else {
                sendError(out, "Wrong message type!");
            }
        }

    }
}
