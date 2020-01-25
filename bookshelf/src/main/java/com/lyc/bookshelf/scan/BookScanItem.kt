package com.lyc.bookshelf.scan

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Liu Yuchuan on 2020/1/24.
 */
data class BookScanItem(
    val name: String?,
    val ext: String?,
    val uri: Uri?,
    val lastModified: Long,
    val length: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(ext)
        parcel.writeParcelable(uri, flags)
        parcel.writeLong(lastModified)
        parcel.writeLong(length)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BookScanItem> {
        override fun createFromParcel(parcel: Parcel): BookScanItem {
            return BookScanItem(parcel)
        }

        override fun newArray(size: Int): Array<BookScanItem?> {
            return arrayOfNulls(size)
        }
    }
}
