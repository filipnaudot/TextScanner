package se.umu.fina0006.textscanner

import android.app.AlertDialog
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
import se.umu.fina0006.textscanner.databinding.FragmentListBinding
import java.io.IOException


class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var scanResultList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val binding get() = _binding!! // This property is only valid between onCreateView and onDestroyView.


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the associated view is created.
     * Initializes the behavior and interactions for the fragment's UI elements.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState The saved state of the fragment, if available.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.scanStorage.observe(viewLifecycleOwner) { newText ->
            if(newText != sharedViewModel.lastEmittedValue) {
                addNewScan(newText)
            }
            sharedViewModel.lastEmittedValue = newText
        }
        setListListener()
        initScanResultList()
    }

    /**
     * Adds a new scanned text to list of scans and notifies
     * adapter for ListView.
     */
    private fun addNewScan(text: String) {
        scanResultList.add(0, text)
        adapter.notifyDataSetChanged()
        storeScanResultToJson()
    }

    /**
     * Deletes a scan of a given index and and notifies
     * adapter for ListView.
     */
    private fun deleteScanAtIndex(index: Int) {
        scanResultList.removeAt(index)
        adapter.notifyDataSetChanged()
        storeScanResultToJson()
    }

    /**
     * Reads scans stored on local storage as JSON and binds data to adapter.
     */
    private fun initScanResultList() {
        readScanResultFromJson()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, scanResultList)
        binding.scanResultList.adapter = adapter
    }

    /**
     * Sets on click listener for list items.
     * On simple click, navigates to fragment for editing text.
     * On long click, displays popup for deleting the item.
     */
    private fun setListListener() {
        binding.scanResultList.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = scanResultList[position] // Get the clicked item data
            val bundle = bundleOf(
                "scannedText" to clickedItem,
                "isSaved" to true,
                "index" to position,
            )
            findNavController().navigate(R.id.action_ListFragment_to_EditFragment, bundle)
        }

        binding.scanResultList.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(context)
                .setTitle("Delete scan")
                .setMessage("Are you sure you want to delete this scan?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteScanAtIndex(position)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
            true
        }
    }

    /**
     * Read JSON file from local memory containing scanned text.
     */
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

    /**
     * Write scanned text to JSON and store in local memory with MODE_PRIVATE.
     */
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