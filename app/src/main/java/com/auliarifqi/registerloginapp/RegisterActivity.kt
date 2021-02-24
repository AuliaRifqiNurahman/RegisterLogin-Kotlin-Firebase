package com.auliarifqi.registerloginapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private val PASSWORD_PATTERN = Pattern.compile(
        "(?=.*[0-9])" +         //minimal 1 digit number
                "(?=.*[a-zA-Z])" +    //any letter
                "(?=\\S+$)" +         //tidak boleh memakai spasi
                ".{8,}"               //minimal 8 karakter
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val firstname = etFirstName.text.toString().trim()
            val lastname = etLastName.text.toString().trim()
            val phonenumber = etPhoneNumber.text.toString().trim()
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

            if (firstname.isEmpty()) {
                etFirstName.error = "First name harus diisi"
                etFirstName.requestFocus()
                return@setOnClickListener
            }

            if (lastname.isEmpty()) {
                etLastName.error = "Last name harus diisi"
                etLastName.requestFocus()
                return@setOnClickListener
            }

            if (phonenumber.isEmpty()) {
                etPhoneNumber.error = "Phone number harus diisi"
                etPhoneNumber.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password harus diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }
            else if (!PASSWORD_PATTERN.matcher(password).matches()) {
                etPassword.error = "Minimal 8 karakter, terdiri dari alphabet & numerik"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            registerUser(email, firstname, lastname, phonenumber, password)
        }

//        btnLogin.setOnClickListener {
//            Intent(this, LoginActivity::class.java).also {
//                startActivity(it)
//            }
//        }

    }

    private fun registerUser(
        email: String,
        firstname: String,
        lastname: String,
        phonenumber: String,
        password: String
    ) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){

            if (it.isSuccessful) {
                Objects.requireNonNull(auth.currentUser)!!.sendEmailVerification()
                    .addOnCompleteListener { task ->

                        if (it.isSuccessful) {

                            currentUser = FirebaseAuth.getInstance().currentUser!!
                            assert(currentUser != null)
                            val uid: String = currentUser.getUid()

                            val mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(uid)

                            val mUserDatabase = FirebaseDatabase.getInstance().reference.child("Users")

                            val deviceToken: String? = FirebaseInstanceId.getInstance().getToken()

                            val userMap = HashMap<String, String>()
                            userMap["email"] = email
                            userMap["first_name"] = firstname
                            userMap["last_name"] = lastname
                            userMap["phone_number"] = phonenumber
                            userMap["password"] = password
                            userMap["device_token"] = deviceToken!!

                            mDatabase.setValue(userMap).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Pendaftaran berhasil, Silahkan cek email Anda untuk verifikasi", Toast.LENGTH_LONG).show()

                                    val current_user_id = Objects.requireNonNull(auth.currentUser)?.uid
                                    val deviceToken = FirebaseInstanceId.getInstance().token

                                    mUserDatabase.child(current_user_id!!).child("device_token")
                                        .setValue(deviceToken).addOnSuccessListener {
                                            Intent(this, LoginActivity::class.java).also {
                                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(it)
                                            }
                                        }
                                }
                            }

                        } else {
                            Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Tidak Bisa Mendaftar, Silahkan Cek Form dan Coba Lagi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}