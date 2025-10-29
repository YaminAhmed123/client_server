package io.github.yaminahmed123;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;

import io.github.yaminahmed123.protocol.BINTP;
import io.github.yaminahmed123.protocol.Protocol;

public class Server extends Thread{
    private ServerSocket server_socket;
    private Socket client_socket;
    private boolean server_on = true;
    private InputStream is;
    private byte[] SERVER_BUFFER = new byte[1000 * 1000 * 512];

    private Protocol protocol;

    public Server(Protocol protocol){
        this.protocol = protocol;
        setServer_socket(this.protocol.DEFAULT_PORT);
    }

    private void setServer_socket(int port){
        try{
            this.server_socket = new ServerSocket(port, 1);
            Logger.APPLICATION_LOG("SERVER-MAIN", "Listening on port: "+this.protocol.DEFAULT_PORT);
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

    private void run_server(){
        if(waitForConnection() == 0){
            String ip = this.client_socket.getInetAddress().getHostAddress();
            Logger.APPLICATION_LOG("SERVER-MAIN", "Connection established with IP: "+ip);
            try{
                this.is = this.client_socket.getInputStream();
            } catch(IOException e){
                e.printStackTrace();
            }
            this.protocol.receive_data(this.is, SERVER_BUFFER);

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
            Logger.ERROR_LOG("SERVER-MAIN", "Could not turn off server !");
            e.printStackTrace();
        }
    }
}
