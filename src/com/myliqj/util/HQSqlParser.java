package com.myliqj.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.myliqj.db.dao.SQLDesc;

/**
 * HophyQuery工具 的SQL语句分析部份 unt_SQLParser.pas
 * @author liqj 2018
 */
public class HQSqlParser {

	public String f_Sqls;      // 总体SQL语句
	public int f_CurPos;       // 字符当前位置
	public int f_Length;       // 语句总长度
	public char f_Separator;   // 语句分隔符
	
	
	
	public HQSqlParser(String i_Sqls,char i_Separator){
		this.f_CurPos = -1;
		this.f_Sqls = i_Sqls;
		this.f_Length = f_Sqls.length();
		this.f_Separator = i_Separator;
	}
	
	public int P_GetCurChrCount(){
		return f_CurPos;
	}
	
	public boolean P_HasNext(){
		return f_CurPos < f_Length-1;
	}
	
	public char P_GetNext(){
		if (P_HasNext()){
			f_CurPos++;
			return f_Sqls.charAt(f_CurPos);
		}
        return '\0';
	}
	
	public void P_SetBack(){
	  if (f_CurPos > 0)
		  f_CurPos = f_CurPos - 1;
	}
	
	public static boolean isDigit(final char argChar) {
        return '0' <= argChar && argChar <= '9';
    }

	public static boolean isIdentifier(final char c) {
        return ('0'<=c && c<='9') || ('A'<=c && c<='Z') || ('a'<=c && c<='z') || (c=='_');
    }
		
