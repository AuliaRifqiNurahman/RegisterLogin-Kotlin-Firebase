package com.auliarifqi.registerloginapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mUserRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        mUserRef = FirebaseDatabase.getInstance()

        tvLogout.setOnClickListener{
            auth.signOut()
            sendToStart()
        }

        if (auth.getCurrentUser() != null)
        {
            val ref = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            ref.keepSynced(true)
            val menuListener = object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //get data
                    val name = Objects.requireNonNull(dataSnapshot.child("first_name").value).toString()
                    val email = Objects.requireNonNull(dataSnapshot.child("email").value).toString()
                    val phone = Objects.requireNonNull(dataSnapshot.child("phone_number").value).toString()

                    //set data
                    tvNama2.setText(name)
                    tvEmail2.setText(email)
                    tvPhone2.setText(phone)
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            ref.addListenerForSingleValueEvent(menuListener)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            sendToStart()
        }
    }

    private fun sendToStart() {
        Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

}