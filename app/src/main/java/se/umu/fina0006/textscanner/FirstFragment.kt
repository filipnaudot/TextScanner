package se.umu.fina0006.textscanner

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import org.json.JSONArray
import se.umu.fina0006.textscanner.databinding.FragmentFirstBinding
import java.io.IOException


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var scanResultList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readScanResultFromJson()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, scanResultList)
        binding.scanResultList.adapter = adapter

        sharedViewModel.scanStorage.observe(viewLifecycleOwner) { newText ->
            scanResultList.add(newText)
            adapter.notifyDataSetChanged()
            storeScanResultToJson()
        }
    }

    private fun readScanResultFromJson() {
        scanResultList = mutableListOf()

        try {
            val fileInputStream = context?.openFileInput("scan_results")
            val byteArray = fileInputStream?.readBytes()
            val jsonArray = JSONArray(byteArray?.toString(Charsets.UTF_8))

            for (i in 0 until jsonArray.length()) {
                scanResultList.add(jsonArray.getString(i))
            }

            fileInputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun storeScanResultToJson() {
        val jsonArray = JSONArray(scanResultList)

        try {
            val fileOutputStream = context?.openFileOutput("scan_results", Context.MODE_PRIVATE)
            fileOutputStream?.write(jsonArray.toString().toByteArray())
            fileOutputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}