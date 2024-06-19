package com.example.worker.Model

import android.os.Parcel
import android.os.Parcelable

data class BlogItemModel(
    var heading: String? = "null",
    val userName: String? = "null",
    val date: String? = "null",
    var post: String? = "null",
    val userId: String? = "null",
    var likeCount: Int = 0,
    val profileImage: String? = "null",
    var isSaved: Boolean = false,
    var postId: String = "null",
    val likedBy: MutableList<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "null",
        parcel.readString() ?: "null",
        parcel.readString() ?: "null",
        parcel.readString() ?: "null",
        parcel.readString() ?: "null",
        parcel.readInt(),
        parcel.readString() ?: "null",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "null",
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(heading)
        parcel.writeString(userName)
        parcel.writeString(date)
        parcel.writeString(post)
        parcel.writeString(userId)  // Fixed: Write userId correctly
        parcel.writeInt(likeCount)
        parcel.writeString(profileImage)
        parcel.writeByte(if (isSaved) 1 else 0)
        parcel.writeString(postId)  // Fixed: Write postId correctly
        parcel.writeStringList(likedBy)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BlogItemModel> {
        override fun createFromParcel(parcel: Parcel): BlogItemModel = BlogItemModel(parcel)
        override fun newArray(size: Int): Array<BlogItemModel?> = arrayOfNulls(size)
    }
}
