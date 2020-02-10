package bg.sofia.uni.fmi.mjt.spotify.client.model;

/**
 * Object representation of the messages received from the server.
 *
 * @author angel.beshirov
 */
public class Message {
    private MessageType messageType;
    private String value;

    public Message() {
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
    } // TODO remove them to try to deserialize message?
}
