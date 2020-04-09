package org.pentaho.di.sdk.samples.modbus.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * 该类的主要作用就是，设置插件弹窗样式。及将用户设置写入到StepMetaInterface的实现类 弹窗开发，参考swt开发
 */

public class ModbusInputStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = ModbusInputStepMeta.class;
	private Label stepnameLabel;
	private Text stepnameText;
	private FormData stepnameLabelFormData, stepnameTextFormData;

//	private Label ipLabel;
//	private Text ipText;
//	private FormData ipLabelFormData, ipTextFormData;
//
//
//	private Label portLabel;// 标签组件
//	private Text portText;// 输入框组件
//	private FormData portLabelFormData, portTextFormData;// 用于给组件定位
	
	private Label[] itemLabels;
	private Text[] itemTexts;
	private FormData[] itemLabelFormDatas, itemTextFormDatas;
	
	private Label wLabel;
	private TableView wTableView;
	private FormData wLableFormData, wTableViewFormData;
	
	private ModbusInputStepMeta input;

	private Map<String, Integer> inputFields;

	private ColumnInfo[] colinf;

	// shell相当于一个活动的窗口，in为StepMetaInterface实现类，transMeta转换的元信息
	public ModbusInputStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (ModbusInputStepMeta) in;
		inputFields = new HashMap<String, Integer>();
	}

	// kettle加载插件后，双击插件会调用open方法，进行弹窗
	public String open() {

		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ModbusInputStep.Shell.Label"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		// Stepname line
		stepnameLabel = new Label(shell, SWT.RIGHT);
		stepnameLabel.setText(BaseMessages.getString(PKG, "ModbusInputStep.stepname.Label"));
		props.setLook(stepnameLabel);
		stepnameLabelFormData = new FormData();
		stepnameLabelFormData.left = new FormAttachment(0, 0);
		stepnameLabelFormData.right = new FormAttachment(middle, -margin);
		stepnameLabelFormData.top = new FormAttachment(0, margin);
		stepnameLabel.setLayoutData(stepnameLabelFormData);
		stepnameText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		stepnameText.setText(stepname);
		props.setLook(stepnameText);
		stepnameText.addModifyListener(lsMod);
		stepnameTextFormData = new FormData();
		stepnameTextFormData.left = new FormAttachment(middle, 0);
		stepnameTextFormData.top = new FormAttachment(0, margin);
		stepnameTextFormData.right = new FormAttachment(100, 0);
		stepnameText.setLayoutData(stepnameTextFormData);
		String[] items = ModbusInputStepMeta.ITEMS;
		int itemLength = items.length;
		itemLabels = new Label[itemLength];
		itemTexts = new Text[itemLength];
		itemLabelFormDatas = new FormData[itemLength];
		itemTextFormDatas = new FormData[itemLength];
		for(int i =0; i<itemLength; i++) {
			// 标签
			Label itemLabel = new Label(shell, SWT.RIGHT);
			itemLabels[i] = itemLabel;
			itemLabel.setText(items[i]);
			props.setLook(itemLabel);
			
			FormData itemLabelFormData = new FormData();
			itemLabelFormDatas[i]= itemLabelFormData;
			itemLabelFormData.left = new FormAttachment(0, 0);
			itemLabelFormData.right = new FormAttachment(middle, -margin);
			itemLabelFormData.top = new FormAttachment(i==0?stepnameText:itemTexts[i-1], margin);
			itemLabel.setLayoutData(itemLabelFormData);
			
			// 输入框
			Text itemText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
			itemTexts[i] = itemText;
			itemText.setText(itemLabel.getText());
			props.setLook(itemText);
			itemText.addModifyListener(lsMod);
			
			FormData itemTextFormData = new FormData();
			itemTextFormDatas[i] = itemTextFormData;
			itemTextFormData.left = new FormAttachment(middle, 0);
			itemTextFormData.top = new FormAttachment(i==0?stepnameText:itemTexts[i-1], margin);
			itemTextFormData.right = new FormAttachment(100, 0);
			itemText.setLayoutData(itemTextFormData);
		}

		wLabel = new Label(shell, SWT.NONE);
		wLabel.setText(BaseMessages.getString(PKG, "ModbusInputStep.Fields.Label"));
		props.setLook(wLabel);
		wLableFormData = new FormData();
		wLableFormData.left = new FormAttachment(0, 0);
		wLableFormData.top = new FormAttachment(itemTexts[itemLength-1], margin);
		wLabel.setLayoutData(wLableFormData);
		String[] columns = ModbusInputStepMeta.COLUMNS;
		final int FieldsCols = columns.length;
		final int FieldsRows = input.getColumnDatas().get(ModbusInputStepMeta.COLUMNS[0]).length;

		colinf = new ColumnInfo[FieldsCols];
		for(int i =0;i<columns.length;i++) {
			colinf[i] = new ColumnInfo(columns[i],ColumnInfo.COLUMN_TYPE_TEXT, new String[] { "" }, false);
		}
		
		wTableView = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows,
				lsMod, props);

		wTableViewFormData = new FormData();
		wTableViewFormData.left = new FormAttachment(0, 0);
		wTableViewFormData.top = new FormAttachment(wLabel, margin);
		wTableViewFormData.right = new FormAttachment(100, 0);
		wTableViewFormData.bottom = new FormAttachment(100, -50);
		wTableView.setLayoutData(wTableViewFormData);

		//
		// Search the fields in the background

		final Runnable runnable = new Runnable() {
			public void run() {
				StepMeta stepMeta = transMeta.findStep(stepname);
				if (stepMeta != null) {
					try {
						RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);

						// Remember these fields...
						for (int i = 0; i < row.size(); i++) {
							inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
						}

						setComboBoxes();// 设置表格中的下拉框
					} catch (KettleException e) {
						logError("It was not possible to get the list of input fields from previous steps", e);
					}
				}
			}
		};
