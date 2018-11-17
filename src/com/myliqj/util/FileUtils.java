/*
 * cn.edu.hfut.dmic.webcollector.util
 */
package com.myliqj.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author hu
 */
public class FileUtils {

	public static Vector<String> getFileLines(String file) throws Exception{
		Vector<String> items = new Vector<String>();
		String line; 		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		while ((line = br.readLine()) != null){ 
				items.add(line); 
		}
		br.close();
		return items;
	}
	
	/**
	* ���������ļ�
	* @param destFileName �ļ���
	* @return �����ɹ�����true�����򷵻�false
	*/
	public static boolean CreateFile(String destFileName) {
		File file = new File(destFileName);

		if (file.exists()) {
			System.out.println("���������ļ�" + destFileName + "ʧ�ܣ�Ŀ���ļ��Ѵ��ڣ�");
			return false;
		}

		if (destFileName.endsWith(File.separator)) {
			System.out.println("���������ļ�" + destFileName + "ʧ�ܣ�Ŀ�겻����Ŀ¼��");
			return false;
		}

		if (!file.getParentFile().exists()) {
			System.out.println("Ŀ���ļ�����·�������ڣ�׼������������");
			if (!file.getParentFile().mkdirs()) {
				System.out.println("����Ŀ¼�ļ����ڵ�Ŀ¼ʧ�ܣ�");
				return false;
			}
		}
		// ����Ŀ���ļ�
		try {
			if (file.createNewFile()) {
				System.out.println("���������ļ�" + destFileName + "�ɹ���");
				return true;
			} else {
				System.out.println("���������ļ�" + destFileName + "ʧ�ܣ�");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("���������ļ�" + destFileName + "ʧ�ܣ�");
			return false;
		}
	}
	/**
	* ����Ŀ¼
	* @param destDirName Ŀ��Ŀ¼��
	* @return Ŀ¼�����ɹ�����true�����򷵻�false
	*/
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			System.out.println("����Ŀ¼" + destDirName + "ʧ�ܣ�Ŀ��Ŀ¼�Ѵ��ڣ�");
			return false;
		}
		if (!destDirName.endsWith(File.separator))
			destDirName = destDirName + File.separator;
		// ��������Ŀ¼
		if (dir.mkdirs()) {
			System.out.println("����Ŀ¼" + destDirName + "�ɹ���");
			return true;
		} else {
			System.out.println("����Ŀ¼" + destDirName + "�ɹ���");
			return false;
		}
	}
	/**
	* ������ʱ�ļ�
	* @param prefix ��ʱ�ļ���ǰ׺
	* @param suffix ��ʱ�ļ��ĺ�׺
	* @param dirName ��ʱ�ļ����ڵ�Ŀ¼���������null�������û����ĵ�Ŀ¼�´�����ʱ�ļ�
	* @return ��ʱ�ļ������ɹ����س���·�����Ĺ淶·�����ַ��������򷵻�null
	*/
	public static String createTempFile(String prefix, String suffix,
			String dirName) {
		File tempFile = null;
		try {
			if (dirName == null) {
				// ��Ĭ���ļ����´�����ʱ�ļ�
				tempFile = File.createTempFile(prefix, suffix);
				return tempFile.getCanonicalPath();
			} else {
				File dir = new File(dirName);
				// �����ʱ�ļ�����Ŀ¼�����ڣ����ȴ���
				if (!dir.exists()) {
					if (!createDir(dirName)) {
						System.out.println("������ʱ�ļ�ʧ�ܣ����ܴ�����ʱ�ļ�����Ŀ¼��");
						return null;
					}
				}
				tempFile = File.createTempFile(prefix, suffix, dir);
				return tempFile.getCanonicalPath();
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("������ʱ�ļ�ʧ��" + e.getMessage());
			return null;
		}
	}
	
    public static void deleteDir(File dir) {
        File[] filelist = dir.listFiles();
        for (File file : filelist) {
            if (file.isFile()) {
                file.delete();
            } else {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    public static void copy(File origin, File newfile) throws FileNotFoundException, IOException {
        if (!newfile.getParentFile().exists()) {
            newfile.getParentFile().mkdirs();
        }
        FileInputStream fis = new FileInputStream(origin);
        FileOutputStream fos = new FileOutputStream(newfile);
        byte[] buf = new byte[2048];
        int read;
        while ((read = fis.read(buf)) != -1) {
            fos.write(buf, 0, read);
        }
        fis.close();
        fos.close();
    }

    public static void writeFile(String fileName, String contentStr, String charset) throws FileNotFoundException, IOException {
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content);
        fos.close();
    }

    public static void writeFile(File file, String contentStr, String charset) throws FileNotFoundException, IOException {
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(String fileName, String contentStr, String charset) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(File file, String contentStr, String charset) throws FileNotFoundException, IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFile(String fileName, byte[] content) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content);
        fos.close();
    }

    public static void writeFile(File file, byte[] content) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(String fileName, byte[] content) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(File file, byte[] content) throws FileNotFoundException, IOException {

        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[2048];
        int read;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((read = fis.read(buf)) != -1) {
            bos.write(buf, 0, read);
        }

        fis.close();
        return bos.toByteArray();
    }

    public static byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        return readFile(file);
    }

    public static String readFile(File file, String charset) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[2048];
        int read;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((read = fis.read(buf)) != -1) {
            bos.write(buf, 0, read);
        }

        fis.close();
        return new String(bos.toByteArray(), charset);
    }

    public static String readFile(String fileName, String charset) throws Exception {
        File file = new File(fileName);
        return readFile(file, charset);
    }

	/**
	 * �ر�
	 * @param closeable ���رյĶ���
	 */
	public static void close(Closeable closeable) {
		if (closeable == null) return;
		try {
			closeable.close();
		} catch (Exception e) {
		}
	}
	
	/**
	 * �ر�
	 * @param closeable ���رյĶ���
	 * @since 1.7
	 */
	public static void close(AutoCloseable closeable) {
		if (closeable == null) return;
		try {
			closeable.close();
		} catch (Exception e) {
		}
	}
}
