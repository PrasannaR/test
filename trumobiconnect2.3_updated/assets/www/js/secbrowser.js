//Global variables
var db = null;
var defaultIcon = "images/globe_icon.png";
var HUrlStr;
var addToBookMarkInitialURL;
var pimCheckFlag = 1;

//defer db initialisation
function definitDatabase(homepage,pimsettings,key,dbname){
	var t = setTimeout("initDatabase('" + homepage + "','" + pimsettings + "','" + key + "','"+dbname+"')",500);
}

//Creates db and tables and sets default values for browser preferences
function initDatabase(homepage,pimsettings,key,dbname){
	if ( db == null )
	{
		try {
			if (!window.openDatabase) {
				// If database cannot be opened, show an alert msg
				function alertDismissed() {
				}
				navigator.notification.alert(
				    'Database not supported in the device',  // message
				    alertDismissed,         // callback
				    'Database not supported',            // title
				    'OK'                  // buttonName
				);
			} else {
				//create a db with dbname and key
				try {
					db = window.sqlitePlugin.openDatabase({
						name: dbname,
						key: key
					});
				} catch(e) {
				}
			}
		}
		catch(e){
		}
	}

	//create history table
	db.transaction(function (transaction) {
		transaction.executeSql('CREATE TABLE IF NOT EXISTS URLHistory(SearchId INTEGER NOT NULL PRIMARY KEY, added_on DATETIME, PageTitle TEXT NOT NULL, PageUrl TEXT NOT NULL, RecentFlag INTEGER default 0, FavIcon VARCHAR(100), FavIconFlag INTEGER default 0)', [], nullHandler, errorHandler);
	}, errorHandler, successCallBack);
	function successCallBack() {
	}

	//create bookmarks table
	db.transaction(function (transaction) {
		transaction.executeSql('CREATE TABLE IF NOT EXISTS Bookmarks(BookmarkId INTEGER NOT NULL PRIMARY KEY, added_on DATETIME, Bookmark TEXT NOT NULL, Url TEXT NOT NULL, FavIcon VARCHAR(100), FavIconFlag INTEGER default 0)', [], nullHandler, errorHandler);
	}, errorHandler, successCallBack);

	//create enterprise bookmarks table
	db.transaction(function (transaction) {
		transaction.executeSql('CREATE TABLE IF NOT EXISTS EnterpriseBM(EnterpriseBMName TEXT NOT NULL, EnterpriseBMUrl TEXT NOT NULL, EnterpriseBMFlag INTEGER default 0, FavIcon VARCHAR(100), FavIconFlag INTEGER default 0)', [], nullHandler, errorHandler);
	}, errorHandler, successCallBack);

	//create preferences table
	db.transaction(function (transaction) {
		transaction.executeSql('CREATE TABLE IF NOT EXISTS SetHomePage(SetHomePageName TEXT NOT NULL, SUrl TEXT NOT NULL, AcceptCookiesFlag INTEGER default 1, HomePageFlag INTEGER default 0, PIMSettingsFlag INTEGER default 0)', [], nullHandler, errorHandler);
	}, errorHandler, successCallBack1);

	var shouldInsert = 0;
	function successCallBack1()
	{
		//Set default values for preferences if creation of tables successful
		db.transaction(function (transaction) {
			var sqlite = 'SELECT * FROM SetHomePage';
			transaction.executeSql(sqlite, [],
					function (transaction, result) {
				if(result.rows.length == 0){
					shouldInsert = 1;
				}
			}, errorHandler);
		}, errorHandler, insertInitSettings);


	}
	function insertInitSettings(){
		//set default values for the preferences for first time
		if ( shouldInsert == 1 ){
			var name="";
			var url="";
			var AcceptCookiesFlag=1;
			var HomePageFlag=0;
			var PIMSettingsFlag=0;
			db.transaction(function (transaction) {
				transaction.executeSql('INSERT INTO SetHomePage(SetHomePageName,SUrl,AcceptCookiesFlag,HomePageFlag,PIMSettingsFlag) VALUES (?,?,?,?,?)', [name, url,AcceptCookiesFlag,HomePageFlag,PIMSettingsFlag], nullHandler, errorHandler);
			}, errorHandler, successCallBack2);
		}
		else{
			successCallBack2();
		}
	}
	function successCallBack2() {
		if(pimsettings == "true"){
			//if pimsettings true update the preferences
			if ( homepage.length > 4 )
			{
				db.transaction(function (transaction) {
					transaction.executeSql('UPDATE SetHomePage SET SUrl=(?),HomePageFlag=(?),PIMSettingsFlag=(?)', [homepage,1,1], nullHandler, errorHandler);
				}, errorHandler, successCallBack);
			}
			else{
				db.transaction(function (transaction) {
					transaction.executeSql('UPDATE SetHomePage SET PIMSettingsFlag=(?)', [0], nullHandler, errorHandler);
				}, errorHandler, successCallBack);
			}
		}
	}

	//Success Message
	function nullHandler() {
	}
	function successCallBack() {
	}
	function errorHandler(e) {
	}

}

//creates or opens database and initialises db handler
function openTruBrowserTables(key,dbname) {
	try {
		if (!window.openDatabase) {
			// If database cannot be opened, show an alert msg
			function alertDismissed() {
			    // do nothing
			}

			navigator.notification.alert(
			    'Database not supported in the device',  // message
			    alertDismissed,         // callback
			    'Database not supported',            // title
			    'OK'                  // buttonName
			);
		} else {
			//create a db with dbname and key
			try {
				db = window.sqlitePlugin.openDatabase({
					name: dbname,
					key: key
				});
			} catch(e) {
			}
		}
	}
	catch(e){
	}

	//Error Handler - return Error Name and Code
	function errorHandler(transaction, error) {
	}

	//Success Message
	function successCallBack() {
	}

	//Null Handler
	function nullHandler() {}
}


