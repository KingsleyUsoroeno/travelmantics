package com.charity.travelmantics.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charity.travelmantics.R
import com.charity.travelmantics.ui.adapter.UserAdapter
import com.charity.travelmantics.ui.data.Destination
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val TAG = "UserFragment"

/**
 * A simple [Fragment] subclass.
 *
 */
class UserFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layoutView = inflater.inflate(R.layout.fragment_user, container, false)
        recyclerView = layoutView.findViewById(R.id.destinationRecyclerView)
        fetchAllDestinations()
        return layoutView
    }

    private fun fetchAllDestinations() {
        // Get a reference to our posts
        val database = FirebaseDatabase.getInstance()
        val ref =
            database.getReference("Data/users/destinations") // path to our data in our firebase Database
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val destinations = ArrayList<Destination>()
                for (ds in dataSnapshot.children) {
                    val destination = ds.getValue(Destination::class.java)
                    Log.i(TAG, "The read Succeeded $destination")
                    destinations.add(destination!!)
                    Log.i(TAG, "List size $destinations")
                    setUpRecyclerView(destinations)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "The read failed ${databaseError.code}")
            }
        })
    }

    private fun setUpRecyclerView(destinations : ArrayList<Destination>?){
        destinations.let {
            recyclerView.setHasFixedSize(true)
            val adapter = UserAdapter()
            adapter.submitList(destinations)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }
    }
}
