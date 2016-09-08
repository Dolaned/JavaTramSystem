package utilities;

import exceptions.MessageLengthMismatch;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by dylanaird on 5/09/2016.
 */
public class Message implements Serializable {
    protected byte data[] = null;
    protected int length = 0;

    public Message() {
    }

    public void marshal(RPCMessage rpcMessage) {

        //get size of Rpc Object and allocate a buffer to the same size
        int buffSize = rpcMessage.getsize();
        ByteBuffer bb = ByteBuffer.allocate(buffSize);
        //get the type of message, either request or reply.
        short typeNum = (short) rpcMessage.getMessageType().ordinal();

        //set the byte order to BIG_ENDIAN;
        bb.order(ByteOrder.BIG_ENDIAN);

        int index = 0;
        //request short
        bb.putShort(0, typeNum);
        index += 2;

        //transaction and rpcid
        bb.putLong(index, rpcMessage.getTransactionId());
        index += 8;
        bb.putLong(index, rpcMessage.getRPCId());
        index += 8;

        //get request id
        bb.putLong(index, rpcMessage.getRequestId());
        index += 8;

        //procedureid
        bb.putShort(index, rpcMessage.getProcedureId());
        index += 2;

        //statusid
        bb.putShort(index, rpcMessage.getStatus());
        index += 2;


        String data = rpcMessage.getCsv_data();
        for (int i = 0; i < data.length(); i++, index += 2) {
            bb.putChar(index, data.charAt(i));
        }
        this.data = bb.array();
        this.length = index;
    }

    public RPCMessage unMarshal() throws MessageLengthMismatch {

        RPCMessage rpcMessage = new RPCMessage();
        ByteBuffer bb = ByteBuffer.wrap(this.data);
        if (this.data.length != this.length) throw new MessageLengthMismatch("message was not completely received");
        bb.order(ByteOrder.BIG_ENDIAN);

        //get request type.
        int index = 0;
        rpcMessage.setMessageType(RPCMessage.MessageType.values()[bb.getShort(index)]);
        index += 2;

        //get transaction and rpc id
        rpcMessage.setTransactionId(bb.getLong(index));
        index += 8;
        rpcMessage.setRPCId(bb.getLong(index));
        index += 8;

        //request id
        rpcMessage.setRequestId(bb.getLong(index));
        index += 8;

        //procedure id
        rpcMessage.setProcedureId(bb.getShort(index));
        index += 2;

        //status id.
        rpcMessage.setStatus(bb.getShort(index));
        index += 2;


        StringBuffer sb = new StringBuffer();
        for (; index < bb.array().length; index += 2) {
            sb.append(bb.getChar(index));
        }
        rpcMessage.setCsv_data(sb.toString());

        return rpcMessage;

    }

}
