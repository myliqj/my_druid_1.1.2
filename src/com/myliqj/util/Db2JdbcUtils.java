package com.myliqj.util;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.JdbcUtils;
import com.myliqj.db.dao.Base;
import com.myliqj.db.dao.FieldInfo;
import com.myliqj.db.dao.IndexInfo;
import com.myliqj.db.dao.ProcInfo;
import com.myliqj.db.dao.ProcParamInfo;
import com.myliqj.db.dao.SqlXML;
import com.myliqj.db.dao.TableInfo;

public class Db2JdbcUtils {
	
	public static Logger Log = Logger.getLogger(Db2JdbcUtils.class.getName());
		
	public String C_SQL_SEPARATOR = ";";
	public String C_PROC_SEPARATOR = "@";
	public boolean isWindows = true;
	public String C_LineBreak = isWindows?"\r\n":"\n";		
	
	private String dbType = "jdbc";
	private String dbversion = "";
	private String driver;
	private String url;
	private String user;
	private String password;
	private Throwable last_e,last_e_meta;
	public String last_messages,last_messages_meta,last_call_procedure_info;
	
	
	public boolean isDebug = true;
	private boolean isInludeSystemObject = false; // 是否包含 表/过程 的系统对象
	private boolean isShowDefaultSettingValue = false; // 是否显示默认设置的值
	
	/**
	 * 数据库联接
	 */
	private Connection conn = null;
	/**
	 * 表示当前已执行完成查询，记录保存来获取数据用。
	 */
	private Map<Integer,ResultSet> queryRs = new HashMap<Integer, ResultSet>();
	/**
	 * 表示正在执行中的查询，执行完成后就移除，用来异步中断正在运行的查询。 也可查到正在执行的语句情况。
	 */
	private Map<Integer,Statement> current_Statement = new HashMap<Integer, Statement>();
	
	/**
	 * 表示正在执行的非查询语句 ， 一个联接只有建立一个，不释放。  
	 */
	private Statement stmt = null;

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public boolean P_SetKeyValue(String key,String val){
		if ("IncludeSysProc".equals(key)){
			setIsInludeSystemObject(Integer.valueOf(val));
			return true;
		}
		return false;
	}
	
	public void setIsInludeSystemObject(int includeSys) {
		this.isInludeSystemObject = includeSys==1;
	}
	public int getIsInludeSystemObject() {
		return isInludeSystemObject?1:0;
	}
	public Throwable getLast_e_meta() {
		return last_e_meta;
	}

	private void setException(Exception e){
		last_e = e;
		if (last_e==null){
			last_messages = "";
		}else{
			last_messages = last_e.getMessage();
		}
	}
	
	public void setLast_e_meta(Throwable last_e_meta) {
		this.last_e_meta = last_e_meta;
	}

	public String getLast_messages_meta() {
		return last_messages_meta;
	}
	public String getLast_call_procedure_info(){
		return last_call_procedure_info;
	}

	public void setLast_messages_meta(String last_messages_meta) {
		this.last_messages_meta = last_messages_meta;
	}

