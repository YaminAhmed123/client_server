package io.github.yaminahmed123.protocol;

import io.github.yaminahmed123.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/*
 *  BINTP (Binary Transfer Protocol) is a simple protocol developed by CortexR7 that uses a TCP connection to safely transfer binary data.
 *  This Comment will explain the Protocols structure and how servers are supposed to handle it.
 *  The first 4 bytes of the byte array must always be 0xFF this indicates that this is the Start of the header
 *  and which after the rest of the bytes are used to represent the filename using UTF-16 encoding.
 *  After the file name there must always be 4 bytes that have the value of a (byte) 0x00.
 *  In order for the server to understand the difference between filename bytes and binary data at the end of the filename
 *  there are 10 bytes that must always be 0xAA
 */

public class BINTP extends Protocol{

    public static int DEFAULT_PORT = 8066;
    private final String file_path;
    private final OutputStream os;
    private final int LOCAL_HEADER_SIZE_LIMIT = 1000;

    public BINTP(String file_name, String file_path, OutputStream os){

        byte[] file_name_binary_utf_16 = file_name.getBytes(StandardCharsets.UTF_16);
        super.PROTOCOL_HEADER = new byte[file_name_binary_utf_16.length + 8];

        // Entrypoint sequence for the header
        super.PROTOCOL_HEADER[0] = (byte) 0xFF;
        super.PROTOCOL_HEADER[1] = (byte) 0xFF;
        super.PROTOCOL_HEADER[2] = (byte) 0xFF;
        super.PROTOCOL_HEADER[3] = (byte) 0xFF;

        // Write filename to header
        for(int i = 4; i < file_name_binary_utf_16.length+4; ++i){
            super.PROTOCOL_HEADER[i] = file_name_binary_utf_16[i-4];
        }

        // Write exit point to header
        int last_header_index = file_name_binary_utf_16.length+7;
        super.PROTOCOL_HEADER[last_header_index] = (byte) 0x00;
        super.PROTOCOL_HEADER[last_header_index-1] = (byte) 0x00;
        super.PROTOCOL_HEADER[last_header_index-2] = (byte) 0x00;
        super.PROTOCOL_HEADER[last_header_index-3] = (byte) 0x00;

        // After the header there will be binary data and then a last terminator in byte format
        super.PROTOCOL_ENDING_SEQUENCE = new byte[10];   // the last 10bytes of the data that got send must always be 0xAA
        Arrays.fill(super.PROTOCOL_ENDING_SEQUENCE, (byte) 0xAA);


        // These variables need to be set for sending data
        this.file_path = file_path;
        this.os = os;
    }

    // This Constructor is only for server usage !
    public BINTP(){
        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "BINTP for server constructor was chosen");
        this.os = null;
        this.file_path = null;

