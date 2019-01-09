package com.cmcc.wltx.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static void save(String title, List<String> content, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(fileName));

			writer.println(title);
			for (String string : content) {
				writer.println(string);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void save(List<String> content, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(fileName));
			for (String string : content) {
				writer.println(string);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void append(String line, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(fileName, true));
			writer.println(line);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void mkdir(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static boolean exists(String fileName) {
		File file = new File(fileName);
		return (file.exists());
	}

	public static void append(List<String> lineList, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(fileName, true));
			for (String line : lineList) {
				writer.println(line);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clear(String fileName) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(fileName));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 通过外部文件获得各行组成的字符串列表
	public static ArrayList<String> readList(String fileName) {

		ArrayList<String> uidList = new ArrayList<String>();
		File file = new File(fileName);
		if (!file.exists() || file.isDirectory()) {
			return uidList;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String instring;
			while ((instring = reader.readLine()) != null) {
				if (instring != null) {
					uidList.add(new String(instring.getBytes(), "UTF-8"));
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uidList;
	}

	// 通过外部文件获得各行组成的字符串列表
	public static HashSet<String> readSet(String fileName) {

		HashSet<String> uidSet = new HashSet<String>();
		File file = new File(fileName);
		if (!file.exists() || file.isDirectory()) {
			return uidSet;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String instring;
			while ((instring = reader.readLine()) != null) {
				if (instring != null) {
					uidSet.add(new String(instring.getBytes(), "UTF-8"));
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uidSet;
	}

	public static PrintWriter openWriter(String fileName, boolean isAppend) {

		PrintWriter writer = null;
		File file = new File(fileName);
		File parentDir = file.getParentFile();
		if (parentDir.isDirectory() && !parentDir.exists()) {
			parentDir.mkdir();
		}

		try {
			writer = new PrintWriter(new FileWriter(fileName, isAppend));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	public static void println(PrintWriter writer, String line) {
		if (writer == null)
			return;
		writer.println(line);
	}

	public static void print(PrintWriter writer, String line) {
		if (writer == null)
			return;
		writer.print(line);
	}

	public static void closeWriter(PrintWriter writer) {

		if (writer == null)
			return;
		writer.close();
	}

	public static BufferedReader openReader(String fileName) {

		BufferedReader reader = null;
		File file = new File(fileName);
		if (!file.exists() || file.isDirectory()) {
			return reader;
		}
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}

	public static String readLine(BufferedReader reader) {
		String line = null;
		if (reader == null)
			return line;
		String instring;
		try {
			if ((instring = reader.readLine()) != null) {
				line = new String(instring.getBytes(), "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	public static void closeReader(BufferedReader reader) {

		if (reader == null)
			return;
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void mergeTwoFiles(String inFileName1, String inFileName2,
			String outFileName) {
		FileUtil.clear(outFileName);
		FileUtil.appendFileAtoFileB(inFileName1, outFileName);
		FileUtil.appendFileAtoFileB(inFileName2, outFileName);
	}
	
	public static void mergeTwoFilesOmitSameLines(String inFileName1, String inFileName2,
			String outFileName) {

		FileUtil.clear(outFileName);
		FileUtil.appendFileAtoFileB(inFileName1, outFileName);
		
		HashSet<String> file1Set = FileUtil.readSet(inFileName1);
		BufferedReader reader = FileUtil.openReader(inFileName2);
		PrintWriter writer = FileUtil.openWriter(outFileName, true);
		
		String line;
		while ((line = FileUtil.readLine(reader))!=null){
			if (!file1Set.contains(line)) FileUtil.println(writer, line);
		}
		writer.flush();
		FileUtil.closeWriter(writer);
		FileUtil.closeReader(reader);
		
	}
	

	public static void appendFileAtoFileB(String fileNameA, String fileNameB) {
		BufferedReader reader = FileUtil.openReader(fileNameA);
		PrintWriter writer = FileUtil.openWriter(fileNameB, true);
		String line = null;
		while ((line = FileUtil.readLine(reader)) != null) {
			FileUtil.println(writer, line);
		}
		FileUtil.closeReader(reader);
		FileUtil.closeWriter(writer);
	}

	public static void sampleFileAtoFileB(String fileNameA, String fileNameB,
			double sampleRate) {
		BufferedReader reader = FileUtil.openReader(fileNameA);
		PrintWriter writer = FileUtil.openWriter(fileNameB, false);
		String line = null;
		double sampleCNT = 0;
		while ((line = FileUtil.readLine(reader)) != null) {
			sampleCNT += 1.0;
			if (sampleCNT >= sampleRate) {
				FileUtil.println(writer, line);
				sampleCNT -= sampleRate;
			}
		}
		FileUtil.closeReader(reader);
		FileUtil.closeWriter(writer);
	}

	public static void filterFileAtoFileB(String fileNameA, String fileNameB,
			ArrayList<String> filterList) {
		BufferedReader reader = FileUtil.openReader(fileNameA);
		PrintWriter writer = FileUtil.openWriter(fileNameB, false);
		String line = null;
		while ((line = FileUtil.readLine(reader)) != null) {
			boolean doMatch = false;
			for (String filterStr : filterList) {
				if (line.indexOf(filterStr) >= 0) {
					doMatch = true;
					continue;
				}
			}
			if (!doMatch)
				FileUtil.println(writer, line);
		}
		FileUtil.closeReader(reader);
		FileUtil.closeWriter(writer);
	}

	public static void omitSameLines(String inFileName, String outFileName) {
		TreeSet<String> lineSet = new TreeSet<String>();
		BufferedReader reader = FileUtil.openReader(inFileName);
		PrintWriter writer = FileUtil.openWriter(outFileName, false);
		String line;
		while ((line = FileUtil.readLine(reader)) != null) {
			if (!lineSet.contains(line)) {
				lineSet.add(line);
				writer.println(line);
			}
		}
		FileUtil.closeReader(reader);
		FileUtil.closeWriter(writer);
	}

	public static List<String> getDirFileNames(String directory,
			boolean isRecursive) {
		List<String> fileNameList = new ArrayList<String>();
		try {
			File file = new File(directory);
			if (file.isDirectory()) {
				String[] fileList = file.list();
				for (String fileStr : fileList) {
					String fileNameStr = directory + "/" + fileStr;
					File readfile = new File(fileNameStr);
					if (readfile.isDirectory() && isRecursive) {
						List<String> subFileNameList = getDirFileNames(
								fileNameStr, true);
						fileNameList.addAll(subFileNameList);
					} else if (!readfile.isDirectory() && readfile.exists()) {
						fileNameList.add(fileNameStr);
					}
				}
			} else {
				return fileNameList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileNameList;
	}

	public static void mergeDirFiles(String inDir, String outFileName,
			String prefix, boolean isRecursive) {
		List<String> fileNameList = getDirFileNames(inDir, isRecursive);
		FileUtil.clear(outFileName);
		int count = 0;
		for (String fileName : fileNameList) {
			String[] tempStr = fileName.split("/");
			String noDirFileName = tempStr[tempStr.length - 1];
			if (prefix != null && !prefix.equals("")
					&& noDirFileName.indexOf(prefix) != 0)
				continue;
			logger.info(++count + "/" + fileNameList.size()
					+ ": Merging file " + fileName + "...");
			appendFileAtoFileB(fileName, outFileName);
		}
	}

	public static void colmergeTwoFiles(String fileName1, String commaStr,
			String fileName2, String outFileName) {
		BufferedReader read1 = FileUtil.openReader(fileName1);
		BufferedReader read2 = FileUtil.openReader(fileName2);
		PrintWriter writer = FileUtil.openWriter(outFileName, false);
		String line1 = null;
		String line2 = null;
		while ((line1 = FileUtil.readLine(read1)) != null) {
			if ((line2 = FileUtil.readLine(read2)) == null)
				break;
			FileUtil.println(writer, line1 + commaStr + line2);
		}
		FileUtil.closeReader(read1);
		FileUtil.closeReader(read2);
		FileUtil.closeWriter(writer);
	}

	public static List<String> readLastLines(String filename, int maxLineNum) {
		List<String> lineList = new ArrayList<String>();
		RandomAccessFile rf = null;
		try {
			rf = new RandomAccessFile(filename, "r");
			long len = rf.length();
			long start = rf.getFilePointer();
			long nextend = start + len - 1;
			String line;
			rf.seek(nextend);
			int c = -1;
			boolean isTail = true;
			while (nextend > start) {
				c = rf.read();
				if (c == '\n' || c == '\r') {
					line = rf.readLine();
					if (isTail
							&& (line == null || line.equals("")
									|| line.indexOf(" ") >= 0 || line
										.startsWith("\u0000"))) {// 处理文件末尾是空行或异常结束符
						nextend--;
						rf.seek(nextend);
						continue;
					}
					isTail = false;
					if (!line.startsWith("\u0000")) {
						lineList.add(new String(line.getBytes("ISO-8859-1"),
								"UTF-8"));
					}
					if (lineList.size() >= maxLineNum)
						break;
					nextend--;
				}
				nextend--;
				rf.seek(nextend);
				if (nextend == 0) {// 当文件指针退至文件开始处，输出第一行
					lineList.add(new String(rf.readLine()
							.getBytes("ISO-8859-1"), "UTF-8"));
				}
			}
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rf != null)
					rf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lineList;
	}

	// 通过外部文件获得各行组成的字符串列表
	public static String readContent(String fileName) {
		
//		String content = null;
//		try {
//			Scanner s = new Scanner(new File(fileName));
//			content = s.useDelimiter("\\A").next();
//			s.close();
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		return content;
		
		String content = "";
		File file = new File(fileName);
		if (!file.exists() || file.isDirectory()) {
			return content;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String instring;
			while ((instring = reader.readLine()) != null) {
				if (instring != null) {
					content += instring + "\n";
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void main(String[] args) throws Exception {

		String keyword = "HBO";
		String prefix = "201";
		String fileName = "E:\\java\\data\\SearchSpider\\search_status\\"
				+ keyword + "-" + prefix + ".txt";
		String dir = "E:\\java\\data\\SearchSpider\\search_status\\" + keyword;
		mergeDirFiles(dir, fileName, prefix, false);
	}
}
