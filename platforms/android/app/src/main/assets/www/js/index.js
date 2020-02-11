/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var setting = function(callback) {
   try {
     const requestDistanceBeacons = [
       {
         uuid: "F7826DA6-4FA2-4E98-8024-BC5B71E0893B",
         major: 1,
         minor: 47
       }
     ]

     cordova.plugins.BmsCordovaSdkPublic.setting(true, true,
       true, "LIST", 0, true, true, true, 2, 2, requestDistanceBeacons,
       "DEV", 5, true, true, true, 120, (success) => {
       console.log("setting success", success);
       callback();
     }, (error) => {
       console.log("setting error", error);
     });
   } catch(e) {
     console.log("exception", e);
   }
}

var initCustomer = function(callback) {
	try {
		cordova.plugins.BmsCordovaSdkPublic.initCustomer("khoa_cordova", "+65 88268725", "khoa_cordova@viatick.com", (success) => {
			console.log("initCustomer success", success);
      callback();
		}, (error) => {
			console.log("initCustomer error", error);
		});
	} catch(e) {
		console.log("initCustomer exception", e);
	}
};

var initSDK = function(callback) {
	try {
		cordova.plugins.BmsCordovaSdkPublic.initSDK("71b20b69d6c313e5a226b910ccac09d35c68caaec7c7303984f8caae0a7fdb25", (success) => {
			console.log("initSDK success", success);
      callback();
		}, (error) => {
			console.log("initSDK error", error);
		});
	} catch(e) {
		console.log("initSDK exception", e);
	}
};

var startService = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.startSDK((success) => {
			console.log("startService success", success);
		}, (error) => {
			console.log("startService error", error);
		});
	} catch(e) {
		console.log("startService xception", e);
	}
};

var endService = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.endSDK((success) => {
			console.log("endService success", success);
		}, (error) => {
			console.log("endService error", error);
		});
	} catch(e) {
		console.log("endService exception", e);
	}
};

var checkIn = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.checkIn((success) => {
			console.log("checkIn success", success);
		}, (error) => {
			console.log("checkIn error", error);
		});
	} catch(e) {
		console.log("checkIn exception", e);
	}
};

var checkOut = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.checkOut((success) => {
			console.log("checkOut success", success);
		}, (error) => {
			console.log("checkOut error", error);
		});
	} catch(e) {
		console.log("checkOut exception", e);
	}
};

var onDistanceBeacons = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.onDistanceBeacons((success) => {
			console.log("onDistanceBeacons success", success);
			if (success) {
			    const filteredBeacons = success.filter((b) => b.distance < 2.5);

			    console.log("onDistanceBeacons filteredBeacons", filteredBeacons);
			}
		}, (error) => {
			console.log("onDistanceBeacons error", error);
		});
	} catch(e) {
		console.log("onDistanceBeacons exception", e);
	}
};

var openDeviceSite = function(url) {
	try {
		cordova.plugins.BmsCordovaSdkPublic.openDeviceSite(url, (success) => {
			console.log("openDeviceSite success", success);
		}, (error) => {
			console.log("openDeviceSite error", error);
		});
	} catch(e) {
		console.log("openDeviceSite exception", e);
	}
};

var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
        setting(function () {
          initSDK(function () {
            initCustomer(function () {
              startService();

              checkIn(); // check-in listener
              checkOut(); // check-out listener
              onDistanceBeacons();

              openDeviceSite("https://bms.viatick.com/link/?serial=TEST0000001&env=dev&key=df3ac43dd0ce4d11887edabdca7bfb6e071f800f09001d05df68ddb7747bd130");
            });
          });
        });
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();
