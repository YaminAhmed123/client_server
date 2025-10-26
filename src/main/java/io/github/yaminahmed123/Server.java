package io.github.yaminahmed123;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;

import io.github.yaminahmed123.protocol.BINTP;

public class Server extends Thread{
    private ServerSocket server_socket;
    private Socket client_socket;
    public boolean server_on = true;
    private InputStream is;
    private byte[] SERVER_BUFFER = new byte[1000 * 1000 * 512];

    public Server(int port){
        setServer_socket(port);
    }

    private void setServer_socket(int port){
        try{
            this.server_socket = new ServerSocket(port, 1);
        } catch(IOException e){
            Logger.APPLICATION_LOG("SERVER-MAIN", "Server shutting down now");
        }
    }

    private int waitForConnection(){
        try{
            this.client_socket = server_socket.accept();
            return 0;
        } catch(IOException e){
            if(!server_on){
                Logger.APPLICATION_LOG("SERVER-MAIN", "Exit return 0");
            } else{
                e.printStackTrace();
            }
            return -1;
        }
    }

    @Override
    public void run(){
        Logger.APPLICATION_LOG("SERVER-MAIN","Server started now");
        while(this.server_on){
            Logger.APPLICATION_LOG("SERVER-MAIN", "Waiting for new connections");
            System.out.println(" ");
            run_server();
        }
    }

    // Test function to test data received from client
    private void receive_data(){
        try{
            InputStream is = this.client_socket.getInputStream();
            byte[] data_buffer = new byte[8*1000];      // 8KB large buffer
            int r_bytes = is.read(data_buffer);
            String text = new String(data_buffer, StandardCharsets.UTF_16);
            System.out.println(text);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void run_server(){
        if(waitForConnection() == 0){
            String ip = this.client_socket.getInetAddress().getHostAddress();
            Logger.APPLICATION_LOG("SERVER-MAIN", "Connection established with IP: "+ip);
            try{
                this.is = this.client_socket.getInputStream();
            } catch(IOException e){
                e.printStackTrace();
            }
            BINTP bintp = new BINTP();
            bintp.receive_data(this.is, SERVER_BUFFER);

            Logger.APPLICATION_LOG("SERVER-MAIN", "File received successfully");
        } else{
            if(!server_on){
                Logger.APPLICATION_LOG("SERVER-MAIN", "Server shutting down");
            } else{
                Logger.ERROR_LOG("SERVER-ERROR", "Server cant establish connection !");
            }
        }
    }

    public void turnOffServer(){
        try{
            this.server_on = false;
            this.server_socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
