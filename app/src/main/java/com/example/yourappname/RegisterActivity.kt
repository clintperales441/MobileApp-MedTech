package com.example.yourappname

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class RegisterActivity : Activity() {
    private fun saveAccount(context: Context, username: String, password: String) {
        val sharedPreferences : SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("password", password)
        editor.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val edittext_username = findViewById<EditText>(R.id.username)
        val edittext_password = findViewById<EditText>(R.id.password)
        val edittext_confirm_password = findViewById<EditText>(R.id.confirm_password)

        val button_register = findViewById<Button>(R.id.button_register)
        button_register.setOnClickListener {
            val savedUsername = edittext_username.text.toString()
            val savedPassword = edittext_password.text.toString()
            val confirmPassword = edittext_confirm_password.text.toString()


            if (savedPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(savedUsername.isNotEmpty() && savedPassword.isNotEmpty() && confirmPassword.isNotEmpty()){
                saveAccount(this, savedUsername, savedPassword);

                Toast.makeText(this, "Account registered successfully!", Toast.LENGTH_LONG).show()
            }

            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("username", savedUsername)  // Pass username
            intent.putExtra("password", savedPassword)  // Pass password
            startActivity(intent)
        }

        val backToLoginLink = findViewById<TextView>(R.id.button_back_to_login)
        backToLoginLink.paintFlags = backToLoginLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        backToLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
