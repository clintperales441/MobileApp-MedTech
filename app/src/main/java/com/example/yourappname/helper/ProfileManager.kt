
package com.example.yourappname.helper

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.example.yourappname.R
import java.io.File

class ProfileManager {
    companion object {
        private const val PROFILE_PREFS = "user_profile"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_MIDDLE_NAME = "middle_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_HAS_PROFILE_IMAGE = "has_profile_image"


        private const val PROFILE_IMAGES_DIR = "profile_images"
        private const val PROFILE_IMAGE_NAME = "profile_picture.jpg"


        fun getFullName(context: Context): String {
            val sharedPrefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
            val firstName = sharedPrefs.getString(KEY_FIRST_NAME, "Clint") ?: "Clint"
            val middleName = sharedPrefs.getString(KEY_MIDDLE_NAME, "Reboquio") ?: "Reboquio"
            val lastName = sharedPrefs.getString(KEY_LAST_NAME, "Perales") ?: "Perales"

            return if (middleName.isEmpty()) {
                "$firstName $lastName"
            } else {
                "$firstName $middleName $lastName"
            }
        }


        fun getFirstName(context: Context): String {
            val sharedPrefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
            return sharedPrefs.getString(KEY_FIRST_NAME, "Clint") ?: "Clint"
        }


        fun loadProfilePicture(context: Context, imageView: ImageView): Boolean {
            val sharedPrefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
            val hasProfileImage = sharedPrefs.getBoolean(KEY_HAS_PROFILE_IMAGE, false)

            if (hasProfileImage) {
                // Load from internal storage using same path as ProfileActivity
                val profileImageFile = getProfileImageFile(context)
                if (profileImageFile.exists()) {
                    try {
                        imageView.setImageURI(Uri.fromFile(profileImageFile))
                        return true
                    } catch (e: Exception) {
                        // If there's an error loading the image, use the default
                        imageView.setImageResource(R.drawable.profile_user)
                    }
                }
            }


            imageView.setImageResource(R.drawable.profile_user)
            return false
        }


        private fun getProfileImageFile(context: Context): File {
            val dir = File(context.filesDir, PROFILE_IMAGES_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return File(dir, PROFILE_IMAGE_NAME)
        }


        fun getProfileImageUri(context: Context): Uri? {
            val sharedPrefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
            val hasProfileImage = sharedPrefs.getBoolean(KEY_HAS_PROFILE_IMAGE, false)

            if (hasProfileImage) {
                val profileImageFile = getProfileImageFile(context)
                if (profileImageFile.exists()) {
                    return Uri.fromFile(profileImageFile)
                }
            }

            return null
        }
    }
}