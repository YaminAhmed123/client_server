package io.github.yaminahmed123.protocol.bintp;

import io.github.yaminahmed123.Logger;

/*
* This class is used to monitor the amount of bytes that have been written to disk by the application.
* Take in mind that this class is not supposed to be used by any other class or when using the library.
* Its only exists to help monitor the state in the BINTP class.
*/

class Monitoring extends Thread{
    private long bytesRead = 0;

    @Override
    public void run(){
        while(this.bytesRead!=-1){
            double d = this.bytesRead;
            double MB_TOTAL = d / (1000.0*1000.0);
            Logger.APPLICATION_LOG_R("MONITORING", "Total Mega bytes written: "+MB_TOTAL+" MB");
            try{
                Thread.sleep(250);
            } catch (Exception e){
                Logger.ERROR_LOG("MONITORING", "Monitoring function failed !");
            }
        }
        System.out.println(" "); // adding line break after finishing monitoring
    }

    public void addBytes(long bytes){
        this.bytesRead += bytes;
    }

    public void setBytes(long bytes){
        this.bytesRead = bytes;
    }
}
