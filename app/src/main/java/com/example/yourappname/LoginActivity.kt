package com.example.yourappname

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edittext_username = findViewById<EditText>(R.id.username)
        val edittext_password = findViewById<EditText>(R.id.password)

        intent?.let {
            it.getStringExtra("username")?.let { username ->
                edittext_username.setText(username)
            }
            it.getStringExtra("password")?.let { password ->
                edittext_password.setText(password)
            }
        }

        val button_login = findViewById<Button>(R.id.button_login)
        button_login.setOnClickListener {
            val inputUsername = edittext_username.text.toString()
            val inputPassword = edittext_password.text.toString()

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Username and Password cannot be empty.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (validateLogin(this, inputUsername, inputPassword)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("username", inputUsername)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid username/password", Toast.LENGTH_LONG).show()
            }
        }

        val registerLink = findViewById<TextView>(R.id.button_register)
        registerLink.paintFlags = registerLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateLogin(context: Context, username: String, password: String): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)
        val savedPassword = sharedPreferences.getString("password", null)

        return username == savedUsername && password == savedPassword
    }
}
