package com.myliqj.db.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.myliqj.util.MetaDataSql;

public class SqlXML {	
	private String name;
	private String type;
	private String sql;
	
	public SqlXML(String name,String type,String sql){
		setName(name);
		setType(type);
		setSql(sql);
	}
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSql() {
		return sql;
	}
	
	protected String replaceBlank(String str){
	       String dest = null;
	       if(str == null){
	           return dest;
	       }else{
	           Pattern p = Pattern.compile("^\\s*|\t|\r|\n");
	           Matcher m = p.matcher(str);
	           dest = m.replaceAll("");
	           return dest;
	       }
	}
	public void setSql(String sql) {
		
		// 处理一些前缀空格（空白）
		if (sql!=null){			
			String v[] = sql.replaceAll("\r", "").split("\n");
			//String r[] = new String[v.length];
			StringBuilder sb = new StringBuilder();
			int row = 0; int left1 = -1;
			for (String str : v) {
				// 去除前面空行
				if (row==0 && str.trim().length()==0){
					continue;
				}
				row ++;
				
				// 处理，如果首行，前缀空格（空白）去除
				if (row>=1){
					sb.append("  ").append(replaceBlank(str)).append("\r\n");
				}
				
			}
			this.sql = sb.toString();
			return;
		}
		
		
		this.sql = sql;
	}
	
	
}