//Displays Home page if set or displays recently viewed page
function ShowHomePage(dbcall,dbname) {
	//Set default values for preferences
	db.transaction(function (transaction) {
		var sqlite = 'SELECT * FROM SetHomePage';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {
			if(result != null && result.rows != null) {
				for(var i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);

					if(row.HomePageFlag == 1) {
						var url = row.SUrl;
						var comparator = url.slice(0, 4);
						if(comparator != "http") {
							url = "http://" + url;
						}
						searchQuery(url, false);
					} else {
						cordova.exec(getKeyResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "getkey", ["success", "recently-closed-page"]);
					}
					return;
				}
			}
		}, errorHandler);
	}, errorHandler, nullHandler);

	//Error Handler
	function errorHandler() {}

	//Null Handler
	function nullHandler() {}
}

//Displays enterprise corporate bookmarks
function EnterpriseBMshow() {
	EnterpriseBMDel();
	db.transaction(function (transaction) {
		transaction.executeSql('INSERT INTO EnterpriseBM(EnterpriseBMName,EnterpriseBMUrl) VALUES (?,?)', ["Cognizant", "http://cognizant.com"], nullHandler, errorHandler);
		transaction.executeSql('INSERT INTO EnterpriseBM(EnterpriseBMName,EnterpriseBMUrl) VALUES (?,?)', ["Cognizant MyPay", "https://mypay.cognizant.com"], nullHandler, errorHandler);
		transaction.executeSql('INSERT INTO EnterpriseBM(EnterpriseBMName,EnterpriseBMUrl) VALUES (?,?)', ["Cognizant Mail", "https://mail.cognizant.com"], nullHandler, errorHandler);
		transaction.executeSql('INSERT INTO EnterpriseBM(EnterpriseBMName,EnterpriseBMUrl) VALUES (?,?)', ["One Cognizant", "https://onecognizant.cognizant.com"], nullHandler, errorHandler);
		transaction.executeSql('INSERT INTO EnterpriseBM(EnterpriseBMName,EnterpriseBMUrl) VALUES (?,?)', ["Cognizant GSD", "https://gsd.cognizant.com"], nullHandler, errorHandler);
	});

	db.transaction(function (transaction) {
		var sqlite = 'SELECT * FROM EnterpriseBM';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {
			if(result != null && result.rows != null) {
				for(var i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);
					$('.ShowCorporateBookmarks').append("<div class='bookmark-site-title'><div class='fleft left-wrapper' id='" + row.EnterpriseBMUrl + "'><img src='" + defaultIcon + "'alt='Site Icon'  class='fleft' /><p class='fleft'>" + row.EnterpriseBMName + "</p></div><div class='clearfix'></div></div>");
				}
			}
		}, errorHandler);
	}, errorHandler, nullHandler);

	function errorHandler(transaction, error) {
	}

	$(document).delegate('.left-wrapper', 'click', function () {
		searchQuery($(this).attr('id'), true);
		$(this).parent().css('background-color', '#f0e0de');
	});

	function nullHandler() {}
	return;
}

// Clears enterprise bookmarks list
function EnterpriseBMDel() {
	//Enterprise Table (EnterpriseBM) - Columns(EnterpriseBMName,EnterpriseBMUrl,EnterpriseBMFlag)
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM EnterpriseBM', []);
	});
}

//Adds an item to history
function AddToHistory(name, url, key,dbname) {
	var comparator = url.slice(0, 7);
	
	//get db handle
	if ( db == null )
	{
		try {
			if (!window.openDatabase) {
				function alertDismissed() {
				}
				navigator.notification.alert(
				    'Database not supported in the device',  // message
				    alertDismissed,         // callback
				    'Database not supported',            // title
				    'OK'                  // buttonName
				);
			} else {
				try {
					db = window.sqlitePlugin.openDatabase({
						name: dbname,
						key: key
					});
				} catch(e) {
				}
			}
		}
		catch(e){
		}
	}
	
	//if local file, don't add to history
	if(comparator == "file://") {
		return;
	}
	
	//if bookmarked already update the bookmark icon
	isbookmarked(url,key);

	//get the favicon 
	var n = url.split("/");
	var favIconURL = n[0] + "//" + n[2] + "/favicon.ico";
	var iconFlag = 0;
	var gloimg = "";
	var img = new Image();

	img.src = favIconURL;
	img.crossOrigin = 'anonymous';
	if(img.complete) {
		iconFlag = 1;
		var canvas = document.createElement("canvas");
		canvas.width = img.width;
		canvas.height = img.height;
		var dataURL;
		var ctx = canvas.getContext("2d");
		ctx.drawImage(img, 0, 0);
		try {
			dataURL = canvas.toDataURL("./image/png");
			gloimg = dataURL;
		} catch(err) {}
	} else {}
	
	//Add item to history
	try {
		db.transaction(function (transaction) {
			transaction.executeSql('SELECT COUNT(*) as count FROM URLHistory', [], 
					function (transaction, result) {
				if(result != null && result.rows != null) {
					var row = result.rows.item(0);
					//Max limit of rows is 150. If no. of items equals 150 just update the least recent item in table else add the item
					if ( row.count >= 150 ){
						db.transaction(function (transaction) {
							transaction.executeSql('UPDATE URLHistory SET added_on =?,PageTitle=?,PageUrl=?,FavIcon=?,FavIconFlag=?  WHERE added_on = (SELECT min(added_on) FROM URLHistory)', [new Date(), name, url, gloimg, iconFlag], nullHandler, errorHandler);
						});
					}
					else{
						db.transaction(function (transaction) {
							transaction.executeSql('INSERT INTO URLHistory(added_on,PageTitle,PageUrl,FavIcon,FavIconFlag) VALUES (?,?,?,?,?)', [new Date(), name, url, gloimg, iconFlag], nullHandler, errorHandler);
						});
					}
				}
			}, errorHandler);
		}, errorHandler, successCallBack);

	} catch(e) {
	}

	function errorHandler(transaction, error) {
	}

	function successCallBack() {
	}

	function nullHandler() {
	}

	return false;
}

