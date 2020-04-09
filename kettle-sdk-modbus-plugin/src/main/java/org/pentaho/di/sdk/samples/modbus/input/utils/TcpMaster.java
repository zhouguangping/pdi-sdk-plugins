package org.pentaho.di.sdk.samples.modbus.input.utils;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;

public class TcpMaster {
    
    private static ModbusFactory modbusFactory;
 
    static {
        if (modbusFactory == null) {
            modbusFactory = new ModbusFactory();
        }
    }
 
    /**
     * 获取master
     *
     * @return master
     */
    public static ModbusMaster getMaster(String ip, int port) {
        IpParameters params = new IpParameters();
        params.setHost(ip);
        params.setPort(port);
        params.setEncapsulated(true);//这个属性确定了协议帧是否是通过tcp封装的RTU结构，采用modbus tcp/ip时，要设为false, 采用modbus rtu over tcp/ip时，要设为true
        ModbusMaster master = modbusFactory.createTcpMaster(params, false);// TCP 协议
        try {
            //设置超时时间
            master.setTimeout(1000);
            //设置重连次数
            master.setRetries(3);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
        return master;
    }


    /**
     * 获取master
     *
     * @return master
     */
    public static ModbusMaster getMaster(String ipAdd) {
        IpParameters params = new IpParameters();
        params.setHost(ipAdd);
        params.setPort(502);
        params.setEncapsulated(true);
        ModbusMaster master = modbusFactory.createTcpMaster(params, false);// TCP 协议
        try {
            //设置超时时间
            master.setTimeout(1000);
            //设置重连次数
            master.setRetries(3);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
        return master;
    }

}
