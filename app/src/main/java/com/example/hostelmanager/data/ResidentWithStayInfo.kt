package com.example.hostelmanager.data

data class ResidentWithStayInfo(
    val personId: String,
    val name: String,
    val phone: String,
    val profilePhotoPath: String,
    val roomNumber: String,
    val monthlyRent: Long,
    val stayId: String,
    val isActive: Boolean,
    val checkoutDate: String?,
    val billingAnchorDay: Int
)