package cards.service;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

/**
 * Created by asus on 2016/10/30.
 */
public interface Robot {

    String  send(String cmd, String param) throws RemoteException, ServiceException;
}
