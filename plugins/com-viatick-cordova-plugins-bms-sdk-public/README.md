# bms-cordova-sdk-public

> Native iOS SDK link https://github.com/Viatick-co/bms-ios-sdk-public

> Native Android SDK link https://github.com/Viatick-co/bms-android-sdk-public

> Sample link https://github.com/Viatick-co/bms-cordova-sdk-sample

## Installation

Run following command in your `ionic / cordova` project root directory.

```
$ ionic cordova plugin add https://github.com/Viatick-co/bms-cordova-sdk-public.git

or

$ cordova plugin add https://github.com/Viatick-co/bms-cordova-sdk-public.git
```

## Settings

### iOS xcode

* Build Settings
	- SWIFT_VERSION => ^5.1

* Build Phases
	- UserNotifications.framework
	- CoreLocation.framework
	- NotificationCenter.framework

* Info.plist
```xml
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>description for location access</string>
<key>NSLocationWhenInUseUsageDescription</key>
<string>description for location access</string>
<key>NSLocationAlwaysUsageDescription</key>
<string>description for location access</string>
```

* Capabilities
	- Enable `Location updates` in `Background Modes`


## Sample Codes
```javascript

// Step 1 required
public setting = function() {
	let enableAlert = true; // whether to show notification when a minisite is triggered
	let enableBackground = true; // whether to enable beacon scanning in background mode
	let enableSite = true; // whether to enable minisite feature
	let minisitesView = "LIST"; // choose either 'LIST' to display
	let autoSiteDuration = 0; // if minisite view mode is 'AUTO', this specifies number of
	let tracking = true; // whether to enable tracking feature and send tracking data to BMS
	let enableMQTT = true; // whether to use MQTT or normal RESTful endpoint to send tracking data
	let attendance = true; // whether to enable attendance feature
	let checkinDuration = 15; // duration of the device staying in the authorized zones to be considered "checked in"
	let checkoutDuration = 15; // duration of the device staying out of the authorized zones to be considered "checked out"

	// ibeacons that you want to return distance callback
	let iBeacons = [
       {
         uuid: "F7826DA6-4FA2-4E98-8024-BC5B71E0893E",
         major: 50,
         minor: 40
       },
       {
         uuid: "F7826DA6-4FA2-4E98-8024-BC5B71E0893E",
         major: 100,
         minor: 1
       }
  ]

	let bmsEnvironment = "DEV"; // BMS environment, default is "PROD"

	try {
		cordova.plugins.BmsCordovaSdkPublic.setting(enableAlert, enableBackground, enableSite,
			minisitesView, autoSiteDuration, tracking, enableMQTT, attendance, checkinDuration,
			checkoutDuration, iBeacons, bmsEnvironment, (success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
}

// Step 2 required, suggested to call after initial setting
public initSDK = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.initSDK("SDK_KEY_FROM_BMS", (success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

// Step 3 optional, only call after initSDK returns successful
public initCustomer = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.initCustomer("customer_identifier", "customer_email", "customer_phone", (success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

// Step 4 required, only call after initSDK returns successful
public startService = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.startSDK((success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

// Step 5 required, only call after initSDK returns successful
public endService = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.endSDK((success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

// Step 6 optional, only call after initSDK returns successful
// every time the device checkin, this callback will be triggered
public checkIn = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.checkIn((success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

// Step 7 optional, only call after initSDK returns successful
// every time the device checkout, this callback will be triggered
public checkOut = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.checkIn((success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

// Step 8 optional, return callback of iBeacon information and distance
// everytime an iBeacon among the filtered iBeacons on settings is detected
public checkOut = function() {
	try {
		cordova.plugins.BmsCordovaSdkPublic.onDistanceBeacons((iBeacons) => {
			// Example:
			// iBeacons = [{
			// 	uuid: "F7826DA6-4FA2-4E98-8024-BC5B71E0893E",
			// 	major: 50,
			// 	minor: 40,
			// 	distance: 5.5
			// }]
		}]
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
};

```
