cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "com-viatick-cordova-plugins-bms-sdk-public.BmsCordovaSdkPublic",
      "file": "plugins/com-viatick-cordova-plugins-bms-sdk-public/www/BmsCordovaSdkPublic.js",
      "pluginId": "com-viatick-cordova-plugins-bms-sdk-public",
      "clobbers": [
        "cordova.plugins.BmsCordovaSdkPublic"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-add-swift-support": "2.0.2",
    "com-viatick-cordova-plugins-bms-sdk-public": "0.1.2",
    "cordova-plugin-whitelist": "1.3.4"
  };
});