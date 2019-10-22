//
//  ViaBmsStructs.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 24/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import Foundation

struct ViaSetting {
    var enableAlert: Bool;
    var enableBackground: Bool;
    var enableSite: Bool;
    var enableAttendance: Bool;
    var enableTracking: Bool;
    var isModal: Bool;
    init() {
        enableAlert = false;
        enableBackground = false;
        enableSite = false;
        enableAttendance = false;
        enableTracking = false;
        isModal = false;
    }
}

struct ViaIBeaconRegion {
    var uuid: String;
    var major: Int;
    var minor: Int;
    init() {
        uuid = "00000000-0000-0000-0000-000000000000";
        major = 0;
        minor = 0;
    }
}

struct ViaEddystoneRegion {
    var namespace: String;
    var instance: String;
    init() {
        namespace = "00000000000000000000";
        instance = "000000000000";
    }
}

struct ViaFetchInterval {
    var attendance: Int;
    var tracking: Int;
    init() {
        attendance = 0;
        tracking = 0;
    }
}

struct ViaCustomer {
    var customerId: Int;
    var identifier: String;
    var email: String;
    var phone: String;
    var remark: String;
    init() {
        customerId = 0;
        identifier = "";
        email = "";
        phone = "";
        remark = "BMS iOS SDK v2.0";
    }
}

enum ViaKey: String {
    case APIKEY = "apiKey";
    case RANGE = "range";
    case IBEACON = "iBeacon";
    case UUID = "uuid";
    case MAJOR = "major";
    case MINOR = "minor";
    case DISTANCE = "distance";
    case EDDYSTONE = "eddystone";
    case NAMESPACE = "namespace";
    case INSTANCE = "instance";
    case FETCHRATE = "fetchRate";
    case ATTENDANCE = "attendance";
    case TRACKING = "tracking";
    case CUSTOMERID = "customerId";
    case IDENTIFIER = "identifier";
    case PHONE = "phone";
    case EMAIL = "email";
    case REMARK = "remark";
    case DATA = "data";
    case ATTENDANCEID = "attendanceId";
    case TRACKINGID = "trackingId";
    case DATE = "date";
    case TITLE = "title";
    case DESCRIPTION = "description";
    case URL = "url";
    case COVER = "cover";
    case TYPE = "type";
}

enum ViaApiName: String {
    case APP_HANDSHAKE = "APP_HANDSHAKE";
    case CORE_CUSTOMER = "CORE_CUSTOMER";
    case CORE_SITE = "CORE_SITE";
    case CORE_ATTENDANCE = "CORE_ATTENDANCE";
    case CORE_TRACKING = "CORE_TRACKING";
    case NOT_FOUND = "NOT_FOUND";
}

enum ViaHeaderKey: String {
    case API_KEY = "API-Key";
    case SDK_KEY = "SDK-Key";
}
