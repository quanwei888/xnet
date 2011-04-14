package xnet.test;

import java.util.HashMap;
import java.util.Map;

import com.baidu.gson.Gson;
import com.baidu.gson.GsonBuilder;
import com.baidu.gson.JsonElement;
import com.baidu.mcpack.Mcpack;
import com.baidu.mcpack.McpackException;

import junit.framework.TestCase;

public class TestMcpack extends TestCase {
	public void testMcpack() throws McpackException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("a", "b");
		Gson gson = new GsonBuilder().create(); // 用于将Java对象序列化成JsonElement的序列化器
		JsonElement oje = gson.toJsonElement(map);
		gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().serializeSpecialFloatingPointValues().create(); // 用于将Java对象序列化成JsonElement的序列化器

		// 转换成mcpack格式
		Mcpack mcpack = new Mcpack();// 用于将JsonElement序列化成Mcpack的序列化器
		byte[] data = mcpack.toMcpack("UTF-8", oje);
		JsonElement j = mcpack.toJsonElement("UTF-8", data); // content是mcpack内容
		System.out.println(j);
	}
}
