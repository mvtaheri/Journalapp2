package com.mohammad.journalapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mohammad.journalapp.databinding.ActivityAddJournalBinding
import java.util.Date

class AddJournalActivity : AppCompatActivity() {
    var currentUserName: String = ""
    var currentUserId: String = ""
    lateinit var binding: ActivityAddJournalBinding
    lateinit var user: FirebaseUser
    lateinit var auth: FirebaseAuth
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var storageReference: StorageReference
    var collectionReference: CollectionReference = db.collection("Journal")
    lateinit var imgUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("TAGY", "start add journal activity")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_journal)

        storageReference = FirebaseStorage.getInstance().getReference()
        auth = Firebase.auth
        binding.apply {
            postProgressBar.visibility = View.INVISIBLE
            if (JournalUser.instance != null) {
                currentUserId = auth.currentUser?.uid.toString()
                currentUserName = auth.currentUser?.displayName.toString()
                postUsernameTextview.text = currentUserName
            }
            postSaveJournalButton.setOnClickListener {
                SaveJournal()
            }
            // Getting image from Gallery
            postCameraButton.setOnClickListener() {
                var i: Intent = Intent(Intent.ACTION_GET_CONTENT)
                i.setType("image/*")
                startActivityForResult(i, 1)
            }
        }
    }

    private fun SaveJournal() {
        var title: String = binding.postTitleEt.text.toString().trim()
        var thoughts: String = binding.postDescriptionEt.text.toString().trim()

        binding.postProgressBar.visibility = View.VISIBLE

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imgUri != null) {
            // Saving the path of images in Storage
            //   ....../journal_images/our_image.png
            val filePath: StorageReference = storageReference.child("journal_images")
                .child("my_image_" + Timestamp.now().seconds)

            // Uploading the images

            filePath.putFile(imgUri)
                .addOnSuccessListener() {
                    filePath.downloadUrl.addOnSuccessListener {
                        val imageUri: Uri = it

                        var timeStamp: Timestamp = Timestamp(Date())


                        // Creating the object of Journal
                        var journal: Journal = Journal(
                            title,
                            thoughts,
                            imageUri.toString(),

                            currentUserId,
                            timeStamp,
                            currentUserName

                        )

                        // adding the new journal
                        collectionReference.add(journal)
                            .addOnSuccessListener {
                                binding.postProgressBar.visibility = View.INVISIBLE
                                var i: Intent = Intent(this, JournalListActivity::class.java)

                                startActivity(i)
                                finish()
                            }

                    }
                    binding.postProgressBar.visibility = View.INVISIBLE
                    var i: Intent = Intent(this, JournalListActivity::class.java)

                    startActivity(i)
                    finish()
                }.addOnFailureListener() {
                    binding.postProgressBar.visibility = View.INVISIBLE
                }


        } else {
            binding.postProgressBar.visibility = View.INVISIBLE
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                imgUri = data.data!!  // getting the actual image path
                binding.postImageView.setImageURI(imgUri)  // showing the image
            }
        }
    }


    override fun onStart() {
        super.onStart()
        user = auth.currentUser!!
    }

    override fun onStop() {
        super.onStop()
        if (auth != null) {

        }
    }


}