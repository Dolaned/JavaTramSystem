package client;

import exceptions.MessageLengthMismatch;
import exceptions.ProcedureException;
import server.TramServer;
import utilities.Message;
import utilities.RPCMessage;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
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

    public enum Direction {
        UP, BACK
    }

    public TramClient(int line) throws RemoteException, NotBoundException, ProcedureException, MessageLengthMismatch {
        this.registry = LocateRegistry.getRegistry("localhost", PORT);
        this.remoteServer = (TramServer) registry.lookup("rmi://localhost/TramServer/");
        this.tramLine = line;
        this.tramId = UUID.randomUUID();
        this.tramLineList = this.getTramLineList();
    }

    public static void main(String[] args) {
        if (args.length != 3) {

            System.err.println();
            System.err.println("Usage: TramClient <linenumber> <NoOfTrams>");
            System.exit(1);
        }
        try {
            int noOfTrams = Integer.parseInt(args[2]);
            if(noOfTrams > 5){
                System.err.println("Tram Value must be lower than 5");
                System.exit(1);
            }

            ExecutorService taskExecutor = Executors.newFixedThreadPool(noOfTrams);
            for(int i = 0;i < Integer.parseInt(args[2]); i++){
                taskExecutor.submit(new TramClient(Integer.parseInt(args[1])));
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
        }
    }

    /*
    * Pass the line received from the startup arguments into the remote server method and retrieve the line for that
    * tram.
    *
    * */
    private List getTramLineList() throws ProcedureException, RemoteException, MessageLengthMismatch {
        List<Integer> stops = new LinkedList();
        String[] query = {this.tramId.toString(), this.tramLine.toString()};

        //create Rpc Message with the details to be marshalled.
        RPCMessage rpcSentMessage = new RPCMessage(RPCMessage.MessageType.REQUEST, 1, new Short("2"), generateCSVString(query), new Short("1"));
        rpcSentMessage.setTransactionId(System.nanoTime() * 5);
        rpcSentMessage.setRPCId(System.nanoTime() * 10);


        Message message = new Message();
        message.marshal(rpcSentMessage);

        Message receivedMarshalled = remoteServer.requestLineList(message);
        RPCMessage rpcReceivedMessage = receivedMarshalled.unMarshal();

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
    private void updateLocation() throws ProcedureException, RemoteException, MessageLengthMismatch {

        String[] query = {this.tramLine.toString(),this.tramId.toString(),this.currentStop};

        RPCMessage rpcSentMessage = new RPCMessage(RPCMessage.MessageType.REQUEST, 1, new Short("1"), generateCSVString(query), new Short("1"));
        rpcSentMessage.setTransactionId(System.nanoTime() * 5);
        rpcSentMessage.setRPCId(System.nanoTime() * 10);


        Message message = new Message();
        message.marshal(rpcSentMessage);

        Message receivedMarshalled = remoteServer.updateTramLocation(message);
        RPCMessage rpcReceivedMessage = receivedMarshalled.unMarshal();


    }

    private void requestNextStop() throws ProcedureException, RemoteException, MessageLengthMismatch {

        String[] query = {this.tramLine.toString(),this.currentStop,this.previousStop};

        RPCMessage rpcSentMessage = new RPCMessage(RPCMessage.MessageType.REQUEST, 1, new Short("0"), generateCSVString(query), new Short("1"));
        rpcSentMessage.setTransactionId(System.nanoTime() * 5);
        rpcSentMessage.setRPCId(System.nanoTime() * 10);


        Message message = new Message();
        message.marshal(rpcSentMessage);

        Message receivedMarshalled = remoteServer.retrieveNextStop(message);
        RPCMessage rpcReceivedMessage = receivedMarshalled.unMarshal();

        this.previousStop = this.currentStop;
        this.currentStop = rpcReceivedMessage.getCsv_data().split(",")[0];

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


    @Override
    public void run() {
        try {
            while(true){
                printTramDetails();
                updateLocation();
                Thread.sleep(1000);
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
        }
    }
}
