import BmsSDK

@objc(BmsCordovaSdkPublic) class BmsCordovaSdkPublic : CDVPlugin {
    private var viaBmsCtrl:ViaBmsCtrl!;
	private var initSdkCallbackId: String!;
	private var initCustomerCallbackId: String!;
	private var checkinCallbackId: String!
	private var checkoutCallbackId: String!
    private var onDistanceBeaconsCallbackId: String!
    private var openDeviceSiteCallbackId: String!
    private var zones: [ViaZone]!

	override func pluginInitialize() {
        viaBmsCtrl = ViaBmsCtrl.sharedInstance;
        viaBmsCtrl.delegate = self;
	}

	@objc(initSDK:)
	func initSDK(command: CDVInvokedUrlCommand) {
		let sdk_key = command.arguments[0] as? String ?? "no value";
		viaBmsCtrl.initSdk(uiViewController: self.viewController, sdk_key: sdk_key);

		initSdkCallbackId = command.callbackId;
	}

	@objc(initCustomer:)
	func initCustomer(command: CDVInvokedUrlCommand) {
		let identifier = command.arguments[0] as? String ?? "no value";
		let phone = command.arguments[1] as? String ?? "no value";
		let email = command.arguments[2] as? String ?? "no value";
        viaBmsCtrl.initCustomer(identifier: identifier, email: email, phone: phone, authorizedZones: self.zones);

		initCustomerCallbackId = command.callbackId;
	}

	@objc(setting:)
	func setting(command: CDVInvokedUrlCommand) {
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		let alert = command.arguments[0] as? Bool ?? false;
		let background = command.arguments[1] as? Bool ?? false;
		let site = command.arguments[2] as? Bool ?? false;
        let minisitesViewString = command.arguments[3] as? String;
        let autoSiteDuration = command.arguments[4] as! Double;
		let tracking = command.arguments[5] as? Bool ?? false;
        let enableMQTT = command.arguments[6] as? Bool ?? false;
        let attendance = command.arguments[7] as? Bool ?? false;
        let checkinDuration = command.arguments[8] as! Double;
        let checkoutDuration = command.arguments[9] as! Double;
        let beaconsInput:NSArray = command.arguments[10] as! NSArray;
        let environmentStr = command.arguments[11] as? String;
        let beaconRegionRange = command.arguments[12] as! Double;
        let beaconRegionUUIDFilter = command.arguments[12] as? Bool ?? false;
        let isBroadcasting = command.arguments[13] as? Bool ?? false;
        let proximityAlert = command.arguments[14] as? Bool ?? false;
        let proximityAlertThreshold = command.arguments[15] as! Double;

        var minisitesView: MinisiteViewType = .LIST;
        if (minisitesViewString == "AUTO") {
            minisitesView = .AUTO;
        }

        var bmsEnvironment: BmsEnvironment = BmsEnvironment.PROD;
        if (environmentStr == "CHINA") {
            bmsEnvironment = BmsEnvironment.CHINA;
        } else if (environmentStr == "DEV") {
            bmsEnvironment = BmsEnvironment.DEV;
        }

        var beacons:[IBeacon] = [];
        for beaconInput in (beaconsInput as NSArray as! [NSDictionary]) {
            let uuidStr:String = beaconInput.value(forKey: "uuid") as! String;
            print("uuid ", uuidStr);

            let beacon = IBeacon.init(uuid: beaconInput.value(forKey: "uuid") as! String,
            major: beaconInput.value(forKey: "major") as! Int,
            minor: beaconInput.value(forKey: "minor") as! Int);

            beacons.append(beacon);
        }

        viaBmsCtrl.setting(alert: alert, background: background, site: site, minisitesView: minisitesView, autoSiteDuration: autoSiteDuration,
                           tracking: tracking,
                           enableMQTT: enableMQTT, attendance: attendance, checkinDuration: checkinDuration, checkoutDuration: checkoutDuration, requestDistanceBeacons: beacons, bmsEnvironment: bmsEnvironment,
                               beaconRegionRange: beaconRegionRange,
                               beaconRegionUUIDFilter: beaconRegionUUIDFilter,
                               isBroadcasting: isBroadcasting,
                               proximityAlert: proximityAlert,
                               proximityAlertThreshold: proximityAlertThreshold);

		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "setting done!");

		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}

	@objc(startSDK:)
	func startSDK(command: CDVInvokedUrlCommand) {
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		let bmsRunning = viaBmsCtrl.isBmsRunning();
        let sdkInited = viaBmsCtrl.isSdkInited();

        if (!bmsRunning && sdkInited) {
				viaBmsCtrl.startBmsService();
		}

		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "startSDK done!");
		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}

	@objc(endSDK:)
	func endSDK(command: CDVInvokedUrlCommand) {
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		viaBmsCtrl.stopBmsService();

		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "endSDK done!");
		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}

	@objc(checkIn:)
	func checkIn(command: CDVInvokedUrlCommand) {
        self.checkinCallbackId = command.callbackId;
	}

	@objc(checkOut:)
	func checkOut(command: CDVInvokedUrlCommand) {
        self.checkoutCallbackId = command.callbackId;
	}

    @objc(onDistanceBeacons:)
	func onDistanceBeacons(command: CDVInvokedUrlCommand) {
        self.onDistanceBeaconsCallbackId = command.callbackId;
	}

    @objc(openDeviceSite:)
    func openDeviceSite(command: CDVInvokedUrlCommand) {
        let deviceSiteURL = command.arguments[0] as? String;
        viaBmsCtrl.openDeviceSite(deviceSiteURL: deviceSiteURL!)
        self.openDeviceSiteCallbackId = command.callbackId;
    }
}

