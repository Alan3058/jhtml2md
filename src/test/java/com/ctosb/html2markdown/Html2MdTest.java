
package com.ctosb.html2markdown;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class Html2MdTest {

	@Test
	public void convert() throws IOException {
		File file = new File(getClass().getClassLoader().getResource("test.html").getFile());
		String content = Html2Md.convertFile(file, "UTF-8");
		System.out.println(content);
	}
}
