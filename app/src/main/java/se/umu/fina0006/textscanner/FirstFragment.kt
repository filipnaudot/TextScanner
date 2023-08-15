package se.umu.fina0006.textscanner

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import org.json.JSONArray
import se.umu.fina0006.textscanner.databinding.FragmentFirstBinding
import java.io.IOException


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var scanResultList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val binding get() = _binding!! // This property is only valid between onCreateView and onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel.scanStorage.observe(this) { newText ->
            scanResultList.add(0, newText)
            adapter.notifyDataSetChanged()
            storeScanResultToJson()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initScanResultList()
        setListListener()
    }

    private fun initScanResultList() {
        readScanResultFromJson()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, scanResultList)
        binding.scanResultList.adapter = adapter
    }

    private fun setListListener() {
        binding.scanResultList.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = scanResultList[position] // Get the clicked item data
            val bundle = bundleOf(
                "scannedText" to clickedItem,
                "isSaved" to true,
                )
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
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