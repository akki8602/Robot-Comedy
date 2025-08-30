// Socket Bridge to accept input from GPT API
import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class MiloSocketBridge {

    public static final int BRIDGE_PORT = 9999;  
    public static final String MILO_IP = "100.64.235.149";
    public static final int MILO_PORT = 4001;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
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

                String line = "", expression = "", thoughts = "", speech = "";
                while((line = inFromPython.readLine()) != null) {
                    JsonNode root = mapper.readTree(line);

                    expression = root.path("Expression").asText("");
                    System.out.println("Expression: " + expression);
                    thoughts   = root.path("Thoughts").asText("");
                    speech     = root.path("Speech").asText("");
                    System.out.println("Speech: " + speech);
                }
                String command = inFromPython.readLine();

                String out = "";
                    
                    out += "keyframe set " + expression + "\n";
                    miloOut.write(out.getBytes());
                    out = "speak " + speech + "\n";
                    System.out.println("out: " + out);
                    miloOut.write(out.getBytes());

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
