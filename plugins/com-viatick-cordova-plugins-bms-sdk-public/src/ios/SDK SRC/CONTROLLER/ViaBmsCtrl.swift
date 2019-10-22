//
//  ViaBMSCtrl.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 23/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import Foundation
import UIKit

protocol ViaBmsCtrlDelegate {
    func viaBmsCtrl(controller: ViaBmsCtrl, inited status: Bool);
}

class ViaBmsCtrl: NSObject {
    static let sharedInstance = ViaBmsCtrl();
    
    var SDK_KEY: String = "";
    var API_KEY: String = "";
    var SETTING: ViaSetting = ViaSetting();
    var CUSTOMER: ViaCustomer = ViaCustomer();
    var IBEACON_REGION: ViaIBeaconRegion = ViaIBeaconRegion();
    var EDDYSTONE_REGION: ViaEddystoneRegion = ViaEddystoneRegion();
    var FETCH_INTERVAL: ViaFetchInterval = ViaFetchInterval();
    
    var MINISITES: [ViaMinisite] = [];
    
    var delegate: ViaBmsCtrlDelegate?;
    
    let viaApiCtrl = ViaApiCtrl();
    let viaIBeaconCtrl = ViaIBeaconCtrl();
    
    var parentUIViewController: UIViewController?;
    let viaMinisiteTableViewController: ViaMinisiteTableViewController = ViaMinisiteTableViewController();
    
    let viaNotificationCenter: ViaNotificationCenter = ViaNotificationCenter();
    let viaBackgroundTaskCenter: ViaBackgroundTaskCenter = ViaBackgroundTaskCenter();
    
