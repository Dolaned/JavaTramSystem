package server;

import exceptions.*;
import utilities.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TramServer extends Remote{
    Message retrieveNextStop(Message m) throws RemoteException, ProcedureException, MessageLengthMismatch;
    Message updateTramLocation(Message m) throws RemoteException, MessageLengthMismatch, MessageTypeException, ProcedureException, TramNotValidException;
    Message requestLineList(Message m) throws RemoteException, ProcedureException, MessageLengthMismatch, LineNotFoundException, TramNotValidException, MessageTypeException;
}