	public List<SQLDesc> P_GetSqlList(int i_curpos){
		
		List<SQLDesc> sqls = new ArrayList<SQLDesc>(); // sql语句列表
		Set<String> params = null; // 参数名列表
		
		char fChar,fNextChar;
		StringBuilder curSql = new StringBuilder();
		String sSqlType = "";
		String str = "";
		String sqlTitle = "";
		boolean isSetSqlType = false;
		boolean isLegalSql = false;
		boolean isIncludeMinusComment = true; // 是否保留减号注释, true-保留，false-不保留
		// oracle 最好保留 /**/ 因为可能使用指定选项
		
		boolean isSplitSql = false;
		
		int iCurSqlBegPos=-1;
		int iCurSqlEndPos=0;
		int iSquareBracketCount=0;
		int iParenthesisCnt=0;
		int iRelaPos = 0; // 正式SQL语句开始位置
		boolean isStartSql = false;
		
		TMinusCommentState minusCommentState = null;
		TCommentState commentState = null;
		TStringState stringState = null;
		TTitleState titleState = null;
		TParamState paramState = null;
		
		while (P_HasNext()) {
			fChar = P_GetNext();

			if (!isLegalSql && ( ('A'<=fChar && fChar<='Z') || ('a'<=fChar && fChar<='z') )) { // 大小写				
				isLegalSql = true; // 说明有字母
			}
					
			if (!isSetSqlType && !isIdentifier(fChar)){
				sSqlType = sSqlType.trim();
				if (!sSqlType.isEmpty()){
					// 求出SQL语句的类型 即 select / with / update / delete 等
					isSetSqlType = true;
				}
			}
			
			
			if (fChar == '-'){
				// 注释处理 : 注释有二类 ，单行-- ，多行 /**/
				fNextChar = P_GetNext();
				if (fNextChar == '-'){
					// 单行注释
					if (minusCommentState ==null) minusCommentState = new TMinusCommentState(this);
					str = minusCommentState.P_WallThrough();
					if (isIncludeMinusComment) curSql.append("--" + str); // 需要注释，增加到当前SQL语句中
				}else{
					// 不是注释，回退 fNextChar ，由下次主循环处理
					P_SetBack();
					curSql.append(fChar);
				}
			}else if (fChar == '/'){
				// 多行注释 /* ... */
				fNextChar = P_GetNext();
				if (fNextChar=='*'){ 
					if (commentState ==null) commentState = new TCommentState(this);
					curSql.append("/*" + commentState.P_WallThrough());
				}else if (fChar == f_Separator){ // 支持使用  / 做语句分隔符
					// L_Split_SQL();
					isSplitSql = true;
				}else{ 
					P_SetBack();
					curSql.append(fChar);
				}
			}else if (fChar == '#'){  
				//<> # 结果集名称定义 ，必须在正式语句前才有效
				if ("".equals(sqlTitle) && !isLegalSql){ // 在SQL语句前面的才是有效的Title
					if (titleState ==null) titleState = new TTitleState(this);
					sqlTitle = titleState.P_WallThrough(); 
				}else{
					curSql.append(fChar);
				}
			}else if ("[]()".indexOf(fChar)>=0){   
				// <> 括号，有[] () , 分隔符不允许在这二个括号内
				curSql.append(fChar);
				if (fChar=='[') iSquareBracketCount++;
				if (fChar==']') iSquareBracketCount--;
				if (fChar=='(') iParenthesisCnt++;
				if (fChar==')') iParenthesisCnt--;
				if (iSquareBracketCount<0) iSquareBracketCount = 0;
				if (iParenthesisCnt<0) iParenthesisCnt = 0;			
			}else if (fChar == ':'){  
				// <> 参数定义前缀，如果程序段中要使用做标签定义(db2)，在 : 后不要直接有 ? * 或标标识符号。
				//    参数有三类，普通 :xx , 单值引用 :?title.col , 列表引用 col in (:*.title.col)                      
				curSql.append(fChar);

				if (paramState ==null) paramState = new TParamState(this);
				str = paramState.P_WallThrough();
				if (str!=null && !str.isEmpty()){
					curSql.append(str);
					if (params==null) params = new LinkedHashSet<String>(); // 保持插入顺序  new HashSet<String>();
					params.add(str); // 参数仅简单记录名称，多个名称只保留一个
				}
			}else if ("'\"`".indexOf(fChar)>=0){ //  "\'\"`" 与  "'\"`"  相同 
				// <>字符定义有： ' " ` 三类
				curSql.append(fChar);
				if (stringState ==null) stringState = new TStringState(this,'\0');
				stringState.setQuoteMark(fChar);
				curSql.append(stringState.P_WallThrough());
			}else if (fChar == f_Separator){        
				// <>分隔符 ,目前仅支持单个字符
				// L_Split_SQL();
				isSplitSql = true;
			}else {
				curSql.append(fChar);
				if (!isSetSqlType){
					// 没遇到首个非标识符时，拼接SQL语句类型 select / with / update / delete 等
					sSqlType = sSqlType + fChar;
					if (!isStartSql){
						iRelaPos = P_GetCurChrCount();
						isStartSql = true;
					}
				}		        	
			}
			
			if (isSplitSql){
				// 如果已找到分隔符号，获取上句SQL语句
				if (iParenthesisCnt == 0 && iSquareBracketCount == 0){ // 不在特殊符号 [] () 内的情况才认为是本语句结束，即分隔符不在在有效的 []() 内。
					sSqlType = sSqlType.toUpperCase(); 
					boolean isQuery = "SELECT,WITH,VALUES,".indexOf(sSqlType+",")>=0; // 是否查询语句
					
					String sql = curSql.toString();
					if (isLegalSql && sql!=null && !sql.isEmpty()){
						// 一定是有字母才认为有正常语句，防止继续多个分隔符时无正常语句
						SQLDesc sqldesc = new SQLDesc(sql,f_Sqls.substring(iCurSqlBegPos+1,iCurSqlEndPos+1),isQuery,sSqlType,params,sqlTitle
								,iCurSqlBegPos+1,iCurSqlEndPos,iRelaPos);						
						sqls.add(sqldesc);
						iCurSqlBegPos = iCurSqlEndPos + 1;
					}
					
					// init
					isLegalSql = false;
					sSqlType = "";
					sqlTitle = "";
					isSetSqlType = false;
					isStartSql = false;
					curSql.setLength(0);
					params = null;
				}else{
					curSql.append(fChar);
				}
				
				isSplitSql = false;
			}
			
			
			iCurSqlEndPos = P_GetCurChrCount(); // 记录当前语句结束在总SQL语句中位置索引
		}
		
		// 最后一个语句
		sSqlType = sSqlType.toUpperCase(); 
		boolean isQuery = "SELECT,WITH,VALUES,".indexOf(sSqlType+",")>=0;
		
		String sql = curSql.toString();
		if (/*isLegalSql && */sql!=null && !sql.isEmpty()){
			iCurSqlBegPos = iCurSqlBegPos + (sqls.size()>0?1:0); // + 1 是为了语句中不包含分隔符
			if (iCurSqlBegPos<0) iCurSqlBegPos = 0;              // 初始化为－1，防止截取出错
			SQLDesc sqldesc = new SQLDesc(sql,f_Sqls.substring(iCurSqlBegPos,iCurSqlEndPos+1),isQuery,sSqlType,params,sqlTitle
					,iCurSqlBegPos
					,iCurSqlEndPos,iRelaPos);						
			sqls.add(sqldesc);
		}
				
		return sqls;
	}
	
	
	
	
	
