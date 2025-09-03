package com.example.yourappname

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val oldPasswordInput = findViewById<EditText>(R.id.old_password)
        val newPasswordInput = findViewById<EditText>(R.id.new_password)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password)
        val changeButton = findViewById<Button>(R.id.button_change_password)

        changeButton.setOnClickListener {
            val oldPassword = oldPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedPassword = sharedPrefs.getString("password", null)

            if (oldPassword != savedPassword) {
                Toast.makeText(this, "Old password is incorrect.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            sharedPrefs.edit().putString("password", newPassword).apply()
            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_LONG).show()

            // Redirect to Home Page
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
