//
//  ViaMinisiteTableViewCell.swift
//  BLE MS SDK
//
//  Created by Bie Yaqing on 25/4/18.
//  Copyright © 2018 Bie Yaqing. All rights reserved.
//

import UIKit

class ViaMinisiteTableViewCell: UITableViewCell {

    @IBOutlet weak var minisiteCover: UIImageView!
    @IBOutlet weak var minisiteTitle: UILabel!
    @IBOutlet weak var minisiteDescription: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        minisiteTitle.layer.shadowColor = UIColor.white.cgColor;
        minisiteTitle.layer.shadowRadius = 4.0;
        minisiteTitle.layer.shadowOpacity = 100.0;
        minisiteTitle.layer.shadowOffset = CGSize.zero;
        minisiteTitle.layer.masksToBounds = false;
        minisiteDescription.layer.shadowColor = UIColor.white.cgColor;
        minisiteDescription.layer.shadowRadius = 4.0;
        minisiteDescription.layer.shadowOpacity = 100.0;
        minisiteDescription.layer.shadowOffset = CGSize.zero;
        minisiteDescription.layer.masksToBounds = false;
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