//		new Thread(runnable).start();

		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, wTableView);

		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsGet = new Listener() {
			public void handleEvent(Event e) {
				get();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener(SWT.Selection, lsGet);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		itemTexts[0].addSelectionListener(lsDef);
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();
		shell.setSize(500, 350);// 设置窗口的大小
		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return stepname;
	}

	// 设置表格单元格后的下拉框
	protected void setComboBoxes() {
		// Something was changed in the row.
		//
		final Map<String, Integer> fields = new HashMap<String, Integer>();

		// Add the currentMeta fields...
		fields.putAll(inputFields);

		Set<String> keySet = fields.keySet();
		List<String> entries = new ArrayList<String>(keySet);

		String[] fieldNames = entries.toArray(new String[entries.size()]);
		Const.sortStrings(fieldNames);
		colinf[0].setComboValues(fieldNames);
		colinf[1].setComboValues(fieldNames);
		colinf[2].setComboValues(fieldNames);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {
		stepnameText.setText(stepname);
		for(int i =0;i<ModbusInputStepMeta.ITEMS.length;i++) {
			String item  = ModbusInputStepMeta.ITEMS[i];
			String data = input.getItemDatas().get(item);
			Text text = itemTexts[i];
			text.setText(data==null?text.getText():data);
			text.selectAll();
			text.setFocus();
		}

		Map<String, String[]> columnDatas = input.getColumnDatas();
		String[] columns = ModbusInputStepMeta.COLUMNS;
		int columnsLenth = columns.length;
		for (int i = 0; i < columnDatas.get(columns[0]).length; i++) {
			TableItem tableItem = wTableView.table.getItem(i);
			for(int j =0;j<columnsLenth;j++) {
				String column = columns[j];
				String data = columnDatas.get(column)[i];
				if (data != null) {
					tableItem.setText(j+1, data);
				}
			}
		}

		wTableView.setRowNums();
		wTableView.optWidth(true);


	}

	public void setData() {
		Map<String, String[]> columnDatas = input.getColumnDatas();
		for (int i = 0; i < columnDatas.get(ModbusInputStepMeta.COLUMNS[0]).length; i++) {
			TableItem tableItem = wTableView.table.getItem(i);
			for(String column:ModbusInputStepMeta.COLUMNS) {
				String data = columnDatas.get(column)[i];
				if (data != null) {
					tableItem.setText(i+1, data);
				}
			}
		}

		wTableView.setRowNums();
		wTableView.optWidth(true);

		for(int i =0;i<ModbusInputStepMeta.ITEMS.length;i++) {
			String item  = ModbusInputStepMeta.ITEMS[i];
			String data = input.getItemDatas().get(item);
			Text text = itemTexts[i];
			text.selectAll();
			text.setFocus();
		}
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok() {
		if (stepnameText.getText() == null || "".equals(stepnameText.getText().trim())) {
			return;
		}

		stepname = stepnameText.getText(); // return value
		int count = wTableView.nrNonEmpty();
		input.allocate(count);
		for(int i=0;i<itemTexts.length;i++) {
			Text text = itemTexts[i];
			String data = text.getText();
			input.getItemDatas().put(ModbusInputStepMeta.ITEMS[i], data);
		}
		// CHECKSTYLE:Indentation:OFF
		int columnLength = ModbusInputStepMeta.COLUMNS.length;
		for (int i = 0; i < count; i++) {
			TableItem item = wTableView.getNonEmpty(i);
			for(int j =0; j < columnLength;j++) {
				String column = ModbusInputStepMeta.COLUMNS[j];
				input.getColumnDatas().get(column)[i] = item.getText(j+1);
			}
		}
		dispose();
	}

	private void get() {
		try {
			RowMetaInterface r = transMeta.getPrevStepFields(stepMeta);
			if (r != null) {
				BaseStepDialog.getFieldsFromPrevious(r, wTableView, 1, new int[] { 1 }, null, -1, -1, null);
			}
		} catch (KettleException ke) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"),
					BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
		}
	}
}