        // After the header there will be binary data and then a last terminator in byte format
        super.PROTOCOL_ENDING_SEQUENCE = new byte[10];   // the last 10bytes of the data that got send must always be 0xAA
        Arrays.fill(super.PROTOCOL_ENDING_SEQUENCE, (byte) 0xAA);
    }

    /*
    * This function "works" but that is the only good thing about it.
    * It is hard to maintain and there is a chance where the last 10 bytes that are 0xAA are not in one buffer.
    * Which will cause a lot of errors. This will end up causing a lot of Exceptions, and it will corrupt the file that was sent.
    */

    private long BYTES_READ_TOTAL = 0;
    private synchronized void TOTAL_BYTES_READ(){
        while(this.BYTES_READ_TOTAL!=-1){
            double d = this.BYTES_READ_TOTAL;
            double MB_TOTAL = d / (1000.0*1000.0);
            Logger.APPLICATION_LOG_R("MONITORING", "Total Mega bytes written: "+MB_TOTAL+" MB");
            try{
                Thread.sleep(250);
            } catch (Exception e){
                Logger.ERROR_LOG("MONITORING", "Monitoring function failed !");
            }
        }
        System.out.println(" ");
    }
    
    @Override
    public void receive_data(InputStream is, byte[] buffer){
        String temp_file_name = "temp.bin";
        String filename = "ERROR";

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                TOTAL_BYTES_READ();
            }
        });

        try{
            boolean firstcall = true;
            int bytesRead;
            OutputStream os = new FileOutputStream(temp_file_name);
            while((bytesRead=writeInputStreamToBuffer(is, buffer)) != -1){
                BYTES_READ_TOTAL += bytesRead;
                if(firstcall){
                    // Total bytes read print
                    t.start();

                    if(checkFirst4Bytes(buffer)==-1){
                        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Quiting loop no header found !");
                        firstcall = false;
                        break;
                    }
                    firstcall = false;
                }
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            this.BYTES_READ_TOTAL = -1;

            t.join();
            this.BYTES_READ_TOTAL = 0;
            Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Raw data written to disk as: "+temp_file_name);
        } catch(Exception e ) {
            e.printStackTrace();
        }

        Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Entering filter stage for temp.bin");
        // Fetch from disk and filter the header and ending sequence
        try {
            FileInputStream fis = new FileInputStream(temp_file_name);
            byte[] first_4_bytes = new byte[4];
            if(fis.read(first_4_bytes) == 4){
                if(checkFirst4Bytes(first_4_bytes) == 0){
                    Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Header passed the entry");
                } else{
                    Logger.ERROR_LOG("SERVER-ERROR", "Header entry checking failed !");
                }
            } else {
                Logger.ERROR_LOG("SERVER-ERROR", "File rejected by server !");
            }

            // fetch header string name
            byte[] string_name_buffer = new byte[LOCAL_HEADER_SIZE_LIMIT];
            int bytesRead_STRING_NAME_BUFFER = fis.read(string_name_buffer);
            int index = exitHeaderIndex(string_name_buffer, 0, bytesRead_STRING_NAME_BUFFER);

            int AA = compareLast10Bytes(string_name_buffer, bytesRead_STRING_NAME_BUFFER);
            Logger.FUNCTION_LOG_D("DEBUG-MAIN", "Position for AA at: "+AA);

            if(index != -1){
                byte[] string_name = new byte[index];
                for(int i = 0; i < index; ++i){
                    string_name[i] = string_name_buffer[i];
                }
                filename = new String(string_name, StandardCharsets.UTF_16);
                Logger.FUNCTION_LOG_D("DEBUG-MAIN", "The filename is: "+filename);
                FileOutputStream fos = new FileOutputStream(filename);

                if(AA!=-1){ // if all data is already in header
                    int element_count = 0;
                    for(int i = index+4; i < AA; ++i){
                        ++element_count;
                    }
                    Logger.FUNCTION_LOG_D("DEBUG-MAIN", element_count+" size of raw data bytes in header");
                    byte[] arr = new byte[element_count];
                    int mini_int  = 0;
                    for(int i = index+4; i < AA; ++i){
                        arr[mini_int] = string_name_buffer[i];
                        ++mini_int;
                    }
                    fos.write(arr);
                    fos.close();
                } else{
                    int element_count = 0;
                    for(int i = index+4; i < bytesRead_STRING_NAME_BUFFER; ++i){
                        ++element_count;
                    }
                    Logger.FUNCTION_LOG_D("DEBUG-MAIN", element_count+" size of raw data bytes in header");
                    byte[] arr = new byte[element_count];
                    int mini_int  = 0;
                    for(int i = index+4; i < bytesRead_STRING_NAME_BUFFER; ++i){
                        arr[mini_int] = string_name_buffer[i];
                        ++mini_int;
                    }
                    fos.write(arr);

                    int actual_bytes_read;
                    while((actual_bytes_read=fis.read(buffer))!=-1){
                        if(compareLast10Bytes(buffer, actual_bytes_read)!=-1){
                            fos.write(buffer, 0, actual_bytes_read-10);
                            break;
                        }
                        fos.write(buffer, 0, actual_bytes_read);
                    }
                    fos.close();
                }
                fis.close();
                try{
                    Path path = Paths.get("temp.bin");
                    Files.delete(path);
                } catch(IOException e){
                    e.printStackTrace();
                }
            } else{
                fis.close();
                try{
                    Path path = Paths.get("temp.bin");
                    Files.delete(path);
                } catch(IOException e){
                    e.printStackTrace();
                }
                Logger.ERROR_LOG("SERVER-ERROR", "Deleted file temp.bin");
                Logger.ERROR_LOG("SERVER-ERROR", "Exiting request since it was not send via BINTP !");
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private int compareLast10Bytes(byte[] buffer, int length){
        int index = 0;
        for(int i = 0; i<length-10; ++i){
            if(
                    buffer[i] == (byte)0xAA &&
                            buffer[i+1] == (byte)0xAA &&
                            buffer[i+2] == (byte)0xAA &&
                            buffer[i+3] == (byte)0xAA &&
                            buffer[i+4] == (byte)0xAA &&
                            buffer[i+5] == (byte)0xAA &&
                            buffer[i+6] == (byte)0xAA &&
                            buffer[i+7] == (byte)0xAA &&
                            buffer[i+8] == (byte)0xAA &&
                            buffer[i+9] == (byte)0xAA
            ){
                return i;
            }
        }
        return -1;
    }

    // The buffer must be 4 bytes large at min and max
    private int checkFirst4Bytes(byte[] buffer){
        if(
                buffer[0] == (byte)0xFF &&
                buffer[1] == (byte)0xFF &&
                buffer[2] == (byte)0xFF &&
                buffer[3] == (byte)0xFF
        ){
            return 0;
        } else{
            return -1;
        }
    }

    private int check4bytes1mode(byte[] buffer){
        if(
                buffer[0] == -1 &&
                buffer[1] == -1 &&
                buffer[2] == -1 &&
                buffer[3] == -1
        ){
            return 0;
        } else{
            return -1;
        }
    }

    private int writeInputStreamToBuffer(InputStream is, byte[] buffer){
        try {
            return is.read(buffer);
        } catch(IOException e){
            System.out.println("Not able to read stream! at BINTP writeInputStreamToBuffer !");
            e.printStackTrace();
            return -1;
        }
    }

    private int exitHeaderIndex(byte[] buffer, int offsetIndex, int length){
        for(int i = offsetIndex; i<length; ++i){
            if(buffer[i] == 0x00 && buffer[i+1] == 0x00 && buffer[i+2] == 0x00 && buffer[i+3] == 0x00){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void send_data(byte[] buffer) {
        try{
            for(int i = 0; i < super.PROTOCOL_HEADER.length; ++i){
                buffer[i] = super.PROTOCOL_HEADER[i];
            }
            this.os.write(buffer, 0, super.PROTOCOL_HEADER.length);

            FileInputStream fis = new FileInputStream(this.file_path);
            int bytesRead;
            while((bytesRead = fis.read(buffer)) != -1){
                this.os.write(buffer, 0, bytesRead);
            }

            for(int i = 0; i < super.PROTOCOL_ENDING_SEQUENCE.length; ++i){
                buffer[i] = super.PROTOCOL_ENDING_SEQUENCE[i];
            }
            this.os.write(buffer, 0 , super.PROTOCOL_ENDING_SEQUENCE.length);
            this.os.close();
        } catch(IOException e){
            e.printStackTrace();
            System.out.println("FAILED AT BINTP function send_data");
        }
    }
}
