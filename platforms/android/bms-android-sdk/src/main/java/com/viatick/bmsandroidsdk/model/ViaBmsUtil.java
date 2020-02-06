package com.viatick.bmsandroidsdk.model;

public class ViaBmsUtil {

    public static class ViaIBeaconRegion {
        public String uuid;
        public int major;
        public int minor;

        public ViaIBeaconRegion() {
        }

        public void init() {
            uuid = "00000000-0000-0000-0000-000000000000";
            major = 0;
            minor = 0;
        }
    }

    public static class ViaEddystoneRegion {
        public String namespace;
        public String instance;

        public ViaEddystoneRegion() {
        }

        ;

        public void init() {
            namespace = "00000000000000000000";
            instance = "000000000000";
        }
    }

    public static class ViaFetchInterval {
        public int attendance;
        public int tracking;

        public ViaFetchInterval() {
        }

        ;

        public void init() {
            attendance = 0;
            tracking = 0;
        }
    }

    public static class ViaCustomer {
        private int customerId;
        private String identifier;
        private String email;
        private String phone;
        private String remark;
        private String os;
        private String uuid;
        private int major;
        private int minor;

        public ViaCustomer() {
        }

        public ViaCustomer(int customerId, String identifier, String email,
                           String phone, String remark, String os, String uuid, int major, int minor) {
            this.customerId = customerId;
            this.identifier = identifier;
            this.email = email;
            this.phone = phone;
            this.remark = remark;
            this.os = os;
            this.uuid = uuid;
            this.major = major;
            this.minor = minor;
        }

        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public int getMajor() {
            return major;
        }

        public void setMajor(int major) {
            this.major = major;
        }

        public int getMinor() {
            return minor;
        }

        public void setMinor(int minor) {
            this.minor = minor;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }
    }

    public static class ViaPermission {
        public static boolean allowReadPhoneState = false;
        public static boolean isModal = false;
    }

    public static class ViaConstants {
        public static String MY_RANGING_UNIQUE_ID = "ViaBeacon";

        public static String MINISITE_MENU_UPDATE_RESULT =
                "viabeaconsdk.Service.ViaBeaconService.MINISITE_MENU_UPDATE_RESULT";
        public static String MINISITE_MENU_UPDATE_CONTENT =
                "viabeaconsdk.Service.ViaBeaconService.MINISITE_MENU_UPDATE_CONTENT";
        public static String MINISITE_VIEW_UPDATE_INTENT =
                "viabeaconsdk.Service.ViaBeaconService.MINISITE_VIEW_UPDATE_INTENT";
        public static String MINISITE_VIEW_CLOSE_INTENT =
                "viabeaconsdk.Service.ViaBeaconService.MINISITE_VIEW_CLOSE_INTENT";
        public static String NEXT_MINISITE_VIEW_INTENT =
                "viabeaconsdk.Service.ViaBeaconService.NEXT_MINISITE_VIEW_INTENT";

        public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        public static final int MY_REQUEST_READ_PHONE_STATE = 2;
    }

    public enum AttendanceStatus {
        CHECKOUT,
        PRE_CHECKIN,
        CHECKIN
    }

    public enum MinisiteType {
        ADVERTISEMENT,
        COUPON,
        DEEP_LINK,
        VOTING,
        POLLING
    }


    public enum MinisiteViewType {
        LIST,
        AUTO;

        private MinisiteViewType() {
        }
    }


}