	abstract class TSQLState {
		StringBuilder F_Str;
		HQSqlParser F_Divider;
		public TSQLState(HQSqlParser I_Divider){
			F_Divider = I_Divider;
			F_Str = new StringBuilder();
		}		
		public abstract String P_WallThrough();
	}
	
	/**
	 * 单行注释
	 * @author liqj 2018-03-24
	 *
	 */
	class TMinusCommentState extends TSQLState { 
		public TMinusCommentState(HQSqlParser I_Divider) {
			super(I_Divider);
		}
		public String P_WallThrough(){
			F_Str.setLength(0);
			//F_Str.append("--");
			char c,c2='\0';
			// 本过程返回内容仅是注释内容+\r\n，不包括 --,但包括最后的换行(回车换行) -- 要包括最后的 \r\n
			// 如果有 \n 或 \r\n 都认为结束
			while (F_Divider.P_HasNext()){
				c = F_Divider.P_GetNext();
				if ( c == '\n' ){
					if ( c2 == '\r' ){
						//F_Divider.P_SetBack();F_Divider.P_SetBack();
						F_Str.append(c2).append(c);
						return F_Str.toString();
					}else{
						//F_Divider.P_SetBack();
						F_Str.append(c);
						return F_Str.toString();
						//F_Str.append(c); 不如仅认 \r\n 结束，则取二行，不要上面二行
						//c2 = '\0';	
					}
				}else if ( c == '\r' ){
					if ( c2 != '\0' ){
						F_Str.append(c2);
					}
					c2 = c;
				}else{
					if ( c2 != '\0' ){
						F_Str.append(c2);
					}
					F_Str.append(c);
					c2 = '\0';
				}
			}
			return F_Str.toString();
		}; 
	}
	
	class TCommentState extends TSQLState { 
		public TCommentState(HQSqlParser I_Divider) {
			super(I_Divider);
		}
		public String P_WallThrough(){
			F_Str.setLength(0);
			//F_Str.append("/*");
			char c,c2='\0';
			// 本过程返回内容仅是注释内容+结束 */，不包括前面 */
			while (F_Divider.P_HasNext()){
				c = F_Divider.P_GetNext();
				F_Str.append(c);
				if ( c == '/' ){
					if ( c2 == '*' ){
						return F_Str.toString();
					}else{
						c2 = '\0';	
					}
				}else if ( c == '*' ){
					c2 = c;
				}else{
					c2 = '\0';
				}
			}
			return F_Str.toString();
		}; 
	}
	

	class TStringState extends TSQLState {
		char F_QuoteMark;
		public TStringState(HQSqlParser I_Divider,char I_QuoteMark) {
			super(I_Divider);
			F_QuoteMark = I_QuoteMark;
		}
		
