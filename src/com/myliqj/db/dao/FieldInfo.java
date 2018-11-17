package com.myliqj.db.dao;

import com.myliqj.util.SqlObjDefine;

public class FieldInfo extends Base{
	
//	TFieldDGL = record
//    No: string; //字段顺序号
//    FieldName: string; //字段名称
//    Description: string; //字段注释
//    TypeName: string; //类型名称
//    Length: string; //长度
//    Scale: string; //精度
//    NotNull: boolean; //是否允许空值, Y-可空,N-不可空
//    Default: string; //默认值
	
	String No;//: string; //字段顺序号
	String FieldName;//: string; //字段名称
	String Description;//: string; //字段注释
	String TypeName;//: string; //类型名称
	String Length;//: string; //长度
	String Scale;//: string; //精度
	String NotNull;//: boolean; //是否不允许空值 Y-可空,N-不可空
	String Default;//: string; //默认值
	
	@Override
	public String toDbToolString(){
		return String.format(SqlObjDefine.getNumFormat(7)+"%s",
				No,FieldName,Description,TypeName,Length,Scale,NotNull,Default);
	}
	
	public String toString(){
		return String.format("序号:%s, 字段名称:%s, 字段注释:%s, 类型名称:%s, 长度:%s, 精度:%s, 是否不允许空值:%s, 默认值:%s", 
				No,FieldName,Description,TypeName,Length,Scale,NotNull,Default);
	}
	
	public String getNo() {
		return No;
	}
	public void setNo(String no) {
		No = no;
	}
	public String getFieldName() {
		return FieldName;
	}
	public void setFieldName(String fieldName) {
		FieldName = fieldName;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getTypeName() {
		return TypeName;
	}
	public void setTypeName(String typeName) {
		TypeName = typeName;
	}
	public String getLength() {
		return Length;
	}
	public void setLength(String length) {
		Length = length;
	}
	public String getScale() {
		return Scale;
	}
	public void setScale(String scale) {
		Scale = scale;
	}
	public String getNotNull() {
		return NotNull;
	}
	public void setNotNull(String notNull) {
		NotNull = notNull;
	}
	public String getDefault() {
		return Default;
	}
	public void setDefault(String default1) {
		Default = default1;
	}		
}
