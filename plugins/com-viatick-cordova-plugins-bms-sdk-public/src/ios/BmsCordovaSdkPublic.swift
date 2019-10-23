import BmsSDK

@objc(BmsCordovaSdkPublic) class BmsCordovaSdkPublic : CDVPlugin {
	private var viaBmsCtrl = ViaBmsCtrl.sharedInstance;
	private var initSdkCallbackId: String!;
	private var initCustomerCallbackId: String!;
	private var checkinCallbackId: String!
	private var checkoutCallbackId: String!

	override func pluginInitialize() {
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
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		let identifier = command.arguments[0] as? String ?? "no value";
		let phone = command.arguments[1] as? String ?? "no value";
		let email = command.arguments[2] as? String ?? "no value";
		viaBmsCtrl.initCustomer(identifier: identifier, phone: phone, email: email);

		initCustomerCallbackId = command.callbackId;
	}

	@objc(setting:)
	func setting(command: CDVInvokedUrlCommand) {
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		let alert = command.arguments[0] as? Bool ?? false;
		let background = command.arguments[1] as? Bool ?? false;
		let site = command.arguments[2] as? Bool ?? false;
		let attendance = command.arguments[3] as? Bool ?? false;
		let tracking = command.arguments[4] as? Bool ?? false;
		viaBmsCtrl.setting(alert: alert, background: background, site: site, attendance: attendance, tracking: tracking);

		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "setting done!");

		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}

	@objc(startSDK:)
	func startSDK(command: CDVInvokedUrlCommand) {
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		viaBmsCtrl.startBmsService();

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
		initCheckinCallbackId = command.callbackId;
	}

	@objc(checkOut:)
	func checkOut(command: CDVInvokedUrlCommand) {
		initCheckoutCallbackId = command.callbackId;
	}
}

extension BmsCordovaSdkPublic: ViaBmsCtrlDelegate {
    // this will be called when sdk is inited
    // list of zones in the sdk application is passed here
    func sdkInited(inited status: Bool, zones: [ViaZone]) {
        print("sdk inited", status);

				if (status) {
					var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initSdkCallbackId);
				} else {
					var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initSdkCallbackId);
				}
    }

    func customerInited(inited: Bool) {
        print("customer inited", inited);

				if (status) {
					var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initCustomerCallbackId);
				} else {
					pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "");
					self.commandDelegate!.send(pluginResult, callbackId: initCustomerCallbackId);
				}
    }

    func checkin() {
        print("check in callback");

				var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
				pluginResult.setKeepCallbackAsBool(true);
				self.commandDelegate!.send(pluginResult, callbackId: checkinCallbackId);
    }

    func checkout() {
        print("check out callback");

				var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "");
				pluginResult.setKeepCallbackAsBool(true);
				self.commandDelegate!.send(pluginResult, callbackId: checkoutCallbackId);
    }
}
