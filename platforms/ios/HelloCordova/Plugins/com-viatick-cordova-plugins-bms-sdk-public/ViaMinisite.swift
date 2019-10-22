//
//  ViaMinisite.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 25/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import Foundation

struct ViaMinisite {
    var title: String;
    var description: String;
    var coverUrl: String;
    var url: String;
    var type: String;
    init(title: String, description: String, coverUrl: String, url: String, type: String) {
        self.title = title;
        self.description = description;
        self.coverUrl = coverUrl;
        self.url = url;
        self.type = type;
    }
    func same(viaMinisite: ViaMinisite) -> Bool {
        if self.title == viaMinisite.title && self.description == viaMinisite.description {
            return true
        } else {
            return false
        }
    }
}
