package utilities;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by dylanaird on 5/09/2016.
 */
public class RPCMessage implements Serializable {

    public static final short REQUEST = 0;
    public static final short REPLY = 1;

    public enum MessageType {REQUEST, REPLY}

    private MessageType messageType;
    private long TransactionId; /* transaction id */
    private long RPCId; /* Globally unique identifier */
    private long RequestId; /* Client request message counter */
    private short procedureId; /* e.g.(1,2,3,4) */
    private String csv_data; /* data as comma separated values*/
    private short status;

    public RPCMessage(MessageType msgType, long reqId, short procId, String csv, short stat) {
        this.messageType = msgType;
        this.RequestId = reqId;
        this.procedureId = procId;
        this.csv_data = csv;
        this.status = stat;
    }

    public RPCMessage(){}

    public int getsize() {
        return 2 + 8 + 8 + 8 + 2 + 2 + this.csv_data.length() * 2;
    }


    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public long getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(long transactionId) {
        TransactionId = transactionId;
    }

    public long getRPCId() {
        return RPCId;
    }

    public void setRPCId(long RPCId) {
        this.RPCId = RPCId;
    }

    public long getRequestId() {
        return RequestId;
    }

    public void setRequestId(long requestId) {
        RequestId = requestId;
    }

    public short getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(short procedureId) {
        this.procedureId = procedureId;
    }

    public String getCsv_data() {
        return csv_data;
    }

    public void setCsv_data(String csv_data) {
        this.csv_data = csv_data;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }


}
