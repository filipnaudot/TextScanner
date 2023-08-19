package se.umu.fina0006.textscanner

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import se.umu.fina0006.textscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var toolbarMenuInitialized = false
    private var fabVisible = true
    private lateinit var navController: NavController
    private val pictureActivity = registerForActivityResult(CameraActivity.TakePicture())
    { text: String? -> launchEditFragment(text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        initFabVisibility(savedInstanceState)
        setListeners()
    }

    /**
     * Initiates the visibility of floating action button for new scan.
     */
    private fun initFabVisibility(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            fabVisible = savedInstanceState.getBoolean(FAB, false)
            if(!fabVisible) {
                binding.fab.hide()
            }
        }
    }

    /**
     * Launch fragment for editing text from a scan.
     * @param text The scanned text.
     */
    private fun launchEditFragment(text: String?) {
        Log.d(TAG, "TEXT: $text")
        // Navigate to SecondFragment and pass the scanned text as an argument
        if (!text.isNullOrEmpty()) {
            val bundle = bundleOf(
                "scannedText" to text,
                "isSaved" to false,
            )
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
        }
    }

    /**
     * Set listeners for "new scan"-fab and destination listener.
     */
    private fun setListeners() {
        setFabListener()
        setDestinationChangedListener()
    }

    /**
     * Sets on click listener on "new scan"-fab.
     * Launches the CameraX activity.
     */
    private fun setFabListener() {
        binding.fab.setOnClickListener {
            pictureActivity.launch(Unit)
        }
    }

    /**
     * Set the visibility for the toolbar menu.
     * Used to hide settings button while in settings.
     */
    private fun showToolbarMenu(status: Boolean) {
        val menu = binding.toolbar.menu
        val menuItem = menu.findItem(R.id.action_settings)
        if(menuItem != null) menuItem.isVisible = status
    }

    /**
     * Sets destination listener. Used to determine what
     * UI-elements should be visible.
     */
    private fun setDestinationChangedListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (toolbarMenuInitialized) {
                when (destination.id) {
                    R.id.ListFragment -> {
                        binding.fab.show()
                        fabVisible = true
                        showToolbarMenu(true)
                    }
                    R.id.SecondFragment -> {
                        binding.fab.hide()
                        fabVisible = false
                        showToolbarMenu(true)
                    }
                    R.id.SettingsFragment -> {
                        showToolbarMenu(false)
                        binding.fab.hide()
                        fabVisible = false
                    }
                    else -> {
                        binding.fab.hide()
                        fabVisible = false
                    }
                }
            }
        }
        toolbarMenuInitialized = true
    }

    /**
     * Inflate the menu in toolbar. Adds menu items to the
     * action bar, settings item in this case.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Handles action bar item clicks. Click on home/up-button is
     * automatically handled if parent activity is specified in AndroidManifest.xml.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(R.id.SettingsFragment)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * This function is called when the user presses the "up" button in the app's navigation bar.
     *
     * @return true if the navigation action was handled successfully, otherwise false.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Called to save the current instance state of the activity before it is destroyed.
     * In this case used before a configuration change to preserve the correct visibility
     * for new scan fab.
     *
     * @param outState A Bundle in which to place the saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FAB, fabVisible)
    }

    companion object {
        private const val TAG = "TextScannerMainActivity"
        private const val FAB = "fabVisibility"
    }
}