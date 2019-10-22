//
//  ApiCtrl.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 23/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import Foundation

protocol ViaApiRequestDelegate {
    func response(name: ViaApiName, code: Int, success data: Dictionary<String, Any>);
    func response(name: ViaApiName, code: Int, success info: String);
    func response(name: ViaApiName, code: Int, error msg: String);
}

class ViaApiCtrl: NSObject {
    let SDK_ENDPOINT = "https://bms.viatick.com/bms/sdk/v1";
    let APP_HANDSHAKE = "/app/handshake";
    let CORE_CUSTOMER = "/core/customer";
    let API_ENDPOINT = "https://bms.viatick.com/bms/api/v1";
    let CORE_SITE = "/core/site";
    let CORE_ATTENDANCE = "/core/attendance";
    let CORE_TRACKING = "/core/tracking";
    
    var delegate: ViaApiRequestDelegate?;
    
    func sendGetRequest(url: URL, input: Dictionary<String, String>, params: Array<String>, headers: Dictionary<String, String>) {
        var actualUrl: URL = url;
        for p in params {
            actualUrl.appendPathComponent(p);
        }
        var urlComponents = URLComponents(url: actualUrl, resolvingAgainstBaseURL: true);
        var queryItems: [URLQueryItem] = [];
        for (k, v) in input {
            queryItems.append(URLQueryItem(name: k, value: v));
        }
        urlComponents?.queryItems = queryItems;
        var request = URLRequest(url: (urlComponents?.url)!, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 5);
        request.httpMethod = "GET";
        request.allHTTPHeaderFields = headers;
        let dataTask = URLSession.shared.dataTask(with: request, completionHandler: responseHandler);
        dataTask.resume();
    }
    
    func sendPostRequest(url: URL, input: Dictionary<String, Any>, params: Array<String>, headers: Dictionary<String, String>) {
        var actualUrl: URL = url;
        for p in params {
            actualUrl.appendPathComponent(p);
        }
        var request = URLRequest(url: actualUrl, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 5);
        request.httpMethod = "POST";
        request.allHTTPHeaderFields = headers;
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: input, options: .prettyPrinted)
        } catch {
            // Nothing...
        }
        let dataTask = URLSession.shared.dataTask(with: request, completionHandler: responseHandler);
        dataTask.resume();
    }
    
    func sendPutRequest(url: URL, input: Dictionary<String, Any>, params: Array<String>, headers: Dictionary<String, String>) {
        var actualUrl: URL = url;
        for p in params {
            actualUrl.appendPathComponent(p);
        }
        var request = URLRequest(url: actualUrl, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 5);
        request.httpMethod = "PUT";
        request.allHTTPHeaderFields = headers;
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: input, options: .prettyPrinted)
        } catch {
            // Nothing...
        }
        let dataTask = URLSession.shared.dataTask(with: request, completionHandler: responseHandler);
        dataTask.resume();
    }
    
    func responseHandler(data: Data?, response: URLResponse?, error: Error?) -> Void {
        var name: ViaApiName = ViaApiName.NOT_FOUND;
        if let httpResponse = response as? HTTPURLResponse {
            let urlString = httpResponse.url!.absoluteString;
            if indexOf(string: urlString, text: APP_HANDSHAKE) != -1 {
                name = ViaApiName.APP_HANDSHAKE;
            }  else if indexOf(string: urlString, text: CORE_CUSTOMER) != -1 {
                name = ViaApiName.CORE_CUSTOMER;
            }  else if indexOf(string: urlString, text: CORE_SITE) != -1 {
                name = ViaApiName.CORE_SITE;
            }  else if indexOf(string: urlString, text: CORE_ATTENDANCE) != -1 {
                name = ViaApiName.CORE_ATTENDANCE;
            }  else if indexOf(string: urlString, text: CORE_TRACKING) != -1 {
                name = ViaApiName.CORE_TRACKING;
            }
            switch(httpResponse.statusCode) {
            case 200:
                do {
                    let dataDictionary: Dictionary<String, Any> = try JSONSerialization.jsonObject(with: data!, options: []) as! Dictionary<String, Any>;
                    delegate?.response(name: name, code: httpResponse.statusCode, success: dataDictionary);
                } catch {
                    let dataString: String = String(data: data!, encoding: .utf8)!;
                    delegate?.response(name: name, code: httpResponse.statusCode, success: dataString);
                }
                break;
            default:
                let dataString: String = String(data: data!, encoding: .utf8)!;
                delegate?.response(name: name, code: httpResponse.statusCode, error: dataString);
            }
        } else {
            delegate?.response(name: name, code: 0, error: "something went wrong...");
        }
    }
    
    func indexOf(string: String, text: String) -> Int {
        if let range = string.range(of: text, options: []) {
            return range.lowerBound.hashValue / 4;
        } else {
            return -1;
        }
    }
}
