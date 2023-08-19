package se.umu.fina0006.textscanner


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import se.umu.fina0006.textscanner.databinding.FragmentSecondBinding


class EditFragment : Fragment() {
    private var _binding: FragmentSecondBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    /**
     * Called when the associated view is created.
     * Initializes UI elements and handles user interactions
     * for displaying scan details and performing actions related to the scanned text.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState The saved state of the fragment, if available.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scannedText = arguments?.getString("scannedText")
        binding.textviewScanDetail.setText(scannedText)

        binding.saveButton.setOnClickListener {
            val text = binding.textviewScanDetail.text.toString()
            sharedViewModel.scanStorage.value = text
            parentFragmentManager.popBackStackImmediate()
        }

        val sharedPreferences = activity?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val autoCopy = sharedPreferences?.getBoolean("CopyToClipboard", false)
        if (autoCopy == true) {
            copyToPrimaryClip(scannedText)
        }
    }

    /**
     * Checks if auto copy setting is on.
     * If should auto copy, copies the provided text to the device's primary clipboard.
     *
     * @param text The text to be copied to the clipboard.
     */
    private fun copyToPrimaryClip(text : String?) {
        Log.d(TAG, "copyToPrimaryClip")
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("Text produced by MLKit text recognition", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "TextScannerSecondFragment"
    }
}