package org.pentaho.di.sdk.samples.modbus.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

/**
 * 该类相当于一个数据承载传递类，有点像实体类的感觉
 *
 */
//使用注解的方式代替plugin.xml配置文件
@Step(id = "ModbusInput.Shell.ID", // 插件ID必须唯一
		image = "org/pentaho/di/sdk/samples/modbus/input/resources/modbusinput.png", // 插件在kettle中显示图标,默认从src目录下找
		name = "ModbusInputStep.Name", // 插件在kettle中显示的名称
		description = "ModbusInputStep.Description", // 插件子啊kettle中的描述
		categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input", // 插件在kettle中的分类
		i18nPackageName = "com.aerotrust.kettle.sdk.modbus.input")
public class ModbusInputStepMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = ModbusInputStepMeta.class; // for i18n purposes
	public static final String IP_NAME = "ip", PORT_NAME = "port", SLAVEID_NAME = "slaveId", ADDRESS_NAME = "address",
			FLAG_NAME = "flag", KEY_NAME = "key", UNIT_NAME = "unit", UPPER_NAME = "upper", FLOOR_NAME = "floor",
			MODBUS_VALUE_NAME = "modbus_value", VALUE_NAME = "value";
	public static final String[] ITEMS = { IP_NAME, PORT_NAME, };
	public static final String[] COLUMNS = { SLAVEID_NAME, ADDRESS_NAME, FLAG_NAME, KEY_NAME, UNIT_NAME, UPPER_NAME,
			FLOOR_NAME};
	public static final String[] FIELDS = {MODBUS_VALUE_NAME, VALUE_NAME};
	
	private Map<String, String> itemDatas = new HashMap<String, String>(); // 输入框
	private Map<String, String[]> columnDatas = new HashMap<String, String[]>(); // 列表
	private String[] fields; // 输出列表

	public ModbusInputStepMeta() {
		super(); // allocate BaseStepMeta
	}

	public Map<String, String> getItemDatas() {
		return itemDatas;
	}

	public void setItemDatas(Map<String, String> itemDatas) {
		this.itemDatas = itemDatas;
	}

	public Map<String, String[]> getColumnDatas() {
		return columnDatas;
	}

	public void setColumnDatas(Map<String, String[]> columnDatas) {
		this.columnDatas = columnDatas;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode, databases);
	}

	public void allocate(int count) {
		for (String column : COLUMNS) {
			String[] data = columnDatas.get(column);
			columnDatas.put(column,new String[count]);
		}

	}
	@Override
	public Object clone() {
		ModbusInputStepMeta retval = (ModbusInputStepMeta) super.clone();

		String[] strings = columnDatas.get(COLUMNS[0]);
		int count = strings.length;

//		retval.allocate(count);
		String jsonString = JSONObject.toJSONString(columnDatas);
		retval.setColumnDatas(JSONObject.parseObject(jsonString,new TypeReference<Map<String, String[]>>(){}));
		return retval;
	}

	// 如果想在下一个步骤获取这个步骤新增的字段名称，必须在这里新增字段
	@Override
	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space) {
		// 给输入数据中新增字段
		fields = Stream.concat(Stream.of(ITEMS), Stream.of(COLUMNS)).toArray(String[]::new); //测试输出
		fields = Stream.concat(Stream.of(fields), Stream.of(FIELDS)).toArray(String[]::new);
		for (String string : fields) {
			ValueMetaDate vmd = new ValueMetaDate(string, ValueMetaInterface.TYPE_STRING);
			vmd.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
			vmd.setOrigin(origin);
			r.addValueMeta(vmd);
		}

	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
		try {
			
			for(String item:ITEMS) {
				Node node = XMLHandler.getSubNode(stepnode, item);
				itemDatas.put(item, XMLHandler.getNodeValue(node));
			}
			
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int count = XMLHandler.countNodes(fields, "field");

			allocate(count);

			for (int i = 0; i < count; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				for(String column: COLUMNS) {
					columnDatas.get(column)[i]=XMLHandler.getTagValue(fnode, column);
				}
			}
		} catch (Exception e) {
			throw new KettleXMLException(
					BaseMessages.getString(PKG, "TemplateStepMeta.Exception.UnableToReadStepInfoFromXML"), e);
		}
	}

	@Override
	public void setDefault() {
		int count = 0;

		allocate(count);

		for (int i = 0; i < count; i++) {
			for(String column: COLUMNS) {
				columnDatas.get(column)[i]="";
			}
		}
	}

	@Override
	public String getXML() {
		StringBuilder retval = new StringBuilder();
		for(String item:ITEMS) {
			retval.append(XMLHandler.addTagValue(item, itemDatas.get(item)));
		}

		retval.append("    <fields>" + Const.CR);

		for (int i = 0; i < columnDatas.get(COLUMNS[0]).length; i++) {
			retval.append("      <field>" + Const.CR);
			for(String column: COLUMNS) {
				retval.append("        " + XMLHandler.addTagValue(column, columnDatas.get(column)[i]));
			}
			retval.append("        </field>" + Const.CR);
		}
		retval.append("      </fields>" + Const.CR);

		return retval.toString();
	}

	// 从资源库读取用户设置的步骤信息
	@Override
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
			throws KettleException {
		try {
			for(String item:ITEMS) {
				itemDatas.put(item,rep.getStepAttributeString(id_step, item));
			}
			int nrfields = rep.countNrStepAttributes(id_step, COLUMNS[0]);
			allocate(nrfields);
			for (int i = 0; i < nrfields; i++) {
				for(String column: COLUMNS) {
					columnDatas.get(column)[i]=rep.getStepAttributeString(id_step, i, column);
				}
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"TemplateStepMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e);
		}
	}

	// 保存用户设置的步骤信息到资源库
	@Override
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			for(String item:ITEMS) {
				itemDatas.put(item,rep.getStepAttributeString(id_step, item));
				rep.saveStepAttribute(id_transformation, id_step, item, itemDatas.get(item));
			}
			for (int i = 0; i < columnDatas.get(COLUMNS[0]).length; i++) {
				for(String column: COLUMNS) {
					rep.saveStepAttribute(id_transformation, id_step, i, column, columnDatas.get(column)[i]);
				}
			}
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages.getString(PKG, "TemplateStepMeta.Exception.UnableToSaveStepInfoToRepository")
							+ id_step,
					e);
		}

	}

	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
			IMetaStore metaStore) {

	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new ModbusInputStepDialog(shell, meta, transMeta, name);
	}

	@Override
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new ModbusInputStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	@Override
	public StepDataInterface getStepData() {
		return new ModbusInputStepData();
	}

	@Override
	public boolean supportsErrorHandling() {
		return true;
	}
}
