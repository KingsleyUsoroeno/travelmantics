package com.charity.travelmantics.ui.fragment


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.charity.travelmantics.R
import com.charity.travelmantics.ui.data.Destination
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
private const val GALLERY_REQUEST_CODE = 20
private const val TAG = "AdminFragment"

class AdminFragment : Fragment() {

    private lateinit var layoutView: View
    private var imageView: ImageView? = null
    private lateinit var auth: FirebaseAuth
    private val mDatabase = FirebaseDatabase.getInstance().getReference("Data")
    // These Path points to the Directory where a user data is Stored on SignUp
    private var descEdt: TextInputEditText? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var filePath: Uri? = null
    private var cityEdt: TextInputEditText? = null
    private var priceEdt: TextInputEditText? = null
    private var progressBar: ProgressBar? = null
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        // Inflate the layout for this fragment
        layoutView = inflater.inflate(R.layout.fragment_admin, container, false)
        val btnSelectImage = layoutView.findViewById(R.id.btnSelectImage) as MaterialButton
        imageView = layoutView.findViewById(R.id.selectedImg)

        storage = FirebaseStorage.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        cityEdt = layoutView.findViewById(R.id.city_edit_text)
        priceEdt = layoutView.findViewById(R.id.price_edit_text)
        descEdt = layoutView.findViewById(R.id.desc_edit_text)
        progressBar = layoutView.findViewById(R.id.bar)
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)

        btnSelectImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                pickFromGallery()
            }
        }
        return layoutView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addDestination -> {
                val city = cityEdt?.text.toString()
                val price = priceEdt?.text.toString().toInt()
                val desc = descEdt?.text.toString()
                filePath?.let {
                    addDestination(city, price, desc, it)
                }
            }
        }
        return true
    }

    private fun pickFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    //data.getData returns the content URI for the selected Image
                    data?.let {
                        filePath = it.data
                    }
                    try {
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                        imageView?.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.size > 1) {
            for (grantedResult in grantResults) {
                if (grantedResult == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery()
                }
            }
        } else return

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addDestination(city: String, price: Int, desc: String, filePath: Uri) {
        progressBar?.visibility = View.VISIBLE

        val ref = storageRef.child("uploads/" + UUID.randomUUID().toString())
        val uploadTask = ref.putFile(filePath)
        uploadTask.addOnCompleteListener { taskUpload ->
            if (taskUpload.isSuccessful) {
                taskUpload.result!!.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    Log.i("imageUrl", "$it")
                    saveDataToDb(city, price, desc, it.toString())
                }

                showMessage("Upload Successful")
                navController.navigate(R.id.action_adminFragment_to_userFragment)
            } else {
                showMessage("Error${taskUpload.exception.toString()}")
            }
        }
    }

    private fun showMessage(message: String) {
        progressBar?.visibility = View.GONE
        Snackbar.make(layoutView, message, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    private fun saveDataToDb(city: String, price: Int, desc: String, filePath: String) {
        val newRef: DatabaseReference = mDatabase.child("users").child("destinations").push()
        val destination = Destination(city, price, desc, filePath)
        newRef.setValue(destination)
    }
}
