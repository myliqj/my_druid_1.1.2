package com.myliqj.db.dao;

import com.myliqj.util.SqlObjDefine;

public class ProcParamInfo extends Base{
	
//	TProcParam = record
//    Seq: integer;
//    ParamName: string;
//    ParamType: TFieldType;
//    Direction: TParameterDirection; // pdUnknown, pdInput, pdOutput, pdInputOutput,pdReturnValue
//    Size: Integer;
//    Value: string;

	String Seq;
	String ParamName;
	String ParamType;
	String Direction;
	String Size;
	String Value;
	
    @Override
	public String toDbToolString() {
		return String.format(SqlObjDefine.getNumFormat(5)+"%s",
				Seq,ParamName,ParamType,Direction,Size,Value);
	}
    
	public String getSeq() {
		return Seq;
	}
	public void setSeq(String seq) {
		Seq = seq;
	}
	public String getParamName() {
		return ParamName;
	}
	public void setParamName(String paramName) {
		ParamName = paramName;
	}
	public String getParamType() {
		return ParamType;
	}
	public void setParamType(String paramType) {
		ParamType = paramType;
	}
	public String getDirection() {
		return Direction;
	}
	public void setDirection(String direction) {
		Direction = direction;
	}
	public String getSize() {
		return Size;
	}
	public void setSize(String size) {
		Size = size;
	}
	public String getValue() {
		return Value;
	}
	public void setValue(String value) {
		Value = value;
	}
	 
}