		public void setQuoteMark(char I_QuoteMark){
			F_QuoteMark = I_QuoteMark;
		}
		public String P_WallThrough(){
			F_Str.setLength(0);
			//F_Str.append(I_QuoteMark);
			char ch;
			boolean isQuoteStr = false;
			// 本过程返回内容仅是字符内容+后面标识， 不包括字符前标识
			while (F_Divider.P_HasNext()){
				ch = F_Divider.P_GetNext();
				F_Str.append(ch);
				if ( ch == F_QuoteMark ){
					ch = F_Divider.P_GetNext();
					if ( ch != F_QuoteMark ){
						if ( ch != '\0'){
							F_Divider.P_SetBack();
						}
						
						if (!isQuoteStr){
							break;
						}
					}else{
						F_Divider.P_SetBack();
					}					
					isQuoteStr = !isQuoteStr; // 字串中的二个 ' 当作是一个 ' ，所以标识还在字串中 
				}
			}
			return F_Str.toString();
		}; 
	}

	

	class TTitleState extends TSQLState { 
		public TTitleState(HQSqlParser I_Divider) {
			super(I_Divider);
		}
		public String P_WallThrough(){
			// 标题指 #号开头，:冒号结束			
			F_Str.setLength(0);
			//F_Str.append('#');
			char ch; 
			// 本过程返回内容仅是标题内容， 不包括标题后面的 :
			while (F_Divider.P_HasNext()){
				ch = F_Divider.P_GetNext();
				if ( ch == ':' ){
					 break;
				}else{
					F_Str.append(ch);					
				}
			}
			return F_Str.toString();
		}; 
	}
	
	

	class TParamState extends TSQLState { 
		public TParamState(HQSqlParser I_Divider) {
			super(I_Divider);
		}
		public String P_WallThrough(){
			// 参数指 :冒号开头，后面带标识符			
			F_Str.setLength(0);
			//F_Str.append(':');
			char c; 
			// 本过程返回内容仅是参数名称， 可以做参数名称包括数字、字母下划线、点、汉字, 引用参数标识 ?和*
			while (F_Divider.P_HasNext()){
				c = F_Divider.P_GetNext();
				if ( ( !(    ('0'<=c && c<='9') || ('A'<=c && c<='Z') || ('a'<=c && c<='z') || (c=='_')
						  || (c=='.') || (c=='?') || (c=='*') 
						  || ('\u4e00'<=c && c<='\u9fa5') 
						)       // 非     标识符,引用参数,点或汉字
				     ) 
				  || (c=='\r' || c=='\n' || c=='\t' || c=='\b') // 或  换行、空格、Tab键
				  )
				{
					F_Divider.P_SetBack();
					break;
				}else{
					F_Str.append(c);					
				}
			}
			return F_Str.toString();
		}; 
	}
	 
	
	public static void main(String[] args) throws Exception {
	  String sql = "#aa:select * from/*fd;afd*/ s_jdmb;;;-- afffddf \r\n#r:select * from ys_grjbxx"
		  		+ ";update aa set 1='2' and b=:aa and x=:?11--aa232中 \r\n and ee=:bb ;;#中a文: select * from t_im_dir a"
		  		+ ";#我来看年-之后的无所:select 1 ;";
	  //sql = "select * from aa;call p_aa(?);";
	  HQSqlParser tool = new HQSqlParser(sql,';');
	  
	  List<SQLDesc> ss = tool.P_GetSqlList(0);
	  System.out.println("sql-cnt:" + ss.size());
	  System.out.println(Arrays.asList(ss));
	  
	  
	  sql = "update ys_grjbxx set ff='dfd',bz='fdfd;dafd' where grbh in (select ';' from sysibm.sysdummy1);";
	  sql+="-- 判断是否应该处理\ndelete from ys_dd where aa='dfdf';";
	  sql+="select * from s_jdmb left join bb on a=1 where exists(select * from s_jdmb where bb=c);";
		
		sql += "with aa as(select 1 f from dual) /*不处理*/ select * from aa;";
	  
      tool = new HQSqlParser(sql,';'); 
	  ss = tool.P_GetSqlList(0);
	  System.out.println("\n\n2-sql-cnt:" + ss.size());
	  System.out.println(Arrays.asList(ss));
	  
	  
	  //System.out.println(HQSqlParser.formatCreateTable("create table a(aa int\n,ff char);"));
	  
	}
	
}
