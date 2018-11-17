package com.myliqj.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.myliqj.db.dao.IndexInfo;

public class JaxbObjectAndXmlUtil {

	/*
https://www.cnblogs.com/happyPawpaw/p/4972675.html java生成解析xml的另外两种方法JAXB
  在JDK1.6时，JAXB 2.0是JDK 1.6的组成部分。JAXB 2.2.3是JDK 1.7的组成部分。
	JDK中JAXB相关的重要Annotation：
  1、@XmlType，将Java类或枚举类型映射到XML模式类型
  2、@XmlAccessorType(XmlAccessType.FIELD) ，控制字段或属性的序列化。FIELD表示JAXB将自动绑定Java类中的每个非静态的（static）、非瞬态的（由@XmlTransient标注）字段到XML。其他值还有XmlAccessType.PROPERTY和XmlAccessType.NONE。
  3、@XmlAccessorOrder，控制JAXB 绑定类中属性和字段的排序。
  4、@XmlJavaTypeAdapter，使用定制的适配器（即扩展抽象类XmlAdapter并覆盖marshal()和unmarshal()方法），以序列化Java类为XML。
  5、@XmlElementWrapper ，对于数组或集合（即包含多个元素的成员变量），生成一个包装该数组或集合的XML元素（称为包装器）。
  6、@XmlRootElement，将Java类或枚举类型映射到XML元素。
  7、@XmlElement，将Java类的一个属性映射到与属性同名的一个XML元素。
  8、@XmlAttribute，将Java类的一个属性映射到与属性同名的一个XML属性。
	
	*/
	
