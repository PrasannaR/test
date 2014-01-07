package com.cognizant.trumobi.securebrowser;
 
import java.io.File;
 
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
 
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import com.TruBoxSDK.TruBoxDatabase;
 
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebStorage;
import android.webkit.WebViewDatabase;
import android.widget.Toast;
 
public class SB_ClearBrowsingData extends CordovaPlugin{
 
       public static final String SUCCESS_PARAMETER = "success";
       public static final String PASS = "Password";
       public static int addbookmarkflag = 0;
 
       @Override
       public boolean execute(String action, JSONArray data,
                     CallbackContext callbackContext) {
              Log.d("Clear Cache",
                           "Hello, this is a native function called from PhoneGap/Cordova!");
              // only perform the action if it is the one that should be invoked
              if (action.equals("clearcache")) {
                     String resultType = null;
                     try {
                           resultType = data.getString(0);
 
                           /*
                           *
                            * clearing cache
                           */
                           CordovaWebView w = new CordovaWebView(cordova.getActivity());
                           w.clearCache(true); // cordova method to clear app cache
 
                           //clear the local storage and webdbs other than dbs of origin file://
                         //  clearWebDB();
                           
                           WebStorage wSt = WebStorage.getInstance();
                           wSt.deleteAllData();
                           clearLocalStorage();

 
                    // wSt.deleteOrigin(origin);
                           // clearing webview db
 
                           WebViewDatabase webDb = WebViewDatabase.getInstance(cordova
                                         .getActivity());
                           webDb.clearFormData();
                           webDb.clearHttpAuthUsernamePassword();
                           webDb.clearUsernamePassword();
                          
                           //wSt.deleteOrigin("http://www.html5rocks.com");
 
                     }
 
                     catch (Exception ex) {
                           Log.d("Clear Cache", ex.toString());
                     }
                     if (resultType.equals(SUCCESS_PARAMETER)) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs"));
                           return true;
 
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR, "Oops, Error :("));
                           return true;
                     }
              } else if (action.equals("clearcookies")) {
                     String resultType = null;
                     try {
                           resultType = data.getString(0);
 
                           /*
                           *
                            * clear cookies
                           */
                           CookieSyncManager.createInstance(cordova.getActivity());
                           CookieManager cm = CookieManager.getInstance();
 
                           // checking to see whether cookies are present in the app
                           if (cm.hasCookies()) {
                                  //System.out.println("cookies present");
                           } else {
                                  //System.out.println("cookies not present");
                           }
 
                           cm.removeSessionCookie();
                           cm.removeAllCookie();
 
                           // checking to see whether cookies are cleared in the app
                           if (cm.hasCookies()) {
                                 // System.out.println("failed clearing cookies");
                           } else {
                                 // System.out.println("success clearing cookies");
                                  Log.d("Clear Cache", "success clearing cookies");
 
                           }
 
                     }
 
                     catch (Exception ex) {
                           Log.d("Clear Cache", ex.toString());
                     }
                     if (resultType.equals(SUCCESS_PARAMETER)) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs"));
                           return true;
 
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR, "Oops, Error :("));
                           return true;
                     }
              } else if (action.equals("disablecookies")) {
                     Log.d("disable",
                                  "Hello, this is a native function called from PhoneGap/Cordova!");
                     String resultType = null;
                     try {
                           resultType = data.getString(0);
 
                           CookieSyncManager.createInstance(cordova.getActivity());
                           CookieManager cm = CookieManager.getInstance();
                           if (cm.hasCookies()) {
                                  cm.removeSessionCookie();
                                  cm.removeAllCookie();
                           }
                           cm.setAcceptCookie(false);
                           Log.d("disable", "cookies disabled..");
 
                     }
 
                     catch (Exception ex) {
                           Log.d("disable", ex.toString());
                     }
                     if (resultType.equals(SUCCESS_PARAMETER)) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs...1"));
                           return true;
 
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR, "Oops, Error :("));
                           return true;
                     }
              } else if (action.equals("enablecookies")) {
                     Log.d("enable",
                                  "Hello, this is a native function called from PhoneGap/Cordova!");
                     String resultType = null;
                     try {
                           resultType = data.getString(0);
 
                           CookieSyncManager.createInstance(cordova.getActivity());
                           CookieManager cm = CookieManager.getInstance();
                           if (!cm.acceptCookie()) {
                                  Log.d("enable", "cookies disabled..");
                                  Log.d("enable", "enabling cookies..");
                                  cm.setAcceptCookie(true);
                           } else {
                                  Log.d("enable", "cookies already enabled");
                           }
                     }
 
                     catch (Exception ex) {
                           Log.d("enable", ex.toString());
                     }
                     if (resultType.equals(SUCCESS_PARAMETER)) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs...1"));
                           return true;
 
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR, "Oops, Error :("));
                           return true;
                     }
              } else if (action.equals("shortToast")) {
                     Log.d("toast", "Short Toast call to cordova plugin!");
                     String message = null;
 
                     try {
                           message = data.getString(0);
                     } catch (JSONException ex) {
                           // TODO Auto-generated catch block
                           Log.d("toast", ex.toString());
                     }
 
                     if (message != null && message.length() > 0) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs...1"));
                           Toast.makeText(cordova.getActivity().getApplicationContext(),
                                         message, Toast.LENGTH_SHORT).show();
                           return true;
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR,
                                         "Oops, Error :Should provide a non-empty string"));
                           return true;
                     }
              } else if (action.equals("longToast")) {
                     Log.d("toast", "Long Toast call to cordova plugin!");
                     String message = null;
 
                     try {
                           message = data.getString(0);
                     } catch (JSONException ex) {
                           // TODO Auto-generated catch block
                           Log.d("toast", ex.toString());
                     }
 
                     if (message != null && message.length() > 0) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs...1"));
                           Toast.makeText(cordova.getActivity().getApplicationContext(),
                                         message, Toast.LENGTH_LONG).show();
                           return true;
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR,
                                         "Oops, Error :Should provide a non-empty string"));
                           return true;
                     }
              } else if (action.equals("isbookmarked")) {
                     Log.d("isbookmarked", "Is Bookmarkded call to cordova plugin!");
                     String message = null;
 
                     try {
                           message = data.getString(0);
                           addbookmarkflag = Integer.parseInt(message);
                           Log.d("isbookmarked", message);
                     } catch (JSONException ex) {
                           // TODO Auto-generated catch block
                           Log.d("isbookmarked", ex.toString());
                     }
 
                     if (message != null && message.length() > 0) {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.OK, "successs...1"));
                           return true;
                     } else {
                           callbackContext.sendPluginResult(new PluginResult(
                                         PluginResult.Status.ERROR,
                                         "Oops, Error :Should provide a non-empty string"));
                           return true;
                     }
              }
              else if(action.equals("getkey")){ 
      	 String resultType = null;
      	 String str="suceesss";
      	 JSONArray pageDetails = null;
           try {
                 resultType = data.getString(0);
                 String pageName = data.getString(1);
                 String encriptKey = TruBoxDatabase.getString();
                 String dbname = TruBoxDatabase.getHashValue("TruBrowser", cordova.getActivity().getApplicationContext() );
                 pageDetails = new JSONArray("[ {\"pageName\": \""+pageName+"\", \"dbpass\":\""+encriptKey+"\" , \"dbname\" : \""+ dbname+ "\" } ]");
                Log.d("getkey", "plugin called");
           }
           catch (Exception ex) {
                 Log.d("getkey", ex.toString());
           }
           if (resultType.equals("success")) {
                 callbackContext.sendPluginResult(new PluginResult(
                               PluginResult.Status.OK, pageDetails));
                 return true;

           } else {
                 callbackContext.sendPluginResult(new PluginResult(
                               PluginResult.Status.ERROR, "Oops, Error :("));
                 return true;
           }
      	
              }
              return false;
       }
 
 
   /*    public void clearWebDB() {
              File cache = cordova.getActivity().getCacheDir();
              File appDir = new File(cache.getParent());
              if (appDir.exists()) {
                     String[] children = appDir.list();
                     for (String s : children) {
                           if (s.equals("app_database")) {
                                  appDir = new File(appDir, s);
                                  if (appDir.exists()) {
                                         String[] children1 = appDir.list();
                                         WebStorage wSt = WebStorage.getInstance();
                                         for (int i = 0; i < children1.length; i++) {
                                                appDir = new File(appDir, children1[i]);
                                                System.out.println(children1[i]+"...before");
                                         //     if (appDir.isDirectory()) {
                                                       System.out.println(children1[i]);
                                                       String originString = children1[i].replaceFirst("_",
                                                                     "://");
                                                       int ind = originString.indexOf("_");
                                                       if (ind > 0) {
                                                              originString = originString.substring(0, ind);
                                                       }
                                                       appDir = new File(appDir, children1[i]);
                                                       if (originString.startsWith("http")) {
                                                              System.out.println(originString);
                                                             
                                                              wSt.deleteOrigin(originString);
                                                              // clearing webview db
                                                       }
                                         //     }
                                         }
                                  }
                           }
                     }
              }
       }*/
      
       public void clearLocalStorage(){
           File cache = cordova.getActivity().getCacheDir();
     File appDir = new File(cache.getParent());
    
     if (appDir.exists()) {
         String[] children = appDir.list();
         for (String s : children) {
            if (s.equals("app_database")) {
                  appDir = new File(appDir, s);
                  if (appDir.exists()) {
                         String[] children1 = appDir.list();
                         for(String l: children1){
                               if(l.equals("localstorage")){
                                      appDir=new File(appDir,l);
                                      deleteDir(appDir);
                              
                               }
                         }
                  }
            }
         }
     }
           Log.d("message", appDir.toString());
          
    }
   
    public  boolean deleteDir(File dir)
    {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

 
 
}