//Display history
function ShowHistory() {
	db.transaction(function (transaction) {
		var sqlite = 'SELECT FavIcon,FavIconFlag,PageTitle,PageUrl, max(added_on) as recently_added_date FROM URLHistory GROUP BY PageUrl, PageTitle ORDER BY 5 DESC';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {

			$(".history-list").html("");
			$(".history-list-older").html("");
			if(result != null && result.rows != null) {
				var i;
				var emptylist = 0;
				var historyRowCount = 1;
				for(i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);
					var date = new Date(row.recently_added_date);
					var currentDate = new Date();
					var iconSrc = defaultIcon;
					if(row.FavIconFlag == 1) {
						iconSrc = row.FavIcon;
					}

					HUrlStr = row.PageUrl;

					if(HUrlStr.length > 27) {
						HUrlStr = HUrlStr.substring(0, 27) + "..";
					}

					if ( historyRowCount <= 100 )
					{
						if(currentDate.getFullYear() == date.getFullYear() && currentDate.getMonth() == date.getMonth() && currentDate.getDate() == date.getDate()) {
							$(".history-list").append("<div id='" + row.SearchId + "' class='history-title'><div><div class='fleft left-wrapper' id='" + row.PageUrl + "'><img src='" + iconSrc + "' alt='Site Icon'  class='fleft' /><p class='fleft'>" + HUrlStr + "</p><div class='clearfix'></div></div><div class='fright right-wrapper'><span class='cross-button'></span></div><div class='clearfix'></div></div><div class='clearfix'></div>");
						} else {
							$(".history-list-older").append("<div id='" + row.SearchId + "' class='history-title'><div><div class='fleft left-wrapper' id='" + row.PageUrl + "'><img src='" + iconSrc + "' alt='Site Icon'  class='fleft' /><p class='fleft'>" + HUrlStr + "</p><div class='clearfix'></div></div><div class='fright right-wrapper'><span class='cross-button'></span></div><div class='clearfix'></div></div><div class='clearfix'></div>");
						}
						historyRowCount++;
					}

				}

				if($(".history-list-older").html() != null && $(".history-list-older").html() != "") {
					$(".history-list-older-wrapper").css("display", "block");
				} else {
					emptylist++;
				}

				if($(".history-list").html() != null && $(".history-list").html() != "") {
					$(".history-list-wrapper").css("display", "block");
				} else {
					emptylist++;
				}

				if(emptylist == 2) {
					//If there is no item in the history table, show an appropriate message
					$(".history-list").append("<p class='empty-message'>No history entries found.</p>");
					$(".history-date").css("display", "none");
					$(".history-list-wrapper").css("display", "block");
					$("#clear-button").css("display", "none");
				} else {
					$("#clear-button").css("display", "block");
				}

				$(".left-wrapper").click(function () {

					searchQuery($(this).attr('id'), true);
					$(this).parent().css('background-color', '#f0e0de');
				});
			}
		}, errorHandler);
	}, errorHandler, nullHandler);

	$('.history-title img').load(function () {
		if($(this).attr("src") == defaultIcon) {
			var extImg = new Image();
			var pageURL = $(this).next().parent().attr("id");
			var n = pageURL.split("/");
			var favIconURL = n[0] + "//" + n[2] + "/favicon.ico";
			extImg.src = favIconURL;
			var displayImg = this;
			if(extImg.complete) {
				$(this).attr('src', favIconURL);
				$(this).css('margin-top', '2px');
				iconFlag = 1;
				var canvas = document.createElement("canvas");
				canvas.width = extImg.width;
				canvas.height = extImg.height;
				var dataURL;
				var ctx = canvas.getContext("2d");
				ctx.drawImage(this, 0, 0);
				try {
					dataURL = canvas.toDataURL("./image/png");
					db.transaction(function (transaction) {
						transaction.executeSql('UPDATE URLHistory SET FavIcon =?,FavIconFlag=?  WHERE PageUrl = ?', [dataURL, 1, pageURL]);
					});
				} catch(err) {}
				//udpate the img in recently closed page
				$(".recently-closed-title").each(function (index) {
					if($(this).attr("id") == pageURL) {
						$(this).children('div').eq(0).children('img').eq(0).attr("src", favIconURL);
					}
				});
			} else {
				extImg.onload = function () {
					$(displayImg).attr('src', favIconURL);
					$(this).css('margin-top', '2px');

					//udpate the img in recently closed page
					$(".recently-closed-title").each(function (index) {
						if($(this).attr("id") == pageURL) {
							$(this).children('div').eq(0).children('img').eq(0).attr("src", favIconURL);
						}
					});

					iconFlag = 1;
					var canvas = document.createElement("canvas");
					canvas.width = extImg.width;
					canvas.height = extImg.height;
					var dataURL;
					var ctx = canvas.getContext("2d");
					ctx.drawImage(this, 0, 0);
					try {
						dataURL = canvas.toDataURL("./image/png");
						db.transaction(function (transaction) {
							transaction.executeSql('UPDATE URLHistory SET FavIcon =?,FavIconFlag=?  WHERE PageUrl = "?"', [dataURL, 1, pageURL]);
						});
					} catch(err) {}

				};
			}
		}
	});

	db.transaction(function (transaction) {
		var sqlite = 'SELECT FavIcon,FavIconFlag,PageTitle,PageUrl, max(added_on) FROM URLHistory WHERE RecentFlag = 0 GROUP BY PageUrl, PageTitle ORDER BY 5 DESC';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {
			$('.recently-closed-list').html("");

			if(result != null && result.rows != null) {
				var i;
				var isEmpty = 0;
				var rowCount = 1;
				for(i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);
					var date = new Date(row.added_on);
					var currentDate = new Date();
					var iconSrc = defaultIcon;

					if(row.FavIconFlag == 1) {
						iconSrc = row.FavIcon;
					}
					HUrlStr = row.PageUrl;

					if(HUrlStr.length > 27) {
						HUrlStr = HUrlStr.substring(0, 27) + "..";
					}

					if(rowCount <= 50) {
						$(".recently-closed-list").append("<div id='" + row.SearchId + "' class='recently-closed-title'><div><div class='fleft left-wrapper' id='" + row.PageUrl + "'><img src='" + iconSrc + "' alt='Site Icon'  class='fleft' /><p class='fleft'>" + HUrlStr + "</p><div class='clearfix'></div></div><div class='fright right-wrapper'><span class='cross-button'></span></div><div class='clearfix'></div></div><div class='clearfix'></div>");
						rowCount++;
						isEmpty = 1;
					}
				}
				if(isEmpty == 0) {
					$("#recently-closed-page").append("<p class='empty-message'>No recent entries found.</p>");
					$("#clear-button-recent").css("display", "none");
				} else {
					$(".left-wrapper").click(function () {
						searchQuery($(this).attr('id'), true);
						$(this).parent().parent().css('background-color', '#f0e0de');
					});
					$("#clear-button-recent").css("display", "block");
				}
			}
		}, errorHandler);
	}, errorHandler, nullHandler);

	function errorHandler(transaction, error) {
	}

	function nullHandler() {}

	return;
}

