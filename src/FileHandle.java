

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

public class FileHandle {
	
	public static void main(String[] args) {
		// 获取系统文件
		FileSystemView fileSystemView = FileSystemView.getFileSystemView();
		// 获取桌面目录
		File file = fileSystemView.getHomeDirectory();
		String deskPaht = file.toString();
		// new 一个左面对象
		File file2 = new File(deskPaht);
		// 获取桌面目录中的所有文件包括文件目录
		File[] listFiles = file2.listFiles();
		// 需要解析的文件名集合
		List<String> pathList = new ArrayList<>();
		for(File file3 : listFiles) {
			// isFile ： 如果是标准文件（不是目录，则为标准文件，包括压缩文件）
			if(file3.isFile()) {
				String name = file3.getName();
				String[] splitName = name.split("\\.");
				// 判断是否是log文件，是则添加
				if(splitName[splitName.length - 1].equals("log") || splitName[splitName.length - 2].equals("log")) {
					pathList.add(name);
				}
			}
		}
		
		// 遍历处理log文件，将其解析并转化输出
		for(String nameString : pathList) {
			String outName = "out" + nameString;
			readFileByLine(deskPaht, nameString, outName);
		}
	}
	
	/**
	 * 
	 * @param deskPaht 路径
	 * @param inputName 需要解析的文件名
	 * @param outName 解析完成后输出的文件名
	 */
	public static void readFileByLine(String deskPaht, String inputName, String outName) {
		String inFileName = deskPaht + "\\" + inputName;
		String outFileName = deskPaht + "\\" + outName;
		File out = new File(outFileName);
		FileOutputStream outputStream = null;
		OutputStreamWriter outputStreamWriter = null;
		File file = new File(inFileName);
		BufferedReader reader = null;
		try {
			outputStream = new FileOutputStream(out);
			outputStreamWriter = new OutputStreamWriter(outputStream);
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String convert = convert(tempString);
				outputStreamWriter.write(convert + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(outputStreamWriter != null) {
				try {
					outputStreamWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String convert(String value) {
		String newCmd = "";
		String flagString = value.substring(31, 35);
		if(flagString.equals("SENT")) {
			String sub = value.substring(38, value.length() - 1);
			String[] subSplit = sub.split(", ");
			
			StringBuffer stringBuffer = new StringBuffer();
			for(int i = 6; i < 18; i++) {
				 stringBuffer.append(subSplit[i]).append(", ");
			}
			String deviceCodeString = stringBuffer.toString().substring(0, stringBuffer.length() -  2);
			// 获取deviceCode
			String deviceCode = stringToHexString(deviceCodeString);
			// 将其组装成原来的模式
			String buffer = value.substring(0, 38);
			for(int i = 0; i < subSplit.length; i++) {
				if(i < 6 || i >= 18) {
					buffer += subSplit[i] + ", ";
				}
				if(i == 6) {
					buffer += "{ " + deviceCode + " }, ";
				}
			}
			newCmd = buffer.substring(0, buffer.length() - 2) + "]";
		}else if(flagString.equals("RECE")){
			if(value.length() < 105) {
				return "";
			}
			String sub = value.substring(42, value.length() - 1);
			String[] subSplit = sub.split(", ");
			if(subSplit.length < 18) {
				return "";
			}
			StringBuffer stringBuffer = new StringBuffer();
			if(subSplit[0].equals("-2")) {// 设备回复
				for(int i = 6; i < 18; i++) {
					 stringBuffer.append(subSplit[i]).append(", ");
				}
				String deviceCodeString = stringBuffer.toString().substring(0, stringBuffer.length() -  2);
				// 获取deviceCode
				String deviceCode = stringToHexString(deviceCodeString);
				// 将其组装成原来的模式
				String buffer = value.substring(0, 38);
				for(int i = 0; i < subSplit.length; i++) {
					if(i < 6 || i >= 18) {
						buffer += subSplit[i] + ", ";
					}
					if(i == 6) {
						buffer += "{ " + deviceCode + " }, ";
					}
				}
				newCmd = buffer.substring(0, buffer.length() - 2) + "]";
			}else {// 网关心跳注册包
				String buffer = value.substring(0, 42);
				newCmd = buffer + asciiToString(sub) + "]";
			}
		}
		return newCmd.toUpperCase();
	}
	
	public static String stringToHexString(String value) {
		StringBuffer sbu = new StringBuffer();
		String[] strings = value.split(", ");
		for(String string : strings) {
			int s = Integer.valueOf(string);
			if(s == 0) {
				sbu.append("00");
			}else {
				sbu.append(Integer.toHexString(s).toString());
			}
		}
		return sbu.toString();
	}
	
	public static String asciiToString(String value)  
	{  
	    StringBuffer sbu = new StringBuffer();  
	    String[] chars = value.split(", ");  
	    for (int i = 0; i < chars.length; i++) {  
	        sbu.append((char) Integer.parseInt(chars[i]));  
	    }  
	    return sbu.toString();  
	}
}
