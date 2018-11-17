package com.myliqj.util;

import java.io.File;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.StringReader;  
import java.io.StringWriter;  
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;  
  






import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;  
import javax.xml.transform.OutputKeys;  
import javax.xml.transform.Transformer;  
import javax.xml.transform.TransformerConfigurationException;  
import javax.xml.transform.TransformerException;  
import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.dom.DOMSource;  
import javax.xml.transform.stream.StreamResult;  
  











import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;  
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;  
import org.xml.sax.SAXException;  

import com.myliqj.db.dao.SqlXML;

public class JdkXml {
	/**  
     * 初始化一个空Document对象返回。  
     *  
     * @return a Document  
     */  
    public static Document newXMLDocument() {  
        try {  
            return newDocumentBuilder().newDocument();  
        } catch (ParserConfigurationException e) {  
            throw new RuntimeException(e.getMessage());  
        }  
    }  
  
    /**  
     * 初始化一个DocumentBuilder  
     *  
     * @return a DocumentBuilder  
     * @throws ParserConfigurationException  
     */  
    public static DocumentBuilder newDocumentBuilder()  
            throws ParserConfigurationException {  
        return newDocumentBuilderFactory().newDocumentBuilder();  
    }  
  
    /**  
     * 初始化一个DocumentBuilderFactory  
     *  
     * @return a DocumentBuilderFactory  
     */  
    public static DocumentBuilderFactory newDocumentBuilderFactory() {  
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
        dbf.setNamespaceAware(true);  
        return dbf;  
    }  
  
    /**  
     * 将传入的一个XML String转换成一个org.w3c.dom.Document对象返回。  
     *  
     * @param xmlString  
     *            一个符合XML规范的字符串表达。  
     * @return a Document  
     */  
    public static Document parseXMLDocument(String xmlString) {  
        if (xmlString == null) {  
            throw new IllegalArgumentException();  
        }  
        try {  
            return newDocumentBuilder().parse(  
                    new InputSource(new StringReader(xmlString)));  
        } catch (Exception e) {  
            throw new RuntimeException(e.getMessage());  
        }  
    }  
  
    /**  
     * 给定一个输入流，解析为一个org.w3c.dom.Document对象返回。  
     *  
     * @param input  
     * @return a org.w3c.dom.Document  
     */  
    public static Document parseXMLDocument(InputStream input) {  
        if (input == null) {  
            throw new IllegalArgumentException("参数为null！");  
        }  
        try {  
            return newDocumentBuilder().parse(input);  
        } catch (Exception e) {  
            throw new RuntimeException(e.getMessage());  
        }  
    }  
  
    /**  
     * 给定一个文件名，获取该文件并解析为一个org.w3c.dom.Document对象返回。  
     *  
     * @param fileName  
     *            待解析文件的文件名  
     * @return a org.w3c.dom.Document  
     */  
    public static Document loadXMLDocumentFromFile(String fileName) {  
        if (fileName == null) {  
            throw new IllegalArgumentException("未指定文件名及其物理路径！");  
        }  
        try {  
            return newDocumentBuilder().parse(new File(fileName));  
        } catch (SAXException e) {  
            throw new IllegalArgumentException("目标文件（" + fileName  
                    + "）不能被正确解析为XML！" + e.getMessage());  
        } catch (IOException e) {  
            throw new IllegalArgumentException("不能获取目标文件（" + fileName + "）！"  
                    + e.getMessage());  
        } catch (ParserConfigurationException e) {  
            throw new RuntimeException(e.getMessage());  
        }  
    }  
  
