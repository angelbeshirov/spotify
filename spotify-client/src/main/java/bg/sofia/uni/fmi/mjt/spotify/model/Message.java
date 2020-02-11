package bg.sofia.uni.fmi.mjt.spotify.model;

import java.io.Serializable;

/**
 * Object representation of the messages exchanged by the server and the client.
 *
 * @author angel.beshirov
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -8330694407465678518L;
    private final MessageType messageType;
    private final byte[] value;

    public Message(MessageType messageType, byte[] value) {
        this.messageType = messageType;
        this.value = value;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
                '}';
    }
}
