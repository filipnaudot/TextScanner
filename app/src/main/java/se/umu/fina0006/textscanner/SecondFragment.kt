package se.umu.fina0006.textscanner


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import se.umu.fina0006.textscanner.databinding.FragmentSecondBinding


class SecondFragment : Fragment() {
    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scannedText = arguments?.getString("scannedText")
        binding.textviewScanDetail.setText(scannedText)


        // Copy button
        // if setting.autoCopy
        copyToPrimaryClip(scannedText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun copyToPrimaryClip(text : String?) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("Text produced by MLKit text recognition", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "TextScannerSecondFragment"
    }
}