//Clears history table
function ClearHistory() {
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM URLHistory', []);
	});
}

//Clears recently viewed items from history table by updating flag value
function UpdateRecentAllFlag() {
	db.transaction(function (transaction) {
		var flagvalue = 1;
		transaction.executeSql('UPDATE URLHistory SET RecentFlag =?', [flagvalue]);
	});
}

//Delete Individual History
function DelHistory(HistoryID) {
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM URLHistory WHERE SearchId = "' + HistoryID + '"');
	});
}

//Delete Individual History based on URL
function DelHistoryURL(HistoryUrl) {
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM URLHistory WHERE PageUrl = "' + HistoryUrl + '"');
	});
}

//Update recently viewed flags based on url
function UpdateRecentFlag(HistoryUrl) {
	db.transaction(function (transaction) {
		var flagvalue = 1;
		transaction.executeSql('UPDATE URLHistory SET RecentFlag =? WHERE PageUrl = "' + HistoryUrl + '"', [flagvalue]);
	});
}

//Adds an item to Bookmarks table
function AddToBookmark(name, url) {

	var comparator = url.slice(0, 4);
	if(comparator != "http") {
		url = "http://" + url;
	}
	var n = url.split("/");
	var favIconURL = n[0] + "//" + n[2] + "/favicon.ico";
	var iconFlag = 0;
	var gloimg = "";
	var img = new Image();
	img.src = favIconURL;
	img.crossOrigin = 'anonymous';

	if(img.complete) {
		iconFlag = 1;
		var canvas = document.createElement("canvas");
		canvas.width = img.width;
		canvas.height = img.height;
		var dataURL;
		var ctx = canvas.getContext("2d");
		ctx.drawImage(img, 0, 0);
		try {
			dataURL = canvas.toDataURL("./image/png");
			gloimg = dataURL;
		} catch(err) {}
	} else {}

	var addedOn = new Date();
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM Bookmarks WHERE Url = "' + url + '"');
		transaction.executeSql('INSERT INTO Bookmarks(added_on,Bookmark,Url,FavIcon,FavIconFlag) VALUES (?,?,?,?,?)', [addedOn, name, url, gloimg, iconFlag], nullHandler, errorHandler);
	}, errorHandler, successCallBack);

	function errorHandler(transaction, error) {
	}

	function successCallBack() {
		//if the URL has not been changed before saving, mark it as bookmarked in menu
		if ( addToBookMarkInitialURL == url){
			cordova.exec(enableCacheResultHandler,nativePluginErrorHandler,"com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "isbookmarked",[ "1" ]);
		}
		history.go(-1);
	}

	function errorHandler() {}

	function nullHandler() {}

	function enableCacheResultHandler() {}

	function nativePluginErrorHandler() {}

	return false;

}


