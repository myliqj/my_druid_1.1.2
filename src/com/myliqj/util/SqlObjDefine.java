package com.myliqj.util;

import java.util.ArrayList;
import java.util.List;

public class SqlObjDefine {
	public static final String META_DATA_SEQ_STR = String.valueOf( (char)8);      // 字段分隔符， 把 8 按ascii码转为字符 , ^H \b 退格 1000
	public static final String META_DATA_SEQ_STR_LINE = String.valueOf( (char)7); // 行分隔符，    把 7按ascii码转为字符 , ^G \a 响铃 0111
	
	public static final String USERNAME = "systems.current_username"; // current user
	public static final String LOGINUSER = "systems.login_username";
	public static final String TABLE_LIST = "tables.table_list"; // table/view/alias/nickname
	public static final String TABLE_LIST_WHERE = "tables.table_list.where";
	public static final String TABLE_LIST_WHERE_NOSYS = "tables.table_list.where_nosys";
	public static final String TABLE_DDL = "tables.table_ddl";
	public static final String TABLE_ARR = "tables.table_arr"; 
	public static final String TABLE_ARR_SPACE = "tables.table_arr_space"; 
	public static final String TABLE_ARR_SPACE_PARTITION_EXP = "tables.table_arr_space_partition_exp";
	public static final String TABLE_ARR_SPACE_PARTITION_VAL = "tables.table_arr_space_partition_val"; 
	public static final String FIELD_LIST = "fields.field_list"; 
	public static final String FIELD_ARR_IDENTITY = "fields.field_arr_identity";  
	public static final String INDEX_LIST = "indexes.index_list"; 
	public static final String VIEW_INFO = "views.view_info"; 
	public static final String VIEW_TEXT = "views.view_text"; 
	public static final String ALIAS_TEXT = "alias.alias_text"; 
	public static final String NICKNAME_TEXT = "nicknames.nickname_text"; 
	public static final String NICKNAME_SERVER = "nicknames.nickname_server"; 
	public static final String NICKNAME_SERVER_OPTION = "nicknames.nickname_server_option"; 
	public static final String NICKNAME_SERVER_WRAPPER = "nicknames.nickname_server_wrapper"; 
	public static final String NICKNAME_SERVER_WRAPPER_OPTION = "nicknames.nickname_server_wrapper_option"; 
	public static final String NICKNAME_SERVER_USER_MAPPING = "nicknames.nickname_server_user_mapping";  
	public static final String PROCEDURE_LIST = "procedures.proc_list";  
	public static final String PROCEDURE_LIST_WHERE = "procedures.proc_list.where";
	public static final String PROCEDURE_LIST_WHERE_NOSYS = "procedures.proc_list.where_nosys";
	public static final String PROCEDURE_REBIND_RUNTEXT = "procedures.proc_rebind_runtext";  
	public static final String PROCEDURE_PARAMS = "procedures.proc_params";  
	public static final String PROCEDURE_PARAM_SIZE = "procedures.proc_param_size"; 
	public static final String PROCEDURE_TEXT = "procedures.proc_text";   
	public static final String TRIGGER_LIST = "triggers.trigger_list";  
	public static final String TRIGGER_TEXT_TAB_ALL = "triggers.trigger_text_tab_all";
	public static final String TRIGGER_TEXT_NO_TAB_ALL = "triggers.trigger_text_no_tab_all";
	public static final String GRANT_TAB = "grants.grant_tab";  
	public static final String GRANT_PROC = "grants.grant_proc";     
    

	@SuppressWarnings("serial")
	public static final List<String> db2_RightNames = new ArrayList<String>(){
		{
			add("CONTROL");
			add("ALTER");
			add("INDEX");
			add("INSERT");
			add("DELETE");
			add("SELECT");
			add("UPDATE");
			add("REFERENCES");
		}
	};
	public static String getNumFormat(int num,String seq){
		String s = "";
		for(int i=1;i<=num;i++){
			s += "%s"+seq;
		}
		return s;
	}
	
	public static String getNumFormat(int num){
		return getNumFormat(num,META_DATA_SEQ_STR);
	}
}
