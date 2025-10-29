package io.github.yaminahmed123;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

import io.github.yaminahmed123.protocol.BINTP;
import io.github.yaminahmed123.protocol.Protocol;

public class Client {
    private Socket client_socket;
    private byte[] DATA_BUFFER = new byte[1000 * 1000 * 128];     //64MB large buffer
    private Protocol protocol;

    public Client(String host, Protocol protocol){
        this.protocol = protocol;
        setClient_socket(host, this.protocol.DEFAULT_PORT);
    }

    private void setClient_socket(String host, int port){
        try{
            this.client_socket = new Socket(host, port);
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    // This function is a dummy to test the server
    public void send_data(String file_path){
        String text = "Hello from client ignore junk at end";
        try{
            OutputStream os = this.client_socket.getOutputStream();
            FileInputStream fis = new FileInputStream(file_path);
            int bytesRead;
            while((bytesRead = fis.read(DATA_BUFFER)) != -1){
                os.write(DATA_BUFFER, 0, bytesRead);
            }
        } catch (IOException e){
                e.printStackTrace();
        }
    }

    public void send_binary_data_bintp(String file_name, String file_path){
        try {
            OutputStream os = this.client_socket.getOutputStream();
            BINTP bintp = new BINTP(
                    file_name,
                    file_path,
                    os
            );
            bintp.send_data(this.DATA_BUFFER);
        } catch(IOException e){
            e.printStackTrace();
            System.out.println("FAILED AT CLIENT function send_binary_data_bintp");
        }
    }

}
