package com.mohammad.journalapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mohammad.journalapp.adapter.JournalAdapter
import com.mohammad.journalapp.databinding.ActivityJournalListBinding

class JournalListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJournalListBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private var db = FirebaseFirestore.getInstance()
    private lateinit var storageRefrence: StorageReference
    var collectionReference: CollectionReference = db.collection("Journal")
    lateinit var journalList: MutableList<Journal>
    lateinit var journalAdapter: JournalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_list)
        var toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        firebaseAuth = Firebase.auth
        user = firebaseAuth.currentUser!!
        journalList = arrayListOf<Journal>()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_journal -> if (user != null && firebaseAuth != null) {
                val intent = Intent(this, AddJournalActivity::class.java)
                startActivity(intent)
            }

            R.id.sign_out -> {
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        collectionReference.whereEqualTo("userId", user.uid)
            .get().addOnSuccessListener {
                if (!it.isEmpty) {
                    Log.i("TAGY", "ELEMENT: ${it}")
                    for (document in it) {
                        var journal = Journal(
                            document.data.get("title").toString(),
                            document.data.get("thoughts").toString(),
                            document.data.get("imageUrl").toString(),
                            document.data.get("userId").toString(),
                            document.data.get("timeAdded") as Timestamp,
                            document.data.get("username").toString()
                        )
                        journalList.add(journal)
                    }
                    journalAdapter = JournalAdapter(this, journalList)
                    binding.recyclerView.setAdapter(journalAdapter)
                    journalAdapter.notifyDataSetChanged()
                } else {
                    binding.listNoPosts.visibility = View.VISIBLE
                }
            }.addOnFailureListener {
                Toast.makeText(this, "NO DATA AVALABLE", Toast.LENGTH_LONG).show()
            }
    }
}