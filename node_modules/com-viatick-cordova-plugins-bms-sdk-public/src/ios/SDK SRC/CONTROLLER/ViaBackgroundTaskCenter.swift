//
//  BackgroundTaskCenter.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 25/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import Foundation
import UIKit

class ViaBackgroundTaskCenter {
    
    var backgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid;
    
    func registerBackgroundTask() {
        backgroundTask = UIApplication.shared.beginBackgroundTask { [weak self] in
            self?.endBackgroundTask();
        }
        assert(backgroundTask != UIBackgroundTaskInvalid);
    }
    
    func endBackgroundTask() {
        UIApplication.shared.endBackgroundTask(backgroundTask);
        backgroundTask = UIBackgroundTaskInvalid;
    }
}
