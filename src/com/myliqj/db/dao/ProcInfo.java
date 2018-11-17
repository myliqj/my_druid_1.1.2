package com.myliqj.db.dao;

import com.myliqj.util.SqlObjDefine;

public class ProcInfo extends Base{
//    ProcName: string;
//    Description: string;
//    CreateTime: string;
//    IsValid: boolean;
//    SpecificName: string;
//    ProcType: string;
//    ObjectID: string;
	
	String ProcSchema;
    String ProcName;
    String Description;
    String CreateTime;
    String IsValid;
    String SpecificName;
    String ProcType;
    String ObjectID;
    
    @Override
	public String toDbToolString() {
		return String.format(SqlObjDefine.getNumFormat(7)+"%s",
				ProcSchema,ProcName,Description,CreateTime,IsValid,SpecificName,ProcType,ObjectID);
	}
    
	public String getProcSchema() {
		return ProcSchema;
	}
	public void setProcSchema(String procSchema) {
		ProcSchema = procSchema;
	}
	public String getProcName() {
		return ProcName;
	}
	public void setProcName(String procName) {
		ProcName = procName;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}
	public String getIsValid() {
		return IsValid;
	}
	public void setIsValid(String isValid) {
		IsValid = isValid;
	}
	public String getSpecificName() {
		return SpecificName;
	}
	public void setSpecificName(String specificName) {
		SpecificName = specificName;
	}
	public String getProcType() {
		return ProcType;
	}
	public void setProcType(String procType) {
		ProcType = procType;
	}
	public String getObjectID() {
		return ObjectID;
	}
	public void setObjectID(String objectID) {
		ObjectID = objectID;
	}
    
    
    
}
