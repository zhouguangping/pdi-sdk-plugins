package org.pentaho.di.sdk.samples.modbus.demo;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;


/**
 * 该类为插件真正处理数据的类，每次数据来都会调用processRow方法
 *
 */

public class TemplateStep extends BaseStep implements StepInterface {

    private TemplateStepData data;
    private TemplateStepMeta meta;
    
    public TemplateStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        Object[] outputRow = null;
        //校验合格为true，不合格为false
        Boolean flag = true;
        meta = (TemplateStepMeta) smi;
        data = (TemplateStepData) sdi;

        Object[] r = getRow(); //获取一行数据
        if (r == null) //如果为空，则代表获取到最后一行数据，调用setOutputDone()方法
        {
            setOutputDone();
            return false;
        }
        if (first) {
            first = false;
            //如果是第一行则保存数据行元信息到data类中,后续使用
            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
            //为数据行新增加一个字段(就是标志字段)
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            logBasic("template step initialized successfully");

        }
        
        String[] checkValue = meta.getCheckValue();
        String[] fieldName = meta.getFieldName();
        //这里为业务逻辑代码，如校验身份证等
        for (int i = 0; i < checkValue.length; i++) {
            //验证身份证
            if("身份证格式校验".equals(checkValue[i])){
                //获取身份证校验字段的索引
                int index = data.outputRowMeta.indexOfValue(fieldName[i]);
                //获取校验字段的值
                String sfzh = data.outputRowMeta.getString(r, index);
                Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                boolean rs = pattern.matcher(sfzh).matches();
                if(!rs){
                    flag = false;
                }
            }else if("民族校验".equals(checkValue[i])) {//验证民族代码，暂停
                
            }
            
        }
        //此方法为刚才新增字段赋值
        outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size() - 1, flag);
        //将结果写入下一个步骤
        putRow(data.outputRowMeta, outputRow);

        if (checkFeedback(getLinesRead())) {
            logBasic("Linenr " + getLinesRead()); // Some basic logging
        }

        return true;
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (TemplateStepMeta) smi;
        data = (TemplateStepData) sdi;

        return super.init(smi, sdi);
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (TemplateStepMeta) smi;
        data = (TemplateStepData) sdi;

        super.dispose(smi, sdi);
    }

    public void run() {
        logBasic("Starting to run...");
        try {
            while (processRow(meta, data) && !isStopped())
                ;
        } catch (Exception e) {
            logError("Unexpected error : " + e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
            stopAll();
        } finally {
            dispose(meta, data);
            logBasic("Finished, processing " + getLinesRead() + " rows");
            markStop();
        }
    }

}
