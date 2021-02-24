package com.auliarifqi.registerloginapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email harus diisi"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Email Anda tidak valid"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password harus diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        btnRegister.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }

    }

    private fun loginUser(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){

            if(it.isSuccessful) {
                if (Objects.requireNonNull(auth.currentUser)!!.isEmailVerified()) {

                    val mUserDatabase = FirebaseDatabase.getInstance().reference.child("Users")

                    val current_user_id = Objects.requireNonNull(auth.currentUser)?.uid

                    val deviceToken = FirebaseInstanceId.getInstance().token

                    mUserDatabase.child(current_user_id!!).child("device_token").setValue(deviceToken)
                        .addOnSuccessListener {
                            Intent(this, HomeActivity::class.java).also {
                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(it)
                            }
                        }
                } else {
                    Toast.makeText(this, "Silahkan Verifikasi Alamat Email Anda.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this, "Tidak Bisa Masuk, Silahkan Cek Email dan Password Anda.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}