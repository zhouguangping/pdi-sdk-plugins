package org.pentaho.di.sdk.samples.modbus.demo;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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

/**
 * 该类相当于一个数据承载传递类，有点像实体类的感觉
 *
 */
//使用注解的方式代替plugin.xml配置文件
@Step(id = "TemplatePlugin", // 插件ID必须唯一
		image = "org/pentaho/di/sdk/samples/modbus/demo/resources/icon.png", // 插件在kettle中显示图标,默认从src目录下找
		name = "自定义数据校验", // 插件在kettle中显示的名称
		description = "根据扩展规则验证数据", // 插件子啊kettle中的描述
		categoryDescription = "Demon"// 插件在kettle中的分类
)
public class TemplateStepMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = TemplateStepMeta.class; // for i18n purposes
	private String[] fieldName;
	private String[] checkType;
	private String flagField = "mt_flag";// 用户设置的标志字段名称就保存在这里，添加set get方法

	public String getFlagField() {
		return flagField;
	}

	public void setFlagField(String flagField) {
		this.flagField = flagField;
	}

	public TemplateStepMeta() {
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the fieldName.
	 */
	public String[] getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName The fieldName to set.
	 */
	public void setFieldName(String[] fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return Returns the checkType.
	 */
	public String[] getCheckValue() {
		return checkType;
	}

	/**
	 * @param checkType The checkType to set.
	 */
	public void setCheckValue(String[] checkType) {
		this.checkType = checkType;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode, databases);
	}

	public void allocate(int count) {
		fieldName = new String[count];
		checkType = new String[count];
	}

	public Object clone() {
		TemplateStepMeta retval = (TemplateStepMeta) super.clone();

		int count = fieldName.length;

		retval.allocate(count);
		System.arraycopy(fieldName, 0, retval.fieldName, 0, count);
		System.arraycopy(checkType, 0, retval.checkType, 0, count);

		return retval;
	}

	// 如果想在下一个步骤获取这个步骤新增的字段名称，必须在这里新增字段
	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space) {

		// 给输入数据中新增字段
		ValueMetaInterface v = new ValueMeta();
		v.setName(flagField);// 设置字段的名称
		v.setType(ValueMeta.TYPE_BOOLEAN);
		v.setTrimType(ValueMeta.TRIM_TYPE_BOTH);
		v.setOrigin(origin);

		r.addValueMeta(v);

	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
		try {
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int count = XMLHandler.countNodes(fields, "field");

			allocate(count);

			for (int i = 0; i < count; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

				fieldName[i] = XMLHandler.getTagValue(fnode, "name");
				checkType[i] = XMLHandler.getTagValue(fnode, "replaceby");
			}
		} catch (Exception e) {
			throw new KettleXMLException(
					BaseMessages.getString(PKG, "TemplateStepMeta.Exception.UnableToReadStepInfoFromXML"), e);
		}
	}

	public void setDefault() {
		int count = 0;

		allocate(count);

		for (int i = 0; i < count; i++) {
			fieldName[i] = "field" + i;
			checkType[i] = "";
		}
	}

	public String getXML() {
		StringBuilder retval = new StringBuilder();

		retval.append("    <fields>" + Const.CR);

		for (int i = 0; i < fieldName.length; i++) {
			retval.append("      <field>" + Const.CR);
			retval.append("        " + XMLHandler.addTagValue("name", fieldName[i]));
			retval.append("        " + XMLHandler.addTagValue("replaceby", checkType[i]));
			retval.append("        </field>" + Const.CR);
		}
		retval.append("      </fields>" + Const.CR);

		return retval.toString();
	}

	// 从资源库读取用户设置的步骤信息
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
			throws KettleException {
		try {
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");

			allocate(nrfields);
			flagField = rep.getStepAttributeString(id_step, "flag_field");// 获取用户设置的标志字段名称
			for (int i = 0; i < nrfields; i++) {
				fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
				checkType[i] = rep.getStepAttributeString(id_step, i, "check_type");
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"TemplateStepMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e);
		}
	}

	// 保存用户设置的步骤信息到资源库
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "flag_field", flagField); // 保存标志字段
			for (int i = 0; i < fieldName.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "check_type", checkType[i]);
			}
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages.getString(PKG, "TemplateStepMeta.Exception.UnableToSaveStepInfoToRepository")
							+ id_step,
					e);
		}

	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
			IMetaStore metaStore) {

	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new TemplateStepDialog(shell, meta, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new TemplateStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	public StepDataInterface getStepData() {
		return new TemplateStepData();
	}

	public boolean supportsErrorHandling() {
		return true;
	}

}
