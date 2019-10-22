//
//  ViaMinisiteViewController.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 25/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import UIKit

class ViaMinisiteViewController: UIViewController {
    
    var minisite: ViaMinisite?
    var customer: ViaCustomer?
    var buttons: [UIButton] = [];
    
    var API_KEY: String?;
    
    let viaApiCtrl: ViaApiCtrl = ViaApiCtrl();
    
    @IBOutlet weak var minisiteWebView: UIWebView!

    override func viewDidLoad() {
        super.viewDidLoad();
        
        viaApiCtrl.delegate = self;
        minisiteWebView.delegate = self;
        // Do any additional setup after loading the view.
    }
    
    override func viewWillAppear(_ animated: Bool) {
        // createButtons();
        let cidString: String = String(customer!.customerId);
        let modifiedUrl: String = (minisite?.url)! + "&cid=" + cidString;
        let url: URL = URL(string: modifiedUrl)!;
        let request: URLRequest = URLRequest(url: url);
        minisiteWebView.loadRequest(request);
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews();
        createButtons();
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        for b in buttons {
            b.removeFromSuperview();
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning();
        // Dispose of any resources that can be recreated.
    }
    
    func createButtons() {
        let width = self.view.frame.size.width;
        let height = self.view.frame.size.height;
        // dismiss
        let dismissBtn = UIButton();
        dismissBtn.frame = CGRect(x: width - 50, y: height - 50, width: 40, height: 40);
        dismissBtn.backgroundColor = UIColor(red: 204/255, green: 230/255, blue: 255/255, alpha: 1);
        let origIcon = UIImage(named: "ic_reply");
        let tintedIcon = origIcon?.withRenderingMode(UIImageRenderingMode.alwaysTemplate);
        dismissBtn.setImage(tintedIcon, for: .normal);
        dismissBtn.tintColor = UIColor(red: 51/255, green: 204/255, blue: 204/255, alpha: 1);
        dismissBtn.addTarget(self, action: #selector(dismissAction), for: .touchUpInside);
        dismissBtn.layer.cornerRadius = 5;
        self.view.addSubview(dismissBtn);
        buttons.append(dismissBtn);
        if minisite?.type != "coupon" {
            let shareBtn = UIButton();
            shareBtn.frame = CGRect(x: width - 100, y: height - 50, width: 40, height: 40);
            shareBtn.backgroundColor = UIColor(red: 204/255, green: 230/255, blue: 255/255, alpha: 1);
            let origIcon2 = UIImage(named: "ic_share");
            let tintedIcon2 = origIcon2?.withRenderingMode(UIImageRenderingMode.alwaysTemplate);
            shareBtn.setImage(tintedIcon2, for: .normal);
            shareBtn.tintColor = UIColor(red: 51/255, green: 204/255, blue: 204/255, alpha: 1);
            shareBtn.addTarget(self, action: #selector(shareAction), for: .touchUpInside);
            shareBtn.layer.cornerRadius = 5;
            self.view.addSubview(shareBtn);
            buttons.append(shareBtn);
        }
    }
    
    @objc func dismissAction(sender: UIButton!) {
        dismiss(animated: true, completion: nil);
    }
    
    @objc func shareAction(sender: UIButton!) {
        shareActivity();
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */
    
    func initiate(minisite: ViaMinisite, customer: ViaCustomer, apiKey: String) {
        self.minisite = minisite;
        self.customer = customer;
        API_KEY = apiKey;
    }
    
    func shareActivity() {
        // let text = minisite?.url;
        let url: URL = URL(string: (minisite?.url)!)!;
        let textToShare: [Any] = [ url ];
        let activityViewController = UIActivityViewController(activityItems: textToShare, applicationActivities: nil);
        activityViewController.popoverPresentationController?.sourceView = self.view;
        activityViewController.excludedActivityTypes = [];
        self.present(activityViewController, animated: true, completion: nil);
        activityViewController.completionWithItemsHandler = { (s, ok, items, error) in
            // print("[VIATICK]: share", s?.rawValue ?? "", ok);
            if ok {
                let code: String = self.minisite?.url.components(separatedBy: "?code=")[1] ?? "";
                self.bccShare(code: code, customer: self.customer!);
            }
        }
    }
    
    func bccShare(code: String, customer: ViaCustomer) {
        let input: Dictionary<String, Int> = [
            ViaKey.CUSTOMERID.rawValue: customer.customerId
        ];
        let params: [String] = [code];
        let headers: Dictionary<String, String> = [
            ViaHeaderKey.API_KEY.rawValue: API_KEY!
        ];
        let url: URL = URL(string: viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_SITE)!;
        viaApiCtrl.sendPutRequest(url: url, input: input, params: params, headers: headers);
    }
    
}

extension ViaMinisiteViewController: UIWebViewDelegate {
    func webViewDidFinishLoad(_ webView: UIWebView) { }
}

extension ViaMinisiteViewController: ViaApiRequestDelegate {
    func response(name: ViaApiName, code: Int, success data: Dictionary<String, Any>) {
        // print(name, code, data);
    }
    
    func response(name: ViaApiName, code: Int, success info: String) {
        print(name, code, info);
    }
    
    func response(name: ViaApiName, code: Int, error msg: String) {
        print(name, code, msg);
    }
    
    
}
