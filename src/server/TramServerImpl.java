package server;

import exceptions.*;
import utilities.Message;
import utilities.RPCMessage;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TramServerImpl implements TramServer {
    private static final String SERVERNAME = "TramServer";
    private String url;
    private static final int PORT = 3419;
    private static TramServerImpl instance = null;
    private HashMap<Integer, List> lines;
    //contains UUID for Tram identifier, first Integer is line, second is position on that line.
    private ConcurrentHashMap<UUID, LineAndStopPair> tramInfo;

    private enum Direction {
        UP, BACK
    }


    private TramServerImpl() {}

    private void init() {
        try {
            //Marking a unique URL
            //String host = InetAddress.getLocalHost().getHostName();
            String host = "localhost";
            url = "rmi://" + host + "/" + SERVERNAME + "/";
            this.lines = new HashMap<>();
            this.tramInfo = new ConcurrentHashMap<>();
//		} catch (UnknownHostException ex) {
//			System.err.println("Couldn't get local host name");
//			ex.printStackTrace();
//			System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TramServerImpl getInstance() {
        if (instance == null) {
            instance = new TramServerImpl();
        }
        return instance;
    }

    private String getURL() {
        return url;
    }


    /*
    * Procedure ID: 0
    * */
    @Override
    public Message retrieveNextStop(Message m) throws RemoteException, MessageLengthMismatch {
        RPCMessage receivedRpcMessage = m.unMarshal();
        String[] query = receivedRpcMessage.getCsv_data().split(",");
        int nextStop = getNextStop(Integer.parseInt(query[0]), Integer.parseInt(query[1]), Integer.parseInt(query[2]));

        String[] returnQuery = {Integer.toString(nextStop)};

        RPCMessage sentRpcMessage = new RPCMessage(RPCMessage.MessageType.REPLY,1 , new Short("0"), generateCSVString(returnQuery), new Short("0"));
        sentRpcMessage.setTransactionId(receivedRpcMessage.getTransactionId());
        sentRpcMessage.setRPCId(receivedRpcMessage.getRPCId());

        Message sentMarshalled = new Message();
        sentMarshalled.marshal(sentRpcMessage);

        return sentMarshalled;
    }

    /*
    * Procedure ID 1
    * */
    @Override
    public Message updateTramLocation(Message m) throws RemoteException, MessageLengthMismatch, MessageTypeException, ProcedureException, TramNotValidException {
        RPCMessage receivedRpcMessage = m.unMarshal();


        if(receivedRpcMessage.getMessageType() != RPCMessage.MessageType.REQUEST){
            throw new MessageTypeException(receivedRpcMessage.getMessageType() + " is incorrect REQUEST expected");
        }
        if(receivedRpcMessage.getProcedureId() != 1){
            throw new ProcedureException(receivedRpcMessage.getProcedureId()+ " is not equal to procedure requested");
        }
        String[] query = receivedRpcMessage.getCsv_data().split(",");

        if(this.tramInfo.get(UUID.fromString(query[1])) == null){
            throw new TramNotValidException(query[1] + " Not Found");
        }

        LineAndStopPair updateTram = this.tramInfo.get(UUID.fromString(query[1]));
        Direction d = getDirection(Integer.parseInt(query[0]), Integer.parseInt(query[2]), updateTram.getStop());
        updateTram.setStop(Integer.parseInt(query[2]));
        updateTram.setLine(Integer.parseInt(query[0]));
        updateTram.setDirection(d);


        RPCMessage sentRpcMessage = new RPCMessage(RPCMessage.MessageType.REPLY, 1, new Short("1"), generateCSVString(new String[0]), new Short("0"));
        sentRpcMessage.setTransactionId(receivedRpcMessage.getTransactionId());
        sentRpcMessage.setRPCId(receivedRpcMessage.getRPCId());

        Message sentMarshalled = new Message();
        sentMarshalled.marshal(sentRpcMessage);

        printTramSystem();
        return sentMarshalled;

    }

    /*
    * Procedure ID: 2
    *
    * */
    @Override
    public Message requestLineList(Message m) throws RemoteException, MessageLengthMismatch, LineNotFoundException, TramNotValidException, ProcedureException, MessageTypeException {
        //unmarshal received message.
        RPCMessage receivedRpcMessage = m.unMarshal();
        //get the query from the orginal message

        if(receivedRpcMessage.getMessageType() != RPCMessage.MessageType.REQUEST){
            throw new MessageTypeException(receivedRpcMessage.getMessageType() + " is incorrect REQUEST expected");
        }
        if(receivedRpcMessage.getProcedureId() != 2){
            throw new ProcedureException(receivedRpcMessage.getProcedureId()+ " is not equal to procedure requested");
        }

        String[] query = receivedRpcMessage.getCsv_data().split(",");

        if(this.lines.get(Integer.parseInt(query[1])) == null){
            throw new LineNotFoundException(query[1] + " Not Found");
        }

        Integer[] stops = (Integer[]) this.lines.get(Integer.parseInt(query[1])).toArray(new Integer[this.lines.get(Integer.parseInt(query[1])).size()]);


        String[] stopString = new String[stops.length];
        for (int i = 0; i < stops.length; i++) {
            stopString[i] = stops[i].toString();
        }

        if(this.tramInfo.get(UUID.fromString(query[0]))!= null){
            throw new TramNotValidException(query[0]+ " Tram With id already exists");
        }

        this.tramInfo.put(UUID.fromString(query[0]), new LineAndStopPair(Integer.parseInt(query[1]), Integer.parseInt(stopString[0]), Direction.UP));

        RPCMessage sentRpcMessage = new RPCMessage(RPCMessage.MessageType.REPLY, 1, new Short("2"), generateCSVString(stopString), new Short("0"));

        sentRpcMessage.setTransactionId(receivedRpcMessage.getTransactionId());
        sentRpcMessage.setRPCId(receivedRpcMessage.getRPCId());
        sentRpcMessage.setRequestId(1);



        Message sentMarshalled = new Message();
        sentMarshalled.marshal(sentRpcMessage);

        return sentMarshalled;
    }


    public static void main(String[] args) {
        try {
            //Create Server Singleton
            TramServerImpl server = getInstance();
            server.init();

            List<Integer> line1 = Arrays.asList(1, 2, 3, 4, 5);
            List<Integer> line96 = Arrays.asList(23, 24, 2, 34, 22);
            List<Integer> line101 = Arrays.asList(123, 11, 22, 34, 5, 4, 7);
            List<Integer> line109 = Arrays.asList(88, 87, 85, 80, 9, 7, 2, 1);
            List<Integer> line112 = Arrays.asList(110, 123, 11, 22, 34, 33, 29, 4);
            server.lines.put(1, line1);
            server.lines.put(96, line96);
            server.lines.put(101, line101);
            server.lines.put(109, line109);
            server.lines.put(112, line112);


            TramServer stub = (TramServer) UnicastRemoteObject.exportObject((TramServer) server, 0);
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.bind(server.getURL(), stub);

            System.out.println("Server bound to: " + server.getURL());
        } catch (RemoteException ex) {
            System.err.println("Couldn't contact rmiregistry.");
            ex.printStackTrace();
            System.exit(1);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateCSVString(String array[]) {
        String r = "";
        for (String a : array) {
            r = r + a + ",";
        }
        return r;
    }

    private int getNextStop(int line, int currentStop, int previousStop) {
       List stops = this.lines.get(line);

        for(int i = 0; i <= stops.size()-1; i++){
            if(currentStop == stops.get(0) && (previousStop == stops.get(1)|| previousStop ==0)){
                return Integer.parseInt(stops.get(1).toString());
            } else if(currentStop == stops.get(i) && currentStop != stops.get(stops.size()-1)){

                if(stops.indexOf(currentStop) > stops.indexOf(previousStop)){
                    return Integer.parseInt(stops.get(i+1).toString());

                }else if(stops.indexOf(currentStop) < stops.indexOf(previousStop)){
                    return Integer.parseInt(stops.get(i- 1).toString());

                }
            }else if(currentStop == stops.get(i) && currentStop == stops.get(stops.size()-1)){
                    return Integer.parseInt(stops.get(stops.size()-2).toString());
            }
        }

        return -1;
    }

    private Direction getDirection(int line,int currentStop, int previousStop){
        List stops = this.lines.get(line);

        for(int i = 0; i <= stops.size()-1; i++){
            if(currentStop == stops.get(0) && (previousStop == stops.get(1)|| previousStop ==0)){
                return Direction.UP;
            } else if(currentStop == stops.get(i) && currentStop != stops.get(stops.size()-1)){

                if(stops.indexOf(currentStop) > stops.indexOf(previousStop)){
                    return Direction.UP;

                }else if(stops.indexOf(currentStop) < stops.indexOf(previousStop)){
                    return Direction.BACK;

                }
            }else if(currentStop == stops.get(i) && currentStop == stops.get(stops.size()-1)){
                return Direction.BACK;
            }
        }
        return Direction.UP;
    }


    public void printTramSystem() {
        System.out.println("System Time Is Currently: " + new Date().toString());
        for (Map.Entry<UUID, LineAndStopPair> entry : tramInfo.entrySet()) {
            LineAndStopPair pair = entry.getValue();

            System.out.println("Tram: " + entry.getKey());
            System.out.println("Is on line: " + pair.getLine() + " and is currently stopped at " + pair.getStop());
            System.out.println("Moving in the direction: " + pair.getDirection());
        }
        System.out.print("\n");
    }


    private class LineAndStopPair {
        private int line;
        private int stop;
        private Direction direction;

        protected LineAndStopPair(int l, int s, Direction d) {
            this.line = l;
            this.stop = s;
            this.direction = d;
        }

        public int getLine() {
            return this.line;
        }

        public int getStop() {
            return this.stop;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public void setStop(int stop) {
            this.stop = stop;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }
    }
}
