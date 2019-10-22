@objc(BmsCordovaSdkPublic) class BmsCordovaSdkPublic : CDVPlugin {

	@objc(initSDK:)
	func initSDK(command: CDVInvokedUrlCommand) {
		let viaBmsCtrl = ViaBmsCtrl.sharedInstance;
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		let sdk_key = command.arguments[0] as? String ?? "no value";
		viaBmsCtrl.initSdk(uiViewController: self.viewController, sdk_key: sdk_key);

		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "initSDK done!");
		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}

	@objc(initCustomer:)
	func initCustomer(command: CDVInvokedUrlCommand) {
		let viaBmsCtrl = ViaBmsCtrl.sharedInstance;
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);

		let identifier = command.arguments[0] as? String ?? "no value";
		let phone = command.arguments[1] as? String ?? "no value";
		let email = command.arguments[2] as? String ?? "no value";
		viaBmsCtrl.initCustomer(identifier: identifier, phone: phone, email: email);

		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "initCustomer done!");

		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}

	@objc(setting:)
	func setting(command: CDVInvokedUrlCommand) {
		let viaBmsCtrl = ViaBmsCtrl.sharedInstance;
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
		let viaBmsCtrl = ViaBmsCtrl.sharedInstance;
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);
		
		viaBmsCtrl.startBmsService();
		
		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "startSDK done!");
		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}
	
	@objc(endSDK:)
	func endSDK(command: CDVInvokedUrlCommand) {
		let viaBmsCtrl = ViaBmsCtrl.sharedInstance;
		var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR);
		
		viaBmsCtrl.stopBmsService();
		
		pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "endSDK done!");
		self.commandDelegate!.send(pluginResult, callbackId: command.callbackId);
	}
}