	/**
     * @param xmlStr 字符串
     * @param c 对象Class类型
     * @return 对象实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T xml2Object(String xmlStr,Class<T> c)
    { 
        try
        { 
            JAXBContext context = JAXBContext.newInstance(c); 
            Unmarshaller unmarshaller = context.createUnmarshaller(); 
             
            T t = (T) unmarshaller.unmarshal(new StringReader(xmlStr)); 
             
            return t; 
             
        } catch (JAXBException e) {  e.printStackTrace();  return null; } 
         
    } 
       
    /**
     * @param object 对象
     * @return 返回xmlStr
     */
    public static String object2Xml(Object object)
    { 
        try
        {   
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(object.getClass()); 
            Marshaller  marshal = context.createMarshaller();
             
            marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // 格式化输出 
            marshal.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// 编码格式,默认为utf-8 
            marshal.setProperty(Marshaller.JAXB_FRAGMENT, false);// 是否省略xml头信息 
            marshal.setProperty("jaxb.encoding", "utf-8"); 
            marshal.marshal(object,writer);
             
            return new String(writer.getBuffer());
             
        } catch (Exception e) { e.printStackTrace(); return null;}    
         
    } 
    
    
//  dom4j_1.6.0.jar
//	public static void create(Object obj, String file) throws Exception {
//		SAXReader reader = new SAXReader();
//		File wj=new File(file);
//		if(!wj.exists())wj.createNewFile();
//		Document document =  DocumentHelper.createDocument();//创建document对象，  ;
//		  
//		String cln = obj.getClass().getName();
//		cln = cln.substring((cln.lastIndexOf(".") + 1))
//				.toLowerCase();
//		Element el = document.addElement(cln + "s"); 
//
//		el.add(parseObjectToElement(obj));
//		OutputFormat former = OutputFormat.createPrettyPrint();// 设置格式化输出器
//		former.setEncoding("UTF-8");
//		XMLWriter writer = new XMLWriter(new OutputStreamWriter(
//				new FileOutputStream(file), "UTF-8"), former);
//		writer.write(document);
//		writer.close();
//	}
//	
//	public static void insert(Object obj, String file) throws Exception {
//		SAXReader reader = new SAXReader();
//		Document document = reader.read(file);
//		String cln = obj.getClass().getName();
//		cln = cln.substring((cln.lastIndexOf(".") + 1))
//				.toLowerCase();
//		Element el = document.getRootElement();
//		List<Element>  lt = el.elements();
//		int i = lt.size();
//
//		lt.add(i, parseObjectToElement(obj));
//		OutputFormat former = OutputFormat.createPrettyPrint();// 设置格式化输出器
//		former.setEncoding("UTF-8");
//		XMLWriter writer = new XMLWriter(new OutputStreamWriter(
//				new FileOutputStream(file), "UTF-8"), former);
//		writer.write(document);
//		writer.close();
//	}
//
//	private static Element parseObjectToElement(Object obj) throws Exception {
//		String objName = obj.getClass().getName();
//		objName = objName.substring((objName.lastIndexOf(".") + 1))
//				.toLowerCase();
//		Element el = DocumentHelper.createElement(objName);
//
//		Method[] ms = obj.getClass().getMethods();
//		String txt, zd, filed;
//		Element tmp = null;
//		for (Method m : ms) {
//			if (m.getName().startsWith("get")) {
//
//				zd = m.getName().substring(3);
//				filed = zd.toLowerCase();
//				tmp = DocumentHelper.createElement(filed);
//				txt = DataUtil
//						.changeEmptyObjectToValue(m.invoke(obj, null), "");
//				tmp.setText(txt);
//				el.add(tmp);
//			}
//		}
//
//		return el;
//	}
//
//	public static void delete(String file,Map<String,String> params,Class cls) throws Exception {
//		SAXReader reader = new SAXReader();
//		Document document = reader.read(file);
//		String cln = cls.getName();
//		cln = cln.substring((cln.lastIndexOf(".") + 1)).toLowerCase();
//		
//		Element el = document.getRootElement();
//		List<Element> els=el.elements(cln);
//		List<Element> det=new ArrayList<Element>();
//		for(Element tmp:els){
//			boolean sffhdj=true;
//			if(params!=null){
//				for (String key:params.keySet()) {
//					String val=params.get(key);
//					if(val.equals(tmp.element(key).getText())){
//										
//					}else{
//						sffhdj=false;
//						break;
//					}
//				}
//			}
//			if(sffhdj){
//				det.add(tmp);	
//			}
//			
//		}
//		for (Element tmp:det) {
//			els.remove(tmp);
//		}
//		OutputFormat format = OutputFormat.createPrettyPrint();
//		format.setEncoding("UTF-8");
//		XMLWriter writer = new XMLWriter(new FileOutputStream(file),format);
    //  writer.setEscapeText(false); // 不会转义 <![CDATA[ ... ]]> 的<部份
//		writer.write(document);
//		writer.close();
//	}
//	
//	public static <T> List<T> select(String file,Map<String,String> params,Class<T> cls) throws Exception {
//		SAXReader reader = new SAXReader();
//		Document document = reader.read(file);
//		String cln = cls.getName();
//		cln = cln.substring((cln.lastIndexOf(".") + 1)).toLowerCase();
//		Element el = document.getRootElement();
//		List<Element> els=el.elements(cln);
//		List<T> target=new ArrayList<T>();
//		String zd,filed;
//		for(Element tmp:els){
//			boolean sffhdj=true;
//			if(params!=null){
//				for (String key:params.keySet()) {
//					String val=params.get(key);
//					if(tmp.element(key)!=null){
//						if(val.equals(tmp.element(key).getText())){
//							
//						}else{
//							sffhdj=false;
//							break;
//						}
//					}else{
//						sffhdj=false;
//						break;
//					}
//					
//				}
//			}
//			if(sffhdj){
//				T t_tmp=cls.newInstance();
//				Method[] mds=cls.getMethods();
//				for (Method md:mds) {
//					if(md.getName().startsWith("set")){ 
//						zd=md.getName().substring(3);
//						filed=zd.toLowerCase();
//						md.invoke(t_tmp, tmp.element(filed).getText());
//					}
//				}
//				target.add(t_tmp);	
//			}
//		}
//		OutputFormat format = OutputFormat.createPrettyPrint();
//		format.setEncoding("UTF-8");
//		XMLWriter writer = new XMLWriter(new FileOutputStream(file),format);
//		writer.write(document);
//		writer.close();
//		return target;
//	}
    
    
    public static void main(String[] args)
    {
    	IndexInfo ii = new IndexInfo();
    	ii.setIndexName("index name");
    	ii.setRemarks("remarks");
    	
    	String xmlStr = JaxbObjectAndXmlUtil.object2Xml(ii);//构造报文 XML 格式的字符串
        System.out.println("对象转xml报文： \n"+xmlStr);
        
        IndexInfo ii2 = JaxbObjectAndXmlUtil.xml2Object(xmlStr, IndexInfo.class);
//        System.out.println("报文转xml转： \n"+JSON.toJSONString(ii2));
        System.out.println(ii2);
        
    }
	
}
