package com.example.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fotografpaylasma.R
import com.example.fotografpaylasma.adapter.FeedRecyclerAdapter
import com.example.fotografpaylasma.model.Post
import com.example.fotografpaylasma.databinding.FragmentFeedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var popup: PopupMenu
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val postList: ArrayList<Post> = arrayListOf()
    private var adapter: FeedRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase Auth ve Firestore bağlantılarını standart şekilde yapıyoruz
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FloatingActionButton click eventi
        binding.floatingActionButton.setOnClickListener { floatingButtonTiklandi(view) }

        // Popup menü hazırlama
        popup = PopupMenu(requireContext(), binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)

        // Firestore'dan verileri çekiyoruz
        fireStoreVerileriAl()

        // RecyclerView setup
        adapter = FeedRecyclerAdapter(postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }

    private fun fireStoreVerileriAl() {
        db.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                if (value != null && !value.isEmpty) {
                    postList.clear()
                    val documents = value.documents
                    for (document in documents) {
                        // Verilerin null olup olmadığını kontrol ediyoruz
                        val comment = document.getString("comment") ?: "No Comment"
                        val email = document.getString("email") ?: "Unknown"
                        val downloadUrl = document.getString("downloadUrl") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""


                        val post = Post(email, comment, downloadUrl)
                        postList.add(post)
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    fun floatingButtonTiklandi(view: View) {
        // Popup menü gösterimi
        val popup = PopupMenu(requireContext(), binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.yuklemeItem -> {
                val action = FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
                Navigation.findNavController(requireView()).navigate(action)
                true
            }
            R.id.cikisItem -> {
                // Çıkış yapma işlemi
                auth.signOut()
                val action = FeedFragmentDirections.actionFeedFragmentToKullaniciFragment()
                Navigation.findNavController(requireView()).navigate(action)
                true
            }
            else -> false
        }
    }
}
