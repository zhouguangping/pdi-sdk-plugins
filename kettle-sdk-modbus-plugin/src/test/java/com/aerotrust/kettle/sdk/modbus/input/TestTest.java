package com.aerotrust.kettle.sdk.modbus.input;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class TestTest {
	@Test
	public void test() {
		String[] strs = new String[1];
		List<String> list = new ArrayList<String>();
		test(strs, list);
		System.out.println(JSONObject.toJSONString(strs));
		System.out.println(JSONObject.toJSONString(list));
	}
	private void test(String[] strs,List<String> list) {
		strs[0] = "1";
		list.add("a");
		System.out.println(JSONObject.toJSONString(strs));
		System.out.println(JSONObject.toJSONString(list));
	}
}
