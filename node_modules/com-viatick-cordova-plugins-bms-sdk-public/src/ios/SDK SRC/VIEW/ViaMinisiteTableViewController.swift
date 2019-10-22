//
//  ViaMinisiteTableViewController.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 25/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import UIKit

class ViaMinisiteTableViewController: UITableViewController {
    
    let cellIdentifier = "ViaMinisiteTableViewCell";
    var minisites: [ViaMinisite] = [];
    var customer: ViaCustomer?;
    var viaBmsCtrl: ViaBmsCtrl?;
    var viaMinisiteViewController: ViaMinisiteViewController = ViaMinisiteViewController();
    
    var API_KEY: String?;
    
    var buttons: [UIButton] = [];
        
    override func viewDidLoad() {
        super.viewDidLoad();
        
        self.tableView.register(UINib(nibName: cellIdentifier, bundle: nil), forCellReuseIdentifier: cellIdentifier)
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }
    
    override func viewDidAppear(_ animated: Bool) {
        createButtons();
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        for b in buttons {
            b.removeFromSuperview();
        }
    }
    
    func createButtons() {
        let width = self.view.frame.size.width;
        let height = self.view.frame.size.height;
        let dismissBtn = UIButton();
        dismissBtn.frame = CGRect(x: width - 50, y: height - 70, width: 40, height: 40);
        dismissBtn.backgroundColor = UIColor(red: 204/255, green: 230/255, blue: 255/255, alpha: 1);
        let origIcon = UIImage(named: "ic_file_download");
        let tintedIcon = origIcon?.withRenderingMode(UIImageRenderingMode.alwaysTemplate);
        dismissBtn.setImage(tintedIcon, for: .normal);
        dismissBtn.tintColor = UIColor(red: 51/255, green: 204/255, blue: 204/255, alpha: 1);
        dismissBtn.addTarget(self, action: #selector(dismissAction), for: .touchUpInside);
        dismissBtn.layer.cornerRadius = 5;
        self.view.addSubview(dismissBtn);
        buttons.append(dismissBtn);
    }
    
    @objc func dismissAction(sender: UIButton!) {
        dismiss(animated: true, completion: onDismissed);
    }
    
    func onDismissed() {
        viaBmsCtrl?.SETTING.isModal = false;
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning();
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1;
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return minisites.count;
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as! ViaMinisiteTableViewCell;

        let minisite: ViaMinisite = minisites[indexPath.row];
        let title: String = minisite.title;
        let description: String = minisite.description;
        let coverUrl: String = minisite.coverUrl;
        
        cell.minisiteTitle.text = title;
        cell.minisiteDescription.text = description;
        
        if let url: URL = URL(string: coverUrl) {
            do {
                let coverData: Data = try Data(contentsOf: url);
                cell.minisiteCover.image = UIImage(data: coverData);
            } catch {
                // Nothing...
            }
        }
        
        return cell;
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        viewMinisite(index: indexPath.row);
    }
    
    /*
    // Override to support conditional editing of the table view.
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */
    
    func initiate(viaBmsCtrl: ViaBmsCtrl, minisites: [ViaMinisite], customer: ViaCustomer, apiKey: String) {
        self.viaBmsCtrl = viaBmsCtrl;
        self.minisites = minisites;
        self.customer = customer;
        API_KEY = apiKey;
        self.tableView.reloadData();
    }
    
    func update(minisites: [ViaMinisite]) {
        self.minisites = minisites;
        self.tableView.reloadData();
    }
    
    func viewMinisite(index: Int) {
        let minisite = minisites[index];
        viaMinisiteViewController.initiate(minisite: minisite, customer: customer!, apiKey: API_KEY!);
        viaMinisiteViewController.modalTransitionStyle = UIModalTransitionStyle.flipHorizontal;
        self.present(viaMinisiteViewController, animated: true, completion: nil);
    }
    
}
