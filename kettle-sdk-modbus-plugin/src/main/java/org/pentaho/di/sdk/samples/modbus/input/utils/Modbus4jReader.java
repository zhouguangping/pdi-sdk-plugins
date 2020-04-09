package org.pentaho.di.sdk.samples.modbus.input.utils;

import java.util.Collection;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.msg.ReadCoilsRequest;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;

public class Modbus4jReader {
    //获取master
    //private static ModbusMaster master = TcpMaster.getMaster();
    private ModbusMaster master = null;
    public Modbus4jReader(ModbusMaster master) {
        this.master = master;
    }
    /**
     * 读（线圈）开关量数据
     *
     * @param slaveId slaveId
     * @param offset  位置
     * @return 读取值
     */
    public boolean[] readCoilStatus(int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
 
        ReadCoilsRequest request = new ReadCoilsRequest(slaveId, offset, numberOfBits);
        ReadCoilsResponse response = (ReadCoilsResponse) master.send(request);
        boolean[] booleans = response.getBooleanData();
        return valueRegroup(numberOfBits, booleans);
    }
 
    /**
     * 开关数据 读取外围设备输入的开关量
     */
    public boolean[] readInputStatus(int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        ReadDiscreteInputsRequest request = new ReadDiscreteInputsRequest(slaveId, offset, numberOfBits);
        ReadDiscreteInputsResponse response = (ReadDiscreteInputsResponse) master.send(request);
        boolean[] booleans = response.getBooleanData();
        return valueRegroup(numberOfBits, booleans);
    }
 
    /**
     * 读取保持寄存器数据
     *
     * @param slaveId slave Id
     * @param offset  位置
     */
    public short[] readHoldingRegister(int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, offset, numberOfBits);
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);
        return response.getShortData();
    }
 
    /**
     * 读取外围设备输入的数据
     *
     * @param slaveId slaveId
     * @param offset  位置
     */
    public short[] readInputRegisters(int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
 
        ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, offset, numberOfBits);
        ReadInputRegistersResponse response = (ReadInputRegistersResponse) master.send(request);
        return response.getShortData();
    }
 
    /**
     * 批量读取 可以批量读取不同寄存器中数据
     */
    public BatchResults<Integer> batchRead(int slaveId, int offset, int quantity) throws ModbusTransportException, ErrorResponseException, ModbusInitException {
 
        BatchRead<Integer> batch = new BatchRead<Integer>();
        for(int i=0 ; i<quantity ;i++) {
            int f = offset+i;
            batch.addLocator(f, BaseLocator.holdingRegister(slaveId, f, DataType.TWO_BYTE_INT_SIGNED));
        }
        batch.setContiguousRequests(true);
        BatchResults<Integer> results = master.send(batch);
        return results;
    }
    /**
     * 批量读取 可以批量读取不同寄存器中数据
     * @throws ErrorResponseException 
     * @throws ModbusTransportException 
     */
    public BatchResults<Integer> batchRead(int slaveId, Collection<Integer>offset) throws ModbusTransportException, ErrorResponseException{
    	
    	BatchRead<Integer> batch = new BatchRead<Integer>();
    	for(Integer f : offset) {
    		batch.addLocator(f, BaseLocator.holdingRegister(slaveId, f, DataType.TWO_BYTE_INT_SIGNED));
    	}
    	batch.setContiguousRequests(true);
    	BatchResults<Integer> results = master.send(batch);
    	return results;
    }
    /**
     * 批量读取 可以批量读取不同寄存器中数据
     * @throws ErrorResponseException 
     * @throws ModbusTransportException 
     */
    public BatchResults<Integer> batchRead(BatchRead<Integer> batch) throws ModbusTransportException, ErrorResponseException{

    	batch.setContiguousRequests(true);
    	BatchResults<Integer> results = master.send(batch);
    	return results;
    }
 
    private boolean[] valueRegroup(int numberOfBits, boolean[] values) {
        boolean[] bs = new boolean[numberOfBits];
        int temp = 1;
        for (boolean b : values) {
            bs[temp - 1] = b;
            temp++;
            if (temp > numberOfBits)
                break;
        }
        return bs;
    }
    public void close() {
    	master.destroy();
    }
}