    func initSdk(uiViewController: UIViewController, sdk_key: String) {
        parentUIViewController = uiViewController;
        SDK_KEY = sdk_key;
        viaNotificationCenter.initiate();
        viaApiCtrl.delegate = self;
        let input: Dictionary<String, String> = [
            "_": String(Date().timeIntervalSince1970.hashValue)
        ];
        let params: [String] = [];
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.SDK_KEY.rawValue: SDK_KEY
        ];
        let url: URL = URL(string: viaApiCtrl.SDK_ENDPOINT + viaApiCtrl.APP_HANDSHAKE)!;
        viaApiCtrl.sendGetRequest(url: url, input: input, params: params, headers: headers);
    }
    
    func setting(alert: Bool, background: Bool, site: Bool, attendance: Bool, tracking: Bool) {
        SETTING.enableAlert = alert;
        SETTING.enableBackground = background;
        SETTING.enableSite = site;
        SETTING.enableAttendance = attendance;
        SETTING.enableTracking = tracking;
    }
    
    func initCustomer(identifier: String, phone: String, email: String) {
        CUSTOMER.identifier = identifier;
        CUSTOMER.email = email;
        CUSTOMER.phone = phone;
    }
    
    func initSdkHandler(data: Dictionary<String, Any>) {
        API_KEY = data[ViaKey.APIKEY.rawValue] as! String;
        if let range: Dictionary<String, Any> = data[ViaKey.RANGE.rawValue] as? Dictionary<String, Any> {
            if let ibeacon: Dictionary<String, Any> = range[ViaKey.IBEACON.rawValue] as? Dictionary<String, Any> {
                if let uuid = ibeacon[ViaKey.UUID.rawValue] {
                    IBEACON_REGION.uuid = uuid as! String;
                }
                if let major = ibeacon[ViaKey.MAJOR.rawValue] {
                    IBEACON_REGION.major = major as! Int;
                }
                if let minor = ibeacon[ViaKey.MINOR.rawValue] {
                    IBEACON_REGION.minor = minor as! Int;
                }
            }
            if let eddystone: Dictionary<String, String> = range[ViaKey.EDDYSTONE.rawValue] as? Dictionary<String, String> {
                if let namespace = eddystone[ViaKey.NAMESPACE.rawValue] {
                    EDDYSTONE_REGION.namespace = namespace;
                }
                if let instance = eddystone[ViaKey.INSTANCE.rawValue] {
                    EDDYSTONE_REGION.instance = instance;
                }
            }
        }
        if let fetchRate: Dictionary<String, Int> = data[ViaKey.FETCHRATE.rawValue] as? Dictionary<String, Int> {
            if let attendance = fetchRate[ViaKey.ATTENDANCE.rawValue] {
                FETCH_INTERVAL.attendance = attendance;
            }
            if let tracking = fetchRate[ViaKey.TRACKING.rawValue] {
                FETCH_INTERVAL.tracking = tracking;
            }
        }
        // print(API_KEY, IBEACON_REGION, EDDYSTONE_REGION, FETCH_INTERVAL);
        exchangeCustomer();
    }
    
    func exchangeCustomer() {
        viaApiCtrl.delegate = self;
        let input: Dictionary<String, String> = [
            ViaKey.IDENTIFIER.rawValue: CUSTOMER.identifier == "" ? (UIDevice.current.identifierForVendor?.uuidString)! : CUSTOMER.identifier,
            ViaKey.PHONE.rawValue: CUSTOMER.phone,
            ViaKey.EMAIL.rawValue: CUSTOMER.email,
            ViaKey.REMARK.rawValue: UIDevice.current.name + " : " + UIDevice.current.model + " : " + UIDevice.current.systemName + " : " + UIDevice.current.systemVersion
        ];
        let params: [String] = [];
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.SDK_KEY.rawValue: SDK_KEY
        ];
        let url: URL = URL(string: viaApiCtrl.SDK_ENDPOINT + viaApiCtrl.CORE_CUSTOMER)!;
        viaApiCtrl.sendPostRequest(url: url, input: input, params: params, headers: headers);
    }
    
    func exchangeCustomerHandler(data: Dictionary<String, Any>) {
        CUSTOMER.customerId = data[ViaKey.CUSTOMERID.rawValue] as! Int;
        CUSTOMER.identifier = data[ViaKey.IDENTIFIER.rawValue] as! String;
        CUSTOMER.email = data[ViaKey.EMAIL.rawValue] as! String;
        CUSTOMER.phone = data[ViaKey.PHONE.rawValue] as! String;
        CUSTOMER.remark = data[ViaKey.REMARK.rawValue] as! String;
        // print("[VIATICK]: customer", CUSTOMER);
        delegate?.viaBmsCtrl(controller: self, inited: true);
    }
    
    func startBmsService() {
        if SETTING.enableBackground {
            if viaBackgroundTaskCenter.backgroundTask != UIBackgroundTaskInvalid {
                viaBackgroundTaskCenter.endBackgroundTask();
            }
            viaBackgroundTaskCenter.registerBackgroundTask();
        }
        viaIBeaconCtrl.delegate = self;
        viaIBeaconCtrl.initiate(viaIBeaconRegion: IBEACON_REGION);
        viaIBeaconCtrl.startRange();
    }
    
    func stopBmsService() {
        if SETTING.enableBackground {
            if viaBackgroundTaskCenter.backgroundTask != UIBackgroundTaskInvalid {
                viaBackgroundTaskCenter.endBackgroundTask();
            }
        }
        viaIBeaconCtrl.stopRange();
        MINISITES.removeAll();
        SETTING.isModal = false;
    }
    
    func siteRequest(viaIBeacon: ViaIBeacon) {
        viaApiCtrl.delegate = self;
        let input: Dictionary<String, String> = [
            "_": String(Date().timeIntervalSince1970.hashValue)
        ];
        let params: [String] = [
            viaIBeacon.iBeacon.proximityUUID.uuidString,
            viaIBeacon.iBeacon.major.stringValue,
            viaIBeacon.iBeacon.minor.stringValue
        ];
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.API_KEY.rawValue: API_KEY
        ];
        let url: URL = URL(string: viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_SITE)!;
        viaApiCtrl.sendGetRequest(url: url, input: input, params: params, headers: headers);
    }
    
    func siteRequestHandler(data: Dictionary<String, Any>) {
        let title: String = data[ViaKey.TITLE.rawValue] as! String;
        let description: String = data[ViaKey.DESCRIPTION.rawValue] as! String;
        let url: String = data[ViaKey.URL.rawValue] as! String;
        var coverUrl: String = "";
        if let cover: Dictionary<String, Any> = data[ViaKey.COVER.rawValue] as? Dictionary<String, Any> {
            coverUrl = cover[ViaKey.URL.rawValue] as! String;
        }
        let type: String = data[ViaKey.TYPE.rawValue] as! String;
        let viaMinisite: ViaMinisite = ViaMinisite(title: title, description: description, coverUrl: coverUrl, url: url, type: type);
        let index = indexOf(viaMinisites: MINISITES, viaMinisite: viaMinisite);
        if index == -1 {
            MINISITES.append(viaMinisite);
        }
        // print("[VIATICK]: minisites", MINISITES);
        if SETTING.enableAlert {
            viaNotificationCenter.shootNotification(title: title, body: description);
        }
        openMinisiteTable();
    }
    
    func attendanceRequest(viaIBeacons: [ViaIBeacon]) {
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.API_KEY.rawValue: API_KEY
        ];
        let url: URL = URL(string: viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_ATTENDANCE)!;
        for vb in viaIBeacons {
            let data: Dictionary<String, Any> = [:];
            let distance: Double = -1 * vb.iBeacon.accuracy.distance(to: 0);
            if distance > 0 {
                let input: Dictionary<String, Any> = [
                    ViaKey.UUID.rawValue: vb.iBeacon.proximityUUID.uuidString,
                    ViaKey.MAJOR.rawValue: vb.iBeacon.major,
                    ViaKey.MINOR.rawValue: vb.iBeacon.minor,
                    ViaKey.DISTANCE.rawValue: distance,
                    ViaKey.IDENTIFIER.rawValue: CUSTOMER.identifier,
                    ViaKey.PHONE.rawValue: CUSTOMER.phone,
                    ViaKey.EMAIL.rawValue: CUSTOMER.email,
                    ViaKey.REMARK.rawValue: CUSTOMER.remark,
                    ViaKey.DATA.rawValue: data
                ];
                viaApiCtrl.sendPostRequest(url: url, input: input, params: [], headers: headers);
            }
        }
    }
    
    func attendanceCheckOutRequest(viaIBeacons: [ViaIBeacon]) {
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.API_KEY.rawValue: API_KEY
        ];
        let url: URL = URL(string: viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_ATTENDANCE)!;
        for vb in viaIBeacons {
            if vb.disappearIdx == 10 {
                let data: Dictionary<String, Any> = [:];
                let distance: Double = -1 * vb.iBeacon.accuracy.distance(to: 0);
                let input: Dictionary<String, Any> = [
                    ViaKey.UUID.rawValue: vb.iBeacon.proximityUUID.uuidString,
                    ViaKey.MAJOR.rawValue: vb.iBeacon.major,
                    ViaKey.MINOR.rawValue: vb.iBeacon.minor,
                    ViaKey.DISTANCE.rawValue: distance,
                    ViaKey.IDENTIFIER.rawValue: CUSTOMER.identifier,
                    ViaKey.PHONE.rawValue: CUSTOMER.phone,
                    ViaKey.EMAIL.rawValue: CUSTOMER.email,
                    ViaKey.REMARK.rawValue: CUSTOMER.remark,
                    ViaKey.DATA.rawValue: data
                ];
                viaApiCtrl.sendPutRequest(url: url, input: input, params: [], headers: headers);
            }
        }
    }
    
    func attendanceRequestHandler(data: Dictionary<String, Any>) {
        let _: Int = data[ViaKey.ATTENDANCEID.rawValue] as! Int;
        let _: String = data[ViaKey.DATE.rawValue] as! String;
        // print("[VIATICK]: attendance", attendanceId, date);
    }
    
    func trackingRequest(viaIBeacons: [ViaIBeacon]) {
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.API_KEY.rawValue: API_KEY
        ];
        let url: URL = URL(string: viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_TRACKING)!;
        for vb in viaIBeacons {
            let data: Dictionary<String, Any> = [:];
            let distance: Double = -1 * vb.iBeacon.accuracy.distance(to: 0);
            if distance > 0 {
                let input: Dictionary<String, Any> = [
                    ViaKey.UUID.rawValue: vb.iBeacon.proximityUUID.uuidString,
                    ViaKey.MAJOR.rawValue: vb.iBeacon.major,
                    ViaKey.MINOR.rawValue: vb.iBeacon.minor,
                    ViaKey.DISTANCE.rawValue: distance,
                    ViaKey.IDENTIFIER.rawValue: CUSTOMER.identifier,
                    ViaKey.PHONE.rawValue: CUSTOMER.phone,
                    ViaKey.EMAIL.rawValue: CUSTOMER.email,
                    ViaKey.REMARK.rawValue: CUSTOMER.remark,
                    ViaKey.DATA.rawValue: data
                ];
                viaApiCtrl.sendPostRequest(url: url, input: input, params: [], headers: headers);
            }
        }
    }
    
    func trackingRequestHandler(data: Dictionary<String, Any>) {
        let _: Int = data[ViaKey.TRACKINGID.rawValue] as! Int;
        let _: Double = data[ViaKey.DISTANCE.rawValue] as! Double;
        let _: String = data[ViaKey.DATE.rawValue] as! String;
        // print("[VIATICK]: tracking", trackingId, distance, date);
    }
    
    func openMinisiteTable() {
        DispatchQueue.main.async {
            if self.SETTING.isModal {
                self.viaMinisiteTableViewController.update(minisites: self.MINISITES);
            } else {
                self.viaMinisiteTableViewController.initiate(viaBmsCtrl: self, minisites: self.MINISITES, customer: self.CUSTOMER, apiKey: self.API_KEY);
                self.viaMinisiteTableViewController.modalTransitionStyle = UIModalTransitionStyle.coverVertical;
                self.parentUIViewController?.present(self.viaMinisiteTableViewController, animated: true, completion: nil);
                self.SETTING.isModal = true;
            }
        }
    }
    
    func indexOf(viaMinisites: [ViaMinisite], viaMinisite: ViaMinisite) -> Int {
        var index: Int = -1;
        for (i, m) in viaMinisites.enumerated() {
            if m.same(viaMinisite: viaMinisite) {
                index = i;
                break
            }
        }
        return index
    }
}

