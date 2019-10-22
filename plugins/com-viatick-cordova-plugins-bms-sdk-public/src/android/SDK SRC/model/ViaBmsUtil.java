package com.viatick.bmsandroidsdk.model;

public class ViaBmsUtil {

    public static class ViaSetting {
        public boolean enableAlert;
        public boolean enableBackground;
        public boolean enableSite;
        public boolean enableAttendance;
        public boolean enableTracking;
        public boolean isModal;

        public ViaSetting() {};

        public void init() {
            enableAlert = false;
            enableBackground = false;
            enableSite = false;
            enableAttendance = false;
            enableTracking = false;
            isModal = false;
        }
    }

    public static class ViaIBeaconRegion {
        public String uuid;
        public int major;
        public int minor;

        public ViaIBeaconRegion() {};

        public void init() {
            uuid = "00000000-0000-0000-0000-000000000000";
            major = 0;
            minor = 0;
        }
    }

    public static class ViaEddystoneRegion {
        public String namespace;
        public String instance;

        public ViaEddystoneRegion() {};

        public void init() {
            namespace = "00000000000000000000";
            instance = "000000000000";
        }
    }

    public static class ViaFetchInterval {
        public int attendance;
        public int tracking;

        public ViaFetchInterval() {};

        public void init() {
            attendance = 0;
            tracking = 0;
        }
    }

    public static class ViaCustomer {
        public static int customerId;
        public static String identifier;
        public static String email;
        public static String phone;
        public static String remark;

        public void init() {
            customerId = 0;
            identifier = "";
            email = "";
            phone = "";
            remark = "BMS iOS SDK v2.0";
        }
    }

    public static class ViaKey {
        public static final String APIKEY = "apiKey";
        public static final String RANGE = "range";
        public static final String IBEACON = "iBeacon";
        public static final String UUID = "uuid";
        public static final String MAJOR = "major";
        public static final String MINOR = "minor";
        public static final String DISTANCE = "distance";
        public static final String EDDYSTONE = "eddystone";
        public static final String NAMESPACE = "namespace";
        public static final String INSTANCE = "instance";
        public static final String FETCHRATE = "fetchRate";
        public static final String ATTENDANCE = "attendance";
        public static final String TRACKING = "tracking";
        public static final String CUSTOMERID = "customerId";
        public static final String IDENTIFIER = "identifier";
        public static final String PHONE = "phone";
        public static final String  EMAIL = "email";
        public static final String  REMARK = "remark";
        public static final String  DATA = "data";
        public static final String  ATTENDANCEID = "attendanceId";
        public static final String  TRACKINGID = "trackingId";
        public static final String  DATE = "date";
        public static final String  TITLE = "title";
        public static final String  DESCRIPTION = "description";
        public static final String  URL = "url";
        public static final String  COVER = "cover";
        public static final String  TYPE = "type";
    }

    public static class ViaApiName {
        public static final String APP_HANDSHAKE = "APP_HANDSHAKE";
        public static final String CORE_CUSTOMER = "CORE_CUSTOMER";
        public static final String CORE_SITE = "CORE_SITE";
        public static final String CORE_ATTENDANCE = "CORE_ATTENDANCE";
        public static final String CORE_TRACKING = "CORE_TRACKING";
        public static final String NOT_FOUND = "NOT_FOUND";
    }

    public static class ViaHeaderKey {
        public static final String API_KEY = "API-Key";
        public static final String SDK_KEY = "SDK-Key";
    }

    public static class ViaPermission {
        public static boolean allowReadPhoneState = false;
        public static boolean isModal = false;
    }

    public class ViaValue {
        public static final String UNRECOGNIZED_USER = "UNRECOGNIZED_USER";
        public static final String ANDROID = "Android";
    }

    public static class ViaConstants {
        public static String MY_RANGING_UNIQUE_ID = "ViaBeacon";

        public static String MINISITE_MENU_UPDATE_RESULT =
                "viabeaconsdk.Service.ViaBeaconService.MINISITE_MENU_UPDATE_RESULT";
        public static String MINISITE_MENU_UPDATE_CONTENT =
                "viabeaconsdk.Service.ViaBeaconService.MINISITE_MENU_UPDATE_CONTENT";

        public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        public static final int MY_REQUEST_READ_PHONE_STATE = 2;
    }
}
