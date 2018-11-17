package com.myliqj.db.dao;

import com.myliqj.util.SqlObjDefine;

public class TableInfo extends Base{

//	TTableDesc = record
//    TableName: string; //表名称。若有模式名，包含模式名，之间用'.'分隔
//    BaseTableName: string; //源表名，对于Alias、NickName等有效。若有模式名，包含模式名，之间用'.'分隔
//    Description: string; //表的注释
//    CreateTime: string; //表创建时间
//    TableSpace: string; //所在表空间
//    IndexTableSpace: string; //索引空间
//    RowsCount: string;
//    TableType: string;
//    ObjectID: string;
	

	String TableName; //表名称。若有模式名，包含模式名，之间用'.'分隔
	String BaseTableName; //源表名，对于Alias、NickName等有效。若有模式名，包含模式名，之间用'.'分隔
	String Description; //表的注释
	String CreateTime; //表创建时间
	String StatsTime; //表最后整理时间
	String TableSpace; //所在表空间
	String IndexTableSpace; //索引空间
	String RowsCount;
	String TableType;
	String ObjectID;
	
    @Override
	public String toDbToolString() {
		return String.format(SqlObjDefine.getNumFormat(9)+"%s",
				TableName,BaseTableName,Description,CreateTime,StatsTime,TableSpace,IndexTableSpace,RowsCount,TableType,ObjectID);
	}
	
	
	
	public String getStatsTime() {
		return StatsTime;
	}
	public void setStatsTime(String statsTime) {
		StatsTime = statsTime;
	}
	public String getTableName() {
		return TableName;
	}
	public void setTableName(String tableName) {
		TableName = tableName;
	}
	public String getBaseTableName() {
		return BaseTableName;
	}
	public void setBaseTableName(String baseTableName) {
		BaseTableName = baseTableName;
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
	public String getTableSpace() {
		return TableSpace;
	}
	public void setTableSpace(String tableSpace) {
		TableSpace = tableSpace;
	}
	public String getIndexTableSpace() {
		return IndexTableSpace;
	}
	public void setIndexTableSpace(String indexTableSpace) {
		IndexTableSpace = indexTableSpace;
	}
	public String getRowsCount() {
		return RowsCount;
	}
	public void setRowsCount(String rowsCount) {
		RowsCount = rowsCount;
	}
	public String getTableType() {
		return TableType;
	}
	public void setTableType(String tableType) {
		TableType = tableType;
	}
	public String getObjectID() {
		return ObjectID;
	}
	public void setObjectID(String objectID) {
		ObjectID = objectID;
	}
	
}
