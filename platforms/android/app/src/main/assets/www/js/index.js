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
		cordova.plugins.BmsCordovaSdkPublic.initSDK("ec562b5f867336a7826b131f97223f6d8d39332dd74bd23ba7b97903218c1769", (success) => {
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

var addTagListener = function (callback) {
    nfc.addNdefListener(callback);
//    nfc.addNdefFormatableListener(
//                    callback
//                );
}

function decodePayload(record) {
    var recordType = nfc.bytesToString(record.type),
        payload;

    console.log('recordType: ' + recordType);

    var tnfString = tnfToString(record.tnf);
    console.log ('tnf: ' + tnfString);

    if (recordType === "T") {
        var langCodeLength = record.payload[0],
        text = record.payload.slice((1 + langCodeLength), record.payload.length);
        payload = nfc.bytesToString(text);

    } else if (recordType === "U") {
        var identifierCode = record.payload.shift(),
        uri =  nfc.bytesToString(record.payload);

        if (identifierCode !== 0) {
            console.log("WARNING: uri needs to be decoded");
        }
        //payload = "<a href='" + uri + "'>" + uri + "<\/a>";
        payload = uri;
    } else if (recordType === "Sp") {
        var decoded =  nfc.bytesToString(record.payload);
        //payload = "<a href='" + uri + "'>" + uri + "<\/a>";
        var uriPart = decoded.substring(decoded.indexOf("U") + 2, decoded.length);
        console.log('uriPart', uriPart);
        var uri = uriPart.substring(0, uriPart.indexOf("T") - 2);

        console.log('uri', uri);

        payload = uri;
    } else {

        // kludge assume we can treat as String
        payload = nfc.bytesToString(record.payload);
    }

    return payload;
}

function tnfToString(tnf) {
    var value = tnf;

    switch (tnf) {
    case ndef.TNF_EMPTY:
        value = "Empty";
        break;
    case ndef.TNF_WELL_KNOWN:
        value = "Well Known";
        break;
    case ndef.TNF_MIME_MEDIA:
        value = "Mime Media";
        break;
    case ndef.TNF_ABSOLUTE_URI:
        value = "Absolute URI";
        break;
    case ndef.TNF_EXTERNAL_TYPE:
        value = "External";
        break;
    case ndef.TNF_UNKNOWN:
        value = "Unknown";
        break;
    case ndef.TNF_UNCHANGED:
        value = "Unchanged";
        break;
    case ndef.TNF_RESERVED:
        value = "Reserved";
        break;
    }
    return value;
}

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

              addTagListener(function(response) {
                console.log('response', response);
                    const uri = decodePayload(response.tag.ndefMessage[0]);
                    console.log('uri', uri);

                    if (uri) {
                        openDeviceSite(uri);
                    }
              });
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
