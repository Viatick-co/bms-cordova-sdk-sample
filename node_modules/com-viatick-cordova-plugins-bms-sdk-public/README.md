# bms-cordova-sdk-public

> Native iOS SDK link https://github.com/Viatick-co/bms-ios-sdk-public

> Native Android SDK link https://github.com/Viatick-co/bms-android-sdk-public

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
	- SWIFT_VERSION => ^3.3

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
// Step 1 optional
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

// Step 2 required
public setting = function() {
	let enableAlert = true;
	let enableBackground = true;
	let enableSite = true;
	let enableAttendance = true;
	let enableTracking = true;
	try {
		cordova.plugins.BmsCordovaSdkPublic.setting(enableAlert, enableBackground, enableSite, enableAttendance, enableTracking, (success) => {
			console.log("success", success);
		}, (error) => {
			console.log("error", error);
		});
	} catch(e) {
		console.log("exception", e);
	}
}

// Step 3 required
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

// Step 4 required
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

// Step 5 required
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


```
