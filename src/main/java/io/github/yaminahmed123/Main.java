package io.github.yaminahmed123;

import io.github.yaminahmed123.protocol.BINTP;

// This branch is to develop client/server not meant for usage !
public class Main {
    public static void main(String[] args) {
        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "The system is running in debug mode !");
        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Consider running a release version !");

        // Run the server in a different Thread
        Server server = new Server(new BINTP());
        server.start();                         // Look for the logs the server might print some stuff.

        // Set up the client

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Client client = new Client("blackspider.zapto.org", new BINTP());
                client.send_binary_data_bintp("A.mkv", "C:\\Users\\Yamin\\Downloads\\a.mkv");
            }
        });
        t.start();

        try {
            Thread.sleep(1000 * 25);  // 1,000 milliseconds = 1 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.turnOffServer();
        Logger.ERROR_LOG("APPLICATION", "Exit was taken do not use master to run server/client !");
    }
}