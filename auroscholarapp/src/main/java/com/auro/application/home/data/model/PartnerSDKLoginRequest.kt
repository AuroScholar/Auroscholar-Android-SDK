package com.auro.application.home.data.model

data class PartnerSDKLoginRequest(
    var mobileNumber:String?=null,
    var apiKey:String?=null,
    var partnerSource:String?=null,
    var partnerUniqueID: String?=null,
    var gradeName:String?=null,
    var streamName:String?=null)