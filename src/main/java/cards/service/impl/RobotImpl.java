package cards.service.impl;

import cards.service.Robot;
import com.yapai.wsdl.robot.TradeService_PortType;
import com.yapai.wsdl.robot.TradeService_ServiceLocator;
import org.springframework.stereotype.Service;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

/**
 * Created by asus on 2016/10/30.
 */
@Service
public class RobotImpl implements Robot {
    private TradeService_PortType port = null;

    @Override
    public String send(String cmd,String param) throws RemoteException, ServiceException {
        if(port == null) {
            port = new TradeService_ServiceLocator().getBasicHttpBinding_TradeService();
        }
        String ret = port.acceptOffer(cmd,param);
        return ret;
    }
}
