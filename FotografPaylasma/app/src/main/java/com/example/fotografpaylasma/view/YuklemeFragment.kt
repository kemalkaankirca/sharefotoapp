package com.example.fotografpaylasma.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.fotografpaylasma.databinding.FragmentYuklemeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID


class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null

    private val binding get() = _binding!!
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
        registerLaunchers()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.yukleButton.setOnClickListener{yukleTiklandi(it) }
        binding.imageView.setOnClickListener{gorselSec(it)}
    }
    fun yukleTiklandi(view: View)
    {
        val uuid = UUID.randomUUID()
        val gorselAdi = "${uuid}.jpg"

        val reference = storage.reference
        val gorselReferansi = reference.child("images").child(gorselAdi)
        if(secilenGorsel != null){
            gorselReferansi.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask ->
                //url alma islemi yapacam
               gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                   if (auth.currentUser != null) {
                       val downloadUrl = uri.toString()
                       //println(downloadUrl)
                       //veri tabanina kayit yapmam gerek
                       val postMap = hashMapOf<String, Any>()
                       postMap.put("downloadUrl",downloadUrl)
                       postMap.put("email",auth.currentUser!!.email.toString())
                       postMap.put("comment",binding.commentText.text.toString())
                       postMap.put("date",Timestamp.now())

                       db.collection("Posts").add(postMap).addOnSuccessListener { documentReference ->
                           //veri databese e yuklendi
                           val action= YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                           Navigation.findNavController(view).navigate(action)
                       }.addOnFailureListener { exception ->
                           Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                       }

                   }


               }

            }.addOnFailureListener{exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }
    fun gorselSec(view: View)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //read media images
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                //izin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)) {
                    //izin mantigini kullaniciya goster
                    Snackbar.make(view,"Galeriye gitmek icin izin vermeniz gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction("Izin ver"
                    ,View.OnClickListener {
                        //izin istememiz lazim
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                }else {
                    //izin istememiz lazim
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else {
                //izin var
                //galeriye gitme kodunu yaz
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else {
            //read external storage
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //izin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //izin mantigini kullaniciya goster
                    Snackbar.make(view,"Galeriye gitmek icin izin vermeniz gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction("Izin ver"
                        ,View.OnClickListener {
                            //izin istememiz lazim
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }else {
                    //izin istememiz lazim
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else {
                //izin var
                //galeriye gitme kodunu yaz
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        }
    }

    private fun registerLaunchers()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->

            if(result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                 if(intentFromResult != null) {
                     secilenGorsel = intentFromResult.data
                     try {
                         if (Build.VERSION.SDK_INT >= 28) {
                             val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                             secilenBitmap = ImageDecoder.decodeBitmap(source)
                             binding.imageView.setImageBitmap(secilenBitmap)

                         }else{
                             secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                             binding.imageView.setImageBitmap(secilenBitmap)
                         }

                     }catch (e:Exception){
                         e.printStackTrace()
                     }
                 }
            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result) {
                //izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else {
                //kullanici iptal etti
                Toast.makeText(requireContext(),"izni reddettiniz izne ihtiyacimiz var",Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}