extension BmsCordovaSdkPublic: ViaBmsCtrlDelegate {
    // this will be called when sdk is inited
    // list of zones in the sdk application is passed here
    func sdkInited(inited status: Bool, zones: [ViaZone]) {
        print("sdk inited", status);

				if (status) {
          self.zones = zones;
					let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initSdkCallbackId);
				} else {
					let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initSdkCallbackId);
				}
    }

    func customerInited(inited: Bool) {
        print("customer inited", inited);

				if (inited) {
					let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initCustomerCallbackId);
				} else {
					let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initCustomerCallbackId);
				}
    }

    func checkin() {
        print("check in callback");

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
        pluginResult.setKeepCallbackAs(true);
        self.commandDelegate!.send(pluginResult, callbackId: checkinCallbackId);
    }

    func checkout() {
        print("check out callback");

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
        pluginResult.setKeepCallbackAs(true);
        self.commandDelegate!.send(pluginResult, callbackId: checkoutCallbackId);
    }

    func onProximityAlert() {
        print("onProximityAlert");
    }

    func onDistanceBeacons(beacons: [IBeacon]) {
        print("onDistanceBeacons callback");

        var beaconsOutput:[NSDictionary] = [];
        for beacon in (beacons as NSArray as! [IBeacon]) {
            var beaconOutput:[String:Any] = [:];
            beaconOutput["uuid"] = beacon.uuid;
            beaconOutput["major"] = beacon.major;
            beaconOutput["minor"] = beacon.minor;
//
            beaconsOutput.append(beaconOutput as NSDictionary);
        }

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: beaconsOutput as [Any]);
        pluginResult.setKeepCallbackAs(true);
        self.commandDelegate!.send(pluginResult, callbackId: onDistanceBeaconsCallbackId);
    }

    func deviceSiteLoaded(loaded: Bool, error: String?) {
        print("deviceSiteLoaded callback");

        if (loaded) {
            let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
            pluginResult.setKeepCallbackAs(true);
            self.commandDelegate!.send(pluginResult, callbackId: checkoutCallbackId);
        } else {
            let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error);
            pluginResult.setKeepCallbackAs(true);
            self.commandDelegate!.send(pluginResult, callbackId: checkoutCallbackId);
        }
    }
}
