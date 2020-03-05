var exec = require('cordova/exec');

exports.initSDK = function (sdkKey, success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'initSDK', [sdkKey]);
};

exports.initCustomer = function (identifier, phone, email, success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'initCustomer', [identifier, phone, email]);
};

exports.setting = function (alert, background, site, minisitesView,
  autoSiteDuration, tracking, enableMQTT, attendance, checkinDuration, checkoutDuration,
  iBeacons, bmsEnvironment, beaconRegionRange, beaconRegionUUIDFilter, isBroadcasting, proximityAlert, proximityAlertThreshold, success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'setting', [alert, background, site, minisitesView,
      autoSiteDuration, tracking, enableMQTT, attendance, checkinDuration, checkoutDuration, iBeacons, bmsEnvironment,
      beaconRegionRange,
      beaconRegionUUIDFilter,
      isBroadcasting,
      proximityAlert,
      proximityAlertThreshold]);
};

exports.startSDK = function (success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'startSDK', []);
};

exports.endSDK = function (success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'endSDK', []);
};

exports.checkIn = function (success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'checkIn', []);
};

exports.checkOut = function (success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'checkOut', []);
};

exports.onDistanceBeacons = function (success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'onDistanceBeacons', []);
};

exports.openDeviceSite = function (url, success, error) {
    exec(success, error, 'BmsCordovaSdkPublic', 'openDeviceSite', [url]);
};
