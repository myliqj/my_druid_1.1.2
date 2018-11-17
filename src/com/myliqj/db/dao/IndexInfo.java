package com.myliqj.db.dao;

import javax.xml.bind.annotation.XmlRootElement;

import com.myliqj.util.SqlObjDefine;

@XmlRootElement
public class IndexInfo extends Base {

//	TIndexDesc = record
//    IndexSchema: string; //索引模式名
//    IndexName: string;   //索引名称
//    FieldNames: string;  //字段名称列表。已逗号分隔
//    IndexType: TIndexType; //索引类别。主键/唯一索引/普通索引
	
    String IndexSchema; //索引模式名
    String IndexName;   //索引名称
    String FieldNames;  //字段名称列表。已逗号分隔
    String IndexType; //索引类别。P主键/U唯一索引/N普通索引 enuPrimary, enuUnique, enuNormal

    String Create_time;
    String Stats_time;
    String Remarks;
    

	@Override
	public String toDbToolString() {
		return String.format(SqlObjDefine.getNumFormat(6)+"%s",
				IndexSchema,IndexName,FieldNames,IndexType,Create_time,Stats_time,Remarks);
	}
	
	public String toString(){
		return String.format("模式名:%s, 名称:%s, 字段:%s, 类型:%s, 建立时间:%s, 整理时间:%s, 说明:%s", 
				IndexSchema,IndexName,FieldNames,IndexType,Create_time,Stats_time,Remarks);
	}
	
	public String getIndexSchema() {
		return IndexSchema;
	}
	public void setIndexSchema(String indexSchema) {
		IndexSchema = indexSchema;
	}
	public String getIndexName() {
		return IndexName;
	}
	public void setIndexName(String indexName) {
		IndexName = indexName;
	}
	public String getFieldNames() {
		return FieldNames;
	}
	public void setFieldNames(String fieldNames) {
		FieldNames = fieldNames;
	}
	public String getIndexType() {
		return IndexType;
	}
	public void setIndexType(String indexType) {
		IndexType = indexType;
	}
	public String getRemarks() {
		return Remarks;
	}
	public void setRemarks(String remarks) {
		Remarks = remarks;
	}
	public String getCreate_time() {
		return Create_time;
	}
	public void setCreate_time(String create_time) {
		Create_time = create_time;
	}
	public String getStats_time() {
		return Stats_time;
	}
	public void setStats_time(String stats_time) {
		Stats_time = stats_time;
	}

	
	
	
}