	public Connection getConn() {
		if (conn == null) connect();
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public Db2JdbcUtils() {
	}
	
	/**
	 * 建立类时设置参数
	 * @param cDriver
	 * @param cUrl
	 * @param cUser
	 * @param cPwd
	 */
	public Db2JdbcUtils(String cDriver,String cUrl, String cUser, String cPwd) {
		this.driver = cDriver;
		this.url = cUrl;
		this.user = cUser;
		this.password = cPwd;
		setDbTypeOfDriver();
		//connect();		
	}
	public void setDbTypeOfDriver(){
		if (driver == null) return;
		/*
		1、Oracle8/8i/9i数据库（thin模式） "oracle.jdbc.driver.OracleDriver" jdbc:oracle:thin:@localhost:1521:SIDorcl"; 
		2、DB2数据库 "com.ibm.db2.jdbc.app.DB2Driver" jdbc:db2://localhost:5000/sample"; //sample为你的数据库名
		3、Sql Server7.0/2000数据库 "com.microsoft.jdbc.sqlserver.SQLServerDriver" "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=mydb"
		4、Sybase数据库 "com.sybase.jdbc.SybDriver" "jdbc:sybase:Tds:localhost:5007/myDB";//myDB为你的数据库名
 		5、Informix数据库 "com.informix.jdbc.IfxDriver" "jdbc:informix-sqli://123.45.67.89:1533/myDB:INFORMIXSERVER=myserver;user=testuser;password=testpassword"; //myDB为数据库名
		6、MySQL数据库 "org.gjt.mm.mysql.Driver" "jdbc:mysql://localhost/myDB?user=soft&password=soft1234&useUnicode=true&characterEncoding=8859_1"  //myDB为数据库名
		7、PostgreSQL数据库 "org.postgresql.Driver" "jdbc:postgresql://localhost/myDB" //myDB为数据库名
		8、access数据库直连用ODBC的  sun.jdbc.odbc.JdbcOdbcDriver "jdbc:odbc:Driver={MicroSoft Access Driver (*.mdb)};DBQ="+application.getRealPath("/Data/ReportDemo.mdb");
       */
		dbType = JdbcUtils.getDbType(this.url, this.driver);
		if (dbType==null){
			dbType = "jdbc";
		}
		
//		if (driver.indexOf("oracle")>=0){
//			dbType = "oracle";
//		}else if (driver.indexOf("db2")>=0){
//			dbType = "db2";
//		}else if (driver.indexOf("sqlserver")>=0){
//			dbType = "sqlserver";
//		}else if (driver.indexOf("sybase")>=0){
//			dbType = "sybase";
//		}else if (driver.indexOf("informix")>=0){
//			dbType = "informix";
//		}else if (driver.indexOf("mysql")>=0){
//			dbType = "mysql";
//		}else if (driver.indexOf("postgresql")>=0){
//			dbType = "postgresql";
//		}else if (driver.indexOf("odbc")>=0){
//			dbType = "odbc";
//		}
	}
	public boolean isDb2(){
		return JdbcConstants.DB2.equals(dbType);
	}
	
	public String getDBVersion(){
		if (!"".equals(dbversion)){
			return dbversion;
		}
		if (this.isDb2()){
			String sql="select versionnumber from SYSIBM.SYSVERSIONS order by versionnumber fetch first 1 row only";
			try {
				Object obj = querySqlToVal(-1,sql);
				if(obj!=null && obj instanceof String){
					dbversion = (String) obj;
					return dbversion;
				}			
			} catch (Exception e) {}
		}
		return "";
	}
	
	public boolean isOracle(){
		return JdbcConstants.ORACLE.equals(dbType);
	}
	public boolean isMySql(){
		return JdbcConstants.MYSQL.equals(dbType);
	}
	public boolean isSqlServer(){
		return JdbcConstants.SQL_SERVER.equals(dbType);
	}
	
	/**
	 * 设置联接参数
	 * @param cDriver
	 * @param cUrl
	 * @param cUser
	 * @param cPwd
	 */
	public void setConnectParams(String cDriver,String cUrl, String cUser, String cPwd) {
		this.driver = cDriver;
		this.url = cUrl;
		this.user = cUser;
		this.password = cPwd;
		setDbTypeOfDriver();
	}
	
	/**
	 * 关闭所有查询对象
	 */
	public void closeAllResultSet(){
		if (queryRs==null) return;
		
		// 正在查询的对象
		for (Statement st : current_Statement.values()) { 
			try {
				if (st!=null){
					st.cancel();
					st.close();
				}
			} catch (Exception e) { }
		}
		current_Statement.clear();
		
		// 已打开的对象
		for (ResultSet value : queryRs.values()) { 
		   //System.out.println("Value = " + value);
			try {
				Statement st = value.getStatement();
				if (st !=null){
					st.cancel();
					st.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (value!=null){
					value.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		queryRs.clear();
	}

	/**
	 * 关闭执行非查询对象
	 */
	public void closeStatement(){ 
		closeStatement(stmt);
		stmt = null;
	}
	public void closeStatement(Statement statement){ 
		try {
			if (statement!=null){
				statement.cancel();
				statement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void closePreparedStatement(PreparedStatement p){
		try { if (p!=null) p.cancel(); p.close(); p=null;} catch (Exception e) {e.printStackTrace();}
	}
	public static void closeResultSet(ResultSet p){
		try { if (p!=null) p.close(); p=null;} catch (Exception e) {e.printStackTrace();}
	}	

	/**
	 * 关闭指定序号的查询结果集，同时从列表移走
	 * @param sqlId
	 * @throws Exception
	 */
	public void closeResultSet(int sqlId) {
		ResultSet rs = queryRs.get(Integer.valueOf(sqlId));
		if (rs==null) return ;
		
		try {
			Statement st = rs.getStatement();
			if (st !=null){
				st.cancel();
				st.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (rs!=null){				
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		queryRs.remove(Integer.valueOf(sqlId));
		//return true;
	}
	
	/**
	 * 取消正在执行的对象，如果已完成执行则从结果集获取，否则从正在执行语句中获取
	 * @param sqlId
	 */
	public void cancelStatement(int sqlId){
		ResultSet rs = queryRs.get(Integer.valueOf(sqlId));
		Statement st = null;
		if (rs==null) {
			st = current_Statement.get(Integer.valueOf(sqlId)); // 正在执行中
			if (st==null) return ;
		}
		try {
			if (rs!=null) st = rs.getStatement();
			if (st !=null){
				st.cancel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		new Thread() {
//			public void run() {
//				try {
//					Statement st = rs.getStatement();
//					if (st !=null){
//						st.cancel();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
	}

	/**
	 * 断开联接，同时关闭所有打开的数据库对象。
	 */
	public void disConnect(){ 
		if (conn != null){
			closeAllResultSet();
			closeStatement();
			try { 
			  conn.close(); 			 
			} catch (Exception e) {
			  last_e = e;
			  e.printStackTrace();
			}
			conn = null;
		} 
	}
	
	/**
	 * 联接数据库
	 * @return true 表示成功，false 表示失败
	 */
	public boolean connect() {
		setException(null);
		if (conn!=null) return false;		
		boolean isConnect = false;
		try {
			Class.forName(this.driver);
			conn = DriverManager.getConnection(url, user, password);
			isConnect = conn != null;
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
		}
		return isConnect;
	}
	/**
	 * 返回最后一次语句错误的异常
	 * @return
	 */
	public Throwable getLastThrowable(){
		return last_e;
	}
	
	/**
	 * 返回指定序号的查询结果集
	 * @param sqlId
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public boolean getResultSet(int sqlId,String sql) {
		setException(null);
		try {
			ResultSet rs = createResultSet(sqlId,sql);
			return rs!=null;
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 返回字符串类型的1个值，取完数据即关闭结果集，无结果返回空串
	 * @param sqlId
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public String getResultSetOneValue(int sqlId,String sql) throws Exception{
		setException(null);
		try{			
			ResultSet rs = createResultSet(sqlId,sql);
			if (rs!=null && rs.next()){
				String result = getValue(rs.getObject(0),""); //(String) rs.getObject(0);
				closeResultSet(sqlId);
				return result;
			}
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
		}
		return "";
	}
	
	public ResultSet createResultSet(int sqlId,String sql) throws Exception{
		setException(null);
		ResultSet rs1 = getResultSet(sqlId);
		if (rs1!=null){ 
			setException(new Exception("已存在相同 sqlid="+sqlId+" 的结果，请先关闭对应sqlid的结果再执行，或换一个sqlid！"));
			//System.out.println(last_messages);
			return null;
		}
		try{
			Statement stmt = getConn().createStatement();
			try{
				// 增加到成员变量，是为了可以中断执行，异步调用 stmt.cancel, 也从另一方面检查是否正在执行SQL语句。
				current_Statement.put(sqlId, stmt);   
				ResultSet rs = stmt.executeQuery(sql);
				if (isDebug) Log.info("executeSQL:" + sql);
				queryRs.put(sqlId, rs);
				return rs;
			}finally{
				current_Statement.remove(sqlId);
			}
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();			
		}
		return null;
	}
	
	/**
	 * 执行事参数的SQL语句
	 * @param sqlId
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public ResultSet createResultSet(int sqlId,String sql,Object[] params) throws Exception{
		setException(null);
		if (params == null) return createResultSet(sqlId,sql);
		ResultSet rs1 = getResultSet(sqlId);
		if (rs1!=null){
			//last_messages = "已存在相同 sqlid="+sqlId+" 的结果，请先关闭对应sqlid的结果再执行，或换一个sqlid！";
			//System.out.println(last_messages);
			setException(new Exception("已存在相同 sqlid="+sqlId+" 的结果，请先关闭对应sqlid的结果再执行，或换一个sqlid！"));
			
			return null;
		}
		try{ 
			if (isDebug) {				
				System.out.println("executeSQL:" + sql);
				System.out.println("参数值:" +Arrays.asList(params));
			}
			PreparedStatement pstm = getConn().prepareStatement(sql);			
			try{
				for (int i = 0; i < params.length; i++) {
					pstm.setObject((i+1), params[i]);
				}
				current_Statement.put(sqlId, pstm);    // 增加到成员变量，是为了可以中断执行，异步调用 pstm.cancel
				ResultSet rs = pstm.executeQuery();	
				queryRs.put(sqlId, rs);
				return rs;
			}finally{
				current_Statement.remove(sqlId);
			}
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();			
		}
		return null;
	}
//	public void getResultSetNoReturn(int sqlId,String sql) throws Exception{
//		ResultSet rs1 = getResultSet(sqlId);
//		if (rs1!=null){
//			last_messages = "已存在相同 sqlid="+sqlId+" 的结果，请先关闭对应sqlid的结果再执行，或换一个sqlid！";
//			System.out.println(last_messages);
//			return ;
//			//throw new Exception("已存在相同 sqlid="+sqlId+" 的结果，请先关闭对应sqlid的结果再执行，或换一个sqlid！");
//		} 
//		try {
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery(sql);
//			queryRs.put(Integer.valueOf(sqlId), rs);
//			return ;
//		} catch (Exception e) {
//			last_messages = e.getMessage();
//			last_e = e;
//			e.printStackTrace();
//		}
//		return ;
//		
//		//Thread.sleep(10*1000);
//		//return true;
//	}
	/**
	 * 获取指定序号的查询结果集
	 * @param sqlId
	 * @return
	 * @throws Exception
	 */
	public ResultSet getResultSet(int sqlId) throws Exception{
		return queryRs.get(Integer.valueOf(sqlId));
	}
	
	
	/**
	 * 执行非查询的SQL语句，返回影响的行数
	 * @param sqlId
	 * @param sql
	 * @return -2 未执行,-3 错误,-9 不是call(最络没执行), ddl 语句返回0, 正常ins/del/ups 返回影响行数
	 *   call-return:-10 表示执行成功,-9不是过程不执行,-12 未执行,-13 报错
	 * @throws Exception
	 */
	public int executeUpdate(int sqlId,String sql) throws Exception{
		/* createStatement(int resultSetType, int resultSetConcurrency,int resultsetSetHoldability)
		 resultSetType 是设置ResultSet对象的类型可滚动，或者是不可滚动: 
		  ** ResultSet.TYPE_FORWARD_ONLY 只能向前滚动
           ResultSet.TYPE_SCROLL_INSENSITIVE 该常量指示可滚动但通常不受其他的更改影响
           ResultSet.TYPE_SCROLL_SENSITIVE 该常量指示可滚动并且通常受其他的更改影响
                                           后面这两个方法都能够实现任意的前后滚动，使用各种移动的ResultSet指针的方法。二者的区别在于前者对于修改不敏感，而后者对于修改敏感
		 resultSetConcurency 是设置ResultSet对象能够修改的:
		  ** ResultSet.CONCUR_READ_ONLY 设置为只读类型的参数。
           ResultSet.CONCUR_UPDATABLE 设置为可修改类型的参数 ，必须单表，表有主键，不能有join,group等。
         resultsetSetHoldability 表示在结果集提交后结果集是否打开
           ResultSet.HOLD_CURSORS_OVER_COMMIT:表示修改提交时，不关闭数据库。
           ResultSet.CLOSE_CURSORS_AT_COMMIT：表示修改提交时ResultSet关闭。
		*/
		setException(null);
		int resultValue = -2; // 未执行
		try {
			if (sql==null) return resultValue;
			int len = sql.length();
			if (len == 0 ) return resultValue; 
						
			int cur = MetaDataSql.getExecuteSqlStartCharIndex(sql);		
			String type = MetaDataSql.getExecuteSqlCharCount(sql,cur,4);  
			if (type!=null && "call".equals(type.toLowerCase())){ 
				resultValue = callProcedure(sqlId,sql,cur);
				if (resultValue != -9){
					return resultValue;
				}
			}
			
			if (stmt==null) {
				stmt = conn.createStatement();	
			}else{
				if(stmt.equals(current_Statement.get(sqlId))){
					setException(new Exception("非查询语句， sqlid="+sqlId+" 正在执行中，不能继续执行！"));
					return -2;
				}
			}
			try{
				current_Statement.put(sqlId, stmt);     // 增加到成员变量，是为了可以中断执行，异步调用 stmt.cancel
				resultValue = stmt.executeUpdate(sql); 	// ddl 语句返回0, 正常ins/del/ups 返回影响行数.
			}finally{
				current_Statement.remove(sqlId);
			}				
		} catch (Exception e) {
			resultValue = -3;
			setException(e);
			e.printStackTrace();
		}
		return resultValue;
	}
	
	
	
	/**
	 * 执行存储过程 call procedure(...) 语句
	 * @param sql 原始SQL语句
	 * @param start 有效开始位置
	 * @return －10 表示执行成功,-9不是过程不执行,-12 未执行,-13 报错
	 * @throws Exception
	 */
	private int callProcedure(int sqlId,String sql,int start) throws Exception{
		// 
		// if (sql.toLowerCase().startsWith("{call")){
		// 解析，是否调用过程 , 不是返回 -9 ，继续直接执行
		setException(null);
		last_call_procedure_info = "";
		int len = sql.length();	
		int startName = sql.toLowerCase().indexOf("call", start);
		if (startName==-1 || startName<start) return -9;
		int endName = sql.indexOf('(', startName);
		if (endName==-1 || endName<startName) return -9;
		
		int resultValue = -12; // 未执行 
		
		String procName = sql.substring(startName+5, endName);
		
		// 开始解析参数
		// call p_proc()
		// call p_proc(1,'dd',?,?)
		// call p_proc(current date,'dd,ddd','w5',?) 
		
		int cur = endName+1;  // 从开始括号之后开始找参数，第一个?参数之后只能是输出参数
		int outParam = 0;
		List<String> in = new ArrayList<String>();
		String param = "";
		while (cur<len) { 
			char c = sql.charAt(cur);
			if (c=='\''){
				// 字符串 '...'
				//param += c;  解析的字符串不包括最边的 ' ，在字符串中的二个''要合并为一个'
				cur++; 
				while (cur<len){
					c = sql.charAt(cur);
					if (c=='\''){
						if (cur+1<len && sql.charAt(cur+1)=='\''){
							param += '\'';
							cur++; cur++;
							continue;
						}else{
							cur++; break;
						}
					} 
					param += c;
					cur++; 
				}
				continue;
			}else if(c=='-'){
				cur ++;
				if (cur<len && sql.charAt(cur)=='-'){
					// 单行注释:  -- ... \r\n
					cur++;
					while (cur<len){ 
						// find: \n or \r\n
						c = sql.charAt(cur);
						if (c=='\r'){
							cur++; 
							if (cur<len) c = sql.charAt(cur);
							if (c=='\n') cur++;
							break;
						}else if (c=='\n') {
							cur++;
							break;
						}						
						cur++;
					}
					continue;
				}else{
					cur--; // 不是注释
				}
			}else if(c=='/'){
				cur ++;
				if (cur<len && sql.charAt(cur)=='*'){
					// 多行注释:  /* ... */
					cur++;
					while (cur<len){ 
						// find: */
						c = sql.charAt(cur);
						if (c=='*'){
							cur++;
							if (cur<len) c = sql.charAt(cur);
							if ( c=='/') {
								cur++;
								break;
							}
						}
						cur++;
					}
					continue;
				}else{
					cur--; // 不是注释
				}
			}else if(c==','){
				// 参数分隔符 
				if (outParam==0){  // 第一个?参数之后只能是输出参数
					in.add(param);
					param = "";
				}
				cur++;
				continue;
			}else if(c=='?'){
				// 输出参数
				outParam++;
				cur++;
				continue;
			}else if(c==')'){
				// 参数结束符
				break;
			}
			param += c;
			cur++;
		}
		
		int inParam = in.size();
		int allParamsSize = inParam+outParam;
		String callStr = "{call "+procName+"(";
		for (int i=0;i<allParamsSize;i++){
			callStr += ((i>0)?",?":"?");
		}
		callStr += ")}";
		
		if (this.isDebug){
			System.out.println("initSQL:" + sql);
			System.out.println("callSQL:" + callStr);
			System.out.println("ProcName:"+procName);
			System.out.println(" inParam-size:"+in.size());
			System.out.println(" inParam:"+in);
			System.out.println("outParam-size:"+outParam);
		}
		
		
		List<ProcParamInfo> ppi = null;
		
		CallableStatement cs = conn.prepareCall(callStr);		
		try {
			// 获取参数列表
			// todo: 要解决重载的问题
			try {				
				ppi = P_GetProcParamsVo(procName);
			} catch (Exception e) { }
			
			for (int i = 1; i <= allParamsSize; i++) {
				ProcParamInfo pi = null;
				if (ppi!=null && ppi.size()>=i){
					pi=ppi.get(i-1);
				}
				int dataType = java.sql.Types.OTHER;
				if(pi!=null){
					dataType = MetaDataSql.db2TypeToJavaType(pi.getParamType());
				}
				
				if (i<=inParam){
					String val = in.get(i-1);
					if (val!=null && ("<NULL>".equals(val.toUpperCase())||"[NULL]".equals(val.toUpperCase()))){
						val = null;
					}
					if (val!=null && "".equals(val.trim())){
						// 检查是否字符类型，不是则设置为空
						if (MetaDataSql.isNumberType(dataType)||MetaDataSql.isDataTimeType(dataType)){
							val = null;
						}
					}
					cs.setObject(i, val);
				}else{
					cs.registerOutParameter(i, dataType); 
				}
			}
			try{
				current_Statement.put(sqlId, cs);     // 增加到成员变量，是为了可以中断执行，异步调用 stmt.cancel 
				cs.execute();
			}finally{
				current_Statement.remove(sqlId);
			}
			resultValue = -10;
			try {
				for (int i = inParam+1; i <=allParamsSize; i++) {
					in.add(String.valueOf(cs.getObject(i)));
				}			
			} catch (Exception e) {}
			
		} catch (Exception e) {
			resultValue = -13;
			setException(e);
			e.printStackTrace();
		}finally{			
			this.closeStatement(cs); 
		}	
		
		// show - params infos
		StringBuilder sb = new StringBuilder();
//		sb.append("    initSQL: ").append(sql).append(C_LineBreak);
//		sb.append("    ProcName=").append(procName).append(C_LineBreak);
		
		int allParamValue = in.size();
		for (int i = 1; i <= allParamsSize; i++) {
			ProcParamInfo pi = null;
			if (ppi!=null && ppi.size()>=i){
				pi=ppi.get(i-1);
			}
			Object val = null;
			try {
				if (i<=allParamValue){
					val = in.get(i-1);
				}else{
					val = null;
				}
			} catch (Exception e) {
				val = null;
			}
			String inout = (i>inParam)?"[OUT]":"[IN]";
			String paramName = ""+i;
			if (pi!=null){
				inout = "["+pi.getDirection()+"]";
				paramName = pi.getParamName();
			}
			sb.append("    ");
			sb.append(StringUtils.L_GetFiexdLengthStrOfChar(inout, 8, true, ' '))
			.append(" ");
			sb.append(StringUtils.L_GetFiexdLengthStrOfChar(paramName, 15, true, ' '));
			sb.append(": ").append(val).append(C_LineBreak);
		}
		if (this.isDebug) System.out.println(sb.toString());
		last_call_procedure_info = sb.toString();
		
		return resultValue;
	}
	
	/**
	 * 返回指定序号的元数据数组,需在结果集未关闭或遍历数据前获取
	 * @param sqlId
	 * @return 返回的1个值为1个字段信息，字段信息之间使用\t分隔
	 * @throws Exception
	 */
	public String[] getResultSetMetaData(int sqlId) throws Exception{
		setException(null);
		ResultSet rs = getResultSet(sqlId);
		if (rs==null || rs.isClosed()) return null;
		String[] r = null;
		try {
			ResultSetMetaData rsmd = rs.getMetaData() ;
			int columnCount = rsmd.getColumnCount();
			if (columnCount ==0) return null;
			r = new String[columnCount];
			for (int i = 1; i <= columnCount ; i++) {
			  // Name,Type(java.sql.Types),size,precision,scale,isNullable,TypeName
			  //   0      1                  2       3      4       5          6
			  String colInfo = String.format("%s\t%d\t%d\t%d\t%d\t%d\t%s", rsmd.getColumnName(i)
					  ,rsmd.getColumnType(i),rsmd.getColumnDisplaySize(i)
					  ,rsmd.getPrecision(i),rsmd.getScale(i)
					  ,rsmd.isNullable(i),rsmd.getColumnTypeName(i));
			  
			  r[i-1] = colInfo;
			}
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
		}
		return r;
	}
	
	/**
	 * 判断指定的结果是否已全部读取，如果已关闭或在最后一条则返回True，否则False
	 * @param sqlId
	 * @return
	 */
	public boolean P_IfReadAll(int sqlId) {
		setException(null);
		ResultSet rs;
		try {
			rs = getResultSet(sqlId);
			if (rs!=null) {
				if (rs.isClosed()) return true; 
				return rs.isLast();
			}
			return false;
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获取指定序号结果集的指定行数, 返回二维数组
	 * @param sqlId 已打开的查询SQL的id号
	 * @param row 返回行数上限
	 * @return 如果返回在行数等于指定行数则返回数组数量等于行数参数，否则返回实际行数的数组。
	 * @throws Exception
	 */
	public String[][] getResultSetData(int sqlId,int row) throws Exception{
		setException(null);
		try {
			ResultSet rs = getResultSet(sqlId);
			if (rs==null) return null;
			if (rs.isClosed()){
				// 已关闭 返回空
				return null;
			}
			ResultSetMetaData rsmd = rs.getMetaData() ;
			int columnCount = rsmd.getColumnCount();
			String[][] r = new String[row][columnCount];
			int startRow = 0; Object obj;
			while (rs.next()){
				for (int i = 1; i <= columnCount ; i++) { 
					obj = rs.getObject(i);
					if (obj != null){
						//r[startRow][i-1] = String.valueOf(obj);
						int type = rsmd.getColumnType(i);
						if (type==-2 || type==-3 || type==-4){
							// 	BINARY	-2, VARBINARY -3 ,LONGVARBINARY -4 
							r[startRow][i-1] = StringUtils.bytesToHexString(rs.getBytes(i));
						}else if (type==2005){
							// CLOB	2005
							java.sql.Clob clob = rs.getClob(i);
							r[startRow][i-1] = clob.getSubString(1, (int) clob.length());
						}else{
						//if (type==2004){
						//    // BLOB	2004 
						//}
						//if (type==91 || type==92 || type==93){
							// 91-date / 92-time / 93-timestamp
							r[startRow][i-1] = jdbcDateTimeObjectToStringOfJdbcType(type, obj, "");
						//}
						}
						
					} else {
						r[startRow][i-1] = "[null]"; // 空值表示法
					}
				} 
				startRow ++;
				if (startRow>=row) break;
			}
			if (startRow==0) return null; // 无数据返回空值
			if (startRow<row){
				// 数据不满时，仅返回有值部份
				String[][] r1 = new String[startRow][columnCount];
				System.arraycopy(r, 0, r1, 0, r1.length);
				return r1;
			}
			return r;
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 返回查询结果集内容,使用\t分隔符，带标题
	 * @param aRs
	 * @throws SQLException
	 */
	public String printResultSet(ResultSet aRs,boolean showTitle) throws SQLException{
		ResultSetMetaData rsmd = aRs.getMetaData() ;
		StringBuilder sb = new StringBuilder();
		int columnCount = rsmd.getColumnCount();
		if (showTitle){			
			for (int i = 1; i <= columnCount ; i++) { 
				if (i>1) sb.append("\t");
				sb.append(rsmd.getColumnName(i));
			}
			sb.append("\n");
		}
		
		while (aRs.next()){
			for (int i = 1; i <= columnCount ; i++) { 
				  if (i>1) sb.append("\t");
				  sb.append( aRs.getObject(i));
			}
			sb.append("\n");
		}
		// 注意：遍历完结果集会关闭 ResultSet
		
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	/**
	 * 返回最后错误消息
	 * @return
	 */
	public String Last_Message(){
		return last_messages;
	}
	/**
	 * 重置最后错误消息为空字条串
	 */
	public void Reset_Last_Message(){
		last_messages = "";
	}
	
	/**
	 * 开始本联接的事务为手动提交
	 * @return
	 * @throws SQLException
	 */
	public boolean beginTrans() throws SQLException{
		if (conn!=null){
			conn.setAutoCommit(false);
			return true;
		}
		return false;
	}
	/**
	 * 如果本联接为手动提交，则执行提交操作，同时设置为自动提交
	 * @return
	 * @throws SQLException
	 */
	public boolean commitTrans() throws SQLException{
		if (conn!=null){
			if(!conn.getAutoCommit()){  
            	conn.commit();
    			conn.setAutoCommit(true);  
            }
			return true;
		}
		return false;
	}
	/**
	 * 如果本联接为手动提交，则执行回滚操作，同时设置为自动提交
	 * @return
	 * @throws SQLException
	 */
	public boolean rollbackTrans() throws SQLException{
		if (conn!=null){
			if(!conn.getAutoCommit()){  
            	conn.rollback();  
    			conn.setAutoCommit(true);
            }
			return true;
		}
		return false;
	}
	/**
	 * 返回是否手动提交
	 * @return True-手动提交,False-自动提交
	 * @throws SQLException
	 */
	public boolean inTransaction() throws SQLException{
		if (conn!=null){ 
			return !conn.getAutoCommit();
		}
		return false;
	}
	/**
	 * 日期时间字段 转为 字符串，按指定格式，自动按对象类型判断
	 * 返回的格式:yyyy-MM-dd HH:mm:ss:SSS
	 * @param obj 字段对象
	 * @param nullVal 如果字段对象为空时，返回的替换字符
	 * @return 不是日期时间类型，返回对象默认toString()
	 */
	public String jdbcDateTimeObjectToString(Object obj,String nullVal){
		if (obj==null) return nullVal;
		if (obj instanceof java.sql.Timestamp) {
			return DateUtils.formatDateTime((java.sql.Timestamp) obj);
		} else if (obj instanceof java.sql.Date) {
			return DateUtils.formatDateTime((java.sql.Date) obj);
		} else if (obj instanceof java.sql.Time) {
			return DateUtils.formatDateTime((java.sql.Time) obj);
		} else if (obj instanceof oracle.sql.TIMESTAMP) {
			try { return DateUtils.formatDateTime(((oracle.sql.TIMESTAMP) obj).timestampValue());
			} catch (Exception e) {}
		}
		return obj.toString();
	}

	/**
	 * 日期时间字段 转为 字符串，按指定jdbctype类型判断
	 * 返回的格式:yyyy-MM-dd HH:mm:ss:SSS
	 * @param jdbcType jdbc数据类型,91-date,92-time,93-timestamp
	 * @param obj 字段对象,如果 jdbcType=93，按obj先判断是否oracle的timestamp类型
	 * @param nullVal 如果字段对象为空时，返回的替换字符
	 * @return 不是日期时间类型，返回对象默认toString()
	 */
	public String jdbcDateTimeObjectToStringOfJdbcType(int jdbcType,Object obj,String nullVal){
		if (obj==null) return nullVal;
		if (jdbcType==91){ // 91 date
			return DateUtils.formatDate( (java.sql.Date)obj );
		}else if (jdbcType==92){ // 92 time
			return DateUtils.formatTime( (java.sql.Time)obj );
		}else if (jdbcType==93){ // 93 TIMESTAMP
			if (obj instanceof oracle.sql.TIMESTAMP){
				try { return DateUtils.formatDateTime(((oracle.sql.TIMESTAMP) obj).timestampValue());
				} catch (Exception e) {}
			}else{
				return DateUtils.formatDateTime( (java.sql.Timestamp)obj );
			}
		}
		return obj.toString();
	}
	
	/**
	 * 返回指定SQL语句查询结果，Bean类值的LIST列表，Bean类的值由SetXXXX方法设置值
	 * @param sqlId 语句id
	 * @param sql SQL语句
	 * @param t Bean类名，直接写 xxx.class
	 * @param limit 返回行数上限，<=0 表示所有记录，否则最多返回指定数量行数
	 * @return 指定类的List对象
	 * @throws Exception
	 */
	public <T> List<T> querySqlToBeanList(int sqlId,String sql,Class<T> t,int limit,Object[] params) throws Exception{
		ResultSet rs= createResultSet(sqlId, sql, params);
		try {
			List<T> li=new ArrayList<T>();			
			Method[] ms= t.getMethods();
			String filed;
			Method[] colms = null; int colmsCount = 0;
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols= rsmd.getColumnCount()+1;
			colms = new Method[cols]; Arrays.fill(colms, null); // 空间换时间，不用每个条记录的每个字段都重新定位bean的set方法
			for (int i = 1; i < cols; i++) {
				String cname= rsmd.getColumnName(i).toLowerCase(); 
				for (Method m:ms) {
					if(m.getName().startsWith("set")){
						filed=m.getName().substring(3).toLowerCase();
	    				if(cname.equals(filed)){
	    					 colms[i-1] = m; // 索引记录
	    					 colmsCount ++;   // 有效的方法
	    					 break;
	    				}
	    			 }
				}
			}
			if (colmsCount==0)  return li; // 无有效方法，直接返回
			 
			int row = 0; 
			Method md=null;
			while (rs.next()) {
				@SuppressWarnings("unchecked")
				T x=(T) Class.forName(t.getName()).newInstance();
				for (int i = 1; i < cols; i++) {
					md = colms[i-1];
					if(md!=null){
 					  md.invoke(x, getValue(rs.getObject(i),""));
					}
				}
				li.add(x);
				row ++;
				if (limit>0 && row>=limit) break;				
			}
			return li;
		} finally { 
			closeResultSet(sqlId);
		}
	}
	public <T> List<T> querySqlToBeanList(int sqlId,String sql,Class<T> t,Object[] params) throws Exception{
		return querySqlToBeanList(sqlId,sql,t,-1,params);
	}
	

	public String getValue(Object obj,String nullVal){
		if (obj==null) return nullVal;
		
		if (obj instanceof String){
			return obj.toString();
		} else if (obj instanceof java.sql.Timestamp) {
			return DateUtils.formatDateTime((java.sql.Timestamp) obj);
		} else if (obj instanceof java.sql.Date) {
			return DateUtils.formatDateTime((java.sql.Date) obj);
		} else if (obj instanceof java.sql.Time) {
			return DateUtils.formatDateTime((java.sql.Time) obj);
		} else if (obj instanceof oracle.sql.TIMESTAMP) {
			try { return DateUtils.formatDateTime(((oracle.sql.TIMESTAMP) obj).timestampValue());
			} catch (Exception e) {}
		}
		return obj.toString();
	}
	
		
	
	/**
	 * 返回指定SQL语句查询结果，Bean类值，Bean类的值由SetXXXX方法设置值
	 * @param sqlId
	 * @param sql
	 * @param t
	 * @return
	 * @throws Exception
	 */
//	public <T> T querySqlToBean(int sqlId,String sql,T t) throws Exception{
//		ResultSet rs= createResultSet(sqlId, sql);
//		try { 
//			Method[] ms=t.getClass().getMethods();
//			Method md=null;
//			String zd,filed;
//			if (rs.next()) {
//				int cols=rs.getMetaData().getColumnCount()+1;
//				for (int i = 1; i < cols; i++) {
//					md=null;
//					String cname=rs.getMetaData().getColumnName(i).toLowerCase();
//					Object val=rs.getObject(i);
//					for (Method m:ms) {
//						if(m.getName().startsWith("set")){
//		    				 zd=m.getName().substring(3);
//		    				 filed=zd.toLowerCase();
//		    				 if(cname.equals(filed)){
//		    					 md=m;
//		    					 break;
//		    				 }
//		    			 }
//					}
//					if(md!=null)
//						md.invoke(t, val!=null?val.toString():"");
//				}
//			} 
//			return t;
//		} finally { 
//			closeResultSet(sqlId);
//		}
//	}

	
	/**
	 * 返回指定SQL语句查询结果，返回 List<Map>，map为：字段名-值
	 * @param sqlId 系统缓存执行ID
	 * @param sql 执行的SQL查询语句
	 * @param limit ，<=0 表示所有记录，否则最多返回指定数量行数
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> querySqlRowDataToMap(int sqlId,String sql,int limit,Object[] params) throws Exception{
		ResultSet rs= createResultSet(sqlId, sql, params);
		try {
			List<Map<String, Object>> li=new ArrayList<Map<String,Object>>();
			Map<String, Object> map=null; int row = 0;
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				map=new HashMap<String, Object>();
				int cols= rsmd.getColumnCount();
				for (int i = 0; i < cols; i++) {
					Object obj = rs.getObject(i+1);
					int colType = rsmd.getColumnType(i+1);
					String key = rsmd.getColumnName(i+1);
					if (obj==null){
						map.put(key, "");
					}else if (MetaDataSql.isNumberType(colType)){
						map.put(key, String.valueOf(obj));
					}else if (colType==91){ // 91 date
						map.put(key, DateUtils.formatDate( (java.sql.Date)obj ));
					}else if (colType==92){ // 92 time
						map.put(key, DateUtils.formatTime( (java.sql.Time)obj ));
					}else if (colType==93){ // 93 TIMESTAMP
						map.put(key, DateUtils.formatDateTime( (java.sql.Timestamp)obj ));
					}else if (colType==2005){ // 2005 CLOB
						//if (obj instanceof oracle.sql.CLOB){}
						java.sql.Clob clob = (java.sql.Clob) obj; 
						map.put(key, clob.getSubString((long)1, (int)clob.length())) ;
					}else if (colType==2004){ // 2004 BLOB 
						java.sql.Blob blob = (java.sql.Blob) obj; 
						byte[] val = blob.getBytes((long)1, (int)blob.length()); 
						map.put(key, new String(val)) ;
					}else{						
						map.put(key, obj);
					}
				}
				li.add(map);
				row ++;
				if (limit>0 && row>=limit) break;
			}
			return li;
		} finally { 
			closeResultSet(sqlId);
		}
	}
	public List<Map<String, Object>> querySqlRowDataToMap(int sqlId,String sql,int limit) throws Exception{
		return querySqlRowDataToMap(sqlId,sql,limit,null);
	}
	
	
	/**
	 * 返回语句执行结果全部记录行
	 * @param sqlId
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> querySqlRowDataToMap(int sqlId,String sql) throws Exception{
		return querySqlRowDataToMap(sqlId,sql,-1);
	}
	public List<Map<String, Object>> querySqlRowDataToMap(int sqlId,String sql,Object []params) throws Exception{
		if (sql==null || sql.trim().length()==0){
			return null; // new ArrayList<Map<String,Object>>()
		}
		return querySqlRowDataToMap(sqlId,sql,-1,params);
	}
	/**
	 * 返回一行Map	
	 * @param sqlId
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> querySqlOneRowDataToMap(int sqlId,String sql) throws Exception{
		List<Map<String, Object>> li=querySqlRowDataToMap(sqlId,sql,1);
		if (li!=null && li.size()>0) return li.get(0);
	    return null;
	}
	public Map<String, Object> querySqlOneRowDataToMap(int sqlId,String sql,Object[] params) throws Exception{
		List<Map<String, Object>> li=querySqlRowDataToMap(sqlId,sql,1,params);
		if (li!=null && li.size()>0) return li.get(0);
	    return null;
	}
	
	/**
	 * 返回查询语句第一行，第一个值。
	 * @param sqlId
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public Object querySqlToVal(int sqlId,String sql,Object[] Params) throws Exception{
//		Object obj=null;
//		List<Map<String, Object>> li=querySqlRowDataToMap(sqlId,sql,1);
//		if(li!=null&&li.size()>0){
//			Map<String, Object> mi=li.get(0);
//			Set<String> ms=mi.keySet();
//			@SuppressWarnings("rawtypes")
//			Iterator it=ms.iterator();
//			while (it.hasNext()) {
//				String ky=(String) it.next();
//				obj=mi.get(ky);
//				break;
//			}
//		}
		
		ResultSet rs = createResultSet(sqlId,sql,Params); // 元数据使用 sqlid 为负数
		try {
			if (rs!=null && rs.next() && rs.getMetaData().getColumnCount()>0){
				return rs.getObject(1);
			}
		} catch (Exception e) {
			last_e_meta = e;
			last_messages_meta = e.getMessage();
		} finally{
			closeResultSet(sqlId);
		}
		return null;
	}
	public Object querySqlToVal(int sqlId,String sql) throws Exception{
		return querySqlToVal(sqlId,sql,null);
	}
	
	/**
	 * 返回查询语句第一行的所有值数组。
	 * @param sqlId
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public Object[] querySqlToVals(int sqlId,String sql,Object[] Params) throws Exception{
//		Object[] obj=null;
//		List<Map<String, Object>> li=querySqlRowDataToMap(sqlId,sql,1);
//		Map<String, Object> mi=li.get(0); // 使用HashMap没有保证最后输出的顺序
//		Set<String> ms=mi.keySet();
//		@SuppressWarnings("rawtypes")
//		Iterator it=ms.iterator();
//		obj=new Object[ms.size()];
//		int i=0;
//		while (it.hasNext()) {
//			String ky=(String) it.next();
//			obj[i]=mi.get(ky);
//			i++;
//		}
		
		ResultSet rs = createResultSet(sqlId,sql,Params); // 元数据使用 sqlid 为负数
		try {
			if (rs!=null && rs.next() && rs.getMetaData().getColumnCount()>0){  
				int cols=rs.getMetaData().getColumnCount();
				Object[] objs = new Object[cols];
				for (int i = 0; i < cols; i++) { 
				  objs[i] = rs.getObject(i+1);
				}
				return objs;
			}
		} catch (Exception e) {
			last_e_meta = e;
			last_messages_meta = e.getMessage();
		} finally{
			closeResultSet(sqlId);
		}		
		return null;
	}
	public Object[] querySqlToVals(int sqlId,String sql) throws Exception{
		return querySqlToVals(sqlId,sql,null);
	}
		
	public Object queryOneValue(String sql) throws Exception{
		return querySqlToVal(-1,sql); // 元数据使用 sqlid 为负数，从-1开始，不指定参数时使用-1
	}
	public Object queryOneValue(int sqlId,String sql) throws Exception{ 		
		return querySqlToVal(sqlId,sql); 
	}
	public Object queryOneValue(int sqlId,String sql,Object[] Params) throws Exception{ 
		return querySqlToVal(sqlId,sql,Params);
	}
	public Map<String,Object> queryMapValue(int sqlId,String sql) throws Exception{ 		
		return querySqlOneRowDataToMap(sqlId,sql); 
	}
	 	
	public String L_GetCurUserName() throws Exception{
		return (String) queryOneValue(-1,getSql(SqlObjDefine.USERNAME));
	}
	public String L_GetLoginUserName() throws Exception{
		return (String) queryOneValue(-1,getSql(SqlObjDefine.LOGINUSER));
	}
	
	public String getSql(String key){
		String sql = "";		
		SqlXML sx = MetaDataSql.getSql(dbType, key);
		if (sx!=null){			
			sql = sx.getSql();
		}
		return sql;
	}
	public String getSql(String key,String ... whereKey){
		String sql = "";		
		SqlXML sx = MetaDataSql.getSql(dbType, key);
		if (sx!=null){			
			sql = sx.getSql();
			if (this.isDb2()){
				if (sql.indexOf(MetaDataSql.DB2_for_read_only_with_ur)>0){
					sql = sql.substring(0,sql.lastIndexOf(MetaDataSql.DB2_for_read_only_with_ur));				
				}
			}
			for (String wkey : whereKey) {
				if (StringUtils.isNotEmpty(wkey)){
					sx = MetaDataSql.getSql(dbType, wkey);
					if (sx!=null){
						String sql2 = sx.getSql();
						if (this.isDb2()){
							if (sql2.indexOf(MetaDataSql.DB2_for_read_only_with_ur)>0){
								sql2 = sql2.substring(0,sql2.lastIndexOf(MetaDataSql.DB2_for_read_only_with_ur));				
							}
						}
						sql += " AND " + sql2;
					}
				}
			}
		}
		return sql;
	}
	
	/**
	 * 元数据由 Bean 转为 一维数组，可以传送给 db tool
	 * @param li
	 * @return
	 * @throws Exception
	 */
	public String[] L_DaoListToDbToolStringArray(List<? extends Base> li) throws Exception{
		String [] result = new String[li.size()];
		for (int i = 0; i < li.size(); i++) { 
			result[i] = ((Base)li.get(i)).toDbToolString();
		}
		return result;
	}
	 
	
	public List<FieldInfo> P_GetFieldList(String I_TableName) throws Exception{
		
		List<FieldInfo> fds = new ArrayList<FieldInfo>();
		
		UserInfo ui = new UserInfo(I_TableName);
//		String sql = "SELECT A.COLNO,A.COLNAME, A.TYPENAME, A.LENGTH, A.SCALE,A.NULLS, A.DEFAULT, A.REMARKS "
//				+ ",A.GENERATED, A.IDENTITY,A.TEXT,A.LOGGED, A.COMPACT,A.CODEPAGE FROM SYSCAT.COLUMNS A	"
//				+ "WHERE TABNAME = ? AND TABSCHEMA = ? ORDER BY A.COLNO "+ for_read_only_with_ur;
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.FIELD_LIST), new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return fds;
		
		for(Map<String,Object> obj :li){
			
			FieldInfo fd = new FieldInfo();
			
			fd.setNo( String.valueOf(obj.get("NO")) );
			fd.setFieldName( (String)obj.get("FIELDNAME") );
			fd.setDescription( (String)obj.get("DESCRIPTION") );
			fd.setTypeName( (String)obj.get("TYPENAME") );
			fd.setLength( String.valueOf(obj.get("LENGTH")) );
			fd.setScale( String.valueOf(obj.get("SCALE")) ); 
			fd.setNotNull( String.valueOf(obj.get("NOTNULL")) );
//			fd.setNotNull( "N".equals(obj.get("NotNull")) ? "TRUE" : "FALSE");
			fd.setDefault( (String)obj.get("DEFAULT"));
			
			if (this.isDb2()){
				// db2
				StringBuilder defsb = new StringBuilder();
				
				// 默认值处理 ，除了基本信息，其实附加信息都记录到默认字段
				// // 昵称在 columns.default 记录下 DEFAULT,实际值不正确
				String def = (String)obj.get("DEFAULT");
				if ("DEFAULT".equals(def) || def==null || def.equals("") ){
					//fd.Default = "";
				}else{ 
					defsb.append("DEFAULT ").append(def);
				}
				//lob options
				if ("N".equals((String)obj.get("LOGGED"))){
					defsb.append(" NOT LOGGED");
				}
				if ("Y".equals((String)obj.get("COMPACT"))){
					defsb.append(" COMPACT");
				}
				
				// 计算或自增字段
				String gen = (String)obj.get("GENERATED");
				if ("A".equals(gen) || "D".equals(gen)){
					
					defsb.append(" GENERATED ").append("A".equals(gen)?"ALWAYS ":"BY DEFAULT ");
					
					String ide = (String)obj.get("IDENTITY");
					if ("Y".equals(ide)){
	//					sql = "SELECT START,INCREMENT,MINVALUE,MAXVALUE,CYCLE,CACHE,NEXTCACHEFIRSTVALUE "
	//						+ "FROM SYSCAT.COLIDENTATTRIBUTES WHERE TABNAME = ? AND TABSCHEMA = ? AND COLNAME= ? " + for_read_only_with_ur;
						Map<String, Object> map = querySqlOneRowDataToMap(-1, getSql(SqlObjDefine.FIELD_ARR_IDENTITY) , new String[]{ui.name,ui.user,fd.getFieldName()});
						if(map!=null){
							defsb.append("AS IDENTITY(START WITH ").append(map.get("START")).append(" , INCREMENT BY ")
							     .append(map.get("INCREMENT"));
							if ("1".equals((String)map.get("MINVALUE"))){
								defsb.append(", MINVALUE ").append((String)map.get("MINVALUE"));
							}
							if (!"2147483647".equals((String)map.get("MAXVALUE")) && !"9223372036854775807".equals((String)map.get("MAXVALUE"))){
								defsb.append(", MAXVALUE ").append((String)map.get("MAXVALUE"));
							}
							if ("Y".equals((String)map.get("CYCLE"))){
								defsb.append(", CYCLE ");
							}
							if ("0".equals((String)map.get("CACHE"))){
								defsb.append(", NO CACHE");
							}else{
								defsb.append(", CACHE ").append((String)map.get("CACHE"));
							}
							defsb.append(" ) ");
							
							// 序列 当前值
							defsb.append(String.format("-- ALTER TABLE %s.%s ALTER COLUMN %s RESTART WITH %s",ui.user,ui.name,fd.getFieldName(),map.get("NEXTCACHEFIRSTVALUE")));
							
						}
						
					}else{
						defsb.append(String.valueOf(obj.get("TEXT")));
					}
					
				}
				
				// 字符集
				if ("0".equals(String.valueOf(obj.get("CODEPAGE")))){
					if ("VARCHAR".equals(fd.getTypeName()) || "CHARACTER".equals(fd.getTypeName())){
						defsb.append(" FOR BIT DATA");
					}
				}		
				fd.setDefault(defsb.toString());
			}
			fds.add(fd);
		}
		
		return fds;
	}
	public String[] StringArray_P_GetFieldList(String I_TableName) throws Exception{
		return L_DaoListToDbToolStringArray(P_GetFieldList(I_TableName));
	}
	
	public UserInfo getUserInfo(String i_Name) throws Exception{
		return new UserInfo(i_Name);
	}
	
	public <T> List<T> P_GetDbObjFromSqlDefine(Class<T> t,String I_Name,String objDefine) throws Exception{
		if (I_Name==null || "".equals(I_Name)){
			return querySqlToBeanList(-1,getSql(objDefine), t, -1,null);	
		}
		UserInfo ui = this.getUserInfo(I_Name);
		return querySqlToBeanList(-1,getSql(objDefine), t, -1,new String[]{ui.name,ui.user});
	}
	
	/**
	 * 返回索引信息列表
	 * @param I_TableName
	 * @return
	 * @throws Exception
	 */
	public List<IndexInfo> P_GetIndexList(String I_TableName) throws Exception{	
		return P_GetDbObjFromSqlDefine(IndexInfo.class,I_TableName,SqlObjDefine.INDEX_LIST);		
	}
	public String[] StringArray_P_GetIndexList(String I_TableName) throws Exception{
		return L_DaoListToDbToolStringArray(P_GetIndexList(I_TableName));
	}
	
	/**
	 * 返回表信息列表
	 * @param I_TableName
	 * @return
	 * @throws Exception
	 */
	public List<TableInfo> P_GetTableList(String I_TableName) throws Exception{
		if (I_TableName!=null && !"".equals(I_TableName)){
			UserInfo ui = this.getUserInfo(I_TableName);
			return querySqlToBeanList(-1,getSql(SqlObjDefine.TABLE_LIST
					,SqlObjDefine.TABLE_LIST_WHERE
					//,(!isInludeSystemObject?SqlObjDefine.TABLE_LIST_WHERE_NOSYS:"") 有条件时，无需加上非系统
					), TableInfo.class, -1,new String[]{ui.name,ui.user});
		}
		return querySqlToBeanList(-1,getSql(SqlObjDefine.TABLE_LIST,(!isInludeSystemObject?SqlObjDefine.TABLE_LIST_WHERE_NOSYS:""))
				,TableInfo.class,-1,null);		
	}
	public String[] StringArray_P_GetTableList(String I_TableName) throws Exception{
		return L_DaoListToDbToolStringArray(P_GetTableList(I_TableName));
	}
	
	public String L_BuildTabArrDDL(String I_Name) throws Exception{	
		
		if (!isDb2()) return "";
		
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.TABLE_ARR), new String[]{ui.name,ui.user});		
		if (li ==null || li.size()<=0) return "";
	
		StringBuilder sb = new StringBuilder();
		for(Map<String,Object> obj :li){
			// 数据捕获
			String val = (String) obj.get("DATACAPTURE");
			if (val!=null && (val.length()>0)){
				char c = val.charAt(0);
				boolean isDef = !(c == 'Y' || c=='L'); // 是否默认
				sb.append( (isDef)?"-- ":"")
				  .append("ALTER TABLE ").append(ui.getFullName()).append(" DATA CAPTURE ")
				  .append( (c=='Y')?"CHANGES":(c=='L')?"CHANGES INCLUDE LONGVAR COLUMNS":"NONE")
				  .append(this.getSeparator(true, false))
				  .append( " -- Default:NONE")
				  .append(C_LineBreak);
			}
			
			// VOLATILE
			val = (String) obj.get("VOLATILE");
			if (val!=null && (val.length()>0)){
				char c = val.charAt(0);
				boolean isDef = c != 'C'; // 是否默认
				sb.append( (isDef)?"-- ":"")
				  .append("ALTER TABLE ").append(ui.getFullName()).append(" ")
				  .append( (c=='C')?"NOT ":"").append("VOLATILE")
				  .append(this.getSeparator(true, false))
				  .append( " -- Default:VOLATILE")
				  .append(C_LineBreak);
			}
			
			// APPEND_MODE
			val = (String) obj.get("APPEND_MODE");
			if (val!=null && (val.length()>0)){
				char c = val.charAt(0);
				boolean isDef = c != 'Y'; // 是否默认
				sb.append( (isDef)?"-- ":"")
				  .append("ALTER TABLE ").append(ui.getFullName()).append(" APPEND ")
				  .append( (c=='Y')?"ON":"OFF")
				  .append(this.getSeparator(true, false))
				  .append( " -- Default:OFF")
				  .append(C_LineBreak);
			} 
			
			// LOCKSIZE
			val = (String) obj.get("LOCKSIZE");
			if (val!=null && (val.length()>0)){
				char c = val.charAt(0);
				boolean isDef = c == 'R'; // 是否默认
				sb.append( (isDef)?"-- ":"")
				  .append("ALTER TABLE ").append(ui.getFullName()).append(" LOCKSIZE ")
				  .append( (c=='R')?"ROW":"TABLE")
				  .append(this.getSeparator(true, false))
				  .append( " -- Default:ROW")
				  .append(C_LineBreak);
			}
		}
		
		return sb.toString();
	}

	public String L_BuildTabInTabSpaceDDL(String I_Name) throws Exception{	
		
		if (!isDb2()) return "";
		
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.TABLE_ARR_SPACE), new String[]{ui.name,ui.user});
		
		StringBuilder sb = new StringBuilder();
		String COMPRESS = "";
		if (li!=null && li.size()>0){
			Map<String,Object> val = li.get(0);
			String tbsp = "";
			tbsp += StringUtils.isEmpty((String)val.get("TBSPACE"))?"":"  IN "+val.get("TBSPACE");
			tbsp += StringUtils.isEmpty((String)val.get("INDEX_TBSPACE"))?"":"  INDEX IN "+val.get("INDEX_TBSPACE");
			tbsp += StringUtils.isEmpty((String)val.get("LONG_TBSPACE"))?"":"  LONG IN "+val.get("LONG_TBSPACE");
			tbsp += ("1".equals(val.get("LOG_ATTRIBUTE")))?"  NOT LOGGED INITIALLY ":"";

			if(StringUtils.isNotEmpty(tbsp)){				
				sb.append(C_LineBreak).append((isShowDefaultSettingValue)?"":"  --").append(tbsp);
			}
			COMPRESS = (String)val.get("COMPRESSION");
		}

		// 分区表判断
		li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.TABLE_ARR_SPACE_PARTITION_EXP), new String[]{ui.name,ui.user});		
		int fqcnt = 0; String sKey = "";
		for(Map<String,Object> obj :li){			
			fqcnt++;
			String k = (String)obj.get("K");
			String n = (String)obj.get("N");

			if(fqcnt>1) sKey += ",";
			
			sKey += k.trim();
			if(n!=null && n.startsWith("Y")){
				sKey += " NULLS FIRST";
			}else{
				if(this.isShowDefaultSettingValue){
					sKey += " NULLS LAST";
				}
			} 
		}
		if(fqcnt>0){
			// 是分区表, 取键值
			sb.append(this.C_LineBreak);
			sb.append("  PARTITION BY RANGE (" + sKey + ") (" + C_LineBreak).append("     ");
			
			li = querySqlRowDataToMap(-1,
					getSql(SqlObjDefine.TABLE_ARR_SPACE_PARTITION_VAL), new String[]{ui.name,ui.user});
			int fqvalcnt = 0;
			for(Map<String,Object> obj :li){
				// [名称] [开始 结束] [表空间]  , 开始与结束只有一个即可
				fqvalcnt ++;
				
				if (fqvalcnt>1) sb.append(C_LineBreak).append("    ,");
				
				String name = (String)obj.get("NAME");
				if (StringUtils.isNotEmpty(name)){
					sb.append( "PARTITION \"" + name +"\"" );
				}
				
				if (StringUtils.isNotEmpty((String)obj.get("LV"))){
					sb.append( " STARTING FROM (" + (String)obj.get("LV") +") ");
					if("Y".equals((String)obj.get("LS"))){
						sb.append( "INCLUSIVE" );
					}else{
						sb.append( "EXCLUSIVE" );
					}
				}
				if (StringUtils.isNotEmpty((String)obj.get("HV"))){
					sb.append( " ENDING AT (" + (String)obj.get("HV") +") ");
					if("Y".equals((String)obj.get("HS"))){
						sb.append( "INCLUSIVE");
					}else{
						sb.append( "EXCLUSIVE");
					}
				}

				if (StringUtils.isNotEmpty((String)obj.get("TB"))){
					sb.append(" IN " + (String)obj.get("TB")); 
				}
				 
			}
			sb.append(")");
			
		}
		if ("B".equals(COMPRESS)){ // B 值压缩
			sb.append(this.C_LineBreak);
			sb.append("  -- COMPRESS YES VALUE COMPRESSION");
		}else if("R".equals(COMPRESS)){ // R 行压缩
			sb.append(this.C_LineBreak);
			sb.append("  -- COMPRESS YES");		
		}
        sb.append(this.C_LineBreak);
		
		return sb.toString();
	}
	

	public String P_GetViewDesc(String I_Name) throws Exception{	
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.VIEW_INFO), new String[]{ui.name,ui.user});
		if(li==null || li.size()==0) return "";
		
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		for(Map<String,Object> obj :li){
			sb.append(obj.get("VALID")).append(SqlObjDefine.META_DATA_SEQ_STR)
			  .append(obj.get("CREATE_TIME")).append(SqlObjDefine.META_DATA_SEQ_STR)
			  .append(obj.get("REMARKS"));
			i++;
			if (i<li.size())
				sb.append(SqlObjDefine.META_DATA_SEQ_STR_LINE);
			//break; // 最多只取1行
		}		
		return sb.toString();
	}
	
	
	public String P_GetViewText(String I_Name) throws Exception{	
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.VIEW_TEXT), new String[]{ui.name,ui.user});		
		if (li==null || li.size()==0) return "";
	
		StringBuilder sb = new StringBuilder();
		
		Map<String,Object> first = li.get(0);
		// 定义/删除
		sb.append("-- DROP VIEW ").append(ui.getFullName()).append(this.getSeparator(true,true));
		if (!"\"SYSIBM\",\"SYSFUN\",\"SYSPROC\",\"SYSIBMADM\"".equals(first.get("FUNC_PATH"))){
		  sb.append("-- SET CURRENT PATH =").append(first.get("FUNC_PATH"))
		    .append(this.getSeparator(true,true)); 
		}
		
		// 内容
		int row = 0;
		for(Map<String,Object> obj :li){
			if (row>0) sb.append(" ");
			sb.append(obj.get("TEXT")); 
			row++;
		}
		sb.append(this.getSeparator(true,true));
		// 是否优化
		String s = (String)first.get("PROPERTY");
		if (s.length()>13 && "Y".equals(s.substring(13, 14))){
		  sb.append("ALTER VIEW ").append(ui.user).append(".").append(ui.name)
		    .append(" ENABLE QUERY OPTIMIZATION")
		    .append(this.getSeparator(true,true)); 
		}
		// 权限
		sb.append(P_GetRightTextOfTab(I_Name));
		// 备注
		sb.append("COMMENT ON TABLE ").append(ui.getFullName())
		  .append(" IS '").append( first.get("REMARKS")==null? "": ((String) first.get("REMARKS")).replaceAll("'", "''"))
          .append("'")
		  .append(this.getSeparator(true,true));
		// 属性
		sb.append("-- Viewcheck:").append(first.get("VIEWCHECK"))
		  .append(",READONLY:").append(first.get("READONLY"))
		  .append(",VALID:").append(first.get("VALID"))
		  .append(",DEFINER:").append(first.get("DEFINER"))
		  .append(C_LineBreak);
		
		//sb.append(SqlObjDefine.META_DATA_SEQ_STR_LINE); 
		
		return sb.toString();
	}
	
	public String P_GetRightTextOfTab(String I_Name) throws Exception{
		boolean bIsSetDef = false;
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.GRANT_TAB), new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";

		StringBuilder sb = new StringBuilder();
		// 内容
		for(Map<String,Object> obj :li){
			boolean isSys = "1".equals(obj.get("ISSYS"));
			String s = "";
			String s_g = "";
			for(int i=0; i<SqlObjDefine.db2_RightNames.size() ; i++){
				String val = (String) obj.get(SqlObjDefine.db2_RightNames.get(i)+"AUTH");
				if ((i==0 && "Y".equals(val)) || (i>0 && !"N".equals(val))) {
					// controlauth 只有 Y 才有效，其它的可以是 Y/N/G					
					if (!isSys || bIsSetDef) {
						if ("G".equals(val)){
							s_g += ","+SqlObjDefine.db2_RightNames.get(i);
						}
						if ("Y".equals(val)){
							s += ","+SqlObjDefine.db2_RightNames.get(i);
						}
					}
				}
			}
			
			if (!"".equals(s)){
				sb.append("GRANT ").append(s.substring(1)).append(" ON ")
				  .append(ui.getFullName()).append(" TO ")
				  .append("U".equals(obj.get("GRANTEETYPE"))?"USER":"GROUP") // 授权类型：U-user/G-group
				  .append(" ").append(obj.get("GRANTEE"))    // 授权给 ...name...
				  .append(this.getSeparator(true,false))
				  .append(" -- ").append(obj.get("GRANTOR")) // 执行授权的用户
				  .append(C_LineBreak);
			}
			if (!"".equals(s_g)){
				sb.append("GRANT ").append(s_g.substring(1)).append(" ON ")
				  .append(ui.getFullName()).append(" TO ")
				  .append("U".equals(obj.get("GRANTEETYPE"))?"USER":"GROUP") // 授权类型：U-user/G-group
				  .append(" ").append(obj.get("GRANTEE"))    // 授权给 ...name...
				  .append(" WITH GRANT OPTION")              // 可继续授权给其它用户或组
				  .append(this.getSeparator(true,false))				  
				  .append(" -- ").append(obj.get("GRANTOR")) // 执行授权的用户
				  .append(C_LineBreak);
			}
			
		}
		
		return sb.toString();
	}

	public String P_GetRightTextOfProc(String I_Name,boolean I_IsFunction) throws Exception{
		boolean bIsSetDef = false;
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.GRANT_PROC), new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";
		
		StringBuilder sb = new StringBuilder();
		// 内容
		String sType = I_IsFunction ? "FUNCTION" : "PROCEDURE"; 
		for(Map<String,Object> obj :li){
			boolean isSys =  "1".equals(obj.get("ISSYS"));
			String val = (String) obj.get("EXECUTEAUTH");
			if (!"N".equals(val)) { 	
				if (!isSys || bIsSetDef) {
					sb.append("-- right ").append(C_LineBreak);
					sb.append("GRANT EXECUTE ON ").append(sType).append(" ")
					  .append(ui.getFullName()).append(" TO ")
					  .append("U".equals(obj.get("GRANTEETYPE"))?"USER":"GROUP") // 授权类型：U-user/G-group
					  .append(" ").append(obj.get("GRANTEE"))    // 授权给 ...name...
					  .append("G".equals(val)?" WITH GRANT OPTION":"")
					  .append(this.getSeparator(false,false))
					  .append(" -- ").append(obj.get("GRANTOR")) // 执行授权的用户
					  .append(C_LineBreak);	
				} 
			}
		}
		
		return sb.toString();
	}	
	/**
	 * 返回分隔符
	 * @param isSQLSeparator 是否sql分隔符， true-是,false-否 (表示存储过程分隔符)
	 * @param isAddLineBreak 返回的分隔符是否带回车换行
	 * @return
	 */
	public String getSeparator(boolean isSQLSeparator,boolean isAddLineBreak) {
		String s = (isSQLSeparator)? C_SQL_SEPARATOR : C_PROC_SEPARATOR;
		return s + (isAddLineBreak ? C_LineBreak:"");
	}
	
	public String P_GetAliasText(String I_Name) throws Exception{	
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.ALIAS_TEXT), new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";
		
		StringBuilder sb = new StringBuilder();
		Map<String,Object> first = li.get(0);
		
		String name = this.isDb2()?"ALIAS":(this.isOracle()?"SYNONYM":"");
		// 定义/删除
		sb.append("-- DROP ").append(name).append(" ").append(ui.getFullName()).append(this.getSeparator(true,true));
        sb.append(first.get("ALIAS_TEXT")).append(this.getSeparator(true,true)); 
		// 权限
		sb.append(P_GetRightTextOfTab(I_Name));
		// 备注
		sb.append("COMMENT ON TABLE ").append(ui.getFullName())
		  .append(" IS '").append( first.get("REMARKS")==null? "": ((String) first.get("REMARKS")).replaceAll("'", "''"))
          .append("'")
		  .append(this.getSeparator(true,true));
		return sb.toString();
	}
	
	public String P_GetNickNameText(String I_Name) throws Exception{	
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.NICKNAME_TEXT), new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";
		
		StringBuilder sb = new StringBuilder();

		int cnt = 0;
		for(Map<String,Object> obj :li){
			cnt ++;
			if (cnt>1) sb.append(this.C_LineBreak);

			// nickname
			sb.append("-- DROP NICKNAME ").append(ui.getFullName()).append(getSeparator(true,true));
			sb.append("CREATE NICKNAME ").append(ui.user).append(".").append(ui.name);
			sb.append(" FOR ").append(obj.get("SERVERNAME")).append(".");
			sb.append("REMOTE_SCHEMA").append(".").append("REMOTE_TABLE").append(getSeparator(true,false));

			sb.append(P_GetRightTextOfTab(I_Name));
			sb.append(C_LineBreak);
			sb.append("COMMENT ON TABLE ").append(ui.getFullName()).append(" IS '");
			sb.append(obj.get("REMARKS")!=null? ((String)obj.get("REMARKS")).replaceAll("'", "''") : "");
			sb.append("'").append(getSeparator(true,true));
			
			String serverName = (String)obj.get("SERVERNAME");
			
			Map<String,Object> server = querySqlOneRowDataToMap(-1,getSql(SqlObjDefine.NICKNAME_SERVER), new String[]{serverName});
			if (server!=null){
				// wrapper && wrapper-options
				Map<String,Object> wrapper = querySqlOneRowDataToMap(-1,getSql(SqlObjDefine.NICKNAME_SERVER_WRAPPER), new String[]{(String)server.get("WRAPNAME")});
				if (wrapper!=null){
					sb.append(C_LineBreak).append(C_LineBreak);
					sb.append("-- DROP WRAPPER ").append(wrapper.get("WRAPNAME")).append(getSeparator(true,true));
					sb.append("CREATE WRAPPER ").append(wrapper.get("WRAPNAME")).append(C_LineBreak);
					sb.append("  LIBRARY '").append(wrapper.get("LIBRARY")).append("'");
					
					List<Map<String, Object>> wops = querySqlRowDataToMap(-1,
							getSql(SqlObjDefine.NICKNAME_SERVER_WRAPPER_OPTION), new String[]{(String)wrapper.get("WRAPNAME")});
					int wopcnt = 0;
					for(Map<String,Object> wop :wops){
						if (wopcnt == 0) {
							sb.append(C_LineBreak);
							sb.append("  OPTIONS (").append(C_LineBreak).append("    ");
						}else{
							sb.append("   ,");
						}
						wopcnt++;
						sb.append(wop.get("OPTION")).append(" '").append(wop.get("SETTING")).append("'").append(C_LineBreak);				
					}
					if(wopcnt>0) sb.append("  )");
					sb.append(getSeparator(true,true));
				}
				
				// server && server-options
				sb.append(C_LineBreak).append(C_LineBreak);
				sb.append("-- DROP SERVER ").append(serverName).append(getSeparator(true,true));
				sb.append("CREATE SERVER ").append(serverName).append(C_LineBreak);
				sb.append("  TYPE ").append(server.get("SERVERTYPE")).append(C_LineBreak);
				sb.append("  VERSION '").append(server.get("SERVERVERSION")).append("'").append(C_LineBreak);
				sb.append("  WRAPPER ").append(server.get("WRAPNAME")).append(C_LineBreak);
				sb.append("  --AUTHORIZATION \"\"").append(C_LineBreak);
				sb.append("  --PASSWORD \"\"");
				
				// server-options
				List<Map<String, Object>> sops = querySqlRowDataToMap(-1,
						getSql(SqlObjDefine.NICKNAME_SERVER_OPTION), new String[]{serverName});
				int opcnt = 0;
				for(Map<String,Object> op :sops){
					if (opcnt == 0) {
						sb.append(C_LineBreak);
						sb.append("  OPTIONS (").append(C_LineBreak).append("    ");
					}else{
						sb.append("   ,");
					}
					opcnt++;
					sb.append(op.get("OPTION")).append(" '").append(op.get("SETTING")).append("'").append(C_LineBreak);				
				}
				if(opcnt>0) sb.append("  )");
				sb.append(getSeparator(true,true));
				
				
				// user-mapping
				List<Map<String, Object>> usermaps = querySqlRowDataToMap(-1,
						getSql(SqlObjDefine.NICKNAME_SERVER_USER_MAPPING), new String[]{serverName});
				int umcnt = 0;
				for(Map<String,Object> um :usermaps){
					if (umcnt == 0) {
						sb.append(C_LineBreak);
					}
					sb.append("-- DROP USER MAPPING FOR ").append(um.get("AUTHID"))
					  .append(" SERVER ").append(serverName).append(getSeparator(true,true));
					sb.append("CREATE USER MAPPING FOR ").append(um.get("AUTHID"))
					  .append(" SERVER ").append(serverName).append(C_LineBreak)
					  .append("  OPTIONS (").append(C_LineBreak)
					  .append("    REMOTE_AUTHID '").append(um.get("REMOTE_AUTHID")).append("',").append(C_LineBreak)
					  .append("    REMOTE_PASSWORD '").append(um.get("REMOTE_PASSWORD")).append("',").append(C_LineBreak)
					  .append("  )").append(getSeparator(true,true));
					if (umcnt==0){
						// ALTER USER MAPPING FOR FSSB SERVER FSYLGZ  OPTIONS (SET REMOTE_AUTHID 'ylsnzy_cs' )
						sb.append("-- ALTER USER MAPPING FOR ").append(um.get("AUTHID")).append(" SERVER ").append(serverName)
						  .append(" OPTIONS (SET REMOTE_AUTHID 'new-user',SET REMOTE_PASSWORD 'new-pwd')").append(getSeparator(true,true));
					}
					umcnt++;
				}
				if(umcnt>0) sb.append(C_LineBreak); 
				
			}
		}
		
		return sb.toString();
	}
		
	/**
	 * 返回存储过程列表
	 * @param I_Name
	 * @return
	 * @throws Exception
	 */
	public List<ProcInfo> P_GetProcList(String I_Name) throws Exception{
		if (I_Name!=null && !"".equals(I_Name)){
			UserInfo ui = this.getUserInfo(I_Name);
			return querySqlToBeanList(-1,getSql(SqlObjDefine.PROCEDURE_LIST
					,SqlObjDefine.PROCEDURE_LIST_WHERE
					//,(!isInludeSystemObject?SqlObjDefine.PROCEDURE_LIST_WHERE_NOSYS:"")
					), ProcInfo.class, -1,new String[]{ui.name,ui.name,ui.user});
		}
		return querySqlToBeanList(-1,getSql(SqlObjDefine.PROCEDURE_LIST,(!isInludeSystemObject?SqlObjDefine.PROCEDURE_LIST_WHERE_NOSYS:"")), ProcInfo.class, -1,null);
				//P_GetDbObjFromSqlDefine(SqlObjDefine.PROCEDURE_LIST,"",ProcInfo.class);		
	}
	public String[] StringArray_P_GetProcList(String I_TableName) throws Exception{
		return L_DaoListToDbToolStringArray(P_GetProcList(I_TableName));
	}
	
	/**
	 * 返回存储过程参数值大小
	 * @param I_Name
	 * @param I_ParamName
	 * @return
	 * @throws Exception
	 */
	public int L_GetProcParamSize(String I_Name,String I_ParamName) throws Exception{
		UserInfo ui = new UserInfo(I_Name);
		Object obj = queryOneValue(-1,getSql(SqlObjDefine.PROCEDURE_PARAM_SIZE)
				 ,new String[]{ui.name,ui.user,I_ParamName});
		return obj==null ? 0 : (Integer) obj  ;
	}

	public String P_RebindProc_RunText(String I_Name) throws Exception{
		UserInfo ui = new UserInfo(I_Name);
		String str = (String)queryOneValue(-1,getSql(SqlObjDefine.PROCEDURE_REBIND_RUNTEXT), new String[]{ui.name,ui.user});
		if (str==null) return "";
		str = str.replaceAll("#procshema#", ui.user).replaceAll("#procname#", ui.name);
		return str;
	}
	public String P_RebindProc(String I_Name) throws Exception{
		
		if (!this.isDb2()) return dbType+" 不支持 重新绑定操作！";
		UserInfo ui = new UserInfo(I_Name);
		String str = null;
		String version = getDBVersion();
		if (version.startsWith("11")){
			str = String.format("CALL SYSPROC.REBIND_ROUTINE_PACKAGE('P','%s','',''%s','')",ui.user,ui.name);
		}else{
			str = String.format("CALL SYSPROC.REBIND_ROUTINE_PACKAGE('P','%s.%s','ANY')",ui.user,ui.name);			
		}  

		int reval = this.executeUpdate(-1, str);
		if (reval==-10){
			str = "succee" + str;
		}else{			
			// 出错
			str = reval+" " + str + " "+ this.Last_Message();
		}
		
		return str;
	}
	
	public List<ProcParamInfo> P_GetProcParamsVo(String I_Name) throws Exception{	
		return P_GetDbObjFromSqlDefine(ProcParamInfo.class,I_Name, SqlObjDefine.PROCEDURE_PARAMS);		
	}
	public String[] StringArray_P_GetProcParamsVo(String I_Name) throws Exception{
		return L_DaoListToDbToolStringArray(P_GetProcParamsVo(I_Name));
	}

	public String P_GetProcText_Oracle(List<Map<String, Object>> li,StringBuilder sb) throws Exception{
		int i =0;
		for(Map<String,Object> obj :li){
			i++;
			Integer line = (Integer)obj.get("LINE"); 
			String text= (String)obj.get("PROCTEXT");
			if (i>1 && line.intValue()==1){
				sb.append(this.getSeparator(false,true)).append(this.C_LineBreak);				
			}
			sb.append(text);
		}
		sb.append(this.getSeparator(false,true));
		return sb.toString();
	}
	/**
	 * 返回存储过程/函数/触发器脚本
	 * @param I_Name
	 * @return
	 * @throws Exception
	 */
	public String P_GetProcText(String I_Name) throws Exception{	
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.PROCEDURE_TEXT), new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";
		
		boolean isHasTrigger = false;
		StringBuilder sb = new StringBuilder();
		if (isOracle()){
			return P_GetProcText_Oracle(li,sb);
		}
		
		for(Map<String,Object> obj :li){
			String schema = (String)obj.get("ROUTINESCHEMA");
			String type= (String)obj.get("PROCTYPE");
			String text= (String)obj.get("PROCTEXT");
			if (text == null ) continue;
			
			// 去除最后空行
			while (!"".equals(text) && text.endsWith("\n")) {
				text = text.substring(0,text.length()-1);
			}
			while (!"".equals(text) && text.endsWith("\r")) {
				text = text.substring(0,text.length()-1);
			}
			
			boolean isHasSchema = false;
			int pos = -1;
			if ("TRIGGER".equals(type)){
				pos = text.toUpperCase().indexOf("ON");
			}else{
				pos = text.indexOf("(");
			}
			if (pos >=10 && pos <=120){
				// create function xx.xx (), create procedures xx.xx (), create trigger xx.xx bef/af up/in/del on sch.tab
				String head = text.substring(0,pos-1);
				int dot = head.indexOf(".");
				isHasSchema = dot>=15 && dot<=32; 
			}
			
			if (!isHasSchema && "1".equals((String)obj.get("SSCHEMA"))){
				// 只有未增加模式名时，才增加设置模式名
				if (sb.length()>0) sb.append(this.C_LineBreak);
				sb.append("-- SET CURRENT SCHEMA = ").append(schema).append(this.getSeparator(false,false));
			}

			if (sb.length()>0) sb.append(this.C_LineBreak);
			
			String typeStr = "";
			boolean isFun = false;
			boolean isTri = false;
			if ("PROCEDURE".equals(type)){
				typeStr = "SPECIFIC PROCEDURE";
			}else if ("FUNCTION".equals(type)){
				typeStr = "SPECIFIC FUNCTION";
				isFun = true;
			}else if ("TRIGGER".equals(type)){
				typeStr = "TRIGGER";
				isTri = true;
				isHasTrigger = true;
			}
			
			// drop
			if (isTri) sb.append("-- ");
			sb.append("DROP ").append(typeStr).append(" ").append(isHasSchema? schema+".":"");
			sb.append((String)obj.get("SPECIFICNAME")).append(this.getSeparator(false, true));
			// text
			sb.append(text).append(this.getSeparator(false, true));
			// coment
			sb.append("COMMENT ON ").append(typeStr).append(" ").append(isHasSchema? schema+".":"");
			sb.append((String)obj.get("SPECIFICNAME"));
			sb.append(" IS '").append(obj.get("REMARKS")!=null? ((String)obj.get("REMARKS")).replaceAll("'", "''") : "");
			sb.append("'");
			sb.append(getSeparator(false,true));
			// right
			if (!isTri) sb.append(this.P_GetRightTextOfProc(I_Name, isFun));
			
		}
		
		if (isHasTrigger){
			String triggerStr = P_GetTriggerText(ui.getFullName(),false,false);
			if (triggerStr!=null && "".equals(triggerStr)){
				sb.append(this.C_LineBreak).append(triggerStr);
			}
		}
		
		return sb.toString();
	}

	/**
	 * 返回解发器脚本
	 * @param I_Name
	 * @param isTab
	 * @param isAll
	 * @return
	 * @throws Exception
	 */
	public String P_GetTriggerText(String I_Name,boolean isTab,boolean isAll) throws Exception{	
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(isTab?SqlObjDefine.TRIGGER_TEXT_TAB_ALL:SqlObjDefine.TRIGGER_TEXT_TAB_ALL)
				, new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";
		
		boolean isSetSchema = false;
		StringBuilder sb = new StringBuilder();
		for(Map<String,Object> obj :li){
			String schema = (String)obj.get("ROUTINESCHEMA");
			String name = (String)obj.get("ROUTINENAME");
			if (!isAll && !isTab){
				if (!(ui.user.equals(schema) && ui.name.equals(name))){
					continue; // 跳过自身
				}
			}
			
			// head
			if (sb.length()==0){
				if (!isTab){					
					sb.append(this.C_LineBreak).append("------------- 此表[")
					.append(obj.get("TABSCHEMA")).append(".").append("TABNAME")
					.append("]其它触发器脚本 -------------").append(this.C_LineBreak);
				}else{
					sb.append(this.C_LineBreak).append("------------- 此表[")
					.append(obj.get("TABSCHEMA")).append(".").append("TABNAME")
					.append("]所有触发器脚本 ------------- 分隔符: ").append(this.getSeparator(false, true));
				}
			}
			
			// text
			String text= (String)obj.get("PROCTEXT");
			// 去除最后空行
			while (!"".equals(text) && text.endsWith("\r\n")) {
				text = text.substring(0,text.length()-2);
			}
			while (!"".equals(text) && text.endsWith("\n")) {
				text = text.substring(0,text.length()-1);
			}
			while (!"".equals(text) && text.endsWith("\r")) {
				text = text.substring(0,text.length()-1);
			}
			
			boolean isHasSchema = false;
			int pos = text.toUpperCase().indexOf("ON");
			if (pos >=10 && pos <=120){
				// create trigger xx.xx bef/af up/in/del on sch.tab
				String head = text.substring(0,pos-1);
				int dot = head.indexOf(".");
				isHasSchema = dot>=15 && dot<=32; 
			}
			
			if (!isHasSchema && !isSetSchema && isAll){
				// 本次建立没带有模式名，没设置过，取所有时，带上可能设置的模式名
				if ("1".equals(obj.get("SSCHEMA"))){
					sb.append("-- SET CURRENT SCHEMA = ").append(schema).append(this.getSeparator(false, false));
				}
				isSetSchema = true;
			}
			
			sb.append("-- DROP TRIGGER ").append(isHasSchema? schema+".":"")
			  .append(this.getSeparator(false, true))
			  .append(text).append(this.getSeparator(false, true))
			  .append("COMMENT ON TRIGGER ").append(isHasSchema? schema+".":"")
			  .append(" IS '").append(obj.get("REMARKS")!=null? ((String)obj.get("REMARKS")).replaceAll("'", "''") : "")
			  .append(this.getSeparator(false, true))
			  .append(this.C_LineBreak);
		}
		return sb.toString();
	}
	
	/**
	 * 返回建立表的脚本
	 * @param I_Name
	 * @return
	 * @throws Exception
	 */
	public String P_GetTableDDl(String I_Name) throws Exception{
		
		if (!isOracle()) return "";
		
		UserInfo ui = this.getUserInfo(I_Name);
		List<Map<String, Object>> li = querySqlRowDataToMap(-1,
				getSql(SqlObjDefine.TABLE_DDL)
				, new String[]{ui.name,ui.user});
		if (li==null || li.size()==0) return "";
		
		return (String)li.get(0).get("TAB_DDL")+ this.getSeparator(true, false);
	}
//	public List<IndexInfo> P_GetIndexList(String I_TableName) throws Exception{		
//		List<IndexInfo> iis = new ArrayList<IndexInfo>();		
//		UserInfo ui = new UserInfo(I_TableName);
//		String sql = "SELECT INDSCHEMA, INDNAME, COLNAMES, UNIQUERULE, REMARKS, CREATE_TIME,STATS_TIME "
//				+ "FROM SYSCAT.INDEXES A "
//				+ "WHERE TABNAME = ? AND TABSCHEMA = ? "
//				+ "ORDER BY CASE WHEN UNIQUERULE='P' THEN 1 WHEN UNIQUERULE='U' THEN 2 ELSE 3 END,CREATE_TIME "+ for_read_only_with_ur;
//		List<Map<String, Object>> li = querySqlRowDataToMap(-1,sql, new String[]{ui.name,ui.user});		
//		for(Map<String,Object> obj :li){			
//			IndexInfo ii = new IndexInfo();			
//			ii.setIndexSchema( (String)obj.get("INDSCHEMA") );
//			ii.setIndexName( (String)obj.get("INDNAME") );
//			String fileName = (String)obj.get("COLNAMES");
//			if (fileName!=null && fileName.startsWith("+")){
//				fileName = fileName.substring(1);
//			}
//			ii.setFieldNames(fileName.replaceAll("\\+", ","));
//			ii.setIndexType( (String)obj.get("UNIQUERULE")  );	// P-PRIMARY,U-Unique, D-Normal	
//			ii.setRemarks( (String)obj.get("REMARKS")  );	
//			ii.setCreate_time( (String)obj.get("CREATE_TIME")  );	
//			ii.setStats_time( (String)obj.get("STATS_TIME")  );	
//			
//			iis.add(ii);
//		}		
//		return iis;
//	}	
	
	/**
	 * 
	 * @param val
	 * @return
	 * @throws Exception
	 */
	public boolean setUserAndName(String[] val) throws Exception{
		// // 从Name中按点号(.)分离出二部份，前部份放到User,后部份放到原字串Name中;有点号拆分时返回True
		boolean isChange = false;
		String user = val[0];
		String defuser = val[1];
		int dot = user.indexOf(".");
		if (dot>0){
			val[0] = user.substring(0, dot);
			val[1] = user.substring(dot+1);
			isChange = true;
		} else if ("L_GetCurUserName".equalsIgnoreCase(defuser)){
			val[1] = L_GetCurUserName();
			isChange = true;
		} else {
			val[0] = defuser;
		}
		if (val[0] != null) val[0] = val[0].toUpperCase();
		if (val[1] != null) val[1] = val[1].toUpperCase();
		return isChange;
	}
	
	public class UserInfo{
		public String user;
		public String name;
		
		/**
		 * 自动匹配用户/名称
		 * @param name , 如果带有点 . ，则分类给类的 user/name，否则表示值name
		 * @param user , 如果 name无点时，设置到实际类的 user, 如果name有点，则忽略此参数
		 * @throws Exception 
		 * 
		 */
		public UserInfo(String name,String user) throws Exception{ 
			int dot = (name!=null) ? name.indexOf(".") : -1;
			if (dot>0){
				this.user = name.substring(0, dot);
				this.name = name.substring(dot+1);
			}else{
				if ("L_GetCurUserName".equalsIgnoreCase(user)){
					this.user = L_GetCurUserName();
				}else{					
					this.user = user;
				}
				this.name = name;
			}

			if (this.user != null) this.user = this.user.toUpperCase();
			if (this.name != null) this.name = this.name.toUpperCase();
		}
		public UserInfo(String name) throws Exception{ 
			this(name,"L_GetCurUserName");
		} 
		
		public String getFullName() {
			return this.user+"."+this.name;
		}
	} 
	
	
	public static void main_oracle() throws Exception{
		//Oracle8/8i/9i数据库（thin模式） "oracle.jdbc.driver.OracleDriver" jdbc:oracle:thin:@localhost:1521:SIDorcl";
		Db2JdbcUtils ju = new Db2JdbcUtils("oracle.jdbc.driver.OracleDriver"
				 , "jdbc:oracle:thin:@localhost:1521:orcl", "liqj", "unisure"); 
		
		ju.connect();
		try{
//			ju.getResultSet(1, "select sysdate from dual");
//			String [] r = ju.getResultSetMetaData(1);
//			ju.closeResultSet(1);
//			System.out.println(Arrays.asList(r));			
//			
//			System.out.println("ju.P_GetProcList().size()=" +ju.P_GetProcList("").size() );
//			ju.setIsInludeSystemObject(1);
//			System.out.println("ju.P_GetProcList().size()=" +ju.P_GetProcList("").size() );
//			
			System.out.println(Arrays.asList(ju.StringArray_P_GetTableList("LIQJ.A2")));
			System.out.println(ju.P_GetIndexList("LIQJ.A2"));
			System.out.println(ju.P_GetRightTextOfTab("LIQJ.A2"));
			System.out.println(ju.P_GetTableDDl("LIQJ.A2"));
//			System.out.println(ju.L_BuildTabArrDDL("LIQJ.A2"));
			List<FieldInfo> fis = ju.P_GetFieldList("LIQJ.A2");
			for(FieldInfo f: fis){
				System.out.println(f);
			}
			 
//			ju.getResultSet(1, "select * from a1");
//			String[][] rd = ju.getResultSetData(1,1);
//			for(String[] s1:rd){
//				for(String s2:s1){
//					System.out.print(s2+"\t");
//				}
//				System.out.println();
//			}
		}finally{			
			ju.disConnect();
		}
	}
	public static void main(String[] args) throws Exception {
//		main_oracle();
		main_db2(args);
	}
	
	public static void main_db2(String[] args) throws Exception {
		
//		System.out.println("".format("abd :%s %s","aa1","aa2"));
//		
//
//		if (true) return;
//		Db2JdbcUtils ju2 = new Db2JdbcUtils();
//		String u =null,n = null;
//		String[] v1 = new String[]{"user.name","abcd","user.name"};
//		System.out.println(Arrays.asList(v1));
//		ju2.setUserAndName(v1);
//		System.out.println(u + "  " +n);
//		System.out.println(Arrays.asList(v1));
//		
		
		
//		if (true) return;
		
		Db2JdbcUtils ju = new Db2JdbcUtils("com.ibm.db2.jcc.DB2Driver"
				 , "jdbc:db2://127.0.0.1:50000/test2", "db2admin", "db2admin"); 
		ju.connect();
		try{
			
			// System.out.println(Arrays.asList(ju.StringArray_P_GetTableList("")));

			//System.out.println(Arrays.asList(ju.StringArray_P_GetTableList("FSSB.T1")));
			//System.out.println(Arrays.asList(ju.StringArray_P_GetFieldList("FSSB.T1")));
			//FSSB.V_MY_MD5
//			System.out.println(ju.P_GetProcText("FSSB.P_BAK_PROC"));
//			System.out.println(ju.P_GetViewText("FSSB.V_MY_MD5"));
//			System.out.println(Arrays.asList(ju.P_GetProcList("")));
			
//			System.out.println( ju.P_GetProcList("").size());
//			ju.setIsInludeSystemObject(1);
//			System.out.println( ju.P_GetProcList("").size());
			
//			System.out.println();
//			System.out.println("FSSB.YZ_YLDYZFMX_TMP");
//			List<FieldInfo> fis = ju.P_GetFieldList("FSSB.YZ_YLDYZFMX_TMP");
//			for(FieldInfo f: fis){
//				System.out.println(f);
//			}
			
//			List<IndexInfo> iis = ju.P_GetIndexList("fssb.my_md5"); //ju.P_GetIndexList("fssb.my_md5");
//			for(IndexInfo i: iis){
//				System.out.println(i);
//			}
			
//			String [] aiis = ju.StringArray_P_GetIndexList("fssb.my_md5");
//			for(String a:aiis) System.out.println(a);
//			
//			System.out.println();
//			System.out.println("FSSB.S_JDMB_XH2");
//			fis = ju.P_GetFieldList("FSSB.S_JDMB_XH2");
//			for(FieldInfo f: fis){
//				System.out.println(f);
//			}
//			String [] arr = ju.StringArray_P_GetTableList("");//"FSSB.S_JDMB_XH2");
//			for(String a:arr){
//				System.out.println( a);
//			}
//
//			String [] arrp = ju.StringArray_P_GetProcList("");
//			for(String a:arrp){
//				System.out.println( a);
//			}
			
//			ju.getResultSet(1, "select st,lx,pc,routinename,routineschema,definer,specificname,parm_signature,routine_id,return_type,origin,function_type,createdts,parm_count,deterministic,external_action,fenced,null_call,cast_function,assign_function,scratchpad,final_call,language,implementation,sourcespecific,sourceschema,ios_per_invoc,insts_per_invoc,ios_per_argbyte,insts_per_argbyte,percent_argbytes,initial_ios,initial_insts,internal_prec1,internal_prec2,remarks,internal_desc,parallel,sql_data_access,dbinfo,result_cols ,text\n" + 
//					",cardinality,parameter_style,methodimplemented,methodeffect,func_path,type_preserving,with_func_access,selectivity,overridden_methodid,subject_typeschema,subject_typename,qualifier,scratchpad_length,lib_id,spec_reg,federated,alteredts,sourceroutineid,subject_typeid,routinetype,threadsafe,result_sets,program_type,valid,text_body_offset,commit_on_return,namespace,methodproperty,newsavepointlevel,execution_control,debug_mode,codepage,encoding_scheme,last_regen_time,inheritlockrequest,sc_pc,sm from fssb.ROUTINES_LS where ROUTINESCHEMA not like 'S%'\n");
//			String [] r = ju.getResultSetMetaData(1);
//			ju.closeResultSet(1);
//			System.out.println(Arrays.asList(r));
			
//			System.out.println();
//			System.out.println("SEQSCHEMA.SEQNAME");
//			fis = ju.P_GetFieldList("SEQSCHEMA.SEQNAME");
//			for(FieldInfo f: fis){
//				System.out.println(f);
//			}
//			ju.getResultSet(1, "select * from fssb.MY_MD5 fetch first 15 row only");
//			ResultSet rs = ju.getResultSet(1);
//			ju.printResultSet(rs,true);
//			ju.CloseResultSet(1);
//			String[] rr;
//			rs = ju.getResultSet(1, "select * from FSSB.YZ_YLDYZFMX_TMP fetch first 3 row only");
//			String[] rr = ju.getResultSetMetaData(1);
//			System.out.println(Arrays.asList(rr));
//			ju.CloseResultSet(1);
//			
			//ju.getResultSet(2, "select * from syscat.tables fetch first 3 row only");
//			ju.getResultSet(2, "select * from fssb.MY_MD5 fetch first 3 row only");
//			
			
			
			//ResultSetMetaData rsmd = rs.getMetaData() ;
			
//			 rr = ju.getResultSetMetaData(2);
//			System.out.println(String.valueOf(Arrays.asList(rr)).replace(",","\n"));
//			ju.printResultSet(rs,true);
			
//			Map<String,Object> vv=ju.querySqlOneRowDataToMap(0, "select * from syscat.tables");
//			for (Map.Entry<String, Object> entry : vv.entrySet()) {  
//			    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  
//			  
//			} 
//			System.out.println(ju.P_GetNickNameText("ORCL_NICK.A2"));
//			System.out.println(Arrays.asList(ju.StringArray_P_GetProcParamsVo("FSSB.P_BAK_PROC")));
			String sql = "--aa\r\n\n/* ddfdk*/ \t \r\n\ncall p_proc(current date,'dd,ddd','w5',?)";
//			sql = "call p_ydy(a,b,'c',current 'd'ate,d,'''e,',?,?,?)";
//			sql = "call FSSB.P_BAK_PROC('zl',?)";
//			int cur = MetaDataSql.getExecuteSqlStartCharIndex(sql);
//			String sqltype=MetaDataSql.getExecuteSqlCharCount(sql, cur, 4);
//			System.out.println(cur + "  #" +sql.substring(cur) + "\n##"+sqltype);
//			ju.callProcedure(0,sql, cur);
			
			//ju.getResultSet(1, "select db2admin.F_GET_UTF8_LEN('a') result,current date from sysibm.sysdummy1");
			ju.getResultSet(1,"select * from syscat.functions where 2=2");
			String[][] rd = ju.getResultSetData(1,1000);
			for(String[] s1:rd){
				for(String s2:s1){
					System.out.print(s2+"\t");
					break;
				}
				System.out.println();
			}

			//rd = ju.getResultSetData(1,1000);
		}finally{			
			ju.disConnect();
		}

	}

}
