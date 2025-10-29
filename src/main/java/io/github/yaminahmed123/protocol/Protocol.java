package io.github.yaminahmed123.protocol;

/*
*  The Protocol class is an abstract class that only defines a skeleton for all Protocols.
*  The client and the server will both get a feature where you can pass in any class that is a subclass of Protocol.
*  This way the client and the server both get more flexibility for different Protocols and do not need to have these implementations included.
*/

public abstract class Protocol implements Binarydata{
    public byte[] PROTOCOL_HEADER;
    public byte[] PROTOCOL_ENDING_SEQUENCE;
    public int DEFAULT_PORT = -1;

    @Override
    public void receive_data() {
        System.out.println("Protocol does not support method");
    }

    @Override
    public void send_data() {
        System.out.println("Protocol does not support method");
    }

    public Protocol() {}
}
