package com.example.yourappname

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.yourappname.helper.ThemeHelper
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var themeHelper: ThemeHelper
    private lateinit var profilePicture: ImageView
    private lateinit var editProfileIcon: ImageView
    private lateinit var txtUserName: TextView
    private lateinit var editFirstName: EditText
    private lateinit var editMiddleName: EditText
    private lateinit var editLastName: EditText
    private lateinit var btnEditSave: Button
    private lateinit var btnBack: ImageView
    private var isEditing = false
    private var profileImageUri: Uri? = null
    private var isProfilePictureChanged = false

    companion object {
        const val PROFILE_PREFS = "user_profile"
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_MIDDLE_NAME = "middle_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        const val KEY_HAS_PROFILE_IMAGE = "has_profile_image"

        const val PROFILE_IMAGES_DIR = "profile_images"
        const val PROFILE_IMAGE_NAME = "profile_picture.jpg"
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                profilePicture.setImageURI(null)
                profilePicture.setImageURI(it)

                profileImageUri = it
                isProfilePictureChanged = true
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeHelper = ThemeHelper.attach(this)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profile_picture)
        editProfileIcon = findViewById(R.id.edit_profile_icon)
        txtUserName = findViewById(R.id.user_name)
        editFirstName = findViewById(R.id.edit_firstname)
        editMiddleName = findViewById(R.id.edit_middlename)
        editLastName = findViewById(R.id.edit_lastname)
        btnEditSave = findViewById(R.id.btn_edit_save)
        btnBack = findViewById(R.id.button_back)

        loadProfile()
        setupTextWatchers()
        setupProfilePictureClick()

        btnEditSave.setOnClickListener {
            if (isEditing) {
                if (validateFields()) {
                    saveProfile()
                }
            } else {
                enableEditing()
            }
        }

        btnBack.setOnClickListener {
            if (isEditing) {
                showDiscardChangesDialog()
            } else {
                navigateBack()
            }
        }
    }

    private fun navigateBack() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Changes")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Discard") { _, _ -> navigateBack() }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (editFirstName.text.toString().trim().isEmpty()) {
            editFirstName.error = "First name cannot be empty"
            isValid = false
        }


        if (editLastName.text.toString().trim().isEmpty()) {
            editLastName.error = "Last name cannot be empty"
            isValid = false
        }

        return isValid
    }

    private fun setupProfilePictureClick() {
        val profileClickListener = View.OnClickListener {
            if (isEditing) {
                pickImage.launch("image/*")
            } else {
                Toast.makeText(this, "Enable edit mode to change profile picture", Toast.LENGTH_SHORT).show()
            }
        }

        profilePicture.setOnClickListener(profileClickListener)
        editProfileIcon.setOnClickListener(profileClickListener)
    }

    private fun setupTextWatchers() {
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) {
                    updateDisplayName()
                }
            }
        }

        editFirstName.addTextChangedListener(nameWatcher)
        editMiddleName.addTextChangedListener(nameWatcher)
        editLastName.addTextChangedListener(nameWatcher)
    }

    private fun updateDisplayName() {
        val firstName = editFirstName.text.toString().trim()
        val middleName = editMiddleName.text.toString().trim()
        val lastName = editLastName.text.toString().trim()

        val displayName = when {
            middleName.isEmpty() -> "$firstName $lastName"
            else -> "$firstName $middleName $lastName"
        }

        txtUserName.text = displayName
    }

    private fun loadProfile() {
        val sharedPrefs = getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE)
        val firstName = sharedPrefs.getString(KEY_FIRST_NAME, "Clint") ?: "Clint"
        val middleName = sharedPrefs.getString(KEY_MIDDLE_NAME, "Reboquio") ?: "Reboquio"
        val lastName = sharedPrefs.getString(KEY_LAST_NAME, "Perales") ?: "Perales"
        val hasProfileImage = sharedPrefs.getBoolean(KEY_HAS_PROFILE_IMAGE, false)

        if (hasProfileImage) {
            val profileImageFile = getProfileImageFile()
            if (profileImageFile.exists()) {
                profilePicture.setImageURI(Uri.fromFile(profileImageFile))
            } else {
                profilePicture.setImageResource(R.drawable.profile_user)

                sharedPrefs.edit().putBoolean(KEY_HAS_PROFILE_IMAGE, false).apply()
            }
        } else {
            profilePicture.setImageResource(R.drawable.profile_user)
        }

        editFirstName.setText(firstName)
        editMiddleName.setText(middleName)
        editLastName.setText(lastName)
        txtUserName.text = "$firstName $middleName $lastName"

        disableEditing()
    }

    private fun enableEditing() {
        animateButton()
        btnEditSave.text = "Save"
        btnEditSave.setBackgroundResource(R.drawable.rounded_button)

        editFirstName.isEnabled = true
        editMiddleName.isEnabled = true
        editLastName.isEnabled = true

        // Give visual indication that fields are editable
        editFirstName.setBackgroundResource(R.drawable.edit_text_active_bg)
        editMiddleName.setBackgroundResource(R.drawable.edit_text_active_bg)
        editLastName.setBackgroundResource(R.drawable.edit_text_active_bg)

        editProfileIcon.visibility = View.VISIBLE

        animateProfileEditIcon()
        editFirstName.requestFocus()

        isEditing = true
    }

    private fun disableEditing() {
        btnEditSave.text = "Edit"
        btnEditSave.setBackgroundResource(R.drawable.rounded_button)

        editFirstName.isEnabled = false
        editMiddleName.isEnabled = false
        editLastName.isEnabled = false

        editFirstName.setBackgroundResource(R.drawable.edit_text_bg)
        editMiddleName.setBackgroundResource(R.drawable.edit_text_bg)
        editLastName.setBackgroundResource(R.drawable.edit_text_bg)

        editProfileIcon.visibility = View.GONE

        isEditing = false
        isProfilePictureChanged = false
    }

    private fun saveProfile() {
        val newFirstName = editFirstName.text.toString().trim()
        val newMiddleName = editMiddleName.text.toString().trim()
        val newLastName = editLastName.text.toString().trim()
        val sharedPrefs = getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putString(KEY_FIRST_NAME, newFirstName)
        editor.putString(KEY_MIDDLE_NAME, newMiddleName)
        editor.putString(KEY_LAST_NAME, newLastName)
        if (isProfilePictureChanged && profileImageUri != null) {
            try {
                if (saveProfileImageToInternalStorage(profileImageUri!!)) {
                    editor.putBoolean(KEY_HAS_PROFILE_IMAGE, true)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to save profile image", Toast.LENGTH_SHORT).show()
            }
        }

        editor.apply()

        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
        updateDisplayName()
        disableEditing()
    }

    private fun getProfileImageFile(): File {
        val dir = File(filesDir, PROFILE_IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, PROFILE_IMAGE_NAME)
    }

    private fun saveProfileImageToInternalStorage(uri: Uri): Boolean {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return false
            val profileImageFile = getProfileImageFile()
            val outputStream = FileOutputStream(profileImageFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.close()
            inputStream.close()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun animateProfileEditIcon() {
        val scaleX = ObjectAnimator.ofFloat(editProfileIcon, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(editProfileIcon, "scaleY", 1f, 1.2f, 1f)
        scaleX.duration = 1000
        scaleY.duration = 1000
        scaleX.repeatCount = 2
        scaleY.repeatCount = 2
        scaleX.start()
        scaleY.start()
    }

    private fun animateButton() {
        val scaleX = ObjectAnimator.ofFloat(btnEditSave, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnEditSave, "scaleY", 1f, 1.2f, 1f)
        scaleX.duration = 300
        scaleY.duration = 300
        scaleX.start()
        scaleY.start()
    }
}