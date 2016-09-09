package client;

import exceptions.*;
import server.TramServer;
import server.TramServerImpl;
import utilities.Message;
import utilities.RPCMessage;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TramClient implements Runnable {
    private UUID tramId;
    private Integer tramLine;
    private List tramLineList;
    private String currentStop;
    private String previousStop = "0";
    private Registry registry;
    private TramServer remoteServer;
    private static final int PORT = 3419;
    private Direction direction;

    private enum Direction {
        UP, BACK
    }

    public TramClient(int line) throws RemoteException, NotBoundException, ProcedureException, MessageLengthMismatch, TramNotValidException, LineNotFoundException, MessageTypeException, StatusException, TransactionException, RpcIdException, RequestException {
        this.registry = LocateRegistry.getRegistry("localhost", PORT);
        this.remoteServer = (TramServer) registry.lookup("rmi://localhost/TramServer/");
        this.tramLine = line;
        this.tramId = UUID.randomUUID();
        this.tramLineList = this.getTramLineList();
    }

    public static void main(String[] args) {
        if (args.length != 2) {

            System.err.println("Welcome to the Tram Service");
            System.err.println("Lines {1, 96, 101, 109, 112} are available");
            System.err.println("Usage: <linenumber> <NoOfTrams>");
            System.exit(1);
        }
        try {
            int noOfTrams = Integer.parseInt(args[1]);
            if(noOfTrams > 5){
                System.err.println("Tram Value must be lower than 5");
                System.exit(1);
            }

            ExecutorService taskExecutor = Executors.newFixedThreadPool(noOfTrams);
            for(int i = 0;i < Integer.parseInt(args[1]); i++){
                taskExecutor.submit(new TramClient(Integer.parseInt(args[0])));
            }


        } catch (RemoteException ex) {
            System.err.println("Couldn't contact registry.");
            System.err.println(ex);
            System.exit(1);
        } catch (NotBoundException ex) {
            System.err.println("There is no object bound to " + args[0]);
            System.exit(1);
        } catch (MessageLengthMismatch messageLengthMismatch) {
            messageLengthMismatch.printStackTrace();
        } catch (ProcedureException e) {
            e.printStackTrace();
        } catch (MessageTypeException e) {
            e.printStackTrace();
        } catch (TramNotValidException e) {
            e.printStackTrace();
        } catch (LineNotFoundException e) {
            e.printStackTrace();
        } catch (StatusException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (RpcIdException e) {
            e.printStackTrace();
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    /*
    * Pass the line received from the startup arguments into the remote server method and retrieve the line for that
    * tram.
    *
    * */
    private List getTramLineList() throws ProcedureException, RemoteException, MessageLengthMismatch, TramNotValidException, MessageTypeException, LineNotFoundException, StatusException, TransactionException, RpcIdException, RequestException {
        List<Integer> stops = new LinkedList();
        String[] query = {this.tramId.toString(), this.tramLine.toString()};

        //create Rpc Message with the details to be marshalled.
        RPCMessage rpcSentMessage = new RPCMessage(RPCMessage.MessageType.REQUEST,1, new Short("2"), generateCSVString(query), new Short("0"));
        rpcSentMessage.setTransactionId(System.nanoTime() * 5);
        rpcSentMessage.setRPCId(System.nanoTime() * 10);


        Message message = new Message();
        message.marshal(rpcSentMessage);

        Message receivedMarshalled = remoteServer.requestLineList(message);
        RPCMessage rpcReceivedMessage = receivedMarshalled.unMarshal();

        if(rpcReceivedMessage.getStatus() != 0){
            throw new StatusException(rpcReceivedMessage.getStatus() +" Status is incorrect, something went wrong");
        }
        if(rpcReceivedMessage.getTransactionId() != rpcSentMessage.getTransactionId()){
            throw new TransactionException(rpcReceivedMessage.getTransactionId()+ ": Transaction Id Mismatch");
        }
        if(rpcReceivedMessage.getRPCId() != rpcSentMessage.getRPCId()){
            throw new RpcIdException(rpcReceivedMessage.getRPCId() + ": Rpc Id Mismatch");
        }
        if(rpcReceivedMessage.getRequestId() != rpcSentMessage.getRequestId()){
            throw new RequestException("Request Id MisMatch");
        }

        if(rpcReceivedMessage.getCsv_data() == null){
            throw new MessageLengthMismatch(rpcReceivedMessage.getCsv_data()+ " Csv Data is not set.");
        }
        String[] csvData = rpcReceivedMessage.getCsv_data().split(",");
        for(String s: csvData){
            stops.add(Integer.parseInt(s));
        }
        this.direction = Direction.UP;
        this.currentStop = csvData[0];
        return stops;
    }

    /*
    * update location on track and der
    * */
    private void updateLocation() throws ProcedureException, IOException, MessageLengthMismatch, StatusException, TransactionException, RpcIdException, RequestException, MessageTypeException, TramNotValidException {

        String[] query = {this.tramLine.toString(),this.tramId.toString(),this.currentStop};

        RPCMessage rpcSentMessage = new RPCMessage(RPCMessage.MessageType.REQUEST, 1, new Short("1"), generateCSVString(query), new Short("0"));
        rpcSentMessage.setTransactionId(System.nanoTime() * 5);
        rpcSentMessage.setRPCId(System.nanoTime() * 10);


        Message message = new Message();
        message.marshal(rpcSentMessage);

        Message receivedMarshalled = remoteServer.updateTramLocation(message);

        RPCMessage rpcReceivedMessage = receivedMarshalled.unMarshal();

        if(rpcReceivedMessage.getStatus() != 0){
            throw new StatusException(rpcReceivedMessage.getStatus() +" Status is incorrect, something went wrong");
        }
        if(rpcReceivedMessage.getTransactionId() != rpcSentMessage.getTransactionId()){
            throw new TransactionException(rpcReceivedMessage.getTransactionId()+ ": Transaction Id Mismatch");
        }
        if(rpcReceivedMessage.getRPCId() != rpcSentMessage.getRPCId()){
            throw new RpcIdException(rpcReceivedMessage.getRPCId() + ": Rpc Id Mismatch");
        }
        if(rpcReceivedMessage.getRequestId() != rpcSentMessage.getRequestId()){
            throw new RequestException("Request Id MisMatch");
        }

    }

    private void requestNextStop() throws ProcedureException, RemoteException, MessageLengthMismatch, StatusException, TransactionException, RpcIdException, RequestException {

        String[] query = {this.tramLine.toString(),this.currentStop,this.previousStop};

        RPCMessage rpcSentMessage = new RPCMessage(RPCMessage.MessageType.REQUEST, 1, new Short("0"), generateCSVString(query), new Short("0"));
        rpcSentMessage.setTransactionId(System.nanoTime() * 5);
        rpcSentMessage.setRPCId(System.nanoTime() * 10);


        Message message = new Message();
        message.marshal(rpcSentMessage);

        Message receivedMarshalled = remoteServer.retrieveNextStop(message);
        RPCMessage rpcReceivedMessage = receivedMarshalled.unMarshal();


        if(rpcReceivedMessage.getStatus() != 0){
            throw new StatusException(rpcReceivedMessage.getStatus() +" Status is incorrect, something went wrong");
        }
        if(rpcReceivedMessage.getTransactionId() != rpcSentMessage.getTransactionId()){
            throw new TransactionException(rpcReceivedMessage.getTransactionId()+ ": Transaction Id Mismatch");
        }
        if(rpcReceivedMessage.getRPCId() != rpcSentMessage.getRPCId()){
            throw new RpcIdException(rpcReceivedMessage.getRPCId() + ": Rpc Id Mismatch");
        }
        if(rpcReceivedMessage.getRequestId() != rpcSentMessage.getRequestId()){
            throw new RequestException("Request Id MisMatch");
        }

        if(rpcReceivedMessage.getCsv_data() == null){
            throw new MessageLengthMismatch(rpcReceivedMessage.getCsv_data()+ " Csv Data is not set.");
        }

        this.previousStop = this.currentStop;
        this.currentStop = rpcReceivedMessage.getCsv_data().split(",")[0];
        this.direction = getDirection(Integer.parseInt(this.currentStop), Integer.parseInt(this.previousStop));

    }

    private void printMessage(RPCMessage msg){

    }

    private void printTramDetails() {

        System.out.println("Tram: " + this.tramId);
        System.out.print("Is Currently Stopped at: " + this.currentStop+ " at ");
        System.out.println("Travelling in Direction: "+ this.direction.toString());


    }

    private String generateCSVString(String array[]) {
        String r = "";
        for (String a : array) {
            r = r + a + ",";
        }
        return r;
    }
    private Direction getDirection(int currentStop, int previousStop){

        for(int i = 0; i <= this.tramLineList.size()-1; i++){
            if(currentStop == this.tramLineList.get(0) && (previousStop == this.tramLineList.get(1)|| previousStop ==0)){
                return Direction.UP;
            } else if(currentStop == this.tramLineList.get(i) && currentStop != this.tramLineList.get(this.tramLineList.size()-1)){

                if(this.tramLineList.indexOf(currentStop) > this.tramLineList.indexOf(previousStop)){
                    return Direction.UP;

                }else if(this.tramLineList.indexOf(currentStop) < this.tramLineList.indexOf(previousStop)){
                    return Direction.BACK;

                }
            }else if(currentStop == this.tramLineList.get(i) && currentStop == this.tramLineList.get(this.tramLineList.size()-1)){
                return Direction.BACK;
            }
        }
        return Direction.UP;
    }


    @Override
    public void run() {
        try {
            while(true){
                printTramDetails();
                updateLocation();
                Random r = new Random();
                int low = 10000;
                int high = 20000;
                int result = r.nextInt(high-low) + low;
                Thread.sleep(result);
                requestNextStop();
            }

        } catch (ProcedureException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MessageLengthMismatch messageLengthMismatch) {
            messageLengthMismatch.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (RequestException e) {
            e.printStackTrace();
        } catch (RpcIdException e) {
            e.printStackTrace();
        } catch (StatusException e) {
            e.printStackTrace();
        } catch (MessageTypeException e) {
            e.printStackTrace();
        } catch (TramNotValidException e) {
            e.printStackTrace();
        }
    }
}
