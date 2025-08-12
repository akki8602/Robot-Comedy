// Socket Bridge to accept input from GPT API
import java.io.*;
import java.net.*;

public class MiloSocketBridge {

    public static final int BRIDGE_PORT = 9999;  
    public static final String MILO_IP = "100.64.235.149";
    public static final int MILO_PORT = 4001;

    public static void main(String[] args) {
        try (
            ServerSocket serverSocket = new ServerSocket(BRIDGE_PORT);
            Socket pythonClient = serverSocket.accept();
            BufferedReader inFromPython = new BufferedReader(new InputStreamReader(pythonClient.getInputStream()));
        ) {
            System.out.println("Python application connected");

            try (
                Socket miloSocket = new Socket(MILO_IP, MILO_PORT);
                OutputStream miloOut = miloSocket.getOutputStream()
            ) {
                System.out.println("Connected to Milo at " + MILO_IP);

                String command = inFromPython.readLine();
                //while ((command = inFromPython.readLine()) != null) {
                    System.out.println("Sending to Milo: " + command);
                    miloOut.write(("speak " + command + "\n").getBytes());
                //}

            } catch (IOException miloErr) {
                System.err.println("Error connecting to Milo:");
                miloErr.printStackTrace();
            }

        } catch (IOException ioErr) {
            System.err.println("Error with socket server:");
            ioErr.printStackTrace();
        }
    }
}
