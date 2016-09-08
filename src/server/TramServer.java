package server;

import exceptions.MessageLengthMismatch;
import exceptions.ProcedureException;
import utilities.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TramServer extends Remote{
    Message retrieveNextStop(Message m) throws RemoteException, ProcedureException, MessageLengthMismatch;
    Message updateTramLocation(Message m) throws RemoteException, MessageLengthMismatch;
    Message requestLineList(Message m) throws RemoteException, ProcedureException, MessageLengthMismatch;
}
