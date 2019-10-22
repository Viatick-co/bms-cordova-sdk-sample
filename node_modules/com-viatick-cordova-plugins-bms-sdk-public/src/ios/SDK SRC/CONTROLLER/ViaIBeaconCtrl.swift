//
//  ViaIBeaconCtrl.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 10/3/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import Foundation
import CoreLocation

protocol ViaIBeaconCtrlDelegate {
    func viaIBeaconCtrl(controller: ViaIBeaconCtrl, discover viaIBeacon: ViaIBeacon);
    func viaIbeaconCtrl(controller: ViaIBeaconCtrl, rangeBeacons viaIBeacons: [ViaIBeacon]);
    func viaIbeaconCtrl(controller: ViaIBeaconCtrl, didEnterRegion status: Bool);
}

class ViaIBeaconCtrl: NSObject {
    var viaIBeaconRegion: ViaIBeaconRegion?;
    
    var locationManager = CLLocationManager()
    var delegate: ViaIBeaconCtrlDelegate?
    
    var viaIBeacons: [ViaIBeacon] = []
    
    func initiate(viaIBeaconRegion: ViaIBeaconRegion) {
        self.viaIBeaconRegion = viaIBeaconRegion;
        locationManager.delegate = self
        locationManager.requestAlwaysAuthorization()
    }
    
    func iBeaconRegion() -> CLBeaconRegion {
        let identifier: String = "ble.viatick.com"
        let uuid = UUID(uuidString: viaIBeaconRegion!.uuid)
        var beaconRegion: CLBeaconRegion;
        if(viaIBeaconRegion!.major == 0 && viaIBeaconRegion!.minor == 0) {
            beaconRegion = CLBeaconRegion(proximityUUID: uuid!, identifier: identifier)
        } else if(viaIBeaconRegion!.minor == 0) {
            beaconRegion = CLBeaconRegion(proximityUUID: uuid!, major: CLBeaconMajorValue(viaIBeaconRegion!.major), identifier: identifier)
        } else {
            beaconRegion = CLBeaconRegion(proximityUUID: uuid!, major: CLBeaconMajorValue(viaIBeaconRegion!.major), minor: CLBeaconMinorValue(viaIBeaconRegion!.minor), identifier: identifier)
        }
        beaconRegion.notifyEntryStateOnDisplay = true;
        beaconRegion.notifyOnEntry = true;
        beaconRegion.notifyOnExit = true;
        return beaconRegion;
    }
    
    func startRange() {
        let region: CLBeaconRegion = iBeaconRegion();
        locationManager.startMonitoring(for: region);
        locationManager.startRangingBeacons(in: region);
    }
    
    func stopRange() {
        if viaIBeaconRegion != nil {
            let region: CLBeaconRegion = iBeaconRegion();
            locationManager.stopMonitoring(for: region);
            locationManager.stopRangingBeacons(in: region);
        }
        viaIBeacons = [];
    }
    
    func processCLBeacons(beacons: [CLBeacon]) {
        for (i, _) in viaIBeacons.enumerated() {
            viaIBeacons[i].disappearIdx += 1;
        }
        for b in beacons {
            let newViaIBeacon: ViaIBeacon = ViaIBeacon(iBeacon: b, maxDistance: 200, isRequested: false, disappearIdx: 0);
            let index = indexOf(viaIBeacons: viaIBeacons, viaIbeacon: newViaIBeacon);
            if index == -1 {
                viaIBeacons.append(newViaIBeacon);
                delegate?.viaIBeaconCtrl(controller: self, discover: newViaIBeacon);
            } else {
                viaIBeacons[index].iBeacon = b;
                if(viaIBeacons[index].disappearIdx > 60) {
                    delegate?.viaIBeaconCtrl(controller: self, discover: newViaIBeacon);
                }
                viaIBeacons[index].disappearIdx = 0;
            }
        }
        // print("viaIBeacons", viaIBeacons);
        delegate?.viaIbeaconCtrl(controller: self, rangeBeacons: viaIBeacons);
    }
    
    func processEnter() {
        delegate?.viaIbeaconCtrl(controller: self, rangeBeacons: viaIBeacons);
    }
    
    func indexOf(viaIBeacons: [ViaIBeacon], viaIbeacon: ViaIBeacon) -> Int {
        var index: Int = -1
        for (i, b) in viaIBeacons.enumerated() {
            if b.same(viaIBeacon: viaIbeacon) {
                index = i
                break
            }
        }
        return index
    }
}

extension ViaIBeaconCtrl: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        // print("didChangeAuthorization", status.rawValue)
    }
    
    func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        // print("didEnterRegion", region.description) // TODO
        delegate?.viaIbeaconCtrl(controller: self, didEnterRegion: true);
    }
    
    func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion) {
        // print("didRangeBeacons", region.description);
        // print("didRangeBeacons", beacons.description);
        processCLBeacons(beacons: beacons);
    }
    
    func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        // print("didExitRegion", region.description) // TODO
        delegate?.viaIbeaconCtrl(controller: self, didEnterRegion: false);
    }
}
