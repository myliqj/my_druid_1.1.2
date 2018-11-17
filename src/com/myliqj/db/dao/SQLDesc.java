package com.myliqj.db.dao;

import java.util.Set;

public class SQLDesc {
	String sql;         // 实际SQL语句，不会包括 标题、多余分隔符（指继续分隔符） ，如果主类去除单行注释--时，不会包括单行注释
	String rSql;        // 在整个语句中按 startPos,endPos截取的语句，可能包括 单行注释、标题、多余分隔符（指继续分隔符）
	boolean isQuery;    // 当前语句是否查询，一般指 sqlType 为 SELECT / WITH 时为 true，其它情况为 false
	String sqlType;     // 当前语句的类型，表示实际语句开始的第一个标识符，一般是 select / with / update / delete ，记录大写
	Set<String> params; // 当前语句的参数名称，为了保持参数名在语句中的顺序，使用 LinkedHashSet 对象
	String title;       // 当前语句的标题，指在正式语句前的以 #号开始的标识符，以:结束， 例如    #a:select ... 中的标题是 a
	int startPos;       // 当前语句在整个语句中的开始索引，从0开始
	int endPos;         // 当前语句在整个语句中的结束索引
	int relaPos;        // 当前语句在的实际开始位置，在整个语句中的索引
	
	public SQLDesc(String sql,String rSql,boolean isQuery,String sqlType,Set<String> params
			,String title,int startPos,int endPos,int relaPos){
		this.sql = sql;
		this.rSql = rSql;
		this.isQuery = isQuery;
		this.sqlType = sqlType;
		this.params = params;
		this.title = title;
		this.startPos = startPos;
		this.endPos = endPos;
		this.relaPos = relaPos;
	}
	
	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
				"\r\n sql="+sql,"\r\n rsql="+rSql,"\r\n isQuery="+isQuery,"sqlType="+sqlType
				,"params="+params,"title="+title
				,"start="+startPos,"end="+endPos,"rela="+relaPos);
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getrSql() {
		return rSql;
	}

	public void setrSql(String rSql) {
		this.rSql = rSql;
	}

	public boolean isQuery() {
		return isQuery;
	}

	public void setQuery(boolean isQuery) {
		this.isQuery = isQuery;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public Set<String> getParams() {
		return params;
	}

	public void setParams(Set<String> params) {
		this.params = params;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStartPos() {
		return startPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	public int getRelaPos() {
		return relaPos;
	}

	public void setRelaPos(int relaPos) {
		this.relaPos = relaPos;
	}
	
	 
	
}
