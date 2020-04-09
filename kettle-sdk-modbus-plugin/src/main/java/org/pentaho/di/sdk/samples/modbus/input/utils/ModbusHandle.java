package org.pentaho.di.sdk.samples.modbus.input.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;

public class ModbusHandle {
	private static final Logger log = LoggerFactory.getLogger(ModbusHandle.class);

	public static Object[][] handle(String ip,int port,String[] slaveIds,String[] addresses,String[] uppers,String[] floors) {
		Modbus4jReader modbus4jReader = new Modbus4jReader(
				TcpMaster.getMaster(ip,port));
		
		BatchRead<Integer> batch = new BatchRead<Integer>();
		
		for (int i=0; i<addresses.length;i++) {
			String address = addresses[i];
			String slaveId = slaveIds[i];
			int f = Integer.parseInt(address);
			int s = Integer.parseInt(slaveId);
			batch.addLocator(i, BaseLocator.holdingRegister(s, f, DataType.TWO_BYTE_INT_SIGNED));
		}
		Short[] modbusValues = new Short[addresses.length];
		Double[] values = new Double[addresses.length];
		try {
			BatchResults<Integer> batchRead = modbus4jReader.batchRead(batch);
			for (int i=0; i<addresses.length;i++) {
			    try {
					short value = (short) batchRead.getValue(i);
					modbusValues[i] = value;
					values[i] = ModbusMaTransform.transform(
							Double.parseDouble(uppers[i]), Double.parseDouble(floors[i]), value/1000.00);
				} catch (Exception e) {
					log.error("modbus数据读取失败: "+ip + ":" + port + "  ", e);

				}
			}
		} catch (ModbusTransportException | ErrorResponseException e) {
			log.error("modbus数据读取失败: "+ip + ":" + port + "  ", e);
		}finally {
			modbus4jReader.close();
		}
		return new Object[][] {modbusValues,values};
	}


}