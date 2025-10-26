package io.github.yaminahmed123.protocol;

import java.io.InputStream;

public interface Binarydata{
    public void send_data();
    public void send_data(byte [] buffer);

    public void receive_data();
    public void receive_data(InputStream is, byte[] buffer);
}
