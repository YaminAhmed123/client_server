package io.github.yaminahmed123;

import io.github.yaminahmed123.protocol.BINTP;

// This branch is to develop client/server not meant for usage !
public class Main {
    public static void main(String[] args) {
        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "The system is running in debug mode !");
        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Consider running a release version !");

        // Run the server in a different Thread
        Server server = new Server(BINTP.DEFAULT_PORT);
        server.start();                         // Look for the logs the server might print some stuff.

        // Set up the client
        Client client = new Client("localhost", BINTP.DEFAULT_PORT);
        client.send_binary_data_bintp("Blender.jpg", "C:\\Users\\Yamin\\Downloads\\SCREEN.jpg");

        try {
            Thread.sleep(1000 * 15);  // 1,000 milliseconds = 1 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.turnOffServer();
        Logger.ERROR_LOG("APPLICATION", "Exit was taken do not use master to run server/client !");
    }
}