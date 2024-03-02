package com.example.old2gold

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.yad2.databinding.ActivityMainBinding
import com.example.yad2.fragments.UserProfileFragment
import com.example.yad2.viewModels.MainViewModel
import com.example.yad2.models.Model
import com.example.yad2.models.User
import com.example.yad2.R
import com.example.yad2.activities.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private var viewModel: MainViewModel? = null
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var binding: ActivityMainBinding? = null
    private var drawer: DrawerLayout? = null
    private var mAuth: FirebaseAuth? = null
    private var navController: NavController? = null
    var headerView: View? = null
    var currentUser: User? = null

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getData().observe(this) { user ->
            val userImage =
                headerView!!.findViewById<ImageView>(R.id.imageView)
            if (user.getUserImageUrl() != null) {
                Picasso.get()
                    .load(user.getUserImageUrl())
                    .into(userImage)
            }
            val userName =
                headerView!!.findViewById<TextView>(R.id.idUserName)
            userName.setText(user.getFirstName() + " " + user.getLastName())
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        setSupportActionBar(binding.appBarMain.toolbar)
        drawer = binding.drawerLayout
        val navigationView: NavigationView = binding.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = Builder(
            R.id.nav_home,
            R.id.nav_my_products,
            R.id.nav_favorites,
            R.id.nav_user_profile,
            R.id.nav_map_view
        )
            .setOpenableLayout(drawer)
            .build()
        navController = findNavController(this, R.id.nav_host_fragment_content_main)
        setupActionBarWithNavController(this, navController!!, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController!!)
        navigationView.menu.findItem(R.id.nav_sign_out)
            .setOnMenuItemClickListener { menuItem: MenuItem? ->
                logout()
                true
            }
        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) {
            headerView = navigationView.getHeaderView(0)
            Model.instance.getUser(mAuth!!.currentUser!!.uid) { user ->
                val userName =
                    headerView.findViewById<TextView>(R.id.idUserName)
                userName.setText(user.getFirstName() + " " + user.getLastName())
                val userImage =
                    headerView.findViewById<ImageView>(R.id.imageView)
                if (user.getUserImageUrl() != null) {
                    Picasso.get()
                        .load(user.getUserImageUrl())
                        .into(userImage)
                }
                currentUser = user
                val imageView =
                    headerView.findViewById<ImageView>(R.id.imageView)
                imageView.setOnClickListener { navigateToUserProfile() }
                navigationView.menu.findItem(R.id.nav_user_profile)
                    .setOnMenuItemClickListener { menuItem: MenuItem? ->
                        navigateToUserProfile()
                        true
                    }
            }
            val mail = headerView.findViewById<TextView>(R.id.idMail)
            mail.text = mAuth!!.currentUser!!.email
        }
    }

    private fun navigateToUserProfile() {
        val bundle = Bundle()
        bundle.putSerializable("user", currentUser)
        val userProfileFragment = UserProfileFragment()
        userProfileFragment.setArguments(bundle)
        drawer!!.closeDrawer(Gravity.LEFT)
        navController!!.navigate(R.id.to_nav_user_profile, bundle)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        return (navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val i = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(i)
        finish()
    }
}