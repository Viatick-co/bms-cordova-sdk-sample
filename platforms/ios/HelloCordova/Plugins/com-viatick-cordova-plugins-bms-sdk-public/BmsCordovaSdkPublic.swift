import BmsSDK

@objc(BmsCordovaSdkPublic) class BmsCordovaSdkPublic : CDVPlugin {
    private var viaBmsCtrl:ViaBmsCtrl!;
	private var initSdkCallbackId: String!;
	private var initCustomerCallbackId: String!;
	private var checkinCallbackId: String!
	private var checkoutCallbackId: String!
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
        let remark = "";
        viaBmsCtrl.initCustomer(identifier: identifier, email: email, phone: phone, remark: remark, authorizedZones: self.zones);

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

        var minisitesView: MinisiteViewType = .LIST;
        if (minisitesViewString == "AUTO") {
            minisitesView = .AUTO;
        }

        viaBmsCtrl.setting(alert: alert, background: background, site: site, minisitesView: minisitesView, autoSiteDuration: autoSiteDuration,
                           tracking: tracking,
                           enableMQTT: enableMQTT, attendance: attendance, checkinDuration: checkinDuration, checkoutDuration: checkoutDuration);

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
}
