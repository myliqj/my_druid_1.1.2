package com.myliqj.util;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CopyDb2Proc {
	
	// 来源数据库
	String srcUrl = "jdbc:db2://127.0.0.1:50000/test";
	String srcUser = "db2admin"; String srcPwd = "db2admin";
	
	// 目标数据库
	String descUrl = "jdbc:db2://127.0.0.1:50000/test2";
	String descUser = "db2admin"; String descPwd = "db2admin";
	

	Connection srcConn ,descConn; 
	
	public static void main(String[] args) throws Exception {
		new CopyDb2Proc().startRun();
	}
	
	public void startRun() throws Exception {
		 
		Class.forName("com.ibm.db2.jcc.DB2Driver");
		srcConn = DriverManager.getConnection(srcUrl, srcUser, srcPwd);
		descConn = DriverManager.getConnection(descUrl, descUser, descPwd);
		
		// 
		copyProc();

	}
	
	
	public void copyProc() throws Exception{
		String sql = "select ROUTINETYPE type,ROUTINESCHEMA ms,ROUTINENAME name,SPECIFICNAME pname,FUNC_PATH,TEXT,REMARKS \n" + 
				"from syscat.routines where routineschema not like 'S%' and ROUTINETYPE in ('P','F')\n" + 
				"order by CREATE_TIME"; 
		List<CopyDb2Proc.ProcInfo> pi_list = querySqlToBeanList(srcConn,sql,CopyDb2Proc.ProcInfo.class);
		
		String descExistsQuerySql = "select 1 from syscat.routines a where a.ROUTINETYPE=? and a.ROUTINESCHEMA=? and a.ROUTINENAME=?";
		PreparedStatement pstmDescExists = descConn.prepareStatement(descExistsQuerySql);
		
		descConn.setAutoCommit(false);
		Statement stmt = descConn.createStatement();
		
		int succ = 0;
		int fail = 0;
		int exists = 0;
		
		for (ProcInfo pi : pi_list) {
			if (!isExistsProc(pi,pstmDescExists)){				
				// 执行
				try { 
					System.out.println("SET CURRENT SCHEMA = \"" + pi.ms+ "\"@");
					stmt.executeUpdate("SET CURRENT SCHEMA = \"" + pi.ms+ "\"");
					
					if (isNotNull(pi.func_path)){
						System.out.println(pi.getFunc_PathSql()+'@');
						stmt.executeUpdate(pi.getFunc_PathSql());
					}
					if (isNotNull(pi.text)){
						System.out.println(pi.text+'@');
						stmt.executeUpdate(pi.text);
						succ ++;
					}
					if (isNotNull(pi.remarks)){
						System.out.println(pi.getCreateRemarksSql()+'@');
						stmt.executeUpdate(pi.getCreateRemarksSql());
					}
				} catch (Exception e) {
					fail ++;
					e.printStackTrace();
					descConn.rollback();
				}finally{
					descConn.rollback();
				}
			}else{
				exists++;
			}
		}
		
		System.out.println(String.format("源数据对象：%d，目标已存在：%d，同步：成功=%d，失败=%d",pi_list.size(),exists, succ, fail));
		
		
	}
	
	public static boolean isNotNull(String obj){
		if (obj==null) return false;
		return obj.trim().length()>0 || !"".equals(obj);
	}
	
	public boolean isExistsProc(ProcInfo pi,PreparedStatement pstm) throws Exception{ 
		pstm.setObject(1, pi.type);
		pstm.setObject(2, pi.ms);
		pstm.setObject(3, pi.name);
		ResultSet rs = pstm.executeQuery();
		boolean isExists = rs.next();
		rs.close();
		return isExists;
	}
	
	

//	public ResultSet getRs(Connection conn,String sql) throws SQLException{ 
//		Statement stmt = conn.createStatement();
//		return stmt.executeQuery(sql);
//	}
	/**
	 * 返回指定SQL语句查询结果，Bean类值的LIST列表，Bean类的值由SetXXXX方法设置值
	 * @param sql
	 * @param t Bean类名
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> querySqlToBeanList(Connection conn,String sql,Class<T> t) throws Exception{
		Statement stmt = conn.createStatement();
		ResultSet rs= stmt.executeQuery(sql);
 
		List<T> li=new ArrayList<T>();
		
		Method[] ms=t.getMethods();
		Method md=null;
		String zd,filed; //int row = 0; 
		Method[] colms = null; int colmsCount = 0;
		int cols=0;
		
		cols=rs.getMetaData().getColumnCount()+1;
		colms = new Method[cols]; Arrays.fill(colms, null); // 空间换时间，不用每个条记录的每个字段都重新定位bean的set方法
		for (int i = 1; i < cols; i++) {
			md=null;
			String cname=rs.getMetaData().getColumnName(i).toLowerCase(); 
			for (Method m:ms) {
				if(m.getName().startsWith("set")){
    				 zd=m.getName().substring(3);
    				 filed=zd.toLowerCase();
    				 if(cname.equals(filed)){
    					 md=m;
    					 colms[i-1] = md; // 索引记录
    					 colmsCount ++;   // 有效的方法
    					 break;
    				 }
    			 }
			}
		}
		if (colmsCount==0)  return li; // 无有效方法，直接返回
		 
		while (rs.next()) {
			@SuppressWarnings("unchecked")
			T x=(T) Class.forName(t.getName()).newInstance();
			for (int i = 1; i < cols; i++) {
				md = colms[i-1];
				if(md!=null){
				  Object obj = rs.getObject(i);
				  if (obj instanceof java.sql.Clob){
					  java.sql.Clob clob = rs.getClob(i);
					  obj = clob.getSubString(1, (int) clob.length());
				  }
				  md.invoke(x, (obj!=null?obj.toString():""));
				}
			}
			li.add(x);
			//row ++; 				
		}
		return li; 
	}
	
	static class ProcInfo{
		String type;
		String ms;
		String name;
		String pname;
		String func_path;
		String text;
		String remarks;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getMs() {
			return ms;
		}
		public void setMs(String ms) {
			this.ms = ms;
		}
		public String getPname() {
			return pname;
		}
		public void setPname(String pname) {
			this.pname = pname;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getFunc_path() {
			return func_path;
		}
		public void setFunc_path(String func_path) {
			this.func_path = func_path;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getRemarks() {
			return remarks;
		}
		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}
		
		public String getCreateRemarksSql(){
			// function:  COMMENT ON SPECIFIC FUNCTION F_GG_SCYWLSH_X IS ''
			// procedure: COMMENT ON SPECIFIC PROCEDURE P_GG_SCYWLSH_2 IS ''
			if ("P".equals(type)){
				return String.format("COMMENT ON SPECIFIC FUNCTION %s.%s IS '%s'", ms,pname,remarks.replaceAll("'", "''"));
			}else{
				return String.format("COMMENT ON SPECIFIC PROCEDURE %s.%s IS '%s'", ms,pname,remarks.replaceAll("'", "''"));
			}
		}
		public String getFunc_PathSql(){
			return isNotNull(func_path)? "SET CURRENT PATH = " + func_path:"";
		}
		
	}

}
