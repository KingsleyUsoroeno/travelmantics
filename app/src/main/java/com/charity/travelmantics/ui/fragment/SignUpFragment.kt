package com.charity.travelmantics.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.charity.travelmantics.databinding.FragmentSignUpBinding
import com.charity.travelmantics.ui.data.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


/**
 * A simple [Fragment] subclass.
 *
 */
private const val TAG = "SignUpFragment"

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val mDatabase = FirebaseDatabase.getInstance().getReference("Data")
    private lateinit var signUpBinding: FragmentSignUpBinding
    private lateinit var navController: NavController
    private var spinner: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        signUpBinding =
            DataBindingUtil.inflate(
                inflater,
                com.charity.travelmantics.R.layout.fragment_sign_up,
                container,
                false
            )
        navController = Navigation.findNavController(
            activity!!,
            com.charity.travelmantics.R.id.nav_host_fragment
        )

        val name = signUpBinding.nameEditText
        val password = signUpBinding.passwordEditText
        val email = signUpBinding.emailEditText
        spinner = signUpBinding.progressBar

        signUpBinding.btnSignUp.setOnClickListener {
            signUpWithEmailAndPass(
                email.text.toString(),
                password.text.toString(),
                name.text.toString()
            )
        }

        return signUpBinding.root
    }

    private fun signUpWithEmailAndPass(email: String, password: String, name: String) {
        Log.i(TAG, "Email is $email")
        spinner?.visibility = View.VISIBLE

        if (email.isNotEmpty() && password.isNotEmpty()) {
            Log.i(TAG, "Email is $email")
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        val uid = currentUser.uid
                        val user = User(uid, name, password, it.email)
                        saveToDb(user)
                    }
                    spinner?.visibility = View.GONE
                    navController.navigate(com.charity.travelmantics.R.id.action_signUpFragment_to_adminFragment)
                    Log.i(TAG, "email ${auth.currentUser?.email}")

                } else {
                    spinner?.visibility = View.GONE
                    showMessage("Error: ${task.exception?.message}")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(signUpBinding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    private fun saveToDb(user: User) {
        val rootRef = FirebaseDatabase.getInstance().getReference("Data/users").push()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //create new user
                    rootRef.setValue(user)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.message) //Don't ignore errors!
            }
        }
        rootRef.addListenerForSingleValueEvent(eventListener)
    }
}


