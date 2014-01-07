package com.cognizant.trumobi.securebrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import android.content.res.AssetManager;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

public class SB_EncrptionDBTables extends ContextWrapper {
	public SB_EncrptionDBTables(Context base) {
		super(base);
		// TODO Auto-generated constructor stub
	}
	String[] dbtableName = {"urlhistorytable","bookmarkstable","enterprisebmtable","sethomepagetable"};
	Map<String,Integer> randomDbtableName = new HashMap<String, Integer>();
	String dbName;
	String deviceId;
	String text = "{\r\n\"query\":{\r\n\"createtable\":{\r\n\"createhistory\":\"create table if not exists urlhistorytable(searchid integer not null primary key, added_on datetime, pagetitle text not null, pageurl text not null, recentflag integer default 0, favicon varchar(100), faviconflag integer default 0)\",\r\n\"createbookmarks\":\"create table if not exists bookmarkstable(bookmarkid integer not null primary key, added_on datetime, bookmark text not null, url text not null, favicon varchar(100), faviconflag integer default 0)\",\r\n\"createenterprisebm\":\"create table if not exists enterprisebmtable(enterprisebmname text not null, enterprisebmurl text not null, enterprisebmflag integer default 0, favicon varchar(100), faviconflag integer default 0)\",\r\n\"createhomepage\":\"create table if not exists sethomepagetable(sethomepagename text not null, surl text not null, acceptcookiesflag integer default 1, homepageflag integer default 0, pimsettingsflag integer default 0)\"\r\n},\r\n\"inserttable\":{\r\n\"insertenterprisebm\":\"insert into enterprisebmtable(enterprisebmname,enterprisebmurl) values (?,?)\",\r\n\"inserthomepageallfields\":\"insert into sethomepagetable(sethomepagename,surl,acceptcookiesflag,homepageflag,pimsettingsflag) values (?,?,?,?,?)\",\r\n\"insertsethomepagefields\":\"insert into sethomepagetable(sethomepagename,surl) values (?,?)\",\r\n\"insertbookmarks\":\"insert into bookmarkstable(added_on,bookmark,url,favicon,faviconflag) values (?,?,?,?,?)\",\r\n\"inserthistory\":\"insert into urlhistorytable(added_on,pagetitle,pageurl) values (?,?,?)\"\r\n},\r\n\"updatetable\":{\r\n\"updatehomepageflag\":\"update sethomepagetable set homepageflag=(?)\",\r\n\"updatehomepageurl\":\"update sethomepagetable set surl=(?)\",\r\n\"updatehomepagecookiesflag\":\"update sethomepagetable set acceptcookiesflag=(?)\",\r\n\"updatehomepageallfields\":\"update sethomepagetable set surl=(?),homepageflag=(?),pimsettingsflag=(?)\",\r\n\"updatehomepagepimflag\":\"update sethomepagetable set pimsettingsflag=(?)\",\r\n\"updatebookmarksallfields\":\"update bookmarkstable set favicon=(?),faviconflag=(?) where url=(?)\",\r\n\"updatehistoryflag\":\"update urlhistorytable set recentflag =?\",\r\n\"updatehistoryflagwithurl\":\"update urlhistorytable set recentflag=(?) where pageurl=(?)\",\r\n\"updatehistoryallfields\":\"update urlhistorytable set favicon =?,faviconflag=? where pageurl = ?\"\r\n},\r\n\"selecttable\":{\r\n\"selecthomepage\":\"select * from sethomepagetable\",\r\n\"selecthistory\":\"select * from urlhistorytable\",\r\n\"selectbookmarks\":\"select * from bookmarkstable\",\r\n\"selectenterprisebm\":\"select * from enterprisebmtable\",\r\n\"bookmarksdescorder\":\"select * from bookmarkstable order by added_on desc\",\r\n\"historydescorder\":\"select distinct * from urlhistorytable order by added_on desc\"\r\n},\r\n\"deleterows\":{\r\n\"delenterprisebm\":\"delete from enterprisebmtable\",\r\n\"delhistory\":\"delete from urlhistorytable\",\r\n\"delhistorybyid\":\"delete from urlhistorytable where searchid=(?)\",\r\n\"delhistorybyurl\":\"delete from urlhistorytable where pageurl =(?)\",\r\n\"delbookmarks\":\"delete from bookmarkstable\",\r\n\"delbookmarkbyurl\":\"delete from bookmarkstable where url =(?)\",\r\n\"delbookmarkbyid\":\"delete from bookmarkstable where bookmarkid =(?)\",\r\n\"delhomepagereset\":\"delete from sethomepagetable where sethomepagename='settohomepage'\"\r\n},\r\n\"droptable:\":{\r\n\"drophistory\":\"drop table if exists urlhistorytable\",\r\n\"dropbookmarks\":\"drop table if exists bookmarkstable\",\r\n\"dropenterprisebm\":\"drop table if exists enterprisebmtable\",\r\n\"drophomepage\":\"drop table if exists sethomepagetable\"\r\n}\r\n}\r\n}";
	public void getFromPersona(){
		 Random rand = new Random();		 
		 dbName = "";
		 deviceId = "";
		 int randamDBName = rand.nextInt(5);
		 int randamDeviceId = rand.nextInt(5);		 
		 for(int i=0;i<dbtableName.length;i++){
			 int randamTable = rand.nextInt(5);			 
			 randomDbtableName.put(dbtableName[i],randamDBName+randamDeviceId+randamTable);
		 }
		
			Log.d("textco","textco"+text);
				for(int i=0;i<dbtableName.length;i++){					
					text= text.replaceAll(dbtableName[i],""+randomDbtableName.get(dbtableName[i]));					
				 }				
				
				File file = new File(getFilesDir().toString() + "/query.json");
				Log.d("check1","check1" + file.getAbsolutePath());
				FileWriter writer;
		        try {
		        	 writer = new FileWriter(file);
		          for(int i=0;i<3;i++){
		            writer.write(text);
		            Log.d("check12","check12");
		          }
		          writer.close();
		          FileReader fr = new FileReader(file); 
		          char [] a = new char[50];
		          fr.read(a); // reads the content to the array
		          for(char c : a){
		              System.out.print(c); //prints the characters one by one		          
				Log.d("check13","check13"+c );
		          }
		          fr.close();
		        }catch(FileNotFoundException f){
		        	Log.d("check11","check11"+"file not found"+f);
		        }catch (IOException e) {
		          e.printStackTrace();
		        }

	}	      

}