    /*  
     * 把dom文件转换为xml字符串  
     */  
    public static String toStringFromDoc(Document document) {  
        String result = null;  
  
        if (document != null) {  
            StringWriter strWtr = new StringWriter();  
            StreamResult strResult = new StreamResult(strWtr);  
            TransformerFactory tfac = TransformerFactory.newInstance();  
            try {  
                javax.xml.transform.Transformer t = tfac.newTransformer();  
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
                t.setOutputProperty(OutputKeys.INDENT, "yes");  
                t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,  
                // text  
                t.setOutputProperty(  
                        "{http://xml.apache.org/xslt}indent-amount", "4");  
                t.transform(new DOMSource(document.getDocumentElement()),  
                        strResult);  
            } catch (Exception e) {  
                System.err.println("XML.toString(Document): " + e);  
            }  
            result = strResult.getWriter().toString();  
            try {  
                strWtr.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        return result;  
    }  
  
    /**  
     * 给定一个节点，将该节点加入新构造的Document中。  
     *  
     * @param node  
     *            a Document node  
     * @return a new Document  
     */  
  
    public static Document newXMLDocument(Node node) {  
        Document doc = newXMLDocument();  
        doc.appendChild(doc.importNode(node, true));  
        return doc;  
    }  
  
    /**  
     * 将传入的一个DOM Node对象输出成字符串。如果失败则返回一个空字符串""。  
     *  
     * @param node  
     *            DOM Node 对象。  
     * @return a XML String from node  
     */  
  
    /*  
     * public static String toString(Node node) { if (node == null) { throw new  
     * IllegalArgumentException(); } Transformer transformer = new  
     * Transformer(); if (transformer != null) { try { StringWriter sw = new  
     * StringWriter(); transformer .transform(new DOMSource(node), new  
     * StreamResult(sw)); return sw.toString(); } catch (TransformerException  
     * te) { throw new RuntimeException(te.getMessage()); } } return ""; }  
     */  
  
    /**  
     * 将传入的一个DOM Node对象输出成字符串。如果失败则返回一个空字符串""。  
     *  
     * @param node  
     *            DOM Node 对象。  
     * @return a XML String from node  
     */  
  
    /*  
     * public static String toString(Node node) { if (node == null) { throw new  
     * IllegalArgumentException(); } Transformer transformer = new  
     * Transformer(); if (transformer != null) { try { StringWriter sw = new  
     * StringWriter(); transformer .transform(new DOMSource(node), new  
     * StreamResult(sw)); return sw.toString(); } catch (TransformerException  
     * te) { throw new RuntimeException(te.getMessage()); } } return ""; }  
     */  
  
    /**  
     * 获取一个Transformer对象，由于使用时都做相同的初始化，所以提取出来作为公共方法。  
     *  
     * @return a Transformer encoding gb2312  
     */  
  
    public static Transformer newTransformer() {  
        try {  
            Transformer transformer = TransformerFactory.newInstance()  
                    .newTransformer();  
            Properties properties = transformer.getOutputProperties();  
            properties.setProperty(OutputKeys.ENCODING, "gb2312");  
            properties.setProperty(OutputKeys.METHOD, "xml");  
            properties.setProperty(OutputKeys.VERSION, "1.0");  
            properties.setProperty(OutputKeys.INDENT, "no");  
            transformer.setOutputProperties(properties);  
            return transformer;  
        } catch (TransformerConfigurationException tce) {  
            throw new RuntimeException(tce.getMessage());  
        }  
    }  
  
    /**  
     * 返回一段XML表述的错误信息。提示信息的TITLE为：系统错误。之所以使用字符串拼装，主要是这样做一般 不会有异常出现。  
     *  
     * @param errMsg  
     *            提示错误信息  
     * @return a XML String show err msg  
     */  
    /*  
     * public static String errXMLString(String errMsg) { StringBuffer msg = new  
     * StringBuffer(100);  
     * msg.append("<?xml version="1.0" encoding="gb2312" ?>");  
     * msg.append("<errNode title="系统错误" errMsg="" + errMsg + ""/>"); return  
     * msg.toString(); }  
     */  
    /**  
     * 返回一段XML表述的错误信息。提示信息的TITLE为：系统错误  
     *  
     * @param errMsg  
     *            提示错误信息  
     * @param errClass  
     *            抛出该错误的类，用于提取错误来源信息。  
     * @return a XML String show err msg  
     */  
    /*  
     * public static String errXMLString(String errMsg, Class errClass) {  
     * StringBuffer msg = new StringBuffer(100);  
     * msg.append("<?xml version='1.0' encoding='gb2312' ?>");  
     * msg.append("<errNode title="  
     * 系统错误" errMsg=""+ errMsg + "" errSource=""+ errClass.getName()+ ""/>");  
     * 　return msg.toString(); }  
     */  
    /**  
     * 返回一段XML表述的错误信息。  
     *  
     * @param title  
     *            提示的title  
     * @param errMsg  
     *            提示错误信息  
     * @param errClass  
     *            抛出该错误的类，用于提取错误来源信息。  
     * @return a XML String show err msg  
     */  
  
    public static String errXMLString(String title, String errMsg,  
            Class errClass) {  
        StringBuffer msg = new StringBuffer(100);  
        msg.append("<?xml version='1.0' encoding='utf-8' ?>");  
        msg.append("<errNode title=" + title + "errMsg=" + errMsg  
                + "errSource=" + errClass.getName() + "/>");  
        return msg.toString();  
    }  
    
    public static void saveToFile(Document doc,String fileName){
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			Properties properties = transformer.getOutputProperties();  
            properties.setProperty(OutputKeys.ENCODING, "utf-8");  
            properties.setProperty(OutputKeys.METHOD, "xml");  
            properties.setProperty(OutputKeys.VERSION, "1.0");   
            properties.setProperty(OutputKeys.STANDALONE, "yes"); // yes 自包含，no 有外部dtd
            properties.setProperty(OutputKeys.INDENT, "yes");     // 是否缩进, 仅换行!
            transformer.setOutputProperties(properties);  
            transformer.setOutputProperty(  
                    "{http://xml.apache.org/xslt}indent-amount", "4");  ;
			
			DOMSource source = new DOMSource(doc);
			StreamResult streamResult = new StreamResult(new File( fileName ));
			transformer.transform(source, streamResult);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void getSQLMAP(String fileName,Map<String,SqlXML> allstr,String sqlTail)
    {
		//String path="D:\\wkMyEclipse2015\\druid_1.1.2\\src\\com\\myliqj\\util\\";
    	
		//Map<String,SqlXML> allstr = new LinkedHashMap<String, SqlXML>(); 
		Document doc  =loadXMLDocumentFromFile(fileName); 
    	Element element = doc.getDocumentElement(); // roo
        System.out.println("根元素：" + element.getNodeName()); // 获得根节点  
        NodeList nl1 = element.getChildNodes();
        for (int i = 0; i < nl1.getLength(); i++) // 遍历这些子节点  
        {
        	// 第一层
        	Node n1 = nl1.item(i); 
    		NodeList nl2 = n1.getChildNodes();
        	//System.out.println(i + " "+n1.getNodeName() + " " + n1.getNodeType() + " sl=" + nl2.getLength());
        	
        	//if (n1.getNodeType() == Node.ELEMENT_NODE){ 
        		
        		//System.out.println(n1.getNodeName() + "    " + i);

        		for (int j = 0; j < nl2.getLength(); j++) // 遍历这些子节点  
                {
                	// 第二层
        			Node n2 = nl2.item(j);
            		NodeList nl3 = n2.getChildNodes();
                	//System.out.println("  " +j + " "+n2.getNodeName() + " " + n2.getNodeType() + " sl=" + nl3.getLength());
        			//if (n2.getNodeType() == Node.ELEMENT_NODE){

                		//System.out.println("  " +n2.getNodeName()+ "    " + j);
//                		String ns = n2.getTextContent();
//                		if (ns != null && !"".equals(ns)){
//                			System.out.println("    " + ns);
//                		}
            			if (n2.getNodeType() == Node.CDATA_SECTION_NODE){
            				try { 
                				SqlXML v = new SqlXML(n1.getNodeName(),n1.getAttributes().getNamedItem("type").getNodeValue(),n2.getTextContent()+sqlTail);
                				allstr.put(n1.getNodeName(), v);
							} catch (Exception e) {
								System.err.println("CDATA - "+n1.getNodeName() + " not Attributes:type !");
								e.printStackTrace();								
							}
            			}
                		
                		for (int k = 0; k < nl3.getLength(); k++) // 遍历这些子节点  
                        {
                        	// 第三层
                			Node n3 = nl3.item(k);
                			NodeList nl4 = n3.getChildNodes();
                        	//System.out.println("    " + k + " "+n3.getNodeName() + " " + n3.getNodeType()+ " sl=" + nl4.getLength());
                			if (n3.getNodeType() == Node.CDATA_SECTION_NODE){

                        		//System.out.println("      " + n3.getNodeValue());
                				//allstr.put(n1.getNodeName()+"."+n2.getNodeName(), n3.getTextContent());
                				
                				try { 
	                				SqlXML v = new SqlXML(n1.getNodeName()+"."+n2.getNodeName(),n2.getAttributes().getNamedItem("type").getNodeValue(),n3.getTextContent()+sqlTail);
	                				allstr.put(v.getName(), v);
								} catch (Exception e) {
									System.err.println("CDATA [ "+n1.getNodeName()+"."+n2.getNodeName() + "] not Attributes:type !");
									e.printStackTrace();								
								}
                			}
                			
                    		for (int l = 0; l < nl4.getLength(); l++) // 遍历这些子节点  
                            {
                            	// 第四层
                    			Node n4 = nl3.item(l);
                    			//NodeList nl5 = n4.getChildNodes();
                            	//System.out.println("      " + l + " "+n4.getNodeName() + " " + n4.getNodeType()+ " sl=" + nl5.getLength());
                    			if (n4.getNodeType() == Node.CDATA_SECTION_NODE){

                            		//System.out.println("        " + n4.getNodeValue());
                    				//allstr.put(n1.getNodeName()+"."+n2.getNodeName()+"."+n3.getNodeName(), n4.getTextContent());
                    				try { 
	                    				SqlXML v = new SqlXML(n1.getNodeName()+"."+n2.getNodeName()+"."+n3.getNodeName(),n3.getAttributes().getNamedItem("type").getNodeValue(),n4.getTextContent()+sqlTail);
	                    				allstr.put(v.getName(), v);
    								} catch (Exception e) {
    									System.err.println("CDATA [ "+n1.getNodeName()+"."+n2.getNodeName()+"."+n3.getNodeName() + "] not Attributes:type !");
    									e.printStackTrace();								
    								}
                    			}
                    			                    			
                            }
                        }
                		
                	//}
                }
        		
        		
        	//}
        	
        }
        
//        for (Map.Entry<String, SqlXML> entry : allstr.entrySet()) {
//        	String type=entry.getValue().getType();
//        	if ("select".equals(type))
//        		System.out.println(entry.getKey() + "  :  \n" + entry.getValue().getSql());
//		}
    }
    public static void main(String[] args)
    {
    	
      String fileName = "D:\\wkMyEclipse2015\\druid_1.1.2\\src\\com\\myliqj\\util\\sqlXml.xml";
      URL url = JdkXml.class.getResource("/resource/sqlXml_db2.xml");
      fileName = url.getFile();
      Map<String,SqlXML> allstr1 = new HashMap<String, SqlXML>();
 	
      Map<String,SqlXML> allstr = new LinkedHashMap<String, SqlXML>();  
      
      //JdkXml.class.getClassLoader().
      getSQLMAP(fileName,allstr,MetaDataSql.DB2_for_read_only_with_ur);
      
      //allstr.putAll(allstr1);
      
      for (Map.Entry<String, SqlXML> entry : allstr.entrySet()) {
    	String type=entry.getValue().getType();
    	if ("select".equals(type))
    		System.out.println(entry.getKey());
//    		System.out.println(entry.getKey() + "  :  \n" + entry.getValue().getSql());
	}	
    }
    
    public static void main2(String[] args)
    {
		Element element = null;
		String path="D:\\wkMyEclipse2015\\druid_1.1.2\\src\\com\\myliqj\\util\\";
	    File f = new File(path +"sqlXml.xml");  
	    DocumentBuilder db = null; // documentBuilder为抽象不能直接实例化(将XML文件转换为DOM文件)  
	    DocumentBuilderFactory dbf = null;  
	    try {  
	        dbf = DocumentBuilderFactory.newInstance(); // 返回documentBuilderFactory对象  
	        db = dbf.newDocumentBuilder();// 返回db对象用documentBuilderFatory对象获得返回documentBuildr对象  
	        Document dt = db.parse(f); // 得到一个DOM并返回给document对象  
	        element = dt.getDocumentElement();// 得到一个elment根元素  
	        System.out.println("根元素：" + element.getNodeName()); // 获得根节点  
	        NodeList childNodes = element.getChildNodes(); // 获得根元素下的子节点  
	        Node node1 = null;
	        for (int i = 0; i < childNodes.getLength(); i++) // 遍历这些子节点  
	        {  
	            node1 = childNodes.item(i); // childNodes.item(i); 
	            if (node1.getNodeType() == Node.ELEMENT_NODE){
	            	
	            	System.out.println(node1.getNodeName() +" : " + node1.getNodeValue());
	            	if (node1.hasAttributes()){
	            		NamedNodeMap nnm= node1.getAttributes();
	            		for (int j=0; j< nnm.getLength(); j++) {
	            			Node n1 = nnm.item(j);
	            			System.out.println("  " +n1.getNodeType() + " : " + n1.getNodeName() +" : " + n1.getNodeValue());
						}
	            	}
	            	if (node1.hasChildNodes()){
	            		NodeList c2 = node1.getChildNodes(); 
	            		for (int j=0; j< c2.getLength(); j++) {
	            			Node n1 = c2.item(j);
	            			System.out.println("  " +n1.getNodeType() + " : " + n1.getNodeName() +" : " + n1.getNodeValue());
						
	            			if (n1.getNodeType() == Node.ELEMENT_NODE){
	    	            		NodeList c3 = n1.getChildNodes(); 
	    	            		for (int k=0; k< c3.getLength(); k++) {
	    	            			Node n3 = c3.item(k); 
	    	            			System.out.println("  " +n3.getNodeType() + " : " + n3.getNodeName() +" : " + n1.getNodeValue());
	    	            			if (n3.getNodeType() == Node.TEXT_NODE){
	    	            				System.out.println("    " + n3.getTextContent());
	    	            			}
	    	            		}
	            			}
	            		
	            		}
	            	}
	            	
	            }
	        }
	         
	        // add node
	       Element e = dt.createElement("test1");
	       e.setTextContent("textConten<>!t");
	       element.appendChild(e) ;
	       

	       e = dt.createElement("test2"); 
	       CDATASection c = dt.createCDATASection("ttd<>df");
	       e.appendChild(c);
	       element.appendChild(e) ;
	       
	       saveToFile(dt, path+"test01.xml");
	       
	       //e.setTextContent(dt.createCDATASection("我的阿波罗<>fff"));
	        
	    } catch (Exception e) {  
           e.printStackTrace();  
        }  
    }
    
}