//Show the Bookmark
function ShowBookmarks() {
	db.transaction(function (transaction) {
		var sqlite = 'SELECT * FROM Bookmarks ORDER BY added_on DESC';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {
			if(result != null && result.rows != null) {
				$('.bookmark-site-list').html("");
				var BookmarkName;
				for(var i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);
					var iconSrc = defaultIcon;
					if(row.FavIconFlag == 1) {
						iconSrc = row.FavIcon;
					}
					BookmarkName = row.Bookmark;
					if(BookmarkName.length > 15) {
						BookmarkName = BookmarkName.substr(0, 15) + "...";
					}
					$('.bookmark-site-list').append("<div id='" + row.BookmarkId + "' class='bookmark-site-title'><div class='fleft left-wrapper' id='" + row.Url + "'><img src='" + iconSrc + "' alt='Site Icon'  class='fleft' /><p class='fleft'>" + BookmarkName + "</p><div class='clearfix'></div></div><div class='fright right-wrapper'><span class='cross-button'></span></div><div class='clearfix'></div>");
				}
				if($('#bookmark-list').text() == "" || $('#bookmark-list').text() == null) {
					$('#bookmark-list').append("<p class='empty-message'>No bookmark found.</p>");
					$("#clear-button-bookmarks").css("display", "none");
				} else {
					$("#clear-button-bookmarks").css("display", "block");
				}
			}
		}, errorHandler);
	}, errorHandler, nullHandler);

	$(document).delegate('.left-wrapper', 'click', function () {
		searchQuery($(this).attr('id'), true);
		$(this).parent().css('background-color', '#f0e0de');
	});

	$('.bookmark-site-title img').load(function () {
		if($(this).attr("src") == defaultIcon) {
			var extImg = new Image();
			var pageURL = $(this).parent().attr("id");
			var n = pageURL.split("/");
			var favIconURL = n[0] + "//" + n[2] + "/favicon.ico";
			extImg.src = favIconURL;
			var displayImg = this;
			if(extImg.complete) {
				$(this).attr('src', favIconURL);
				$(this).css('margin-top', '2px');
				iconFlag = 1;
				var canvas = document.createElement("canvas");
				canvas.width = extImg.width;
				canvas.height = extImg.height;
				var dataURL;
				var ctx = canvas.getContext("2d");
				ctx.drawImage(this, 0, 0);
				try {
					dataURL = canvas.toDataURL("./image/png");
					db.transaction(function (transaction) {
						transaction.executeSql('UPDATE Bookmarks SET FavIcon =?,FavIconFlag=?  WHERE Url = ?', [dataURL, 1, pageURL]);
					});
				} catch(err) {}
			} else {
				extImg.onload = function () {
					$(displayImg).attr('src', favIconURL);
					$(this).css('margin-top', '2px');
					iconFlag = 1;
					var canvas = document.createElement("canvas");
					canvas.width = extImg.width;
					canvas.height = extImg.height;
					var dataURL;
					var ctx = canvas.getContext("2d");
					ctx.drawImage(this, 0, 0);
					try {
						dataURL = canvas.toDataURL("./image/png");
						db.transaction(function (transaction) {
							transaction.executeSql('UPDATE Bookmarks SET FavIcon =?,FavIconFlag=?  WHERE Url = ?', [dataURL, 1, pageURL]);
						});
					} catch(err) {}
				};
			}
		}
	});

	function errorHandler(transaction, error) {
	}

	function nullHandler() {}

	return;
}

//Delete All Bookmarks
function DelBookmarks() {
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM Bookmarks', []);
	});
}

//Set Home Page
function SetHomePage(name, url) {
	db.transaction(function (transaction) {
		transaction.executeSql('INSERT INTO SetHomePage(SetHomePageName,SUrl) VALUES (?,?)', [name, url], nullHandler, errorHandler);
	});

	function errorHandler(transaction, error) {
	}

	function successCallBack() {}

	function nullHandler() {}

	return false;
}

//Delete Individual Bookmark
function DelBookmark(BookmardID) {
	db.transaction(function (transaction) {
		transaction.executeSql('DELETE FROM Bookmarks WHERE BookmarkId = "' + BookmardID + '"');
	});
}

//Drop the Tables (URLHistory and Bookmarks) Note : IF any error occur call this function
function DropTables() {
	db.transaction(function (transaction) {
		transaction.executeSql('DROP TABLE IF EXISTS URLHistory', []);
	});
	db.transaction(function (transaction) {
		transaction.executeSql('DROP TABLE IF EXISTS Bookmarks', []);
	});
}

//Display preferences
function ShowPreferences() {
	db.transaction(function (transaction) {
		var sqlite = 'SELECT * FROM SetHomePage';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {
			if(result != null && result.rows != null) {
				for(var i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);
					$("#home-page-text").val(row.SUrl);
					if(row.AcceptCookiesFlag == 0) {
						$("#accept-cookies-switch").addClass('active');
					} else {
						$("#accept-cookies-switch").removeClass('active');
					}

					var pimCheckFlag  = row.PIMSettingsFlag;

					if(pimCheckFlag==1){
						$("#set-home-page-switch").removeClass('active');
						$("#set-home-page-switch").off('click');
						$("#home-page-text").css("display", "block");
						$("#home-page-text").attr("disabled", "disabled");
					}
					else{

						if(row.HomePageFlag == 0) {
							$("#set-home-page-switch").addClass('active');
							$("#home-page-text").css("display", "none");
						} else {
							$("#set-home-page-switch").removeClass('active');
							$("#home-page-text").css("display", "block");
						}

					}

				}
			}
		}, errorHandler);
	}, errorHandler, nullHandler);

	function errorHandler(transaction, error) {}

	function nullHandler() {}

}

//Save preferences
function SaveSettings() {
	db.transaction(function (transaction) {
		var HomePage = $("#home-page-text").val();
		var HomePageFlag = 1;
		if($("#set-home-page-switch").hasClass("active")) {
			HomePageFlag = 0;
		}
		if(!(HomePageFlag) || validateURL(HomePage)) {
			var AcceptCookiesFlag = 1;
			if($("#accept-cookies-switch").hasClass("active")) {
				AcceptCookiesFlag = 0;
			}

			transaction.executeSql('UPDATE SetHomePage SET SetHomePageName=?, SUrl=?, AcceptCookiesFlag =?, HomePageFlag =?', [HomePage, HomePage, AcceptCookiesFlag, HomePageFlag], nullHandler, errorHandler);
			AcceptCookies(AcceptCookiesFlag); //Enable/Disable cookies based on the db flag value

			history.go(-1);
		} else {
			$("#preferences-error-tag").html("Please enter a valid URL");
		}

	});

	function errorHandler(transaction, error) {}

	function nullHandler() {}

}

