package bg.sofia.uni.fmi.mjt.spotify.server;

/**
 * @author angel.beshirov
 */
public class Message {
    private MessageType messageType;
    private String value;

    public Message(MessageType messageType, String value) {
        this.messageType = messageType;
        this.value = value;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
