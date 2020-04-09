package org.pentaho.di.sdk.samples.modbus.input;

import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.sdk.samples.modbus.input.utils.ModbusHandle;
import org.pentaho.di.sdk.samples.modbus.input.utils.ModbusMaTransform;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * 该类为插件真正处理数据的类，每次数据来都会调用processRow方法
 *
 */

public class ModbusInputStep extends BaseStep implements StepInterface {

	private ModbusInputStepData data;
	private ModbusInputStepMeta meta;

	public ModbusInputStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (ModbusInputStepMeta) smi;
		data = (ModbusInputStepData) sdi;
		if (!super.init(meta, data)) {
			return false;
		}
		return super.init(smi, sdi);
	}

	@Override
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		if (first) {
			first = false;
			meta = (ModbusInputStepMeta) smi;
			data = (ModbusInputStepData) sdi;
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}else {
			return false;
		}
		String[] items = ModbusInputStepMeta.ITEMS;
		String[] columns = ModbusInputStepMeta.COLUMNS;
		Map<String, String> itemDatas = meta.getItemDatas();
		Map<String, String[]> columnDatas = meta.getColumnDatas();
		String ip = itemDatas.get(ModbusInputStepMeta.IP_NAME);
		String port = itemDatas.get(ModbusInputStepMeta.PORT_NAME);
		String[] slaveId = columnDatas.get(ModbusInputStepMeta.SLAVEID_NAME);
		String[] address = columnDatas.get(ModbusInputStepMeta.ADDRESS_NAME);
		String[] upper = columnDatas.get(ModbusInputStepMeta.UPPER_NAME);
		String[] floor = columnDatas.get(ModbusInputStepMeta.FLOOR_NAME);

		Object[][] result = ModbusHandle.handle(ip, Integer.parseInt(port), slaveId, address, upper, floor);
		Object[] modbusValues = result[0];
		Object[] values = result[1];
		for (int i = 0; i < slaveId.length; i++) {
			Object[] outputRow = null;
			Object[] r = new Object[meta.getFields().length];
			int j =0;
			for(String item : items) {
				outputRow = RowDataUtil.addValueData(r, j, itemDatas.get(item));
				j++;
			}
            for(String column: columns) {
            	outputRow = RowDataUtil.addValueData(r, j, columnDatas.get(column)[i]);
				j++;
            }
			outputRow = RowDataUtil.addValueData(r, j, modbusValues[i].toString());
			j++;
			outputRow = RowDataUtil.addValueData(r, j, values[i].toString());
			// 将结果写入下一个步骤
			putRow(data.outputRowMeta, outputRow);

		}

		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}
		setOutputDone();
		return true;
	}

	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (ModbusInputStepMeta) smi;
		data = (ModbusInputStepData) sdi;

		super.dispose(smi, sdi);
	}
//    public void run() {
//        logBasic("Starting to run...");
//        try {
//            while (processRow(meta, data) && !isStopped())
//                ;
//        } catch (Exception e) {
//            logError("Unexpected error : " + e.toString());
//            logError(Const.getStackTracker(e));
//            setErrors(1);
//            stopAll();
//        } finally {
//            dispose(meta, data);
//            logBasic("Finished, processing " + getLinesRead() + " rows");
//            markStop();
//        }
//    }

}