//If accept cookies flag is zero, clear the cookies and update the webview settings not to accept cookies
function AcceptCookies(AcceptCookiesFlag) {

	if ( AcceptCookiesFlag == 0 )
	{
		cordova.exec(enableCacheResultHandler,nativePluginErrorHandler,"com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "disablecookies",[ "success" ]);
	}
	else
	{
		cordova.exec(disableCacheResultHandler,nativePluginErrorHandler,"com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "enablecookies",[ "success" ]);
	}

	function disableCacheResultHandler(){}
	function enableCacheResultHandler(){}
	function nativePluginErrorHandler(){}

}

//Update to native if the page is bookmarked already
function isbookmarked(Url, key) {

	if ( db == null){
	}
	db.transaction(function (transaction) {
		var sqlite = 'SELECT * FROM Bookmarks';
		transaction.executeSql(sqlite, [],
				function (transaction, result) {
			if(result != null && result.rows != null) {
				for(i = 0; i < result.rows.length; i++) {
					var row = result.rows.item(i);
					if(row.Url == Url) {
						cordova.exec(enableCacheResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "isbookmarked", ["1"]);
						return;
					}
				}
				cordova.exec(disableCacheResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "isbookmarked", ["0"]);
			}


		}, errorHandler);
	}, errorHandler, nullHandler);

	function errorHandler() {}

	function nullHandler() {}

	function disableCacheResultHandler() {}

	function enableCacheResultHandler() {}

	function nativePluginErrorHandler() {}
}

//Validating for a valid URL in address field of Add Bookmarks Page
function validateURL(value) {
	var comparator = value.slice(0, 4);
	if(comparator != "http") {
		value = "http://" + value;
	}
	return /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value);
}

//Gets key and dbname from the native side and open a page
function getKeyResultHandler(result) {
	window.location.href = "file:///android_asset/www/Main.html?" + result[0].pageName + "&" + result[0].dbpass + "&" + result[0].dbname;
}
function nativePluginErrorHandler(error) {
}

$(document).ready(function () {

	//Setting Dynamic Height for Each Internal page
	$("#history-page").css('height', $(window).height());
	$("#add-bookmarks-page").css('height', $(window).height());
	$("#recently-closed-page").css('height', $(window).height());
	$("#view-bookmarks-page").css('height', $(window).height());
	$("#settings-page").css('height', $(window).height());
	$("#corporate-bookmarks-page").css('height', $(window).height());
	$("#personal-bookmarks-page").css('height', $(window).height());
	$(".popup-background").css('height', $(window).height());    
	$("#error-tag").html("");
	$(".error-page").css('height',$(window).height());

	$(window).resize(function(){
		$("#history-page").css('height', $(window).height());
		$("#add-bookmarks-page").css('height', $(window).height());
		$("#recently-closed-page").css('height', $(window).height());
		$("#view-bookmarks-page").css('height', $(window).height());
		$("#settings-page").css('height', $(window).height());
		$("#corporate-bookmarks-page").css('height', $(window).height());
		$("#personal-bookmarks-page").css('height', $(window).height());
		$(".popup-background").css('height', $(window).height());
		$(".error-page").css('height',$(window).height());
	});

	// Code added on 09-Aug-2013 for phase 2. 
	//Edited on (04-Sep-2013) for phase 2 DB changes @added by Saranya
	$('#personal-bookmarks').click(function () {
		cordova.exec(getKeyResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "getkey", ["success", "personal-bookmarks-page"]);
	});
	$('#corporate-bookmarks').click(function () {
		cordova.exec(getKeyResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "getkey", ["success", "corporate-bookmarks-page"]);
	});

	$('#clear-browser-data').click(function () {
		cordova.exec(getKeyResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "getkey", ["success", "clear-browser-data-page"]);
	});

	$('#preferences').click(function () {
		cordova.exec(getKeyResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "getkey", ["success", "preferences-page"]);
	});

	$('#save-button').click(function () {
		SaveSettings();

	});

	$('#set-home-page-switch').click(function () {
		if($(this).hasClass('active')) {
			$(this).removeClass('active');
			$("#home-page-text").css("display", "block");
		} else {
			$(this).addClass('active');
			$("#home-page-text").css("display", "none");
			$("#preferences-error-tag").html("");
		}
	});
	$('#accept-cookies-switch').click(function () {

		if($(this).hasClass('active')) {

			$(this).removeClass('active');
		} else {
			$(this).addClass('active');
		}
	});

	$("#home-page-text").focus(function () {
		$("#preferences-error-tag").html("");
	});

	
	function onClearHistoryConfirm(button){
		if(button == 2){
			ClearHistory();
			//There is no item in the history list
			$(".history-list").html("");
			$(".history-list-older").html("");
			$(".history-list").append("<p class='empty-message'>No history entries found.</p>");
			$(".history-date").css("display", "none");
			$(".history-list-wrapper").css("display", "block");
			$("#clear-button").css("display", "none");
		}
	}

	function onClearBookmarksConfirm(button){
		if(button == 2){
			$('#bookmark-list').html("");
			$('#bookmark-list').append("<p class='empty-message'>No bookmark found.</p>");
			$("#clear-button-bookmarks").css("display", "none");
			DelBookmarks();
		}

	}

	function onClearRecentlyViewed(button){
		if ( button == 2 ){
			$('.popup-background').css('display','none');
			$('#recently-viewed-confirm').css('display','none');

			$(".recently-closed-list").html("");
			$("#recently-closed-page").append("<p class='empty-message'>No recent entries found.</p>");
			$("#clear-button-recent").css("display", "none");
			UpdateRecentAllFlag();
		}
	}

	$('#alert-ok').click(function(){

		$('.popup-background').css('display','none');
		$('.popup-alert').css('display','none');

	});


	//Clear Browser Data click events
	function clearCacheResultHandler(result) {
		encString = result;
	}
	function clearCookiesResultHandler(result) {
		encString = result;
	}

	$('#clearcache').click(function(){
		navigator.notification.confirm(
				'Are you sure you want to clear cache?',  // message
				onClearCacheConfirm,              // callback to invoke with index of button pressed
				'Clear Cache',            // title
				'Cancel,OK'          // buttonLabels
		);

	});

	function onClearCacheConfirm(button){
		if ( button == 2 ){
			cordova.exec(clearCacheResultHandler,nativePluginErrorHandler,"com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "clearcache",[ "success" ]);
			cordova.exec(clearCookiesResultHandler,nativePluginErrorHandler,"com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "longToast",[ "Cache cleared" ]);
		}
	}

	$('#clearcookies').click(function () {
		navigator.notification.confirm(
				'Are you sure you want to clear cookies?',  // message
				onClearCookiesConfirm,              // callback to invoke with index of button pressed
				'Clear Cookies',            // title
				'Cancel,OK'          // buttonLabels
		);

	});

	function onClearCookiesConfirm(button){
		if ( button == 2 ){
			cordova.exec(clearCookiesResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "clearcookies", ["success"]);
			cordova.exec(clearCookiesResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "longToast", ["Cookies cleared"]);
		}
	}

	$('#clearhistory').click(function () {

		navigator.notification.confirm(
				'Are you sure you want to clear history?',  // message
				onClearHistoryConfirmSettings,              // callback to invoke with index of button pressed
				'Clear History',            // title
				'Cancel,OK'          // buttonLabels
		);

	});

	function onClearHistoryConfirmSettings(button){
		if ( button == 2 ){
			ClearHistory();
			cordova.exec(clearCookiesResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "longToast", ["History cleared"]);
		}
	}

	$('#clear-all-browser-data').click(function () {
		navigator.notification.confirm(
				'Are you sure you want to clear all data?',  // message
				onClearAllConfirm,              // callback to invoke with index of button pressed
				'Clear All Data',            // title
				'Cancel,OK'          // buttonLabels
		);

	});

	function onClearAllConfirm(button){
		if(button == 2){
			ClearHistory();
			cordova.exec(clearCacheResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "clearcache", ["success"]);
			cordova.exec(clearCookiesResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "clearcookies", ["success"]);
			cordova.exec(clearCookiesResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "longToast", ["History, Cookies, Cache cleared"]);
		}
	}

	// Individual Delete Functionality
	$(document).delegate('.cross-button', 'click', function () {
		if($(this).parent().parent().parent().parent().attr('id') == "personal-bookmarks-page") {
			DelBookmark($(this).parent().parent().attr('id'));
			$(this).parent().parent().css('background-color', '#f0e0de');
			$(this).parent().parent().remove();
			if($('#bookmark-list').text() == "" || $('#bookmark-list').text() == null) {
				$('#bookmark-list').append("<p class='empty-message'>No bookmark found.</p>");
				$("#clear-button-bookmarks").css("display", "none");
			}
		} else if($(this).parent().parent().parent().parent().parent().parent().attr('id') == "history-page") {
			DelHistoryURL($(this).parent().prev().attr('id'));
			$(this).parent().parent().parent().css('background-color', '#f0e0de');
			$(this).parent().parent().parent().remove();

			var emptylist = 0;

			if($(".history-list-older").html() == null || $(".history-list-older").html() == "") {
				emptylist++;
				$(".history-list-older-wrapper").css("display", "none");
			}

			if($(".history-list").html() == null || $(".history-list").html() == "") {
				emptylist++;
				$(".history-list-wrapper").css("display", "none");
			}
			if(emptylist == 2) {
				//There is no item in the history list
				$(".history-list").append("<p class='empty-message'>No history entries found.</p>");
				$(".history-date").css("display", "none");
				$(".history-list-wrapper").css("display", "block");

				$("#clear-button").css("display", "none");
			}

		} else if($(this).parent().parent().parent().parent().parent().attr('id') == "recently-closed-page") {

			var parent = $(this).parent().parent().parent().parent();

			UpdateRecentFlag($(this).parent().prev().attr('id'));
			$(this).parent().parent().parent().css('background-color', '#f0e0de');
			$(this).parent().parent().parent().remove();

			if(parent.html() == null || parent.html() == "") {
				$("#recently-closed-page").append("<p class='empty-message'>No recent entries found.</p>");
				$("#clear-button-recent").css("display", "none");
			}
		}
	});

	//Actions in Add Bookmarks Page
	$('#BMsave').click(function () {
		bookmarkname = $('#BMname').val();
		bookmarkaddr = $('#BMaddress').val();
		if(bookmarkname == "" || bookmarkname == " ") {

			$("#error-tag").html("Please enter a name for bookmark");
		} else if(bookmarkaddr == "" || bookmarkaddr == " ") {

			$("#error-tag").html("Please enter the URL to be bookmarked");
		} else {
			var urltest = validateURL(bookmarkaddr);
			if(urltest) {
				cordova.exec(getKeyResultHandler, nativePluginErrorHandler, "com.cognizant.trumobi.securebrowser.SB_ClearBrowsingData", "getkey", ["success", "save-bookmarks"]);
			} else {

				$("#error-tag").html("Please enter a valid URL");
			}
		}

		function getKeyResultHandler(result) {
			openTruBrowserTables(result[0].dbpass,result[0].dbname);
			AddToBookmark(bookmarkname, bookmarkaddr);
		}

		function nativePluginErrorHandler() {}
	});

	//Clear All funtionality in History Page
	$('#clear-button').click(function () {

		navigator.notification.confirm(
				'Are you sure you want to clear History?',  // message
				onClearHistoryConfirm,              // callback to invoke with index of button pressed
				'Clear History',            // title
				'Cancel,OK'          // buttonLabels
		);

	});

	// Clear All funtionality in Bookmarks Page
	$('#clear-button-bookmarks').click(function () {

		navigator.notification.confirm(
				'Are you sure you want to clear bookmarks?',  // message
				onClearBookmarksConfirm,              // callback to invoke with index of button pressed
				'Clear Bookmarks',            // title
				'Cancel,OK'          // buttonLabels
		);

	});

	// Clear All funtionality in Recently Viewed Page
	$('#clear-button-recent').click(function () {

		navigator.notification.confirm(
				'Are you sure you want to clear recently viewed entry?',  // message
				onClearRecentlyViewed,              // callback to invoke with index of button pressed
				'Clear  Recent Entries',            // title
				'Cancel,OK'          // buttonLabels
		);

	});
});

var blacklistFlag = false;
var TLDs = ["ac", "ad", "ae", "aero", "af", "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "arpa", "as", "asia", "at", "au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "biz", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz", "ca", "cat", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "com", "coop", "cr", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "edu", "ee", "eg", "er", "es", "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn", "gov", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "im", "in", "info", "int", "io", "iq", "ir", "is", "it", "je", "jm", "jo", "jobs", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mil", "mk", "ml", "mm", "mn", "mo", "mobi", "mp", "mq", "mr", "ms", "mt", "mu", "museum", "mv", "mw", "mx", "my", "mz", "na", "name", "nc", "ne", "net", "nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "org", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pro", "ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st", "su", "sv", "sy", "sz", "tc", "td", "tel", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tp", "tr", "travel", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu", "wf", "ws", "xn--0zwm56d", "xn--11b5bs3a9aj6g", "xn--3e0b707e", "xn--45brj9c", "xn--80akhbyknj4f", "xn--90a3ac", "xn--9t4b11yi5a", "xn--clchc0ea0b2g2a9gcd", "xn--deba0ad", "xn--fiqs8s", "xn--fiqz9s", "xn--fpcrj9c3d", "xn--fzc2c9e2c", "xn--g6w251d", "xn--gecrj9c", "xn--h2brj9c", "xn--hgbk6aj7f53bba", "xn--hlcj6aya9esc7a", "xn--j6w193g", "xn--jxalpdlp", "xn--kgbechtv", "xn--kprw13d", "xn--kpry57d", "xn--lgbbat1ad8j", "xn--mgbaam7a8h", "xn--mgbayh7gpa", "xn--mgbbh1a71e", "xn--mgbc0a9azcg", "xn--mgberp4a5d4ar", "xn--o3cw4h", "xn--ogbpf8fl", "xn--p1ai", "xn--pgbs0dh", "xn--s9brj9c", "xn--wgbh1c", "xn--wgbl6a", "xn--xkc2al3hye2a", "xn--xkc2dl3a5ee0h", "xn--yfro4i67o", "xn--ygbi2ammx", "xn--zckzah", "xxx", "ye", "yt", "za", "zm", "zw"].join();

function getDomainName(url) {
	var n = url.indexOf("/", 8);
	if(n != -1) {
		url = url.substr(0, n);
	}
	var parts = url.split('.'),
	ln = parts.length,
	i = ln,
	minLength = parts[parts.length - 1].length,
	part

	while(part = parts[--i]) {

		if(TLDs.indexOf(part) < 0 || part.length < minLength || i < ln - 2 || i === 0) {
			var data = part.split('/');
			if(data.length > 0) {
				part = data[data.length - 1];
			}
			break;
		}
	}
	return part;
}

//check for blacklist urls, add to history and open the url
function searchQuery(geturl, status,key,dbname) {
	
	//remove white spaces at both ends
	geturl = $.trim(geturl);
    
    var a = document.createElement('a');
    a.href = geturl;
    var host=a.hostname;
    var extension =  host.substring(host.lastIndexOf(".") + 1, host.length);

	if(TLDs.indexOf(extension)!=-1){
	var url = validateURL(geturl);
    if(url) {

	var domainName;
	blacklistFlag = false;
	$.getJSON('blacklist.json', function (data) {
		$.each(data, function (key, val) {
			domainName = getDomainName(geturl);
			if(val.indexOf(domainName) != -1) {
				blacklistFlag = true;
			}
		});
		if(!blacklistFlag) {       
			if(status == true) {
				var RandVar = "";
				var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
				for(var i = 0; i < 10; i++) {
					RandVar += possible.charAt(Math.floor(Math.random() * possible.length));
				}
				var tabname = RandVar;                
				RandVar = window.open(geturl, '_self'); 
				if (!(geturl.substring(0, 7).toLowerCase() == "file://")) {                    
					AddToHistory(geturl, geturl,key,dbname);                    
				}                             
			} else {
				window.open(geturl, '_self');             
			}
		} else {
			window.location.href = 'error.html?blockedsite';
		}
	});
    }
    else{
    	//If not a valid URL make a google search on it
    	var comparator = geturl.slice(0, 7);
        if(comparator == "http://") {
        	geturl = geturl.substring(7,geturl.length);
        }
        var encodeURIformat=encodeURIComponent(geturl);
        window.location.href="http://www.google.com/search?q=" + encodeURIformat;

    }
	}
	else{
		//If not a valid URL make a google search on it
    	var comparator = geturl.slice(0, 7);
        if(comparator == "http://") {
        	geturl = geturl.substring(7,geturl.length);
        }
        var encodeURIformat=encodeURIComponent(geturl);
        window.location.href="http://www.google.com/search?q=" + encodeURIformat;
	}

}

//Update bookmark textbox with url
function bookMark(url) {
	var protocol = url.slice(0, 4);
	if(protocol.toLowerCase() != 'file') {
		$("#BMaddress").val(url);
		addToBookMarkInitialURL = url;
	}
}

//Update bookmark name textbox with title
function bookMarkTitle(title) {

	if(title == "" || title == null){
		title = "Untitled";
	}
	if($("#BMaddress").val() == "") {
		title = "";
	}
	$("#BMname").val(title);
}

//Reload current url
function reloadwindow(geturl) {
	window.location.href = geturl;
}
