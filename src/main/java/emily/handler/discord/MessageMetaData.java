package emily.handler.discord;

public class MessageMetaData {
    private final long channelId;
    private final long messageId;

    public MessageMetaData(long channelId, long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getMessageId() {
        return messageId;
    }
}