extension ViaBmsCtrl: ViaApiRequestDelegate {
    func response(name: ViaApiName, code: Int, success data: Dictionary<String, Any>) {
        switch(name) {
        case .APP_HANDSHAKE:
            initSdkHandler(data: data);
            break;
        case .CORE_CUSTOMER:
            exchangeCustomerHandler(data: data);
            break;
        case .CORE_SITE:
            siteRequestHandler(data: data);
            break;
        case .CORE_ATTENDANCE:
            attendanceRequestHandler(data: data);
            break;
        case .CORE_TRACKING:
            trackingRequestHandler(data: data);
            break;
        default: break;
        }
    }
    
    func response(name: ViaApiName, code: Int, success info: String) {
        print(name, code, info);
    }
    
    func response(name: ViaApiName, code: Int, error msg: String) {
        print(name, code, msg);
    }
    
}

extension ViaBmsCtrl: ViaIBeaconCtrlDelegate {
    func viaIBeaconCtrl(controller: ViaIBeaconCtrl, discover viaIBeacon: ViaIBeacon) {
        if SETTING.enableSite {
            siteRequest(viaIBeacon: viaIBeacon);
        }
    }
    
    func viaIbeaconCtrl(controller: ViaIBeaconCtrl, rangeBeacons viaIBeacons: [ViaIBeacon]) {
        if SETTING.enableAttendance {
            attendanceRequest(viaIBeacons: viaIBeacons);
            attendanceCheckOutRequest(viaIBeacons: viaIBeacons);
        }
        if SETTING.enableTracking {
            trackingRequest(viaIBeacons: viaIBeacons);
        }
    }
    
    func viaIbeaconCtrl(controller: ViaIBeaconCtrl, didEnterRegion status: Bool) {
        // viaNotificationCenter.shootNotification(title: "enter", body: status.description);
    }
}
