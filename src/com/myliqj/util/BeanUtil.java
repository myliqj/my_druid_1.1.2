package com.myliqj.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import com.xiaoleilu.hutool.exceptions.UtilException;
//import com.xiaoleilu.hutool.lang.Conver;

/**
 * Bean工具类
 * 
 * @author Looly
 *
 */
public class BeanUtil {

	/**
	 * 比较两个对象是否相等。<br>
	 * 相同的条件有两个，满足其一即可：<br>
	 * 1. obj1 == null && obj2 == null; 2. obj1.equals(obj2)
	 * 
	 * @param obj1 对象1
	 * @param obj2 对象2
	 * @return 是否相等
	 */
	public static boolean equals(Object obj1, Object obj2) {
		return (obj1 != null) ? (obj1.equals(obj2)) : (obj2 == null);
	}

	/**
	 * 计算对象长度，如果是字符串调用其length函数，集合类调用其size函数，数组调用其length属性，其他可遍历对象遍历计算长度
	 * 
	 * @param obj 被计算长度的对象
	 * @return 长度
	 */
	public static int length(Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof CharSequence) {
			return ((CharSequence) obj).length();
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).size();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).size();
		}

		int count;
		if (obj instanceof Iterator) {
			Iterator<?> iter = (Iterator<?>) obj;
			count = 0;
			while (iter.hasNext()) {
				count++;
				iter.next();
			}
			return count;
		}
		if (obj instanceof Enumeration) {
			Enumeration<?> enumeration = (Enumeration<?>) obj;
			count = 0;
			while (enumeration.hasMoreElements()) {
				count++;
				enumeration.nextElement();
			}
			return count;
		}
		if (obj.getClass().isArray() == true) {
			return Array.getLength(obj);
		}
		return -1;
	}

	/**
	 * 对象中是否包含元素
	 * 
	 * @param obj 对象
	 * @param element 元素
	 * @return 是否包含
	 */
	public static boolean contains(Object obj, Object element) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof String) {
			if (element == null) {
				return false;
			}
			return ((String) obj).contains(element.toString());
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).contains(element);
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).values().contains(element);
		}

		if (obj instanceof Iterator) {
			Iterator<?> iter = (Iterator<?>) obj;
			while (iter.hasNext()) {
				Object o = iter.next();
				if (equals(o, element)) {
					return true;
				}
			}
			return false;
		}
		if (obj instanceof Enumeration) {
			Enumeration<?> enumeration = (Enumeration<?>) obj;
			while (enumeration.hasMoreElements()) {
				Object o = enumeration.nextElement();
				if (equals(o, element)) {
					return true;
				}
			}
			return false;
		}
		if (obj.getClass().isArray() == true) {
			int len = Array.getLength(obj);
			for (int i = 0; i < len; i++) {
				Object o = Array.get(obj, i);
				if (equals(o, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 检查对象是否为null
	 * 
	 * @param obj 对象
	 * @return 是否为null
	 */
	public static boolean isNull(Object obj) {
		return null == obj;
	}

	/**
	 * 检查对象是否不为null
	 * 
	 * @param obj 对象
	 * @return 是否为null
	 */
	public static boolean isNotNull(Object obj) {
		return null != obj;
	}
	
	/**
	 * 克隆对象<br>
	 * 对象必须实现Serializable接口
	 * 
	 * @param obj 被克隆对象
	 * @return 克隆后的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
//	public static <T extends Cloneable> T clone(T obj) {
//		return ClassUtil.invoke(obj, "clone");
//	}

	/**
	 * 克隆对象<br>
	 * 对象必须实现Serializable接口
	 * 
	 * @param obj 被克隆对象
	 * @return 克隆后的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(T obj) {
		final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(byteOut);
			out.writeObject(obj);
			out.flush();
			final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
			return (T) in.readObject();
		} catch (Exception e) {
			throw new UtilException(e);
		} finally {
			FileUtils.close(out);
		}
	}

	/**
	 * 序列化<br>
	 * 对象必须实现Serializable接口
	 * 
	 * @param <T>
	 * @param t 要被序列化的对象
	 * @return 序列化后的字节码
	 */
	public static <T> byte[] serialize(T t) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(byteOut);
			oos.writeObject(t);
			oos.flush();
		} catch (Exception e) {
			throw new UtilException(e);
		} finally {
			FileUtils.close(oos);
		}
		return byteOut.toByteArray();
	}

	/**
	 * 反序列化<br>
	 * 对象必须实现Serializable接口
	 * 
	 * @param <T>
	 * @param bytes 反序列化的字节码
	 * @return 反序列化后的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unserialize(byte[] bytes) {
		ObjectInputStream ois = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			return (T) ois.readObject();
		} catch (Exception e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 是否为基本类型，包括包装类型和非包装类型
	 * @param object 被检查对象
	 * @return 是否为基本类型
	 */
	public static boolean isBasicType(Object object){
		return object instanceof Byte || 
				object instanceof Character || 
				object instanceof Short || 
				object instanceof Integer || 
				object instanceof Long || 
				object instanceof Boolean || 
				object instanceof Float || 
				object instanceof Double || 
				object instanceof String || 
				object instanceof BigInteger || 
				object instanceof BigDecimal;
	}
	
	/**
	 * 检查是否为有效的数字<br>
	 * 检查Double和Float是否为无限大，或者Not a Number<br>
	 * 非数字类型和Null将返回true
	 * @param obj 被检查类型
	 * @return 检查结果，非数字类型和Null将返回true
	 */
	public static boolean isValidIfNumber(Object obj) {
		if (obj != null && obj instanceof Number) {
			if (obj instanceof Double) {
				if (((Double) obj).isInfinite() || ((Double) obj).isNaN()) {
					return false;
				}
			} else if (obj instanceof Float) {
				if (((Float) obj).isInfinite() || ((Float) obj).isNaN()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 对象是否为数组对象
	 * @param obj 对象
	 * @return 是否为数组对象
	 */
	public static boolean isArray(Object obj) {
		if(null == obj){
			throw new NullPointerException("Object check for isArray is null");
		}
		return obj.getClass().isArray();
	}
	
	
	
	
	/**
	 * 获得Bean字段描述数组
	 * 
	 * @param clazz Bean类
	 * @return 字段描述数组
	 * @throws IntrospectionException
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws IntrospectionException {
		return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
	}
	
	/**
	 * 获得字段名和字段描述Map
	 * @param clazz Bean类
	 * @return 字段名和字段描述Map
	 * @throws IntrospectionException
	 */
	public static Map<String, PropertyDescriptor> getFieldNamePropertyDescriptorMap(Class<?> clazz) throws IntrospectionException{
		final PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(clazz);
		Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			//System.out.println(propertyDescriptor.getName()+ "  "+ propertyDescriptor);
			map.put(propertyDescriptor.getName(), propertyDescriptor);
		}
		return map;
	}

	/**
	 * 获得Bean类属性描述
	 * 
	 * @param clazz Bean类
	 * @param fieldName 字段名
	 * @return PropertyDescriptor
	 * @throws IntrospectionException
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, final String fieldName) throws IntrospectionException {
		PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(clazz);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (BeanUtil.equals(fieldName, propertyDescriptor.getName())) {
				return propertyDescriptor;
			}
		}
		return null;
	}
	
	/**
	 * Map转换为Bean对象
	 * 
	 * @param map Map
	 * @param beanClass Bean Class
	 * @return Bean
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
//	public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass) throws InstantiationException, IllegalAccessException {
//		return fillBeanWithMap(map, (T) beanClass.newInstance());
//	}

	/**
	 * Map转换为Bean对象<br>
	 * 忽略大小写
	 * 
	 * @param map Map
	 * @param beanClass Bean Class
	 * @return Bean
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
//	public static <T> T mapToBeanIgnoreCase(Map<?, ?> map, Class<T> beanClass) throws InstantiationException, IllegalAccessException {
//		return fillBeanWithMapIgnoreCase(map, (T) beanClass.newInstance());
//	}

	/**
	 * 使用Map填充Bean对象
	 * 
	 * @param map Map
	 * @param bean Bean
	 * @return Bean
	 */
//	public static <T> T fillBeanWithMap(final Map<?, ?> map, T bean) {
//		return fill(bean, new ValueProvider(){
//			@Override
//			public Object value(String name) {
//				return map.get(name);
//			}
//		});
//	}

	/**
	 * 使用Map填充Bean对象，忽略大小写
	 * 
	 * @param map Map
	 * @param bean Bean
	 * @return Bean
	 */
//	public static <T> T fillBeanWithMapIgnoreCase(Map<?, ?> map, T bean) {
//		final Map<Object, Object> map2 = new HashMap<Object, Object>();
//		for (Entry<?, ?> entry : map.entrySet()) {
//			final Object key = entry.getKey();
//			if (key instanceof String) {
//				final String keyStr = (String) key;
//				map2.put(keyStr.toLowerCase(), entry.getValue());
//			} else {
//				map2.put(key, entry.getValue());
//			}
//		}
//
//		return fill(bean, new ValueProvider(){
//			@Override
//			public Object value(String name) {
//				return map2.get(name.toLowerCase());
//			}
//		});
//	}

	/**
	 * ServletRequest 参数转Bean
	 * 
	 * @param request ServletRequest
	 * @param beanClass Bean Class
	 * @return Bean
	 */
//	public static <T> T requestParamToBean(javax.servlet.ServletRequest request, Class<T> beanClass) {
//		return fillBeanWithRequestParam(request, ClassUtil.newInstance(beanClass));
//	}

	/**
	 * ServletRequest 参数转Bean
	 * 
	 * @param request ServletRequest
	 * @param bean Bean
	 * @return Bean
	 */
//	public static <T> T fillBeanWithRequestParam(final javax.servlet.ServletRequest request, T bean) {
//		final String beanName = StrUtil.lowerFirst(bean.getClass().getSimpleName());
//		return fill(bean, new ValueProvider(){
//			@Override
//			public Object value(String name) {
//				String value = request.getParameter(name);
//				if (StrUtil.isEmpty(value)) {
//					// 使用类名前缀尝试查找值
//					value = request.getParameter(beanName + StrUtil.DOT + name);
//					if (StrUtil.isEmpty(value)) {
//						// 此处取得的值为空时跳过，包括null和""
//						value = null;
//					}
//				}
//				return value;
//			}
//		});
//	}

	/**
	 * ServletRequest 参数转Bean
	 * 
	 * @param <T>
	 * @param beanClass Bean Class
	 * @param valueProvider 值提供者
	 * @return Bean
	 */
//	public static <T> T toBean(Class<T> beanClass, ValueProvider valueProvider) {
//		try {
//			return fill((T) beanClass.newInstance(), valueProvider);
//		} catch (Exception e) {
//			throw new UtilException(StringUtils.format("Instance class [{}] error!", beanClass), e);
//		}
//	}

	/**
	 * 填充Bean
	 * 
	 * @param <T>
	 * @param bean Bean
	 * @param valueProvider 值提供者
	 * @return Bean
	 */
//	public static <T> T fill(T bean, ValueProvider valueProvider) {
//		if (null == valueProvider) {
//			return bean;
//		}
//
//		Class<?> beanClass = bean.getClass();
//		try {
//			PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
//			String propertyName;
//			Object value;
//			for (PropertyDescriptor property : propertyDescriptors) {
//				propertyName = property.getName();
//				value = valueProvider.value(propertyName);
//				if (null == value) {
//					// 此处取得的值为空时跳过，包括null和""
//					continue;
//				}
//
//				try {
//					property.getWriteMethod().invoke(bean, Conver.parse(property.getPropertyType(), value));
//				} catch (Exception e) {
//					throw new UtilException(StrUtil.format("Inject [{}] error!", property.getName()), e);
//				}
//			}
//		} catch (Exception e) {
//			throw new UtilException(e);
//		}
//		return bean;
//	}

	/**
	 * 对象转Map
	 * 
	 * @param bean bean对象
	 * @return Map
	 */
	public static <T> Map<String, Object> beanToMap(T bean) {

		if (bean == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			final PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(bean.getClass());
			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();
				// 过滤class属性
				if (false == key.equals("class")) {
					// 得到property对应的getter方法
					Method getter = property.getReadMethod();
					Object value = getter.invoke(bean);
					if (null != value) {
						map.put(key, value);
					}
				}
			}
		} catch (Exception e) {
			throw new UtilException(e);
		}
		return map;
	}

	/**
	 * 复制Bean对象属性
	 * @param source 源Bean对象
	 * @param target 目标Bean对象
	 */
//	public static void copyProperties(Object source, Object target) {
//		copyProperties(source, target, null, (String[]) null);
//	}
	
	/**
	 * 复制Bean对象属性<br>
	 * 限制类用于限制拷贝的属性，例如一个类我只想复制其父类的一些属性，就可以将editable设置为父类
	 * @param source 源Bean对象
	 * @param target 目标Bean对象
	 * @param ignoreProperties 不拷贝的的属性列表
	 */
//	public static void copyProperties(Object source, Object target, String... ignoreProperties) {
//		copyProperties(source, target, null, ignoreProperties);
//	}
	
	/**
	 * 复制Bean对象属性<br>
	 * 限制类用于限制拷贝的属性，例如一个类我只想复制其父类的一些属性，就可以将editable设置为父类
	 * @param source 源Bean对象
	 * @param target 目标Bean对象
	 * @param editable 限制的类或接口，必须为target对象的实现接口或父类
	 * @param ignoreProperties 不拷贝的的属性列表
	 */
//	private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties) {
//		Class<?> actualEditable = target.getClass();
//		if (editable != null) {
//			//检查限制类是否为target的父类或接口
//			if (!editable.isInstance(target)) {
//				throw new IllegalArgumentException(StringUtils.format("Target class [{}] not assignable to Editable class [{}]", target.getClass().getName(), editable.getName()));
//			}
//			actualEditable = editable;
//		}
//		PropertyDescriptor[] targetPds = null;
//		Map<String, PropertyDescriptor> sourcePdMap;
//		try {
//			sourcePdMap = getFieldNamePropertyDescriptorMap(source.getClass());
//			targetPds = getPropertyDescriptors(actualEditable);
//		} catch (IntrospectionException e) {
//			throw new UtilException(e);
//		}
//		
//		List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);
//
//		for (PropertyDescriptor targetPd : targetPds) {
//			Method writeMethod = targetPd.getWriteMethod();
//			if (writeMethod != null && (ignoreList == null || false == ignoreList.contains(targetPd.getName()))) {
//				PropertyDescriptor sourcePd = sourcePdMap.get(targetPd.getName());
//				if (sourcePd != null) {
//					Method readMethod = sourcePd.getReadMethod();
//					// 源对象字段的getter方法返回值必须可转换为目标对象setter方法的第一个参数
//					if (readMethod != null && ClassUtil.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
//						try {
//							Object value = ClassUtil.setAccessible(readMethod).invoke(source);
//							ClassUtil.setAccessible(writeMethod).invoke(target, value);
//						} catch (Throwable ex) {
//							throw new UtilException(ex, "Copy property [{}] to [{}] error: {}", sourcePd.getName(), targetPd.getName(), ex.getMessage());
//						}
//					}
//				}
//			}
//		}
//	}

	/**
	 * 值提供者，用于提供Bean注入时参数对应值得抽象接口
	 * 
	 * @author Looly
	 *
	 */
	public static interface ValueProvider {
		/**
		 * 获取值
		 * 
		 * @param name Bean对象中参数名
		 * @return 对应参数名的值
		 */
		public Object value(String name);
	}
}

