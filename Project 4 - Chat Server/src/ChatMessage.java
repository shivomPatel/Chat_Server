import java.io.Serializable;

final public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    String msg;
    int type;

    public ChatMessage(String msg, int type) {
        this.msg = msg;
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }
}
